package fr.karspa.hiker_thinker.dtos;


import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
// Classe modélisant les changements d'ordre et de rangement des équipements dans l'inventaire général, d'une randonnée ou d'un modèle.
public class ReorderEquipmentDTO {
    private String categoryId;
    private List<String> orderedEquipmentIds;
}
