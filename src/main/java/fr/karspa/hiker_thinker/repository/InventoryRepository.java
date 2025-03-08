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

    public Map<String, List<Equipment>> getInventory(String userId) {
        Query query = new Query(Criteria.where("_id").is(userId));
        query.fields().include("inventory");
        User user = mongoTemplate.findOne(query, User.class);

        if (user == null || user.getInventory() == null) {
            return Collections.emptyMap();
        }

        Inventory inventory = user.getInventory();

        // Créer une map de correspondance : id -> nom de la catégorie
        Map<String, String> catIdToName = inventory.getCategories().stream()
                .collect(Collectors.toMap(EquipmentCategory::getId, EquipmentCategory::getName));

        // Groupement des équipements en utilisant le nom de la catégorie
        Map<String, List<Equipment>> grouped = inventory.getEquipments().stream()
                .collect(Collectors.groupingBy(equipment ->
                        catIdToName.getOrDefault(equipment.getCategoryId(), equipment.getCategoryId())));

        // S'assurer que toutes les catégories définies (même sans équipement) apparaissent dans le résultat
        for (EquipmentCategory cat : inventory.getCategories()) {
            grouped.putIfAbsent(cat.getName(), new ArrayList<>());
        }

        return grouped;
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

    public UpdateResult addCategoryToCategoryList(String userId, EquipmentCategory category) {

        Query query = new Query(Criteria.where("_id").is(userId));

        Update update = new Update().push("inventory.categories", category);

        return mongoTemplate.updateFirst(query, update, User.class);
    }

//    public UpdateResult modifyCategory(String userId, String category) {
//
//
//        //Modifier le nom de la catégorie
//
//
//        //Modifier le nom de la catégorie de chaque équipement avec l'ancien nom.
//
//        Query query = new Query(Criteria.where("_id").is(userId));
//
//        Update update = new Update().push("inventory.categories", category);
//
//        return mongoTemplate.updateFirst(query, update, User.class);
//    }

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

    public boolean checkEquipmentExistsById(String userId, Equipment equipment) {

        Query query = new Query(Criteria.where("_id").is(userId)
                .and("inventory.equipments._id").is(equipment.getId()));

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


    public String getCategoryIdByCategoryName(String userId, String categoryName) {
        Query query = new Query(
                Criteria.where("_id").is(userId)
                        .and("inventory.categories").elemMatch(
                                Criteria.where("name").is(categoryName)));
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

        // La liste des catégories est supposée être stockée sous forme de List<Document>
        List<Document> categories = (List<Document>) inventoryDoc.get("categories");
        if (categories == null) {
            return null;
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


}
