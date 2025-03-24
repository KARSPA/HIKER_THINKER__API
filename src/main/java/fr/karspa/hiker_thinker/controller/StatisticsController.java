package fr.karspa.hiker_thinker.controller;

import fr.karspa.hiker_thinker.dtos.UserStatisticsDTO;
import fr.karspa.hiker_thinker.services.StatisticsService;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import fr.karspa.hiker_thinker.utils.TokenUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/statistics")
public class StatisticsController {

    private StatisticsService statisticsService;
    private TokenUtils tokenUtils;

    public StatisticsController(StatisticsService statisticsService, TokenUtils tokenUtils) {
        this.statisticsService = statisticsService;
        this.tokenUtils = tokenUtils;
    }


    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseModel<UserStatisticsDTO>> getUserStatistics(@PathVariable("userId") String userId) {

        String requesterId = this.tokenUtils.retreiveUserId();
        if(!requesterId.equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        ResponseModel<UserStatisticsDTO> response = statisticsService.getUserStatistics(userId);

        return ResponseEntity.ok(response);
    }
}
