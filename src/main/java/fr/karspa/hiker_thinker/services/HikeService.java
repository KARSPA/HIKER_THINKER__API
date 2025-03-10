package fr.karspa.hiker_thinker.services;

import fr.karspa.hiker_thinker.dtos.EquipmentDTO;
import fr.karspa.hiker_thinker.dtos.responses.HikeResponseDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.model.Hike;
import fr.karspa.hiker_thinker.utils.ResponseModel;

import java.util.List;

public interface HikeService {

    ResponseModel<List<Hike>> findAll(String ownerId, boolean withInventory);
    ResponseModel<HikeResponseDTO> findByHikeId(String ownerId, String hikeId);
    ResponseModel<Hike> createOne(String ownerId, Hike hike);
    ResponseModel<Hike> modifyOne(String ownerId, Hike hike);

    ResponseModel<Hike> deleteOne(String ownerId, String hikeId);

    ResponseModel<Equipment> addEquipment(String userId, EquipmentDTO equipmentDTO); // Ajouter un équipement à la randonnée

    ResponseModel<Equipment> modifyEquipment(String userId, Equipment equipment); // Modifier un équipement de la randonnée

    ResponseModel<Equipment> removeEquipment(String userId, String equipmentId); // Supprimer un équipement de la randonnée

    ResponseModel<List<EquipmentCategory>> getCategories(String userId);

    ResponseModel<EquipmentCategory> addCategory(String userId, EquipmentCategory category);

    ResponseModel<EquipmentCategory> modifyCategory(String userId, EquipmentCategory category);

    ResponseModel<EquipmentCategory> removeCategory(String userId, String categoryId);



}
