package fr.karspa.hiker_thinker.services.impl;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.dtos.EquipmentDTO;
import fr.karspa.hiker_thinker.dtos.ModifyEquipmentDTO;
import fr.karspa.hiker_thinker.dtos.ReorderEquipmentDTO;
import fr.karspa.hiker_thinker.dtos.filters.EquipmentSearchDTO;
import fr.karspa.hiker_thinker.dtos.responses.EquipmentDetailsDTO;
import fr.karspa.hiker_thinker.dtos.responses.EquipmentPageDTO;
import fr.karspa.hiker_thinker.dtos.responses.HikeSummaryDTO;
import fr.karspa.hiker_thinker.dtos.responses.InventoryDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.model.Hike;
import fr.karspa.hiker_thinker.model.Inventory;
import fr.karspa.hiker_thinker.repository.HikeRepository;
import fr.karspa.hiker_thinker.repository.InventoryRepository;
import fr.karspa.hiker_thinker.services.InventoryService;
import fr.karspa.hiker_thinker.utils.RandomGenerator;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryServiceImpl.class);

    private InventoryRepository inventoryRepository;
    private HikeRepository hikeRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository, HikeRepository hikeRepository) {
        this.inventoryRepository = inventoryRepository;
        this.hikeRepository = hikeRepository;
    }

    @Override
    public ResponseModel<Inventory> findByUserId(String userId) {
        log.info("Récupération de l'inventaire de l'utilisateur : {}", userId);

        var inventory = inventoryRepository.getInventory(userId);

        if(inventory == null)
            return ResponseModel.buildResponse("710", "Aucun inventaire disponible.", null);

        return ResponseModel.buildResponse("200", "Inventaire récupéré avec succès.", inventory);
    }

    @Override
    public ResponseModel<EquipmentPageDTO> findByUserIdWithFilters(String userId, EquipmentSearchDTO searchDTO) {
        log.info("Récupération de l'inventaire de l'utilisateur (avec filtres) : {}", userId);

        EquipmentPageDTO response = inventoryRepository.getInventoryWithFilters(userId, searchDTO);

        if(response == null)
            return ResponseModel.buildResponse("710", "Aucun inventaire disponible.", null);

        return ResponseModel.buildResponse("200", "Inventaire récupéré avec succès.", response);
    }

    @Override
    public ResponseModel<EquipmentDetailsDTO> getEquipmentById(String userId, String equipmentId) {
        log.info("Récupération d'un équipement ({}) de l'inventaire de l'utilisateur : {}", equipmentId, userId);

        Equipment equipment = inventoryRepository.findEquipmentById(userId, equipmentId);

        if(equipment == null)
            return ResponseModel.buildResponse("404", "Aucun équipement trouvé.", null);

        EquipmentCategory category = inventoryRepository.getCategoryById(userId, equipment.getCategoryId());

        //Affecter le nom de la catégorie au DTO.
        EquipmentDetailsDTO equipmentDetailsDTO = (new EquipmentDetailsDTO()).fromEquipment(equipment, category.getName());

        //Chercher et affecter les randonnées dans lesquelles l'équipement apparait.
        List<HikeSummaryDTO> hikes = hikeRepository.findHikesUsedByEquipmentId(userId, equipmentId).stream()
                .map(hike -> new HikeSummaryDTO(hike.getId(), hike.getTitle(), hike.getTotalWeight(), hike.getWeightCorrection()))
                .toList();

        equipmentDetailsDTO.setHikes(hikes);

        return ResponseModel.buildResponse("200", "Équipement récupéré avec succès.", equipmentDetailsDTO);
    }

    @Override
    public ResponseModel<Equipment> addEquipment(String userId, EquipmentDTO equipmentDTO) {
        log.info("Ajout d'un équipement à l'inventaire de l'utilisateur : {}", userId);

        // Créer les instance nécessaires aux vérifications et enregistrement.
        Equipment equipment = equipmentDTO.mapToEntity();
        String categoryName = equipmentDTO.getCategoryName();


        //Vérifier l'unicité du name avant d'enregistrer
        boolean isNameAvailable = this.checkAvailableEquipmentName(userId, equipment);

        if(!isNameAvailable){
            return ResponseModel.buildResponse("400", "Un équipement avec ce nom existe déjà. ("+equipment.getName()+")", null);
        }

        //Générer un nouvel identifiant unique pour cet équipement
        String uniqueId = RandomGenerator.generateUUIDWithPrefix("equip");
        equipment.setId(uniqueId);

        //On récupère l'identifiant de la catégorie avec ce nom e ton agit en fonction.
        String categoryId = inventoryRepository.getCategoryIdByCategoryName(userId, categoryName);

        // Si la catégorie n'existe pas, l'ajouter dans la liste des catégories de l'inventaire
        if (categoryId == null) {

            String newCategoryId = RandomGenerator.generateRandomStringWithPrefix("cat");
            EquipmentCategory newCategory = new EquipmentCategory();
            newCategory.setId(newCategoryId);
            newCategory.setName(categoryName);
            // icon null et on affichera un truc générique

            // Ne pas oublier d'ajouter la référence à la nouvelle catégorie dans l'équipement.
            equipment.setCategoryId(newCategoryId);

            UpdateResult resultCat = inventoryRepository.addCategoryToCategoryList(userId, newCategory);
            // Si aucune catégorie n'a été ajoutée, retourner une erreur
            if (resultCat.getModifiedCount() == 0) {
                return ResponseModel.buildResponse("500", "Échec de l'ajout de la catégorie.", null);
            }
        }else{
            //Si on est là c'est que la catégorie existe, on doit donc ajouter la référence de categoryId dans l'équipement
            equipment.setCategoryId(categoryId);
        }

        // Ajouter l'équipement à la liste des équipements de l'inventaire
        UpdateResult resultEquip = inventoryRepository.addEquipmentToEquipmentList(userId, equipment);

        if (resultEquip.getModifiedCount() > 0) {
            return ResponseModel.buildResponse("201", "Équipement ajouté avec succès.", equipment);
        } else {
            return ResponseModel.buildResponse("500", "Échec de l'ajout de l'équipement.", null);
        }
    }

    @Override
    public ResponseModel<Equipment> modifyEquipment(String userId, ModifyEquipmentDTO equipmentDTO) {
        log.info("Modification d'un équipement ({}) à l'inventaire de l'utilisateur : {}", equipmentDTO.getEquipment().getId(), userId);

        //Vérifier que l'équipement avec cet id existe dans l'inventaire.
        boolean doesIdExists = this.checkEquipmentExistsById(userId, equipmentDTO.getEquipment().getId());

        if(!doesIdExists){
            return ResponseModel.buildResponse("404", "Aucun équipement avec cet identifiant n'existe dans votre inventaire.", null);
        }

        //On appelle la même méthode que pour l'ajout mais les requêtes de vérifications ne sont pas les mêmes si un id à l'équipement est passé ou non.
        boolean isNameAvailable = this.checkAvailableEquipmentName(userId, equipmentDTO.getEquipment());
        if(!isNameAvailable){
            return ResponseModel.buildResponse("409", "Un équipement avec ce nom existe déjà dans votre inventaire. ("+equipmentDTO.getEquipment().getName()+")", null);
        }

        // Vérifier que la nouvelle catégorie existe bien dans inventory.categories
        boolean doesCategoryExist = inventoryRepository.checkCategoryExistsById(userId, equipmentDTO.getEquipment().getCategoryId());
        if(!doesCategoryExist){
            return ResponseModel.buildResponse("400", "La catégorie de l'équipement n'existe pas dans votre inventaire. Veuillez la créée avant.", null);
        }

        //Modifier l'équipement avec ce qui est passé dans la requête.
        UpdateResult result = inventoryRepository.modifyEquipment(userId, equipmentDTO.getEquipment());

        //TODO : Modifier en conséquences les équipements dans les randonnées (et modèles) antérieurs à la date limite indiquée dans le DTO
        if(equipmentDTO.getHasConsequences()){ // Si on va modifier des randos

            //On va modifier l'équipement dans les randonnées dont la date est postérieure (après) à celle limite passée dans le DTO
            // => on garde les anciennes randos et on modifie celles qui ont eu lieu après une certaine date.
            UpdateResult equipmentResult = this.hikeRepository.updateHikesEquipment(userId, equipmentDTO);

            if(equipmentResult.getModifiedCount() != 0) {
                this.hikeRepository.findAndRecalculateHikesWeightByEquipmentIdAndLimitDate(userId, equipmentDTO);
            }
        }

        // TODO : On modifiera les modèles en conséquences quoi-qu'il arrive ... (car utilisés pour de futures randonnées)

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("200", "Équipement modifié avec succès.", equipmentDTO.getEquipment());
        } else {
            return ResponseModel.buildResponse("404", "Erreur bizarre.", null);
        }

    }

    @Override
    public ResponseModel<List<Equipment>> modifyEquipments(String userId, List<ReorderEquipmentDTO> equipmentChanges) {
        log.info("Modification d'ordre d'équipements de l'utilisateur : {}", userId);

        //Vérifier que les catégories existent
        Set<String> categoryIds = equipmentChanges.stream().map(ReorderEquipmentDTO::getCategoryId).collect(Collectors.toSet());
        System.out.println(categoryIds);

        boolean doesCategoriesExist = inventoryRepository.checkMultipleCategoryExistsById(userId, categoryIds);
        if(!doesCategoriesExist){
            return ResponseModel.buildResponse("400", "Une des catégories spécifiées n'existe pas.", null);
        }

        Set<String> equipmentIds = equipmentChanges.stream().flatMap(dto -> dto.getOrderedEquipmentIds().stream()).collect(Collectors.toSet());
        boolean doesEquipmentsExist = inventoryRepository.checkMultipleEquipmentExistsById(userId, equipmentIds);
        if(!doesEquipmentsExist){
            return ResponseModel.buildResponse("400", "Un des équipements spécifiés n'existe pas.", null);
        }

        //Modifier les équipements avec ce qui est passé dans la requête.
        UpdateResult result = inventoryRepository.modifyEquipmentsOrders(userId, equipmentChanges);

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("200", "Équipements modifiés avec succès.", null);
        } else {
            return ResponseModel.buildResponse("200", "Il n'y avait rien à modifier.", null);
        }

    }


    @Override
    public ResponseModel<String> removeEquipment(String userId, String equipmentId) {
        log.info("Suppression d'un équipement ({}) à l'inventaire de l'utilisateur : {}", equipmentId, userId);

        boolean doesEquipmentExists = this.checkEquipmentExistsById(userId, equipmentId);

        if(!doesEquipmentExists){
            return ResponseModel.buildResponse("404", "L'équipement avec cet identifiant n'existe pas dans votre inventaire.", null);
        }

        // Supprimer l'élément (en utilisant l'equipmentId passé en paramètre).
        UpdateResult result = inventoryRepository.removeEquipment(userId, equipmentId);

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("204", "Équipement supprimé avec succès.", equipmentId);
        } else {
            return ResponseModel.buildResponse("404", "Erreur bizarre.", null);
        }
    }


    @Override
    public ResponseModel<EquipmentCategory> addCategory(String userId, EquipmentCategory category){
        log.info("Ajout d'une catégorie à l'inventaire de l'utilisateur : {}", userId);

        boolean doesCategoryExist = inventoryRepository.checkCategoryExistsByName(userId, category.getName());
        if(doesCategoryExist){
            return ResponseModel.buildResponse("409", "La catégorie existe déjà dans l'inventaire.", null);
        }

        category.setId(RandomGenerator.generateRandomStringWithPrefix("cat"));
        UpdateResult result = inventoryRepository.addCategoryToCategoryList(userId, category);

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("201", "Catégorie créée avec succès.", category);
        } else {
            return ResponseModel.buildResponse("404", "Erreur bizarre.", null);
        }
    }

    @Override
    public ResponseModel<EquipmentCategory> modifyCategory(String userId, EquipmentCategory category){
        log.info("Modification d'une catégorie ({}) de l'inventaire de l'utilisateur : {}", category.getId(), userId);

        boolean doesCategoryExist = inventoryRepository.checkCategoryExistsById(userId, category.getId());
        if(!doesCategoryExist){
            return ResponseModel.buildResponse("404", "La catégorie à modifier n'existe pas dans l'inventaire.", null);
        }

        UpdateResult result = inventoryRepository.modifyCategoryInCategoryList(userId, category);

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("200", "Catégorie modifiée avec succès.", category);
        } else {
            return ResponseModel.buildResponse("404", "Erreur bizarre.", null);
        }
    }

    @Override
    public ResponseModel<List<EquipmentCategory>> modifyMultipleCategories(String userId, List<EquipmentCategory> categoryUpdates){
        log.info("Modification de l'ordre des catégories de l'inventaire de l'utilisateur : {}", userId);

        Set<String> categoryIds = categoryUpdates.stream().map(EquipmentCategory::getId).collect(Collectors.toSet());
        // Vérifier que chaque catégorie existe (par ID)
        boolean doesCategoriesExist = inventoryRepository.checkMultipleCategoryExistsById(userId, categoryIds);
        if(!doesCategoriesExist){
            return ResponseModel.buildResponse("400", "Une des catégories spécifiées n'existe pas.", null);
        }

        // Vérifier que les name sont biens uniques
        Set<String> categoryNames = categoryUpdates.stream().map(EquipmentCategory::getName).collect(Collectors.toSet());
        if(categoryNames.size() != categoryUpdates.size()){
            return ResponseModel.buildResponse("400", "Un des noms de catégorie est présent en double.", null);
        }

        UpdateResult result = inventoryRepository.modifyMultipleCategories(userId, categoryUpdates);

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("200", "Ordre des catégories modifiées avec succès.", this.getCategories(userId).getData());
        } else {
            return ResponseModel.buildResponse("200", "Il n'y avait rien à changer.", this.getCategories(userId).getData());
        }
    }

    @Override
    public ResponseModel<EquipmentCategory> removeCategory(String userId, String categoryId){
        log.info("Modification d'un équipement ({}) de l'inventaire de l'utilisateur : {}", categoryId, userId);

        //Vérifier si id = "DEFAULT" (pas supprimable dans ce cas)
        if (Objects.equals(categoryId, "DEFAULT")){
            return ResponseModel.buildResponse("403", "Bien tenté mais pas autorisé.", null);
        }

        boolean doesCategoryExist = inventoryRepository.checkCategoryExistsById(userId,categoryId);
        if(!doesCategoryExist){
            return ResponseModel.buildResponse("404", "La catégorie à supprimer n'existe pas dans l'inventaire.", null);
        }

        UpdateResult removeCatResult = inventoryRepository.removeCategoryInCategoryList(userId, categoryId);

        //Récupérer tous les équipements avec cette catégorie
        List<Equipment> equipmentsToReset = inventoryRepository.findEquipmentsByCategory(userId, categoryId);

        //Reset la catégorie et sauvegarder en bdd
        this.resetEquipmentsCategory(userId, equipmentsToReset);

        if (removeCatResult.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("204", "Catégorie supprimée et équipements mis à jour avec succès.", null);
        } else {
            return ResponseModel.buildResponse("404", "Erreur lors de la suppression de la catégorie.", null);
        }
    }

    @Override
    public ResponseModel<List<EquipmentCategory>> getCategories(String userId) {
        log.info("Récupération des catégories de l'inventaire de l'utilisateur : {}", userId);
        //Récupérer l'inventaire dans la BDD
        var categories = inventoryRepository.getCategories(userId);

        //Check si null
        if(categories.isEmpty())
            return ResponseModel.buildResponse("710", "Aucune catégories disponibles.", null);


        return ResponseModel.buildResponse("200", "Catégories récupérées avec succès.", categories);
    }


    private boolean checkEquipmentExistsById(String userId, String equipmentId){
        return inventoryRepository.checkEquipmentExistsById(userId, equipmentId);
    }


    private boolean checkAvailableEquipmentName(String userId, Equipment equipment){
        return inventoryRepository.checkAvailableEquipmentName(userId, equipment);
    }


    private void resetEquipmentsCategory(String userId, List<Equipment> equipments){
        for(Equipment equipment : equipments){
            equipment.setCategoryId("DEFAULT");
            inventoryRepository.modifyEquipment(userId, equipment);
        }
    }
}
