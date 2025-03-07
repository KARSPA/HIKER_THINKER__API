package fr.karspa.hiker_thinker.services;

import fr.karspa.hiker_thinker.dtos.responses.InventoryDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import org.springframework.stereotype.Service;

@Service
public interface InventoryService {

    ResponseModel<InventoryDTO> findByUserId(String userId); //Récupérer l'inventaire d'un utilisateur

    ResponseModel<Equipment> addEquipment(String userId, Equipment equipmentDTO); // Ajouter un équipement à l'inventaire

    ResponseModel<Equipment> modifyEquipment(String userId, Equipment equipmentDTO); // Modifier un équipement de l'inventaire

    ResponseModel<Equipment> removeEquipment(String userId, String equipmentId); // Supprimer un équipement de l'inventaire


}
