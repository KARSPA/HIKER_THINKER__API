package fr.karspa.hiker_thinker.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Builder
@Document(collection = "equipments")
public class Equipment {

    @MongoId
    private String id;

    private String name;
    private String description;
    private float weight;

    private String category;
}
