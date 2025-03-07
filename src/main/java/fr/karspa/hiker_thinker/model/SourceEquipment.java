package fr.karspa.hiker_thinker.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "sEquipments")
public class SourceEquipment {

    @MongoId
    private String id;

    private String name;
    private String description;
    private String brand;

    private Float weight;

    private String category;

}
