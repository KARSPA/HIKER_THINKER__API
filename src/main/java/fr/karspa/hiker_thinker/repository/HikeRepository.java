package fr.karspa.hiker_thinker.repository;


import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.dtos.EquipmentDTO;
import fr.karspa.hiker_thinker.dtos.HikeDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.model.Hike;
import fr.karspa.hiker_thinker.model.User;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@AllArgsConstructor
public class HikeRepository {

    private MongoTemplate mongoTemplate;


    public List<Hike> findAll(String ownerId, boolean withInventory){
        Query query = new Query(Criteria.where("ownerId").is(ownerId));
        if (!withInventory) {
            query.fields().exclude("inventory");
        }
        return mongoTemplate.find(query, Hike.class);
    }

    public Hike findOne(String ownerId, String hikeId){
        Query query = new Query(Criteria.where("ownerId").is(ownerId).and("_id").is(hikeId));

        return mongoTemplate.findOne(query, Hike.class);
    }

    public Hike createOneHike(Hike hike){
        return mongoTemplate.insert(hike);
    }

    public UpdateResult updateOneHike(Hike hike){
        Query query = new Query(Criteria.where("_id").is(hike.getId()));
        Update update = new Update();
        update.set("title", hike.getTitle());
        update.set("distance", hike.getDistance());
        update.set("positive", hike.getPositive());
        update.set("negative", hike.getNegative());
        update.set("weightCorrection", hike.getWeightCorrection());
        update.set("duration", hike.getDuration());
        update.set("durationUnit", hike.getDurationUnit());
        update.set("date", hike.getDate());
        // Ne pas mettre à jour ownerId, modelId ou inventory !

        return mongoTemplate.updateFirst(query, update, Hike.class);

    }

    public Hike deleteOneHike(String ownerId, String hikeId){

        Query query = new Query(Criteria.where("ownerId").is(ownerId).and("_id").is(hikeId));

        return mongoTemplate.findAndRemove(query, Hike.class);
    }





    public boolean checkHikeTitleAvailable(String ownerId, Hike hike){

        if(hike.getId() != null){
            return this.checkHikeTitleAvailableModify(ownerId, hike);
        }else{
            return this.checkHikeTitleAvailableCreate(ownerId,  hike);
        }


    }

    private boolean checkHikeTitleAvailableCreate(String userId, Hike hike){
        Query query = new Query(Criteria.where("ownerId").is(hike.getOwnerId())
                .and("title").is(hike.getTitle()));

        Document doc = mongoTemplate.findOne(query, Document.class, "hikes");

        return (doc == null);
    }

    public boolean checkHikeTitleAvailableModify(String ownerId, Hike hike){
        Query query = new Query(Criteria.where("ownerId").is(ownerId)
                .and("title").is(hike.getTitle())
                .and("_id").ne(hike.getId()));

        Document doc = mongoTemplate.findOne(query, Document.class, "hikes");

        return (doc == null);
    }

    public boolean checkHikeExistsById(String ownerId, String hikeId){
        Query query = new Query(Criteria.where("ownerId").is(ownerId)
                .and("_id").is(hikeId));

        Document doc = mongoTemplate.findOne(query, Document.class, "hikes");

        return (doc != null);
    }

    public boolean checkAvailableEquipmentName(String ownerId, String hikeId, Equipment equipment){
        Query query = new Query(Criteria.where("ownerId").is(ownerId)
                .and("_id").is(hikeId)
                .and("inventory.equipments").elemMatch(Criteria.where("name").is(equipment.getName())));

        Document doc = mongoTemplate.findOne(query, Document.class, "hikes");

        return (doc == null);
    }

    public UpdateResult addEquipmentToEquipmentList(String ownerId, String hikeId, Equipment equipment) {
        Query query = new Query(Criteria.where("ownerId").is(ownerId).and("_id").is(hikeId));

        Update update = new Update().push("inventory.equipments", equipment);

        return mongoTemplate.updateFirst(query, update, Hike.class);
    }

    public UpdateResult addCategoryToCategoryList(String ownerId, String hikeId, EquipmentCategory category) {

        Query query = new Query(Criteria.where("ownerId").is(ownerId).and("_id").is(hikeId));

        Update update = new Update().push("inventory.categories", category);

        return mongoTemplate.updateFirst(query, update, Hike.class);
    }

    // En vrai le filtrage par utilisateur n'est pas vraiment nécessaire étant donné que les document on un identifiant unique MAIS pour éviter de récupérer les trucs des autres ...
    public boolean checkCategoryExistsById(String userId, String hikeId, String categoryId) {

        Query query = new Query(
                Criteria.where("ownerId").is(userId)
                        .and("_id").is(hikeId)
                        .and("inventory.categories").elemMatch(
                                Criteria.where("_id").is(categoryId)
                        ));

        Document doc = mongoTemplate.findOne(query, Document.class, "hikes");

        return (doc != null);
    }




}
