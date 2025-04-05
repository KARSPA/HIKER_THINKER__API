package fr.karspa.hiker_thinker.dtos.responses;

import lombok.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HikeSummaryDTO {

    private String id;
    private String title;
    private int totalWeight;
    private int weightCorrection;
}
