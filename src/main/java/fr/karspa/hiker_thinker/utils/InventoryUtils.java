package fr.karspa.hiker_thinker.utils;

import fr.karspa.hiker_thinker.dtos.responses.InventoryDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.model.Inventory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class InventoryUtils {

    public static InventoryDTO restructureInventory(Inventory inventory) {

        // Créer une map de correspondance : id -> nom de la catégorie
        Map<String, String> catIdToName = inventory.getCategories().stream()
                .collect(Collectors.toMap(EquipmentCategory::getId, EquipmentCategory::getName));

        // Groupement des équipements en utilisant le nom de la catégorie
        Map<String, List<Equipment>> grouped = inventory.getEquipments().stream()
                .collect(Collectors.groupingBy(equipment ->
                        catIdToName.getOrDefault(equipment.getCategoryId(), equipment.getCategoryId())));

        // S'assurer que toutes les catégories définies (même sans équipement) apparaissent dans le résultat
        for (EquipmentCategory cat : inventory.getCategories()) {
            grouped.putIfAbsent(cat.getName(), new ArrayList<>());
        }

        return new InventoryDTO(grouped);
    }
}
