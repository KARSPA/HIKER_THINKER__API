package fr.karspa.hiker_thinker.repository;


import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.dtos.EquipmentDTO;
import fr.karspa.hiker_thinker.dtos.HikeDTO;
import fr.karspa.hiker_thinker.dtos.HikeEquipmentDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.model.Hike;
import fr.karspa.hiker_thinker.model.User;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
        // Ne pas mettre à jour ownerId, modelId, inventory ou totalWeight !

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

    private boolean checkHikeTitleAvailableCreate(String ownerId, Hike hike){
        Query query = new Query(Criteria.where("ownerId").is(ownerId)
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


    public UpdateResult addEquipmentToEquipmentList(String ownerId, String hikeId, Equipment equipment) {

        return mongoTemplate.update(Hike.class)
                .matching(Criteria.where("ownerId").is(ownerId)
                        .and("_id").is(hikeId)
                        .and("inventory.categories._id").is(equipment.getCategoryId()))
                .apply(new Update()
                        .push("inventory.equipments", equipment)
                        .inc("totalWeight", equipment.getWeight())
                        .inc("inventory.categories.$.accumulatedWeight", equipment.getWeight()))
                .first();
    }

    public UpdateResult removeEquipmentFromEquipmentList(String ownerId, String hikeId, Equipment equipment) {

        // Construire le document de requête (conversion de hikeId si nécessaire)
        Document queryDoc = new Document("ownerId", ownerId)
                .append("_id", new ObjectId(hikeId)); // assurez-vous que hikeId est un ObjectId ou convertissez-le si besoin


        Document updateDoc = new Document();
        updateDoc.put("$inc", new Document("inventory.categories.$[cat].accumulatedWeight", -equipment.getWeight())
                .append("totalWeight", -equipment.getWeight()));
        updateDoc.put("$pull", new Document("inventory.equipments", new Document("_id", equipment.getId())));

        List<Document> arrayFilters = List.of(new Document("cat._id", equipment.getCategoryId()));
        UpdateOptions options = new UpdateOptions().arrayFilters(arrayFilters);

        MongoCollection<Document> collection = mongoTemplate.getCollection("hikes");

        return collection.updateOne(queryDoc, updateDoc, options);

    }

    public UpdateResult addCategoryToCategoryList(String ownerId, String hikeId, EquipmentCategory category) {

        Query query = new Query(Criteria.where("ownerId").is(ownerId).and("_id").is(hikeId));

        Update update = new Update().push("inventory.categories", category);

        return mongoTemplate.updateFirst(query, update, Hike.class);
    }

    // En vrai le filtrage par utilisateur n'est pas vraiment nécessaire étant donné que les document on un identifiant unique MAIS pour éviter de récupérer les trucs des autres ...
    public boolean checkCategoryExistsById(String ownerId, String hikeId, String categoryId) {

        Query query = new Query(
                Criteria.where("ownerId").is(ownerId)
                        .and("_id").is(hikeId)
                        .and("inventory.categories").elemMatch(
                                Criteria.where("_id").is(categoryId)
                        ));

        Document doc = mongoTemplate.findOne(query, Document.class, "hikes");

        return (doc != null);
    }

    public boolean checkEquipmentExistsById(String ownerId, String hikeId, String equipmentId) {

        Query query = new Query(
                Criteria.where("ownerId").is(ownerId)
                        .and("_id").is(hikeId)
                        .and("inventory.equipments").elemMatch(
                                Criteria.where("_id").is(equipmentId)
                        ));

        Document doc = mongoTemplate.findOne(query, Document.class, "hikes");

        return (doc != null);
    }

    public Equipment getEquipmentById(String ownerId, String hikeId, String equipmentId) {

        Query query = new Query(Criteria.where("ownerId").is(ownerId).and("_id").is(hikeId));

        Hike hike = mongoTemplate.findOne(query, Hike.class);

        if (hike == null || hike.getInventory() == null || hike.getInventory().getEquipments() == null) {
            return null;
        }

        return hike.getInventory().getEquipments().stream()
                .filter(e -> equipmentId.equals(e.getId()))
                .findFirst()
                .orElse(null);
    }




    public UpdateResult modifyEquipmentCategory(String ownerId, String hikeId, HikeEquipmentDTO dto, Equipment previousEquipment) {
        Document queryDoc = new Document("ownerId", ownerId)
                .append("_id", new ObjectId(hikeId));

        Document updateDoc = new Document();
        updateDoc.put("$set", new Document("inventory.equipments.$[eq].categoryId", dto.getCategoryId()));
        updateDoc.put("$inc", new Document("inventory.categories.$[newCat].accumulatedWeight", previousEquipment.getWeight())
                .append("inventory.categories.$[oldCat].accumulatedWeight", -previousEquipment.getWeight()));

        List<Document> arrayFilters = List.of(
                new Document("eq._id", dto.getSourceId()),
                new Document("newCat._id", dto.getCategoryId()),
                new Document("oldCat._id", previousEquipment.getCategoryId())
        );
        UpdateOptions options = new UpdateOptions().arrayFilters(arrayFilters);

        MongoCollection<Document> collection = mongoTemplate.getCollection("hikes");

        return collection.updateOne(queryDoc, updateDoc, options);
    }


    public List<EquipmentCategory> getCategories(String ownerId, String hikeId) {
        Query query = new Query(Criteria.where("ownerId").is(ownerId).and("_id").is(hikeId));
        query.fields().include("inventory.categories");

        Hike hike = mongoTemplate.findOne(query, Hike.class);
        if(hike == null || hike.getInventory() == null) {
            return Collections.emptyList();
        }
        return hike.getInventory().getCategories();
    }


    public boolean checkCategoryExistsByName(String ownerId, String hikeId, String category) {

        Query query = new Query(
                Criteria.where("ownerId").is(ownerId)
                        .and("_id").is(hikeId)
                        .and("inventory.categories").elemMatch(
                                Criteria.where("name").is(category)
                        ));

        Document doc = mongoTemplate.findOne(query, Document.class, "hikes");

        return (doc != null);
    }

    public UpdateResult modifyCategoryInCategoryList(String ownerId, String hikeId, EquipmentCategory category) {

        Query query = new Query(
                Criteria.where("ownerId").is(ownerId)
                        .and("_id").is(hikeId)
                        .and("inventory.categories._id").is(category.getId())
        );

        Update update = new Update().set("inventory.categories.$", category);

        return mongoTemplate.updateFirst(query, update, Hike.class);
    }

    public UpdateResult removeCategoryInCategoryList(String ownerId, String hikeId, String categoryId) {

        Query query = new Query(Criteria.where("ownerId").is(ownerId).and("_id").is(hikeId));

        Update update = new Update().pull("inventory.categories", new Document("_id", categoryId));

        return mongoTemplate.updateFirst(query, update, Hike.class);
    }


    public List<Equipment> findEquipmentsByCategory(String ownerId, String hikeId, String categoryId){

        Query query = new Query(Criteria.where("ownerId").is(ownerId).and("_id").is(hikeId));

        Hike hike = mongoTemplate.findOne(query, Hike.class);
        if (hike == null || hike.getInventory() == null || hike.getInventory().getEquipments() == null) {
            return Collections.emptyList();
        }

        return hike.getInventory().getEquipments().stream()
                .filter(e -> categoryId.equals(e.getCategoryId()))
                .collect(Collectors.toList());
    }

}
