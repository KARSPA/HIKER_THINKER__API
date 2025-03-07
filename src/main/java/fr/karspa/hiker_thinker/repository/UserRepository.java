package fr.karspa.hiker_thinker.repository;

import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.Inventory;
import fr.karspa.hiker_thinker.model.User;
import fr.karspa.hiker_thinker.utils.ResponseModel;
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
public class UserRepository {

    private MongoTemplate mongoTemplate;

    public Map<String, List<Equipment>> findInventoryByUserId(String userId) {
        Query query = new Query(Criteria.where("_id").is(userId));
        query.fields().include("inventory");
        User user = mongoTemplate.findOne(query, User.class);

        if (user == null || user.getInventory() == null) {
            return Collections.emptyMap();
        }

        Inventory inventory = user.getInventory();

        // Groupement des équipements par catégorie
        Map<String, List<Equipment>> grouped = inventory.getEquipments()
                .stream()
                .collect(Collectors.groupingBy(Equipment::getCategory));

        // S'assurer que toutes les catégories définies (même sans équipement) apparaissent dans le résultat
        if (inventory.getCategories() != null) {
            for (String cat : inventory.getCategories()) {
                grouped.putIfAbsent(cat, new ArrayList<>());
            }
        }

        return grouped;
    }


    public UpdateResult modifyEquipment(String userId, Equipment equipment) {

        // Construire la query pour cibler l'utilisateur et l'équipement
        Query query = new Query(
                Criteria.where("_id").is(userId)
                        .and("inventory.equipments._id").is(equipment.getId())
        );

        // Utiliser l'opérateur positional "$" pour remplacer l'équipement trouvé
        Update update = new Update().set("inventory.equipments.$", equipment);

        // Exécuter l'update sans avoir besoin d'arrayFilters
        return mongoTemplate.updateFirst(query, update, User.class);
    }


    public UpdateResult removeEquipment(String userId, String equipmentId) {
        // Construire la query pour cibler l'utilisateur par son _id
        Query query = new Query(Criteria.where("_id").is(userId));
        
        Update update = new Update().pull("inventory.equipments", new Document("_id", equipmentId));

        return mongoTemplate.updateFirst(query, update, User.class);
    }



    public UpdateResult addEquipmentToEquipmentList(String userId, Equipment equipment) {
        // Construire la requête pour trouver l'utilisateur par son _id
        Query query = new Query(Criteria.where("_id").is(userId));

        // Construire l'update en utilisant l'opérateur $push sur le champ "inventory.<category>"
        Update update = new Update().push("inventory.equipments", equipment);

        // Effectuer la mise à jour dans la collection "users"
        return mongoTemplate.updateFirst(query, update, User.class);
    }

    public UpdateResult addCategoryToCategoryList(String userId, String category) {

        // Construire la requête pour trouver l'utilisateur par son _id
        Query query = new Query(Criteria.where("_id").is(userId));

        // Construire l'update en utilisant l'opérateur $push sur le champ "inventory.<category>"
        Update update = new Update().push("inventory.categories", category);

        // Effectuer la mise à jour dans la collection "users"
        return mongoTemplate.updateFirst(query, update, User.class);
    }

    public boolean checkAvailableEquipmentName(String userId, Equipment equipment) {

        //SI id dans l'équipement est passé c'est qu'on modifie
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

        // On récupère le document utilisateur avec un champ spécifique
        Document doc = mongoTemplate.findOne(query, Document.class, "users");

        return (doc != null);
    }

    public boolean checkCategoryExistsByName(String userId, Equipment equipment) {

        Query query = new Query(
                Criteria.where("_id").is(userId)
                        .and("inventory.categories").is(equipment.getCategory()));

        Document doc = mongoTemplate.findOne(query, Document.class, "users");

        return (doc != null);
    }


}
