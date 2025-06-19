package fr.karspa.hiker_thinker.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.dtos.ReorderEquipmentDTO;
import fr.karspa.hiker_thinker.dtos.filters.EquipmentSearchDTO;
import fr.karspa.hiker_thinker.dtos.responses.EquipmentPageDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.model.Inventory;
import fr.karspa.hiker_thinker.model.User;
import org.bson.Document;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
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
import java.util.regex.Pattern;
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

    public EquipmentPageDTO getInventoryWithFilters(String userId, EquipmentSearchDTO filter) {
        // 1) build des conditions $and pour $filter
        List<Document> conds = new ArrayList<>();

        // — filtre sur le nom (mots séparés)
        if (trimNotBlank(filter.getName())) {
            for (String term : filter.getName().trim().split("\\s+")) {
                conds.add(new Document(
                        "$regexMatch",
                        new Document("input", "$$eq.name")
                                .append("regex", ".*" + Pattern.quote(term) + ".*")
                                .append("options", "i")
                ));
            }
        }

        // — filtre sur la marque
        if (trimNotBlank(filter.getBrand())) {
            conds.add(new Document(
                    "$regexMatch",
                    new Document("input", "$$eq.brand")
                            .append("regex", ".*" + Pattern.quote(filter.getBrand()) + ".*")
                            .append("options", "i")
            ));
        }

        // — filtre poids min
        if (filter.getMinWeight() != null) {
            conds.add(new Document("$gte", List.of("$$eq.weight", filter.getMinWeight())));
        }
        // — filtre poids max
        if (filter.getMaxWeight() != null) {
            conds.add(new Document("$lte", List.of("$$eq.weight", filter.getMaxWeight())));
        }

        // 2) expression $filter pour ne garder que les équipements valides
        Document filterExpr = new Document(
                "$filter",
                new Document("input", "$inventory.equipments")
                        .append("as",   "eq")
                        .append("cond", new Document("$and", conds))
        );

        // 3) projection initiale : filteredEquipments + totalCount
        Document projectStage = new Document("$project", new Document()
                .append("totalCount", new Document("$size", filterExpr))
                .append("filtered",  filterExpr)
        );

        // 4) pagination et tri sur l’array filtered
        int skip = filter.getPageNumber() * filter.getPageSize();
        int limit = filter.getPageSize();
        int sortDirection = filter.getSortDir().equalsIgnoreCase("ASC") ? 1 : -1;
        String sortField = switch(filter.getSortBy()) {
            case "weight" -> "weight";
            default        -> "name";
        };

        Document pagedStage = new Document("$project", new Document()
                // on recopie totalCount
                .append("totalCount", "$totalCount")
                // tri + slice
                .append("equipments", new Document("$slice", List.of(
                        new Document(
                                "$sortArray",
                                new Document("input",  "$filtered")
                                        .append("sortBy", new Document(sortField, sortDirection))
                        ),
                        skip,
                        limit
                )))
        );

        // 5) on fabrique le pipeline
        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id").is(userId)),
                context -> projectStage,
                context -> pagedStage
        );

        // 6) on exécute et on mappe dans notre DTO
        AggregationResults<EquipmentPageDTO> res =
                mongoTemplate.aggregate(agg, "users", EquipmentPageDTO.class);

        EquipmentPageDTO page = res.getUniqueMappedResult();
        return page != null
                ? page
                : new EquipmentPageDTO(0L, List.of());
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
                    eq.setWeight(weight.intValue());
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

        User user = mongoTemplate.findOne(query, User.class);
        if(user == null || user.getInventory() == null) {
            return Collections.emptyList();
        }
        return user.getInventory().getCategories();
    }

    public EquipmentCategory getCategoryById(String userId, String categoryId) {
        // Construire la query pour récupérer uniquement l'inventaire de l'utilisateur
        Query query = new Query(Criteria.where("_id").is(userId));
        query.fields().include("inventory.categories");

        User user = mongoTemplate.findOne(query, User.class);
        if(user == null || user.getInventory() == null || user.getInventory().getCategories() == null) {
            return null;
        }
        return user.getInventory().getCategories()
                .stream()
                .filter(category -> categoryId.equals(category.getId()))
                .findFirst()
                .orElse(null);
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

    public UpdateResult modifyMultipleCategories(String userId, List<EquipmentCategory> categoryUpdates) {

        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "users");

        // Parcourir les catégories pour mettre à jour leur attribut 'order' (SAUF DEFAULT)
        for (int i = 0; i < categoryUpdates.size(); i++) {
            EquipmentCategory category = categoryUpdates.get(i);

            Query query = new Query(Criteria.where("_id").is(userId)
                    .and("inventory.categories._id").is(category.getId()));

            if(category.getId().equals("DEFAULT")) continue;

            category.setOrder(i);

            Update update = new Update()
                    .set("inventory.categories.$", category);

            bulkOps.updateOne(query, update);
        }

        BulkWriteResult result = bulkOps.execute();
        return UpdateResult.acknowledged(result.getModifiedCount(), (long) result.getMatchedCount(), null);
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

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id").is(new ObjectId(userId))),
                Aggregation.unwind("inventory.categories"),
                Aggregation.match(Criteria.where("inventory.categories._id").in(categoryIds)),
                Aggregation.group().count().as("matchedCount")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "users", Document.class);
        int matchedCount = results.getMappedResults().isEmpty() ? 0 : results.getMappedResults().get(0).getInteger("matchedCount");

        // Vérifier que le nombre d'équipements trouvés correspond au nombre attendu
        return matchedCount == categoryIds.size();
    }

    public boolean checkMultipleEquipmentExistsById(String userId, Set<String> equipmentIds) {

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id").is(new ObjectId(userId))),
                Aggregation.unwind("inventory.equipments"),
                Aggregation.match(Criteria.where("inventory.equipments._id").in(equipmentIds)),
                Aggregation.group().count().as("matchedCount")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "users", Document.class);
        int matchedCount = results.getMappedResults().isEmpty() ? 0 : results.getMappedResults().get(0).getInteger("matchedCount");

        // Vérifier que le nombre d'équipements trouvés correspond au nombre attendu
        return matchedCount == equipmentIds.size();
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


    private boolean trimNotBlank(String s) {
        return s != null && !s.isBlank();
    }
}
