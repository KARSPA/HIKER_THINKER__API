package fr.karspa.hiker_thinker.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
@Data
@Builder
@ToString
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;
    private String password;

    private List<String> roles;
    private Boolean active;

    private String firstName;
    private String lastName;

    private Inventory inventory;

}
