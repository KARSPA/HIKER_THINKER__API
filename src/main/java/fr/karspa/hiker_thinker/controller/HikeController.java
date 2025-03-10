package fr.karspa.hiker_thinker.controller;

import fr.karspa.hiker_thinker.dtos.EquipmentDTO;
import fr.karspa.hiker_thinker.dtos.responses.HikeResponseDTO;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.model.EquipmentCategory;
import fr.karspa.hiker_thinker.model.Hike;
import fr.karspa.hiker_thinker.services.HikeService;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import fr.karspa.hiker_thinker.utils.TokenUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hikes")
public class HikeController {

    private HikeService hikeService;
    private TokenUtils tokenUtils;

    public HikeController(HikeService hikeService, TokenUtils tokenUtils) {
        this.hikeService = hikeService;
        this.tokenUtils = tokenUtils;
    }

    @GetMapping("")
    public ResponseEntity<ResponseModel<List<Hike>>> getHikes() {
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<List<Hike>> response = hikeService.findAll(userId, false);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/{hikeId}")
    public ResponseEntity<ResponseModel<HikeResponseDTO>> getHike(@PathVariable String hikeId) {
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<HikeResponseDTO> response = hikeService.findByHikeId(userId, hikeId);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }



    @PostMapping("/create")
    public ResponseEntity<ResponseModel<Hike>> createHike(@RequestBody Hike hike) {
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<Hike> response = hikeService.createOne(userId, hike);

        if(response.getCode().equals("201")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PatchMapping("/modify")
    public ResponseEntity<ResponseModel<Hike>> modifyHike(@RequestBody Hike hike) {
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<Hike> response = hikeService.modifyOne(userId, hike);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @DeleteMapping("/remove")
    public ResponseEntity<ResponseModel<Hike>> deleteHike(@RequestParam(name = "id") String hikeId) {
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<Hike> response = hikeService.deleteOne(userId, hikeId);

        if(response.getCode().equals("204")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/{hikeId}/equipments")
    public ResponseEntity<ResponseModel<Equipment>> addEquipmentToHike(@PathVariable String hikeId, @RequestBody EquipmentDTO equipmentDTO) {
        String userId = tokenUtils.retreiveUserId();

        ResponseModel<Equipment> response = hikeService.addEquipment(userId, hikeId, equipmentDTO);

        if(response.getCode().equals("201")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
