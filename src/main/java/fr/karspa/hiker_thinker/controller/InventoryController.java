package fr.karspa.hiker_thinker.controller;

import fr.karspa.hiker_thinker.dtos.EquipmentDTO;
import fr.karspa.hiker_thinker.dtos.ModifyEquipmentDTO;
import fr.karspa.hiker_thinker.dtos.ReorderEquipmentDTO;
import fr.karspa.hiker_thinker.dtos.responses.EquipmentDetailsDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.model.Inventory;
import fr.karspa.hiker_thinker.services.InventoryService;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import fr.karspa.hiker_thinker.utils.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private static final Logger log = LoggerFactory.getLogger(InventoryController.class);

    private InventoryService inventoryService;
    private TokenUtils tokenUtils;

    public InventoryController(InventoryService inventoryService, TokenUtils tokenUtils) {
        this.inventoryService = inventoryService;
        this.tokenUtils = tokenUtils;
    }

    @GetMapping("")
    public ResponseEntity<ResponseModel<Inventory>> getInventory(){
        String userId = tokenUtils.retreiveUserId();
        log.info("GET /inventory => par {}", userId);

        ResponseModel<Inventory> response = inventoryService.findByUserId(userId);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/equipments")
    public ResponseEntity<ResponseModel<Equipment>> addEquipment(@RequestBody EquipmentDTO equipmentDTO){
        String userId = tokenUtils.retreiveUserId();
        log.info("POST /inventory/equipments => par {}", userId);

        ResponseModel<Equipment> response = inventoryService.addEquipment(userId, equipmentDTO);

        if(response.getCode().equals("201")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @PatchMapping("/equipments")
    public ResponseEntity<ResponseModel<List<Equipment>>> updateEquipmentsOrders(@RequestBody List<ReorderEquipmentDTO> equipmentModifications){
        String userId = tokenUtils.retreiveUserId();
        log.info("PATCH /inventory/equipments => par {}", userId);

        ResponseModel<List<Equipment>> response = inventoryService.modifyEquipments(userId, equipmentModifications);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/equipments/{equipmentId}")
    public ResponseEntity<ResponseModel<EquipmentDetailsDTO>> getEquipment(@PathVariable String equipmentId){
        String userId = tokenUtils.retreiveUserId();
        log.info("GET /inventory/equipments/{} => par {}", equipmentId, userId);

        ResponseModel<EquipmentDetailsDTO> response = inventoryService.getEquipmentById(userId, equipmentId);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PatchMapping("/equipments/{equipmentId}")
    public ResponseEntity<ResponseModel<Equipment>> modifyEquipment(@PathVariable String equipmentId, @RequestBody ModifyEquipmentDTO equipmentDTO){
        String userId = tokenUtils.retreiveUserId();
        log.info("PATCH /inventory/equipments/{} => par {}", equipmentId, userId);

        equipmentDTO.getEquipment().setId(equipmentId);
        System.err.println(equipmentDTO);
        ResponseModel<Equipment> response = inventoryService.modifyEquipment(userId, equipmentDTO);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/equipments/{equipmentId}")
    public ResponseEntity<ResponseModel<String>> removeEquipment(@PathVariable String equipmentId){
        String userId = tokenUtils.retreiveUserId();
        log.info("DELETE /inventory/equipments/{} => par {}", equipmentId, userId);

        ResponseModel<String> response = inventoryService.removeEquipment(userId, equipmentId);

        if(response.getCode().equals("204")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @GetMapping("/categories")
    public ResponseEntity<ResponseModel<List<EquipmentCategory>>> getCategories(){
        String userId = tokenUtils.retreiveUserId();
        log.info("GET /inventory/categories => par {}", userId);

        ResponseModel<List<EquipmentCategory>> response = inventoryService.getCategories(userId);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PatchMapping("/categories")
    public ResponseEntity<ResponseModel<List<EquipmentCategory>>> updateCategories(@RequestBody List<EquipmentCategory> categoryUpdates){
        String userId = tokenUtils.retreiveUserId();
        log.info("PATCH /inventory/categories => par {}", userId);

        ResponseModel<List<EquipmentCategory>> response = inventoryService.modifyMultipleCategories(userId, categoryUpdates);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<ResponseModel<EquipmentCategory>> addCategory(@RequestBody EquipmentCategory category){
        String userId = tokenUtils.retreiveUserId();
        log.info("POST /inventory/categories => par {}", userId);

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
        log.info("PATCH /inventory/categories/{} => par {}", categoryId, userId);

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
        log.info("DELETE /inventory/categories/{} => par {}", categoryId, userId);

        ResponseModel<EquipmentCategory> response = inventoryService.removeCategory(userId, categoryId);

        if(response.getCode().equals("204")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

}
