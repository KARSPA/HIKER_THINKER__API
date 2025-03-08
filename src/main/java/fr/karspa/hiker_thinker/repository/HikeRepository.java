package fr.karspa.hiker_thinker.repository;


import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.model.Hike;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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

    public Hike createOneHike(Hike hike){
        return mongoTemplate.insert(hike);
    }





    public boolean checkHikeTitleAvailable(String ownerId, String hikeTitle){
        Query query = new Query(Criteria.where("ownerId").is(ownerId)
                .and("title").is(hikeTitle));

        Document doc = mongoTemplate.findOne(query, Document.class, "hikes");

        System.err.println(doc);
        return (doc == null);
    }

}
