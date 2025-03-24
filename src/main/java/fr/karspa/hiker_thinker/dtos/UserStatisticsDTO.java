package fr.karspa.hiker_thinker.dtos;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserStatisticsDTO {

    private int hikeCount;

    private float totalDistance;
    private float averageDistance;

    private int totalPositive;
    private float averagePositive;

    private int totalNegative;
    private float averageNegative;

    private float averageWeight;

    private float totalDurationHours;
    private float averageDurationHours;

}
