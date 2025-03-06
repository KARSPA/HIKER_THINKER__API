package fr.karspa.hiker_thinker.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDTO {

    private Map<String, List<EquipmentDTO>> equipments;
}
