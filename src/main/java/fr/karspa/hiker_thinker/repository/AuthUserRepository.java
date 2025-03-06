package fr.karspa.hiker_thinker.repository;

import fr.karspa.hiker_thinker.model.AuthUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthUserRepository extends MongoRepository<AuthUser, String> {

    Optional<AuthUser> findByEmail(String email);
}
