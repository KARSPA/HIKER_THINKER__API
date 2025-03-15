package fr.karspa.hiker_thinker.model;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class EquipmentCategory {

    private String id;

    private String name;

    private String icon;

    private int order;
}
