package fr.karspa.hiker_thinker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EquipmentCategory {

    private String id;

    private String name;

    private String icon;
}
