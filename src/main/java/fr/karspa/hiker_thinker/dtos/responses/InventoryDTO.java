package fr.karspa.hiker_thinker.dtos.responses;

import fr.karspa.hiker_thinker.model.Equipment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryDTO {

    private Map<String, List<Equipment>> equipments;
}
