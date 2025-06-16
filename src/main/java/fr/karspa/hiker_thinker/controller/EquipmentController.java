package fr.karspa.hiker_thinker.controller;

import fr.karspa.hiker_thinker.dtos.filters.EquipmentSearchDTO;
import fr.karspa.hiker_thinker.dtos.responses.SourceEquipmentPageDTO;
import fr.karspa.hiker_thinker.model.SourceEquipment;
import fr.karspa.hiker_thinker.services.EquipmentService;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/equipments")
public class EquipmentController {

    private static final Logger log = LoggerFactory.getLogger(EquipmentController.class);

    private EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @GetMapping("")
    public ResponseEntity<ResponseModel<SourceEquipmentPageDTO>> findAllWithFilters(@RequestParam(name = "name", required = false) String nameFilter,
                                                                                   @RequestParam(name = "brand", required = false) String brandFilter,
                                                                                   @RequestParam(name = "minWeight", required = false) Integer minWeight,
                                                                                   @RequestParam(name = "maxWeight", required = false) Integer maxWeight,
                                                                                   @RequestParam(name = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
                                                                                   @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
                                                                                    @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
                                                                                    @RequestParam(name = "sortDir", defaultValue = "ASC") String sortDirection) {

        EquipmentSearchDTO equipmentSearchDTO = new EquipmentSearchDTO(nameFilter, brandFilter, minWeight, maxWeight, pageNumber, pageSize, sortBy, sortDirection);

        ResponseModel<SourceEquipmentPageDTO> response = equipmentService.findAll(equipmentSearchDTO);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

}
