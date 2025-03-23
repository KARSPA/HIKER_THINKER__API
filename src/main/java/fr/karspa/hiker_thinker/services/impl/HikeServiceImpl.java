package fr.karspa.hiker_thinker.services.impl;

import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.dtos.EquipmentDTO;
import fr.karspa.hiker_thinker.dtos.HikeEquipmentDTO;
import fr.karspa.hiker_thinker.dtos.responses.HikeResponseDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.model.Hike;
import fr.karspa.hiker_thinker.model.Inventory;
import fr.karspa.hiker_thinker.repository.HikeRepository;
import fr.karspa.hiker_thinker.repository.InventoryRepository;
import fr.karspa.hiker_thinker.services.HikeService;
import fr.karspa.hiker_thinker.utils.RandomGenerator;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class HikeServiceImpl implements HikeService {

    private HikeRepository hikeRepository;
    private InventoryRepository inventoryRepository;

    @Override
    public ResponseModel<List<Hike>> findAll(String ownerId, boolean withInventory) {

        // Récupérer tous les documents de "hikes" avec le ownerId passé en paramètre.
        List<Hike> hikes = hikeRepository.findAll(ownerId, withInventory);

        return ResponseModel.buildResponse("200", "Randonnées récupérées avec succès", hikes);
    }

    @Override
    public ResponseModel<HikeResponseDTO> findByHikeId(String ownerId, String hikeId) {
        Hike hike = hikeRepository.findOne(ownerId, hikeId);

        if (hike == null) {
            return ResponseModel.buildResponse("404", "Aucune randonnée trouvée.", null);
        }

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
    public ResponseModel<Equipment> addEquipment(String ownerId, String hikeId, HikeEquipmentDTO hikeEquipmentDTO) {

        //Vérifier que la randonnée existe :
        boolean doesHikeExists = (hikeId != null) && this.checkHikeExistsById(ownerId, hikeId);
        if(!doesHikeExists) {
            return ResponseModel.buildResponse("400", "Immpossible d'ajouter un équipement à une randonnée qui n'existe pas.", null);
        }

        // Vérifier que la catégorie à laquelle on veut ajouter l'équipement existe bien dans hike.inventory.categories
        boolean doesCategoryExists = hikeRepository.checkCategoryExistsById(ownerId, hikeId, hikeEquipmentDTO.getCategoryId());
        if(!doesCategoryExists) {
            return ResponseModel.buildResponse("400", "La catégorie n'existe pas sur cette randonnée.", null);
        }

        // Vérifier si l'équipement n'est pas déjà dans la randonnée.
        boolean doesEquipmentExistsInHike = hikeRepository.checkEquipmentExistsById(ownerId, hikeId, hikeEquipmentDTO.getSourceId());
        if(doesEquipmentExistsInHike) {
            return ResponseModel.buildResponse("409", "L'équipement est déjà présent dans la randonnée.", null);
        }
        // Récupérer les données de l'équipement source passé dans hikeEquipmentDTO via son id.
        Equipment sourceEquipment = inventoryRepository.findEquipmentById(ownerId, hikeEquipmentDTO.getSourceId());

        if(sourceEquipment == null) {
            return ResponseModel.buildResponse("404", "L'équipement voulu n'existe pas dans l'inventaire.", null);
        }

        // Vérifier si ok et Modifier sa catégorie par celle que l'on souhaite.
        sourceEquipment.setCategoryId(hikeEquipmentDTO.getCategoryId());
        sourceEquipment.setSourceId(hikeEquipmentDTO.getSourceId()); //Doublon avec l'id de l'équipement mais osef

        // Ajouter l'équipement dans l'inventaire de la randonnée.
        UpdateResult result = hikeRepository.addEquipmentToEquipmentList(ownerId, hikeId, sourceEquipment);

        // TODO : Ajouter le poids de l'équipement au total de la randonnée et à celui de la catégorie.

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("201", "Équipement ajouté avec succès.", sourceEquipment);
        } else {
            return ResponseModel.buildResponse("404", "Erreur bizarre.", null);
        }
    }

    @Override
    public ResponseModel<HikeEquipmentDTO> modifyEquipment(String ownerId, String hikeId, HikeEquipmentDTO hikeEquipmentDTO) {
        //Méthode utilisée pour modifier la catégorie d'un équipement

        // Vérifier que la randonnée existe
        boolean doesHikeExists = (hikeId != null) && this.checkHikeExistsById(ownerId, hikeId);
        if(!doesHikeExists) {
            return ResponseModel.buildResponse("404", "La randonnée n'existe pas.", null);
        }

        // Vérifier et récupérer l'équipement si il est bien dans la randonnée.
        Equipment currentEquipment = hikeRepository.getEquipmentById(ownerId, hikeId, hikeEquipmentDTO.getSourceId());
        if(currentEquipment == null) {
            return ResponseModel.buildResponse("404", "Impossible de modifier un équipement qui n'existe pas dans la randonnée.", null);
        }

        // Vérifier que la catégorie demandée existe
        boolean doesCategoyExists = hikeRepository.checkCategoryExistsById(ownerId, hikeId, hikeEquipmentDTO.getCategoryId());
        if(!doesCategoyExists) {
            return ResponseModel.buildResponse("404", "La catégorie demandée n'existe pas dans cette randonnée.", null);
        }

        // Modifier la catégorie de l'équipement (ET LES POIDS) et sauvegarder en base.
        UpdateResult result = hikeRepository.modifyEquipmentCategory(ownerId, hikeId, hikeEquipmentDTO, currentEquipment);

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("200", "Catégorie de l'équipement modifiée avec succès.", hikeEquipmentDTO);
        } else {
            return ResponseModel.buildResponse("404", "Erreur bizarre.", null);
        }
    }

    @Override
    public ResponseModel<String> removeEquipment(String ownerId, String hikeId, String equipmentId) {

        Equipment correspondingEquipment = hikeRepository.getEquipmentById(ownerId, hikeId, equipmentId);
        
        if(correspondingEquipment == null){
            return ResponseModel.buildResponse("404", "L'équipement avec cet identifiant n'existe pas dans cette randonnée.", null);
        }

        UpdateResult result = hikeRepository.removeEquipmentFromEquipmentList(ownerId, hikeId, correspondingEquipment);

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("204", "Équipement supprimé avec succès.", equipmentId);
        } else {
            return ResponseModel.buildResponse("404", "Erreur bizarre.", null);
        }
    }

    @Override
    public ResponseModel<List<EquipmentCategory>> getCategories(String ownerId, String hikeId) {

        // Vérifier que la randonnée existe
        boolean doesHikeExists = (hikeId != null) && this.checkHikeExistsById(ownerId, hikeId);
        if(!doesHikeExists) {
            return ResponseModel.buildResponse("404", "La randonnée n'existe pas.", null);
        }

        //Récupérer l'inventaire dans la BDD
        var categories = hikeRepository.getCategories(ownerId, hikeId);

        //Check si null
        if(categories.isEmpty())
            return ResponseModel.buildResponse("404", "Aucune catégories disponibles.", null);


        return ResponseModel.buildResponse("200", "Catégories récupérées avec succès.", categories);
    }

    @Override
    public ResponseModel<EquipmentCategory> addCategory(String ownerId, String hikeId, EquipmentCategory category) {

        // Vérifier que la randonnée existe
        boolean doesHikeExists = (hikeId != null) && this.checkHikeExistsById(ownerId, hikeId);
        if(!doesHikeExists) {
            return ResponseModel.buildResponse("404", "La randonnée n'existe pas.", null);
        }

        boolean doesCategoryExist = hikeRepository.checkCategoryExistsByName(ownerId, hikeId, category.getName());
        if(doesCategoryExist){
            return ResponseModel.buildResponse("409", "La catégorie existe déjà dans l'inventaire de cette randonnée.", null);
        }

        category.setId(RandomGenerator.generateRandomStringWithPrefix("cat"));
        UpdateResult result = hikeRepository.addCategoryToCategoryList(ownerId, hikeId, category);

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("201", "Catégorie créée avec succès.", category);
        } else {
            return ResponseModel.buildResponse("404", "Erreur bizarre.", null);
        }
    }

    @Override
    public ResponseModel<EquipmentCategory> modifyCategory(String ownerId, String hikeId, EquipmentCategory category) {

        //TODO : FIltrer pour ne pas pouvoir modifier le poids de la catégorie à la main.

        // Vérifier que la randonnée existe
        boolean doesHikeExists = (hikeId != null) && this.checkHikeExistsById(ownerId, hikeId);
        if(!doesHikeExists) {
            return ResponseModel.buildResponse("404", "La randonnée n'existe pas.", null);
        }

        boolean doesCategoryExist = hikeRepository.checkCategoryExistsById(ownerId, hikeId, category.getId());
        if(!doesCategoryExist){
            return ResponseModel.buildResponse("404", "La catégorie à modifier n'existe pas dans cette randonnée.", null);
        }

        UpdateResult result = hikeRepository.modifyCategoryInCategoryList(ownerId, hikeId, category);

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("200", "Catégorie modifiée avec succès.", category);
        } else {
            return ResponseModel.buildResponse("404", "Erreur bizarre.", null);
        }
    }

    @Override
    public ResponseModel<EquipmentCategory> removeCategory(String ownerId, String hikeId, String categoryId) {

        // Vérifier que la randonnée existe
        boolean doesHikeExists = (hikeId != null) && this.checkHikeExistsById(ownerId, hikeId);
        if(!doesHikeExists) {
            return ResponseModel.buildResponse("404", "La randonnée n'existe pas.", null);
        }

        //Vérifier si id = "DEFAULT" (pas supprimable dans ce cas)
        if (Objects.equals(categoryId, "DEFAULT")){
            return ResponseModel.buildResponse("403", "Bien tenté mais pas autorisé.", null);
        }

        boolean doesCategoryExist = hikeRepository.checkCategoryExistsById(ownerId, hikeId, categoryId);
        if(!doesCategoryExist){
            return ResponseModel.buildResponse("404", "La catégorie à supprimer n'existe pas dans cette randonnée.", null);
        }

        UpdateResult removeCatResult = hikeRepository.removeCategoryInCategoryList(ownerId, hikeId, categoryId);

        //Récupérer tous les équipements avec cette catégorie
        List<Equipment> equipmentsToReset = hikeRepository.findEquipmentsByCategory(ownerId, hikeId, categoryId);

        //Reset la catégorie et sauvegarder en bdd
        this.resetEquipmentsCategory(ownerId, hikeId, equipmentsToReset);

        if (removeCatResult.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("204", "Catégorie supprimée et équipements mis à jour avec succès.", null);
        } else {
            return ResponseModel.buildResponse("404", "Erreur lors de la suppression de la catégorie.", null);
        }
    }


    private boolean checkHikeTitleAvailable(String ownerId, Hike hike) {
        return hikeRepository.checkHikeTitleAvailable(ownerId, hike);
    }


    private boolean checkHikeExistsById(String ownerId, String hikeId) {
        return hikeRepository.checkHikeExistsById(ownerId, hikeId);
    }

    private void resetEquipmentsCategory(String userId, String hikeId, List<Equipment> equipments){
        for(Equipment equipment : equipments){
            HikeEquipmentDTO equip = HikeEquipmentDTO.builder().categoryId("DEFAULT").sourceId(equipment.getId()).build();
            hikeRepository.modifyEquipmentCategory(userId, hikeId, equip, equipment);
        }
    }

}
