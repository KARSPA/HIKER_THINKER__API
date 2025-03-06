package fr.karspa.hiker_thinker.model;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Map;

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

    private int duration;
    private String durationUnit;

    private Date date;

    private Map<String, List<Equipment>> equipments;

    private String ownerId;
}
