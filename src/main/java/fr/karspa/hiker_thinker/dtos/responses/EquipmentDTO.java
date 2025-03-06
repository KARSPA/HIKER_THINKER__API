package fr.karspa.hiker_thinker.dtos.responses;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class EquipmentDTO {

    private String id;

    private String name;
    private String description;
    private String brand;

    private Float weight;

    private String sourceId;
}
