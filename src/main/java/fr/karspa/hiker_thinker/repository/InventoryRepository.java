package fr.karspa.hiker_thinker.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.dtos.ReorderEquipmentDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.model.Inventory;
import fr.karspa.hiker_thinker.model.User;
import org.bson.Document;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.MongoExpression;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class InventoryRepository {

    private MongoTemplate mongoTemplate;

    public Inventory getInventory(String userId) {
        Query query = new Query(Criteria.where("_id").is(userId));
        query.fields().include("inventory");
        User user = mongoTemplate.findOne(query, User.class);

        if (user == null || user.getInventory() == null) {
            return null;
        }

        return user.getInventory();
    }


    public UpdateResult modifyEquipment(String userId, Equipment equipment) {

        Query query = new Query(
                Criteria.where("_id").is(userId)
                        .and("inventory.equipments._id").is(equipment.getId())
        );

        Update update = new Update().set("inventory.equipments.$", equipment);

        return mongoTemplate.updateFirst(query, update, User.class);
    }

    public UpdateResult modifyEquipmentsOrders(String userId, List<ReorderEquipmentDTO> changes) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "users");

        for (ReorderEquipmentDTO change : changes) {
            // Pour chaque catégorie, on récupère la liste ordonnée des équipements
            List<String> equipmentIds = change.getOrderedEquipmentIds();
            String categoryId = change.getCategoryId();

            // Parcourir chaque équipement dans la liste pour mettre à jour son index (position)
            for (int i = 0; i < equipmentIds.size(); i++) {
                String equipmentId = equipmentIds.get(i);

                Query query = new Query(Criteria.where("_id").is(userId)
                        .and("inventory.equipments._id").is(equipmentId));

                Update update = new Update()
                        .set("inventory.equipments.$.categoryId", categoryId)
                        .set("inventory.equipments.$.position", i);

                bulkOps.updateOne(query, update);
            }
        }

        BulkWriteResult result = bulkOps.execute();
        return UpdateResult.acknowledged(result.getModifiedCount(), (long) result.getMatchedCount(), null);
    }


    public List<Equipment> getUpdatedEquipments(String userId, List<String> equipmentIds) {
        try {
            // Convertir la liste des equipmentIds en JSON (ex: ["id1", "id2", ...])
            String equipmentIdsJson = new ObjectMapper().writeValueAsString(equipmentIds);

            // Construire l'expression $filter manuellement
            String filterExpr = "{ $filter: { input: '$inventory.equipments', as: 'eq', " +
                    "cond: { $in: ['$$eq._id', " + equipmentIdsJson + "] } } }";

            Aggregation aggregation = Aggregation.newAggregation(
                    // Filtrer le document de l'utilisateur (ou de la randonnée)
                    Aggregation.match(Criteria.where("_id").is(userId)),
                    // Projetter en utilisant l'expression $filter pour extraire les équipements ciblés
                    Aggregation.project()
                            .and(AggregationExpression.from(MongoExpression.create(filterExpr)))
                            .as("filteredEquipments")
            );

            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "users", Document.class);
            Document resultDoc = results.getUniqueMappedResult();
            if (resultDoc == null || !resultDoc.containsKey("filteredEquipments")) {
                return Collections.emptyList();
            }

            List<Document> equipmentsDocs = (List<Document>) resultDoc.get("filteredEquipments");
            return equipmentsDocs.stream().map(doc -> {
                Equipment eq = new Equipment();
                eq.setId(doc.getString("_id"));
                eq.setName(doc.getString("name"));
                eq.setDescription(doc.getString("description"));
                eq.setBrand(doc.getString("brand"));
                Number weight = doc.get("weight", Number.class);
                if (weight != null) {
                    eq.setWeight(weight.floatValue());
                }
                eq.setCategoryId(doc.getString("categoryId"));
                eq.setSourceId(doc.getString("sourceId"));
                Number position = doc.get("position", Number.class);
                if (position != null) {
                    eq.setPosition(position.intValue());
                }
                return eq;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return new ArrayList<>();
        }
    }


    public UpdateResult removeEquipment(String userId, String equipmentId) {
        Query query = new Query(Criteria.where("_id").is(userId));

        Update update = new Update().pull("inventory.equipments", new Document("_id", equipmentId));

        return mongoTemplate.updateFirst(query, update, User.class);
    }


    //Modifier une catégorie



    //Supprimer une catégorie



    public UpdateResult addEquipmentToEquipmentList(String userId, Equipment equipment) {
        Query query = new Query(Criteria.where("_id").is(userId));

        Update update = new Update().push("inventory.equipments", equipment);

        return mongoTemplate.updateFirst(query, update, User.class);
    }


    public List<EquipmentCategory> getCategories(String userId) {
        Query query = new Query(Criteria.where("_id").is(userId));
        query.fields().include("inventory.categories");

        User user = mongoTemplate.findOne(query, User.class);
        if(user == null || user.getInventory() == null) {
            return Collections.emptyList();
        }
        return user.getInventory().getCategories();
    }


    public UpdateResult addCategoryToCategoryList(String userId, EquipmentCategory category) {

        Query query = new Query(Criteria.where("_id").is(userId));

        Update update = new Update().push("inventory.categories", category);

        return mongoTemplate.updateFirst(query, update, User.class);
    }

    public UpdateResult modifyCategoryInCategoryList(String userId, EquipmentCategory category) {

        Query query = new Query(
                Criteria.where("_id").is(userId)
                        .and("inventory.categories._id").is(category.getId())
        );

        Update update = new Update().set("inventory.categories.$", category);

        return mongoTemplate.updateFirst(query, update, User.class);
    }

    public UpdateResult removeCategoryInCategoryList(String userId, String categoryId) {

        Query query = new Query(Criteria.where("_id").is(userId));

        Update update = new Update().pull("inventory.categories", new Document("_id", categoryId));

        return mongoTemplate.updateFirst(query, update, User.class);
    }


    public boolean checkAvailableEquipmentName(String userId, Equipment equipment) {

        if(equipment.getId() != null){
            return this.checkAvailableEquipmentNameModify(userId, equipment);
        }else{
            return this.checkAvailableEquipmentNameAdd(userId,  equipment);
        }
    }

    private boolean checkAvailableEquipmentNameAdd(String userId, Equipment equipment) {
        Query query = new Query(Criteria.where("_id").is(userId)
                .and("inventory.equipments.name").is(equipment.getName()));

        Document doc = mongoTemplate.findOne(query, Document.class, "users");

        return (doc == null);
    }

    private boolean checkAvailableEquipmentNameModify(String userId, Equipment equipment) {
        Query query = new Query(
                Criteria.where("_id").is(userId)
                        .and("inventory.equipments").elemMatch(
                        Criteria.where("name").is(equipment.getName())
                                .and("_id").ne(equipment.getId())
                ));


        Document doc = mongoTemplate.findOne(query, Document.class, "users");

        return (doc == null);
    }

    public boolean checkEquipmentExistsById(String userId, String equipmentId) {

        Query query = new Query(Criteria.where("_id").is(userId)
                .and("inventory.equipments._id").is(equipmentId));

        Document doc = mongoTemplate.findOne(query, Document.class, "users");

        return (doc != null);
    }

    public boolean checkCategoryExistsByName(String userId, String category) {

        Query query = new Query(
                Criteria.where("_id").is(userId)
                        .and("inventory.categories").elemMatch(
                                Criteria.where("name").is(category)
                        ));

        Document doc = mongoTemplate.findOne(query, Document.class, "users");

        return (doc != null);
    }

    public boolean checkCategoryExistsById(String userId, String categoryId) {

        Query query = new Query(
                Criteria.where("_id").is(userId)
                        .and("inventory.categories").elemMatch(
                                Criteria.where("_id").is(categoryId)
                        ));

        Document doc = mongoTemplate.findOne(query, Document.class, "users");

        return (doc != null);
    }

    public boolean checkMultipleCategoryExistsById(String userId, Set<String> categoryIds) {

        Query query = new Query(Criteria.where("_id").is(userId));
        // On peut utiliser une projection pour ne récupérer que l'inventaire
        query.fields().include("inventory.categories._id");

        User user = mongoTemplate.findOne(query, User.class, "users");
        if (user == null || user.getInventory() == null || user.getInventory().getCategories() == null) {
            return false;
        }

        // Récupérer les ids de l'inventaire
        Set<String> userCategoryIds = user.getInventory().getCategories().stream()
                .map(EquipmentCategory::getId)
                .collect(Collectors.toSet());

        // Vérifier que tous les equipmentIds passés en paramètre sont présents
        return userCategoryIds.containsAll(categoryIds);
    }

    public boolean checkMultipleEquipmentExistsById(String userId, Set<String> equipmentIds) {

        Query query = new Query(Criteria.where("_id").is(userId));
        // On peut utiliser une projection pour ne récupérer que l'inventaire
        query.fields().include("inventory.equipments._id");

        User user = mongoTemplate.findOne(query, User.class, "users");
        if (user == null || user.getInventory() == null || user.getInventory().getEquipments() == null) {
            return false;
        }

        // Récupérer les ids de l'inventaire
        Set<String> userEquipmentIds = user.getInventory().getEquipments().stream()
                .map(Equipment::getId)
                .collect(Collectors.toSet());

        // Vérifier que tous les equipmentIds passés en paramètre sont présents
        return userEquipmentIds.containsAll(equipmentIds);
    }

    public List<Equipment> findEquipmentsByCategory(String userId, String categoryId){

        Query query = new Query(Criteria.where("_id").is(userId));
        query.fields().include("inventory.equipments");

        User user = mongoTemplate.findOne(query, User.class);
        if (user == null || user.getInventory() == null || user.getInventory().getEquipments() == null) {
            return Collections.emptyList();
        }

        return user.getInventory().getEquipments().stream()
                .filter(e -> categoryId.equals(e.getCategoryId()))
                .collect(Collectors.toList());
    }


    public String getCategoryIdByCategoryName(String userId, String categoryName) {
        Query query = new Query(
                Criteria.where("_id").is(userId)
                        .and("inventory.categories").elemMatch(
                                Criteria.where("name").is(categoryName)
                        )
        );
        query.fields().include("inventory.categories");

        Document doc = mongoTemplate.findOne(query, Document.class, "users");
        if (doc == null) {
            return null; // Aucun utilisateur trouvé, ou pas d'inventaire
        }

        // Récupérer l'inventaire depuis le document
        Document inventoryDoc = (Document) doc.get("inventory");
        if (inventoryDoc == null) {
            return null;
        }

        // Vérifier que le champ "categories" est bien une List
        Object categoriesObj = inventoryDoc.get("categories");
        if (!(categoriesObj instanceof List<?> rawList)) {
            return null;
        }

        List<Document> categories = new ArrayList<>();
        // Itérer sur la liste et ne conserver que les instances de Document (éviter l'erreur de compilation xlint ...)
        for (Object o : rawList) {
            if (o instanceof Document) {
                categories.add((Document) o);
            }
        }

        // Parcourir la liste pour trouver la catégorie dont le "name" correspond
        for (Document catDoc : categories) {
            String name = catDoc.getString("name");
            if (categoryName.equals(name)) {
                // On suppose que l'identifiant est stocké sous la clé "_id"
                return catDoc.getString("_id");
            }
        }

        return null;
    }


    public Equipment findEquipmentById(String userId, String equipmentId) {
        // Construire la query pour récupérer uniquement l'inventaire de l'utilisateur
        Query query = new Query(Criteria.where("_id").is(userId));
        query.fields().include("inventory.equipments");

        User user = mongoTemplate.findOne(query, User.class);
        if(user == null || user.getInventory() == null || user.getInventory().getEquipments() == null) {
            return null; // ou lancer une exception si l'utilisateur ou l'inventaire est introuvable
        }

        return user.getInventory().getEquipments()
                .stream()
                .filter(equipment -> equipmentId.equals(equipment.getId()))
                .findFirst()
                .orElse(null);
    }

}
