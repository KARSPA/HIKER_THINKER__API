package fr.karspa.hiker_thinker.controller;

import fr.karspa.hiker_thinker.dtos.EquipmentDTO;
import fr.karspa.hiker_thinker.dtos.responses.InventoryDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.services.InventoryService;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import fr.karspa.hiker_thinker.utils.TokenUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private InventoryService inventoryService;
    private TokenUtils tokenUtils;

    public InventoryController(InventoryService inventoryService, TokenUtils tokenUtils) {
        this.inventoryService = inventoryService;
        this.tokenUtils = tokenUtils;
    }

    @GetMapping("")
    public ResponseEntity<ResponseModel<InventoryDTO>> getInventory(){
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<InventoryDTO> response = inventoryService.findByUserId(userId);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/equipments")
    public ResponseEntity<ResponseModel<Equipment>> addEquipment(@RequestBody EquipmentDTO equipmentDTO){
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<Equipment> response = inventoryService.addEquipment(userId, equipmentDTO);

        if(response.getCode().equals("201")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PatchMapping("/equipments/{equipmentId}")
    public ResponseEntity<ResponseModel<Equipment>> modifyEquipment(@PathVariable String equipmentId, @RequestBody Equipment equipment){
        String userId = tokenUtils.retreiveUserId();

        equipment.setId(equipmentId);
        ResponseModel<Equipment> response = inventoryService.modifyEquipment(userId, equipment);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/equipments/{equipmentId}")
    public ResponseEntity<ResponseModel<Equipment>> removeEquipment(@PathVariable String equipmentId){
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<Equipment> response = inventoryService.removeEquipment(userId, equipmentId);

        if(response.getCode().equals("204")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @GetMapping("/categories")
    public ResponseEntity<ResponseModel<List<EquipmentCategory>>> getCategories(){
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<List<EquipmentCategory>> response = inventoryService.getCategories(userId);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<ResponseModel<EquipmentCategory>> addCategory(@RequestBody EquipmentCategory category){
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<EquipmentCategory> response = inventoryService.addCategory(userId, category);

        if(response.getCode().equals("201")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PatchMapping("/categories/{categoryId}")
    public ResponseEntity<ResponseModel<EquipmentCategory>> modifyCategory(@RequestBody EquipmentCategory category, @PathVariable String categoryId){
        String userId = tokenUtils.retreiveUserId();

        category.setId(categoryId);
        ResponseModel<EquipmentCategory> response = inventoryService.modifyCategory(userId, category);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<ResponseModel<EquipmentCategory>> removeCategory(@PathVariable String categoryId){
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<EquipmentCategory> response = inventoryService.removeCategory(userId, categoryId);

        if(response.getCode().equals("204")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

}
