package fr.karspa.hiker_thinker.repository;

import fr.karspa.hiker_thinker.model.AuthUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthUserRepository extends MongoRepository<AuthUser, String> {

    Optional<AuthUser> findByEmail(String email);

    @Query(value = "{ 'email' : ?0 }", fields = "{ 'inventory' : 0 }")
    Optional<AuthUser> findByEmailForAuth(String email);
}
