package fr.karspa.hiker_thinker.dtos;

import fr.karspa.hiker_thinker.model.Equipment;
import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor

public class EquipmentDTO {

    private String name;
    private String description;
    private String brand;

    private Integer weight;

    private String categoryName;

    private String sourceId;



    public Equipment mapToEntity(){
        Equipment equipment = new Equipment();
        equipment.setName(name);
        equipment.setDescription(description);
        equipment.setBrand(brand);
        equipment.setWeight(weight);
        equipment.setSourceId(sourceId);
        return equipment;
    }

}
