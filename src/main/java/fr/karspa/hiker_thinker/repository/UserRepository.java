package fr.karspa.hiker_thinker.repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.dtos.AddEquipmentDTO;
import fr.karspa.hiker_thinker.dtos.responses.EquipmentDTO;
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
public class UserRepository {

    private MongoTemplate mongoTemplate;

    public Map<String, List<EquipmentDTO>> findInventoryByUserId(String userId) {
        Query query = new Query(Criteria.where("_id").is(userId));
        query.fields().include("inventory").exclude("_id");

        Document doc = mongoTemplate.findOne(query, Document.class, "users");
        if (doc != null) {
            Map<String, List<Document>> rawInventory = (Map<String, List<Document>>) doc.get("inventory");
            Map<String, List<EquipmentDTO>> inventory = new HashMap<>();
            for (Map.Entry<String, List<Document>> entry : rawInventory.entrySet()) {
                List<EquipmentDTO> equipmentList = entry.getValue().stream()
                        .map(document -> mongoTemplate.getConverter().read(EquipmentDTO.class, document))
                        .collect(Collectors.toList());
                inventory.put(entry.getKey(), equipmentList);
            }
            return inventory;

        }
        return null;
    }


    public UpdateResult addEquipment(String userId, AddEquipmentDTO addEquipmentDTO) {
        // Récupérer la catégorie et l'équipement à ajouter depuis le DTO
        String category = addEquipmentDTO.getCategory();
        EquipmentDTO equipment = addEquipmentDTO.getEquipment();

        //Générer un nouvel identifiant unique pour cet équipement
        String uniqueId = UUID.randomUUID().toString();
        equipment.setId(uniqueId);

        // Construire la requête pour trouver l'utilisateur par son _id
        Query query = new Query(Criteria.where("_id").is(userId));

        // Construire l'update en utilisant l'opérateur $push sur le champ "inventory.<category>"
        Update update = new Update().push("inventory." + category, equipment);

        System.err.println(update);

        // Effectuer la mise à jour dans la collection "users"
        return mongoTemplate.updateFirst(query, update, User.class);

    }

    public UpdateResult modifyEquipment(String userId, AddEquipmentDTO addEquipmentDTO) {
        // Récupérer la catégorie et l'équipement à ajouter depuis le DTO
        String category = addEquipmentDTO.getCategory();
        EquipmentDTO equipment = addEquipmentDTO.getEquipment();

        // Construire la query pour cibler l'utilisateur et l'équipement dans l'array de la catégorie
        Query query = new Query(
                Criteria.where("_id").is(userId)
                        .and("inventory." + category + "._id").is(equipment.getId())
        );

        // Utiliser l'opérateur positional "$" pour remplacer l'équipement trouvé
        Update update = new Update().set("inventory." + category + ".$", equipment);

        // Exécuter l'update sans avoir besoin d'arrayFilters
        return mongoTemplate.updateFirst(query, update, User.class);
    }


    public boolean checkAvailableEquipmentName(String userId, AddEquipmentDTO addEquipmentDTO) {
        // Récupérer la catégorie et l'équipement à ajouter depuis le DTO
        String category = addEquipmentDTO.getCategory();
        EquipmentDTO equipment = addEquipmentDTO.getEquipment();

        //SI id dans l'équipement est passé c'est qu'on modifie
        System.err.println(equipment);
        if(equipment.getId() != null){
            return this.checkAvailableEquipmentNameModify(userId, category, equipment);
        }else{
            return this.checkAvailableEquipmentNameAdd(userId, category, equipment);
        }
    }

    private boolean checkAvailableEquipmentNameAdd(String userId, String category, EquipmentDTO equipment) {
        Query query = new Query(Criteria.where("_id").is(userId)
                .and("inventory."+category+".name").is(equipment.getName()));

        Document doc = mongoTemplate.findOne(query, Document.class, "users");

        return (doc == null);
    }

    private boolean checkAvailableEquipmentNameModify(String userId, String category, EquipmentDTO equipment) {
        Query query = new Query(
                Criteria.where("_id").is(userId)
                        .and("inventory." + category).elemMatch(
                                Criteria.where("name").is(equipment.getName())
                                        .and("_id").ne(equipment.getId())
                        ));

        Document doc = mongoTemplate.findOne(query, Document.class, "users");

        return (doc == null);
    }

    public boolean checkEquipmentExistsById(String userId, AddEquipmentDTO addEquipmentDTO) {
        // Récupérer la catégorie et l'équipement à ajouter depuis le DTO
        String category = addEquipmentDTO.getCategory();
        EquipmentDTO equipment = addEquipmentDTO.getEquipment();

        Query query = new Query(Criteria.where("_id").is(userId)
                .and("inventory."+category+"._id").is(equipment.getId()));

        // On récupère le document utilisateur avec un champ spécifique
        Document doc = mongoTemplate.findOne(query, Document.class, "users");

        return (doc != null);
    }


}
