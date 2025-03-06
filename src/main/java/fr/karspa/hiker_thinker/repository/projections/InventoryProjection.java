package fr.karspa.hiker_thinker.repository.projections;

import fr.karspa.hiker_thinker.model.Equipment;

import java.util.List;
import java.util.Map;

public interface InventoryProjection {
    Map<String, List<Equipment>> getInventory();
}
