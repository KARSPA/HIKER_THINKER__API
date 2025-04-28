package fr.karspa.hiker_thinker.dtos.responses;

import fr.karspa.hiker_thinker.model.Equipment;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class EquipmentDetailsDTO{

    private String id;

    private String name;
    private String description;
    private String brand;

    private Integer weight;

    private String categoryId;
    private String categoryName;

    private String sourceId;

    private int position;

    private List<HikeSummaryDTO> hikes = new ArrayList<>();


    public EquipmentDetailsDTO fromEquipment(Equipment equipment, String categoryName){
        this.id = equipment.getId();
        this.name = equipment.getName();
        this.description = equipment.getDescription();
        this.brand = equipment.getBrand();
        this.weight = equipment.getWeight();
        this.categoryId = equipment.getCategoryId();
        this.categoryName = categoryName;
        this.sourceId = equipment.getSourceId();
        this.position = equipment.getPosition();
        return this;
    }
}
