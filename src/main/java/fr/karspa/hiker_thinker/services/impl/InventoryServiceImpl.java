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

        //Vérifier l'unicité du name avant d'enregistrer
        boolean isNameAvailable = this.checkAvailableEquipmentName(userId, addEquipmentDTO);

        if(!isNameAvailable){
            return ResponseModel.buildResponse("400", "Un équipement avec ce nom existe déjà. ("+addEquipmentDTO.getEquipment().getName()+")", null);
        }

        UpdateResult result = userRepository.addEquipment(userId, addEquipmentDTO);

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("201", "Équipement ajouté avec succès.", addEquipmentDTO.getEquipment());
        } else {
            return ResponseModel.buildResponse("404", "Utilisateur non trouvé.", null);
        }
    }

    @Override
    public ResponseModel<EquipmentDTO> modifyEquipment(String userId, AddEquipmentDTO addEquipmentDTO) {

        //Vérifier que l'équipement avec cet id existe dans l'inventaire (dans la catégorie indiquée).
        boolean doesIdExists = this.checkEquipmentExistsById(userId, addEquipmentDTO);

        if(!doesIdExists){
            return ResponseModel.buildResponse("404", "Aucun équipement avec cet identifiant n'existe pour cette catégorie.", null);
        }

        //TODO : => VALIDER LES DONNÉES EN ENTRÉE ET S'ASSURER QU'IL Y AI BIEN UN ID.

        //On appelle la même méthode que pour l'ajout mais les requêtes de vérifications ne sont pas les mêmes si un id à l'équipement est passé ou non.
        boolean isNameAvailable = this.checkAvailableEquipmentName(userId, addEquipmentDTO);
        if(!isNameAvailable){
            return ResponseModel.buildResponse("409", "Un équipement avec ce nom existe déjà dans votre inventaire. ("+addEquipmentDTO.getEquipment().getName()+")", null);
        }

        //Modifier l'équipement avec ce qui est passé dans la requête.
        UpdateResult result = userRepository.modifyEquipment(userId, addEquipmentDTO);

        if (result.getMatchedCount() > 0) {
            return ResponseModel.buildResponse("200", "Équipement modifié avec succès.", addEquipmentDTO.getEquipment());
        } else {
            return ResponseModel.buildResponse("404", "Erreur bizarre.", null);
        }

    }



    @Override
    public ResponseModel<EquipmentDTO> removeEquipment(String userId, AddEquipmentDTO addEquipmentDTO) {
        return null;
    }





    private boolean checkEquipmentExistsById(String userId, AddEquipmentDTO addEquipmentDTO){
        return userRepository.checkEquipmentExistsById(userId, addEquipmentDTO);
    }


    private boolean checkAvailableEquipmentName(String userId, AddEquipmentDTO addEquipmentDTO){
        return userRepository.checkAvailableEquipmentName(userId, addEquipmentDTO);
    }
}
