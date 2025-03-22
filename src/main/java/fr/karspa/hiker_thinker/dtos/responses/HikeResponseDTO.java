package fr.karspa.hiker_thinker.dtos.responses;

import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.Inventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HikeResponseDTO {

    private String id;

    private String title;

    private float distance;
    private int positive;
    private int negative;

    private int weightCorrection;

    private int duration;
    private String durationUnit;

    private Date date;

    private Inventory inventory;

    private int totalWeight;
}
