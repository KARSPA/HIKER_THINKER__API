package fr.karspa.hiker_thinker.repository;

import fr.karspa.hiker_thinker.model.AuthUser;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class UserRepository {

    private MongoTemplate mongoTemplate;

    public AuthUser findUserForLogin(String email) {
        Query query = new Query(Criteria.where("email").is(email));

        query.fields().exclude("inventory");
        return mongoTemplate.findOne(query, AuthUser.class);
    }


}
