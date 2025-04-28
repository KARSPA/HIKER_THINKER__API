package fr.karspa.hiker_thinker.repository;


import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.dtos.HikeEquipmentDTO;
import fr.karspa.hiker_thinker.dtos.ModifyEquipmentDTO;
import fr.karspa.hiker_thinker.dtos.ReorderEquipmentDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.model.Hike;
import fr.karspa.hiker_thinker.model.Inventory;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class HikeRepository {

    private final MongoClient mongo;
    private MongoTemplate mongoTemplate;

    public void recalculateHikeWeights(String ownerId, String hikeId){
        Query query = Query.query(
                Criteria.where("ownerId").is(ownerId)
                        .and("_id").is(hikeId)
        );

        Hike hike = mongoTemplate.findOne(query, Hike.class);
        if (hike == null || hike.getInventory() == null) {
            return;
        }

        Inventory inv = hike.getInventory();
        List<Equipment> eqs = inv.getEquipments();
        List<EquipmentCategory> cats = inv.getCategories();

        double totalWeight = eqs.stream().mapToInt(Equipment::getWeight).sum();

        Map<String, Integer> sumsByCat = eqs.stream()
                .collect(Collectors.groupingBy(
                        Equipment::getCategoryId,
                        Collectors.summingInt(Equipment::getWeight)
                ));

        cats.forEach(cat -> cat.setAccumulatedWeight(sumsByCat.getOrDefault(cat.getId(), 0)));

        Update update = new Update()
                .set("totalWeight", totalWeight)
                .set("inventory.categories", cats);

        mongoTemplate.updateFirst(query, update, Hike.class);
    }

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

    public List<Hike> findHikesUsedByEquipmentId(String ownerId, String equipmentId){
        Query hikeQuery = new Query(Criteria.where("ownerId").is(ownerId).and("inventory.equipments.sourceId").is(equipmentId));
        List<Hike> hikes = mongoTemplate.find(hikeQuery, Hike.class);
        return hikes;
    }

    public void findAndRecalculateHikesWeightByEquipmentIdAndLimitDate(String ownerId, ModifyEquipmentDTO equipmentDTO){

        // Si la date est nulle => on modifie toutes les randonnées
        Query hikeQuery = new Query(Criteria.where("ownerId").is(ownerId)
                .and("inventory.equipments.sourceId").is(equipmentDTO.getEquipment().getId()));

        // Sinon => on modifie les randonnées postérieures
        if(equipmentDTO.getConsequencesLimitDate() != null){
            hikeQuery.addCriteria(Criteria.where("date").gte(equipmentDTO.getConsequencesLimitDate()));
        }

        List<String> hikeIds = mongoTemplate.find(hikeQuery, Hike.class)
                .stream()
                .map(Hike::getId)
                .toList();

        hikeIds.forEach(hikeId ->
                this.recalculateHikeWeights(ownerId, hikeId)
        );
    }

    public UpdateResult updateHikesEquipment(String ownerId, ModifyEquipmentDTO equipmentDTO){

        // Si la date est nulle => on modifie toutes les randonnées
        Query hikeQuery = new Query(Criteria.where("ownerId").is(ownerId)
                .and("inventory.equipments.sourceId").is(equipmentDTO.getEquipment().getId()));

        // Sinon => on modifie les randonnées postérieures
        if(equipmentDTO.getConsequencesLimitDate() != null){
           hikeQuery.addCriteria(Criteria.where("date").gte(equipmentDTO.getConsequencesLimitDate()));
        }

        Update update = new Update().filterArray(Criteria.where("inventory.equipments.sourceId").is(equipmentDTO.getEquipment().getId()))
                .set("inventory.equipments.$[elem].name",
                        equipmentDTO.getEquipment().getName())
                .set("inventory.equipments.$[elem].description",
                        equipmentDTO.getEquipment().getDescription())
                .set("inventory.equipments.$[elem].brand",
                        equipmentDTO.getEquipment().getBrand())
                .set("inventory.equipments.$[elem].weight",
                        equipmentDTO.getEquipment().getWeight())
                .set("inventory.equipments.$[elem].categoryId",
                        equipmentDTO.getEquipment().getCategoryId())
                .set("inventory.equipments.$[elem].position",
                        0); // On le met au début des équipements de la catégorie

        return mongoTemplate.update(Hike.class)
                .matching(hikeQuery)
                .apply(update)
                .all();
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
    public boolean checkMultipleCategoryExistsById(String ownerId, String hikeId, Set<String> categoryIds) {

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("ownerId").is(ownerId).and("_id").is(new ObjectId(hikeId))),
                Aggregation.unwind("inventory.categories"),
                Aggregation.match(Criteria.where("inventory.categories._id").in(categoryIds)),
                Aggregation.group().count().as("matchedCount")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "hikes", Document.class);
        int matchedCount = results.getMappedResults().isEmpty() ? 0 : results.getMappedResults().get(0).getInteger("matchedCount");

        // Vérifier que le nombre d'équipements trouvés correspond au nombre attendu
        return matchedCount == categoryIds.size();
    }

    public boolean checkMultipleEquipmentExistsById(String ownerId, String hikeId, Set<String> equipmentIds) {

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("ownerId").is(ownerId).and("_id").is(new ObjectId(hikeId))),
                Aggregation.unwind("inventory.equipments"),
                Aggregation.match(Criteria.where("inventory.equipments._id").in(equipmentIds)),
                Aggregation.group().count().as("matchedCount")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(agg, "hikes", Document.class);
        int matchedCount = results.getMappedResults().isEmpty() ? 0 : results.getMappedResults().get(0).getInteger("matchedCount");

        // Vérifier que le nombre d'équipements trouvés correspond au nombre attendu
        return matchedCount == equipmentIds.size();
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

    public UpdateResult modifyEquipmentsOrders(String ownerId, String hikeId, List<ReorderEquipmentDTO> changes) {
        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "hikes");

        for (ReorderEquipmentDTO change : changes) {
            // Pour chaque catégorie, on récupère la liste ordonnée des équipements
            List<String> equipmentIds = change.getOrderedEquipmentIds();
            String categoryId = change.getCategoryId();

            // Parcourir chaque équipement dans la liste pour mettre à jour son index (position)
            for (int i = 0; i < equipmentIds.size(); i++) {
                String equipmentId = equipmentIds.get(i);

                Query query = new Query(Criteria.where("ownerId").is(ownerId).and("_id").is(new ObjectId(hikeId))
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

    public UpdateResult modifyMultipleCategories(String ownerId, String hikeId, List<EquipmentCategory> categoryUpdates) {

        BulkOperations bulkOps = mongoTemplate.bulkOps(BulkOperations.BulkMode.UNORDERED, "hikes");

        // Parcourir les catégories pour mettre à jour leur attribut 'order' (SAUF DEFAULT)
        for (int i = 0; i < categoryUpdates.size(); i++) {
            EquipmentCategory category = categoryUpdates.get(i);

            Query query = new Query(Criteria.where("ownerId").is(ownerId).and("_id").is(new ObjectId(hikeId))
                    .and("inventory.categories._id").is(category.getId()));

            if(!category.getId().equals("DEFAULT")) category.setOrder(i); //On ne modifie pas l'ordre de la catégorie par défaut

            Update update = new Update()
                    .set("inventory.categories.$", category);

            bulkOps.updateOne(query, update);
        }

        BulkWriteResult result = bulkOps.execute();
        return UpdateResult.acknowledged(result.getModifiedCount(), (long) result.getMatchedCount(), null);
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
