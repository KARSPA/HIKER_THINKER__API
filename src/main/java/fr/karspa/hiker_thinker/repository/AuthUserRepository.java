package fr.karspa.hiker_thinker.repository;

import fr.karspa.hiker_thinker.model.User;
import fr.karspa.hiker_thinker.repository.projections.InventoryProjection;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthUserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    @Query(value = "{ 'email' : ?0 }", fields = "{ 'inventory' : 0 }")
    Optional<User> findByEmailForAuth(String email);

    @Query(value = "{ 'id' : ?0 }", fields = "{ 'inventory' : 1, '_id' : 0}")
    Optional<InventoryProjection> findInventoryById(String id);


}
