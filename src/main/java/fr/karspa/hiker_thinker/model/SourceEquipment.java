package fr.karspa.hiker_thinker.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "equipments")
public class SourceEquipment {

    @MongoId
    private String id;

    private String url;
    private String name;
    private String description;
    private String brand;

    @Field("weight_g")
    private Integer weight;
}
