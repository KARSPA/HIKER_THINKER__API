package fr.karspa.hiker_thinker.dtos;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class EquipmentStatisticsDTO {

    private Integer hikeUsedCount;

    private Float usagePercentage;

    private Float totalDistance;
    private Float averageDistance;

    private Integer totalPositive;
    private Float averagePositive;

    private Integer totalNegative;
    private Float averageNegative;

    private Float totalDurationHours;
    private Float averageDurationHours;

}
