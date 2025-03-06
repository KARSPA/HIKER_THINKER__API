package fr.karspa.hiker_thinker.repository;

import fr.karspa.hiker_thinker.model.Equipment;
import org.bson.Document;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@AllArgsConstructor
public class UserRepository {

    private MongoTemplate mongoTemplate;

    public Map<String, List<Equipment>> findInventoryByUserId(String userId) {
        Query query = new Query(Criteria.where("_id").is(userId));
        query.fields().include("inventory").exclude("_id");

        Document doc = mongoTemplate.findOne(query, Document.class, "users");
        if (doc != null) {
            return (Map<String, List<Equipment>>) doc.get("inventory");
        }
        return null;
    }


}
