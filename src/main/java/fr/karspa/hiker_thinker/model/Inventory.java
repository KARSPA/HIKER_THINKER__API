package fr.karspa.hiker_thinker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    private List<String> categories;

    private List<Equipment> equipments;

}
