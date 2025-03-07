package fr.karspa.hiker_thinker.controller;

import fr.karspa.hiker_thinker.dtos.responses.InventoryDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.services.InventoryService;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import fr.karspa.hiker_thinker.utils.TokenUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/equipment/add")
    public ResponseEntity<ResponseModel<Equipment>> addEquipment(@RequestBody Equipment equipment){
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<Equipment> response = inventoryService.addEquipment(userId, equipment);

        if(response.getCode().equals("201")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PatchMapping("/equipment/modify")
    public ResponseEntity<ResponseModel<Equipment>> modifyEquipment(@RequestBody Equipment equipment){
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<Equipment> response = inventoryService.modifyEquipment(userId, equipment);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/equipment/remove")
    public ResponseEntity<ResponseModel<Equipment>> removeEquipment(@RequestParam(name = "id") String equipmentId){
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<Equipment> response = inventoryService.removeEquipment(userId, equipmentId);

        if(response.getCode().equals("204")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @PostMapping("/category/add")
    public ResponseEntity<ResponseModel<String>> addCategory(@RequestParam(name = "name") String categoryName){
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<String> response = inventoryService.addCategory(userId, categoryName);

        if(response.getCode().equals("201")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

}
