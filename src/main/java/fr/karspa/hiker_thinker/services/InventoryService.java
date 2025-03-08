package fr.karspa.hiker_thinker.services;

import fr.karspa.hiker_thinker.dtos.EquipmentDTO;
import fr.karspa.hiker_thinker.dtos.responses.InventoryDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface InventoryService {

    ResponseModel<InventoryDTO> findByUserId(String userId); //Récupérer l'inventaire d'un utilisateur

    ResponseModel<Equipment> addEquipment(String userId, EquipmentDTO equipmentDTO); // Ajouter un équipement à l'inventaire

    ResponseModel<Equipment> modifyEquipment(String userId, Equipment equipment); // Modifier un équipement de l'inventaire

    ResponseModel<Equipment> removeEquipment(String userId, String equipmentId); // Supprimer un équipement de l'inventaire

    ResponseModel<List<EquipmentCategory>> getCategories(String userId);

    ResponseModel<EquipmentCategory> addCategory(String userId, EquipmentCategory category);

    ResponseModel<EquipmentCategory> modifyCategory(String userId, EquipmentCategory category);

}
