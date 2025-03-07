package fr.karspa.hiker_thinker.services.impl;

import com.mongodb.client.result.UpdateResult;
import fr.karspa.hiker_thinker.dtos.responses.InventoryDTO;
import fr.karspa.hiker_thinker.model.Equipment;
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

        //Récupérer l'inventaire dans la BDD
        var inventory = userRepository.findInventoryByUserId(userId);

        //Check si null
        if(inventory == null)
            return ResponseModel.buildResponse("710", "Aucun inventaire disponible.", null);


        // => Grouper les équipements par catégories et retourner le tableau associatif généré.
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setEquipments(inventory);

        return ResponseModel.buildResponse("200", "Inventaire récupéré avec succès.", inventoryDTO);
    }

    @Override
    public ResponseModel<Equipment> addEquipment(String userId, Equipment equipment) {

        //Vérifier l'unicité du name avant d'enregistrer
        boolean isNameAvailable = this.checkAvailableEquipmentName(userId, equipment);

        if(!isNameAvailable){
            return ResponseModel.buildResponse("400", "Un équipement avec ce nom existe déjà. ("+equipment.getName()+")", null);
        }

        UpdateResult result = userRepository.addEquipment(userId, equipment);

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("201", "Équipement ajouté avec succès.", equipment);
        } else {
            return ResponseModel.buildResponse("404", "Utilisateur non trouvé.", null);
        }
    }

    @Override
    public ResponseModel<Equipment> modifyEquipment(String userId, Equipment equipment) {

        //Vérifier que l'équipement avec cet id existe dans l'inventaire (dans la catégorie indiquée).
        boolean doesIdExists = this.checkEquipmentExistsById(userId, equipment);

        if(!doesIdExists){
            return ResponseModel.buildResponse("404", "Aucun équipement avec cet identifiant n'existe pour cette catégorie.", null);
        }

        //TODO : => VALIDER LES DONNÉES EN ENTRÉE ET S'ASSURER QU'IL Y AI BIEN UN ID.

        //On appelle la même méthode que pour l'ajout mais les requêtes de vérifications ne sont pas les mêmes si un id à l'équipement est passé ou non.
        boolean isNameAvailable = this.checkAvailableEquipmentName(userId, equipment);
        if(!isNameAvailable){
            return ResponseModel.buildResponse("409", "Un équipement avec ce nom existe déjà dans votre inventaire. ("+equipment.getName()+")", null);
        }

        //Modifier l'équipement avec ce qui est passé dans la requête.
        UpdateResult result = userRepository.modifyEquipment(userId, equipment);

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("200", "Équipement modifié avec succès.", equipment);
        } else {
            return ResponseModel.buildResponse("404", "Erreur bizarre.", null);
        }

    }



    @Override
    public ResponseModel<Equipment> removeEquipment(String userId, String equipmentId) {

        // Supprimer l'élément (en utilisant l'equipmentId passé en paramètre).
        UpdateResult result = userRepository.removeEquipment(userId, equipmentId);

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("204", "Équipement supprimé avec succès.", null);
        } else {
            return ResponseModel.buildResponse("404", "Erreur bizarre.", null);
        }
    }





    private boolean checkEquipmentExistsById(String userId, Equipment equipment){
        return userRepository.checkEquipmentExistsById(userId, equipment);
    }


    private boolean checkAvailableEquipmentName(String userId, Equipment equipment){
        return userRepository.checkAvailableEquipmentName(userId, equipment);
    }
}
