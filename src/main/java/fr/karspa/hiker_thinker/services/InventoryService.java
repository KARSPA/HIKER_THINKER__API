package fr.karspa.hiker_thinker.services;

import fr.karspa.hiker_thinker.dtos.responses.EquipmentDTO;
import fr.karspa.hiker_thinker.dtos.responses.InventoryDTO;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import org.springframework.stereotype.Service;

@Service
public interface InventoryService {

    ResponseModel<InventoryDTO> findByUserId(String userId); //Récupérer l'inventaire d'un utilisateur

    ResponseModel<EquipmentDTO> addEquipment(String userId, EquipmentDTO equipmentDTO); // Ajouter un équipement à l'inventaire

    ResponseModel<EquipmentDTO> modifyEquipment(String userId, EquipmentDTO equipmentDTO); // Modifier un équipement de l'inventaire

    ResponseModel<EquipmentDTO> removeEquipment(String userId, String equipmentId); // Supprimer un équipement de l'inventaire


}
