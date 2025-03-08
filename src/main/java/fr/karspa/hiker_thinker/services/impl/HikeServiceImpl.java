package fr.karspa.hiker_thinker.services.impl;

import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.dtos.EquipmentDTO;
import fr.karspa.hiker_thinker.dtos.responses.HikeResponseDTO;
import fr.karspa.hiker_thinker.dtos.responses.InventoryDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.model.Hike;
import fr.karspa.hiker_thinker.model.Inventory;
import fr.karspa.hiker_thinker.repository.HikeRepository;
import fr.karspa.hiker_thinker.services.HikeService;
import fr.karspa.hiker_thinker.utils.InventoryUtils;
import fr.karspa.hiker_thinker.utils.RandomGenerator;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class HikeServiceImpl implements HikeService {

    private HikeRepository hikeRepository;

    @Override
    public ResponseModel<List<Hike>> findAll(String ownerId, boolean withInventory) {

        // Récupérer tous les documents de "hikes" avec le ownerId passé en paramètre.
        List<Hike> hikes = hikeRepository.findAll(ownerId, withInventory);

        return ResponseModel.buildResponse("200", "Randonnées récupérées avec succès", hikes);
    }

    @Override
    public ResponseModel<HikeResponseDTO> findByHikeId(String ownerId, String hikeId) {
        Hike hike = hikeRepository.findOne(ownerId, hikeId);

        //Modifier la structure de l'inventaire pour faciliter l'affichage coté front
        return ResponseModel.buildResponse("200", "Randonnée récupérée avec succès", hike.toDTO());
    }

    @Override
    public ResponseModel<Hike> createOne(String ownerId, Hike hike) {

        // Vérifier le titre de la randonnée (unique)
        boolean isTitleAvailable = checkHikeTitleAvailable(ownerId, hike.getTitle());
        if (!isTitleAvailable) {
            return ResponseModel.buildResponse("409", "Une randonnée avec ce titre existe déjà.", null);
        }

        // Lier hike.ownerId avec celui passé en paramètre.
        hike.setOwnerId(ownerId);

        // Si modelId présent, PLUS TARD
        if(hike.getModelId() != null){
            return ResponseModel.buildResponse("900", "EN TRAVAUX, PAS ENCORE IMPLÉMENTÉ !", null);
        }

        // Sinon, créer inventory basique et enregistrer en BDD
        hike.setInventory(Inventory.getDefaultInventory());

//        hike.setId(RandomGenerator.generateUUIDWithPrefix("hike"));
        Hike createdHike = hikeRepository.createOneHike(hike);

        return ResponseModel.buildResponse("201", "Randonnée créée avec succès.", createdHike);
    }

    @Override
    public ResponseModel<Hike> modifyOne(String ownerId, Hike hike) {

        // Vérifier que la randonnée avec l'identifiant existe pour cet utilisateur

        // Vérifier que le titre est disponible (sans compter celle-ci évidemment ...)


        return null;
    }

    @Override
    public ResponseModel<Hike> deleteOne(String ownerId, Hike hike) {
        return null;
    }

    @Override
    public ResponseModel<Equipment> addEquipment(String userId, EquipmentDTO equipmentDTO) {
        return null;
    }

    @Override
    public ResponseModel<Equipment> modifyEquipment(String userId, Equipment equipment) {
        return null;
    }

    @Override
    public ResponseModel<Equipment> removeEquipment(String userId, String equipmentId) {
        return null;
    }

    @Override
    public ResponseModel<List<EquipmentCategory>> getCategories(String userId) {
        return null;
    }

    @Override
    public ResponseModel<EquipmentCategory> addCategory(String userId, EquipmentCategory category) {
        return null;
    }

    @Override
    public ResponseModel<EquipmentCategory> modifyCategory(String userId, EquipmentCategory category) {
        return null;
    }

    @Override
    public ResponseModel<EquipmentCategory> removeCategory(String userId, String categoryId) {
        return null;
    }


    private boolean checkHikeTitleAvailable(String ownerId, String hikeTitle) {
        return hikeRepository.checkHikeTitleAvailable(ownerId, hikeTitle);
    }
}
