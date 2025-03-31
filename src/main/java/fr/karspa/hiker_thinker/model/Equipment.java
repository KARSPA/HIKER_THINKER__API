package fr.karspa.hiker_thinker.model;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Equipment {

    private String id;

    private String name;
    private String description;
    private String brand;

    private Float weight;

    private String categoryId;

    private String sourceId;

    private int position;

}