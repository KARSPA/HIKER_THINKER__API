package fr.karspa.hiker_thinker.services.impl;

import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.dtos.EquipmentDTO;
import fr.karspa.hiker_thinker.dtos.responses.HikeResponseDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.model.Hike;
import fr.karspa.hiker_thinker.model.Inventory;
import fr.karspa.hiker_thinker.repository.HikeRepository;
import fr.karspa.hiker_thinker.services.HikeService;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
        boolean isTitleAvailable = checkHikeTitleAvailable(ownerId, hike);
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

        // TODO : VALIDATION DES VALEURS DES CHAMPS (NOT NULL, NOT BLANK ETC)

        // Vérifier que la randonnée avec l'identifiant existe pour cet utilisateur
        boolean doesHikeExists = (hike.getId() != null) && this.checkHikeExistsById(ownerId, hike.getId());

        if(!doesHikeExists) {
            return ResponseModel.buildResponse("404", "Randonnée avec cet identifiant non trouvée.", null);
        }

        // Vérifier que le titre est disponible (sans compter celle-ci évidemment ...)
        boolean isTitleAvailable = checkHikeTitleAvailable(ownerId, hike);
        if (!isTitleAvailable) {
            return ResponseModel.buildResponse("409", "Une randonnée existe déjà avec ce titre", null);
        }

        UpdateResult result = hikeRepository.updateOneHike(hike);

        if (result.getModifiedCount() > 0) {
            return ResponseModel.buildResponse("200", "Randonnée modifiée avec succès.", hike);
        } else {
            return ResponseModel.buildResponse("500", "Échec de la modification de la randonnée.", null);
        }
    }

    @Override
    public ResponseModel<Hike> deleteOne(String ownerId, String hikeId) {

        boolean doesHikeExists = (hikeId != null) && this.checkHikeExistsById(ownerId, hikeId);
        if(!doesHikeExists) {
            return ResponseModel.buildResponse("404", "Aucune randonnée trouvée pour cette identifiant.", null);
        }

        Hike deletedHike = hikeRepository.deleteOneHike(ownerId, hikeId);

        return ResponseModel.buildResponse("204", "Randonnée supprimée avec succès.", deletedHike);
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


    private boolean checkHikeTitleAvailable(String ownerId, Hike hike) {
        return hikeRepository.checkHikeTitleAvailable(ownerId, hike);
    }


    private boolean checkHikeExistsById(String ownerId, String hikeId) {
        return hikeRepository.checkHikeExistsById(ownerId, hikeId);
    }
}
