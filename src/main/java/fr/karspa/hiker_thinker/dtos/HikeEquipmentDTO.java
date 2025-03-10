package fr.karspa.hiker_thinker.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HikeEquipmentDTO {

    private String categoryId;

    private String sourceId;
}
