package fr.karspa.hiker_thinker.controller;

import fr.karspa.hiker_thinker.dtos.EquipmentStatisticsDTO;
import fr.karspa.hiker_thinker.dtos.UserStatisticsDTO;
import fr.karspa.hiker_thinker.services.StatisticsService;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import fr.karspa.hiker_thinker.utils.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/statistics")
public class StatisticsController {
    private static final Logger log = LoggerFactory.getLogger(StatisticsController.class);

    private StatisticsService statisticsService;
    private TokenUtils tokenUtils;

    public StatisticsController(StatisticsService statisticsService, TokenUtils tokenUtils) {
        this.statisticsService = statisticsService;
        this.tokenUtils = tokenUtils;
    }


    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseModel<UserStatisticsDTO>> getUserStatistics(@PathVariable("userId") String userId) {
        String requesterId = this.tokenUtils.retreiveUserId();
        log.info("GET /statistics/users/{} => par {}", userId, requesterId);

        if(!requesterId.equals(userId)) {
            log.info("Tentative de filoutage par {}", requesterId);
            return ResponseEntity.status(403).build();
        }

        ResponseModel<UserStatisticsDTO> response = statisticsService.getUserStatistics(userId);

        return ResponseEntity.ok(response);
    }
    @GetMapping("/equipments/{equipmentId}")
    public ResponseEntity<ResponseModel<EquipmentStatisticsDTO>> getEquipmentStatistics(@PathVariable("equipmentId") String equipmentId) {
        String userId = this.tokenUtils.retreiveUserId();
        log.info("GET /statistics/equipments/{} => par {}", equipmentId, userId);

        ResponseModel<EquipmentStatisticsDTO> response = statisticsService.getEquipmentStatistics(userId, equipmentId);

        return ResponseEntity.ok(response);
    }
}
