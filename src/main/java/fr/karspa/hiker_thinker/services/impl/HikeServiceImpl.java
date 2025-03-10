package fr.karspa.hiker_thinker.services.impl;

import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.dtos.EquipmentDTO;
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
    public ResponseModel<Equipment> addEquipment(String ownerId, String hikeId, EquipmentDTO equipmentDTO) {

        // Créer les instance nécessaires aux vérifications et enregistrement.
        Equipment equipment = equipmentDTO.mapToEntity();
        String categoryName = equipmentDTO.getCategoryName();


        //Vérifier que la randonnée existe :
        boolean doesHikeExists = (hikeId != null) && this.checkHikeExistsById(ownerId, hikeId);
        if(!doesHikeExists) {
            return ResponseModel.buildResponse("400", "Immpossible d'ajouter un équipement à une randonnée qui n'existe pas.", null);
        }


        //Vérifier que l'équipement existe dans son inventaire.
        boolean doesSourceEquipmentExists = inventoryRepository.checkEquipmentExistsById(ownerId, equipment.getSourceId());
        if(!doesSourceEquipmentExists) {
            return ResponseModel.buildResponse("404", "L'équipement que vous essayez d'ajouter ne se trouve pas dans votre inventaire.", null);
        }


        //Vérifier l'unicité du name avant d'enregistrer (si filou qui essaye)
        boolean isNameAvailable = this.checkAvailableEquipmentName(ownerId, hikeId, equipment);
        if(!isNameAvailable){
            return ResponseModel.buildResponse("400", "Un équipement avec ce nom existe déjà dans la randonnée. ("+equipment.getName()+")", null);
        }

        //Générer un nouvel identifiant unique pour cet équipement
        String uniqueId = RandomGenerator.generateUUIDWithPrefix("h_equip");
        equipment.setId(uniqueId);

        //On récupère l'identifiant de la catégorie avec ce nom et on agit en fonction.
        String categoryId = hikeRepository.getCategoryIdByCategoryName(ownerId, hikeId, categoryName);

        // Si la catégorie n'existe pas, l'ajouter dans la liste des catégories de l'inventaire
        if (categoryId == null) {

            String newCategoryId = RandomGenerator.generateRandomStringWithPrefix("h_cat");
            EquipmentCategory newCategory = new EquipmentCategory();
            newCategory.setId(newCategoryId);
            newCategory.setName(categoryName);
            // icon null et on affichera un truc générique

            // Ne pas oublier d'ajouter la référence à la nouvelle catégorie dans l'équipement.
            equipment.setCategoryId(newCategoryId);

            UpdateResult resultCat = hikeRepository.addCategoryToCategoryList(ownerId, hikeId, newCategory);
            // Si aucune catégorie n'a été ajoutée, retourner une erreur
            if (resultCat.getModifiedCount() == 0) {
                return ResponseModel.buildResponse("500", "Échec de l'ajout de la catégorie.", null);
            }
        }else{
            //Si on est là c'est que la catégorie existe, on doit donc ajouter la référence de categoryId dans l'équipement
            equipment.setCategoryId(categoryId);
        }

        // Ajouter l'équipement à la liste des équipements de l'inventaire
        UpdateResult resultEquip = hikeRepository.addEquipmentToEquipmentList(ownerId, hikeId, equipment);

        if (resultEquip.getModifiedCount() > 0) {
            return ResponseModel.buildResponse("201", "Équipement ajouté avec succès.", equipment);
        } else {
            return ResponseModel.buildResponse("500", "Échec de l'ajout de l'équipement.", null);
        }
    }

    @Override
    public ResponseModel<Equipment> modifyEquipment(String userId, String hikeId, Equipment equipment) {
        return null;
    }

    @Override
    public ResponseModel<Equipment> removeEquipment(String userId, String hikeId, String equipmentId) {
        return null;
    }

    @Override
    public ResponseModel<List<EquipmentCategory>> getCategories(String userId, String hikeId) {
        return null;
    }

    @Override
    public ResponseModel<EquipmentCategory> addCategory(String userId, String hikeId, EquipmentCategory category) {
        return null;
    }

    @Override
    public ResponseModel<EquipmentCategory> modifyCategory(String userId, String hikeId, EquipmentCategory category) {
        return null;
    }

    @Override
    public ResponseModel<EquipmentCategory> removeCategory(String userId, String hikeId, String categoryId) {
        return null;
    }


    private boolean checkHikeTitleAvailable(String ownerId, Hike hike) {
        return hikeRepository.checkHikeTitleAvailable(ownerId, hike);
    }


    private boolean checkHikeExistsById(String ownerId, String hikeId) {
        return hikeRepository.checkHikeExistsById(ownerId, hikeId);
    }


    private boolean checkAvailableEquipmentName(String userId, String hikeId, Equipment equipment){
        return hikeRepository.checkAvailableEquipmentName(userId, hikeId, equipment);
    }
}
