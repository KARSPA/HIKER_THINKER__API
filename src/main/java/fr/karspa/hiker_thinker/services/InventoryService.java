package fr.karspa.hiker_thinker.services;

import fr.karspa.hiker_thinker.dtos.responses.EquipmentDTO;
import fr.karspa.hiker_thinker.dtos.AddEquipmentDTO;
import fr.karspa.hiker_thinker.dtos.responses.InventoryDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import org.springframework.stereotype.Service;

@Service
public interface InventoryService {

    ResponseModel<InventoryDTO> findByUserId(String userId); //Récupérer l'inventaire d'un utilisateur

    ResponseModel<EquipmentDTO> addEquipment(String userId, AddEquipmentDTO addEquipmentDTO); // Ajouter un équipement à l'inventaire

    ResponseModel<EquipmentDTO> modifyEquipment(String userId, AddEquipmentDTO addEquipmentDTO); // Modifier un équipement de l'inventaire

    ResponseModel<EquipmentDTO> removeEquipment(String userId, AddEquipmentDTO addEquipmentDTO); // Supprimer un équipement de l'inventaire


}
