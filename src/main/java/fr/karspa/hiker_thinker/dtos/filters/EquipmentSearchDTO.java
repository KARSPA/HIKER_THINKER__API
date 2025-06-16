package fr.karspa.hiker_thinker.dtos.filters;

import lombok.*;


@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EquipmentSearchDTO {

    private String name;
    private String brand;

    private Integer minWeight;
    private Integer maxWeight;

    private Integer pageNumber;
    private Integer pageSize;

    private String sortBy;
    private String sortDir;

}
