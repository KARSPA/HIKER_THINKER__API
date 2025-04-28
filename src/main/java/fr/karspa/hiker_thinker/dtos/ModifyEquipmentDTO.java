package fr.karspa.hiker_thinker.dtos;

import fr.karspa.hiker_thinker.model.Equipment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ModifyEquipmentDTO {

    private Equipment equipment;

    private Boolean hasConsequences;
    private Date consequencesLimitDate;
}
