package fr.karspa.hiker_thinker.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {

    private List<EquipmentCategory> categories;

    private List<Equipment> equipments;


    public static Inventory getDefaultInventory(){

        EquipmentCategory defaultCat = new EquipmentCategory("DEFAULT", "Sans catégorie", "no_icon", 100, 0);

        return new Inventory(List.of(defaultCat), new ArrayList<>());

    }

}
