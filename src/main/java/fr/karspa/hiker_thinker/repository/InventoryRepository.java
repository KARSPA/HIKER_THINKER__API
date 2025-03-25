package fr.karspa.hiker_thinker.repository;

import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.model.Inventory;
import fr.karspa.hiker_thinker.model.User;
import org.bson.Document;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
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

        System.out.println(category);
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
