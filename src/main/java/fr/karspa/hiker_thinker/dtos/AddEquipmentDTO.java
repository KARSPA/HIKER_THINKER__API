package fr.karspa.hiker_thinker.dtos;

import fr.karspa.hiker_thinker.dtos.responses.EquipmentDTO;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddEquipmentDTO {

    private String category;

    private EquipmentDTO equipment;
}
