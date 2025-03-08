package fr.karspa.hiker_thinker.dtos;

import fr.karspa.hiker_thinker.model.Inventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HikeDTO {

    private String id;

    private String title;

    private float distance;
    private int positive;
    private int negative;

    private int weightCorrection;

    private int duration;
    private String durationUnit;

    private Date date;
}
