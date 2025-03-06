package fr.karspa.hiker_thinker.services;

import fr.karspa.hiker_thinker.dtos.responses.InventoryDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public interface InventoryService {

    ResponseModel<InventoryDTO> findByUserId(String userId); //Récupérer l'inventaire d'un utilisateur

    boolean addEquipment(String category, Equipment equipment); // Ajouter un équipement à l'inventaire

    boolean modifyEquipment(String category, Equipment equipment); // Modifier un équipement de l'inventaire

    boolean removeEquipment(String category, Equipment equipment); // Supprimer un équipement de l'inventaire


}
