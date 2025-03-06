package fr.karspa.hiker_thinker.services.impl;

import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.dtos.responses.EquipmentDTO;
import fr.karspa.hiker_thinker.dtos.AddEquipmentDTO;
import fr.karspa.hiker_thinker.dtos.responses.InventoryDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.repository.UserRepository;
import fr.karspa.hiker_thinker.services.InventoryService;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
public class InventoryServiceImpl implements InventoryService {

    private UserRepository userRepository;

    public InventoryServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ResponseModel<InventoryDTO> findByUserId(String userId) {

        var inventory = userRepository.findInventoryByUserId(userId);

        if(inventory == null)
            return ResponseModel.buildResponse("710", "Aucun inventaire disponible.", null);


        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setEquipments(inventory);

        return ResponseModel.buildResponse("200", "Inventaire récupéré avec succès.", inventoryDTO);
    }

    @Override
    public ResponseModel<EquipmentDTO> addEquipment(String userId, AddEquipmentDTO addEquipmentDTO) {

        UpdateResult result = userRepository.addEquipment(userId, addEquipmentDTO);

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("201", "Équipement ajouté avec succès.", addEquipmentDTO.getEquipment());
        } else {
            return ResponseModel.buildResponse("404", "Utilisateur non trouvé.", null);
        }
    }

    @Override
    public boolean modifyEquipment(String category, Equipment equipment) {
        return false;
    }

    @Override
    public boolean removeEquipment(String category, Equipment equipment) {
        return false;
    }
}
