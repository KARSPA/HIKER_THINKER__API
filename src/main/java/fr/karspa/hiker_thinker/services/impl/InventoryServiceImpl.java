package fr.karspa.hiker_thinker.services.impl;

import fr.karspa.hiker_thinker.dtos.responses.InventoryDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.repository.AuthUserRepository;
import fr.karspa.hiker_thinker.repository.UserRepository;
import fr.karspa.hiker_thinker.services.InventoryService;
import fr.karspa.hiker_thinker.utils.ResponseModel;
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
    public boolean addEquipment(String category, Equipment equipment) {
        return false;
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
