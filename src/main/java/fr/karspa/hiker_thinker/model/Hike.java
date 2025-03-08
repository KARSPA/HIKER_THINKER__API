package fr.karspa.hiker_thinker.model;

import fr.karspa.hiker_thinker.dtos.responses.HikeResponseDTO;
import fr.karspa.hiker_thinker.utils.InventoryUtils;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Builder
@ToString
@Document(collection = "hikes")
public class Hike {

    @Id
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

    private String ownerId;
    private String modelId;


    public HikeResponseDTO toDTO() {
        return HikeResponseDTO.builder()
                .id(id)
                .title(title)
                .distance(distance)
                .positive(positive)
                .negative(negative)
                .weightCorrection(weightCorrection)
                .duration(duration)
                .durationUnit(durationUnit)
                .date(date)
                .inventory(InventoryUtils.restructureInventory(inventory))
                .build();
    }
}
