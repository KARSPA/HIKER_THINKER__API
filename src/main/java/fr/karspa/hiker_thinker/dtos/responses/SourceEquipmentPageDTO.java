package fr.karspa.hiker_thinker.dtos.responses;

import fr.karspa.hiker_thinker.model.SourceEquipment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SourceEquipmentPageDTO {

    private Long totalCount;
    private List<SourceEquipment> equipments = new ArrayList<>();

}
