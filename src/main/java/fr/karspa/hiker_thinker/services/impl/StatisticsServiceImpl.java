package fr.karspa.hiker_thinker.services.impl;

import fr.karspa.hiker_thinker.dtos.EquipmentStatisticsDTO;
import fr.karspa.hiker_thinker.dtos.UserStatisticsDTO;
import fr.karspa.hiker_thinker.repository.StatisticsRepository;
import fr.karspa.hiker_thinker.services.StatisticsService;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private static final Logger log = LoggerFactory.getLogger(StatisticsServiceImpl.class);

    private StatisticsRepository statisticsRepository;

    @Override
    public ResponseModel<UserStatisticsDTO> getUserStatistics(String userId) {
        log.info("Récupération des statistiques de l'utilisateur : {}", userId);
        // Appeler la méthode du repo qui construit les statistiques de l'utilisateur.
        UserStatisticsDTO statsDTO = this.statisticsRepository.getUserStatistics(userId);

        return ResponseModel.buildResponse("200", "Statistiques récupérées avec succès", statsDTO);
    }

    @Override
    public ResponseModel<EquipmentStatisticsDTO> getEquipmentStatistics(String userId, String equipmentId) {
        log.info("Récupération des statistiques de l'équipment ({}) de l'utilisateur : {}", equipmentId, userId);

        EquipmentStatisticsDTO statsDTO = this.statisticsRepository.getEquipmentStatistics(userId, equipmentId);

        return ResponseModel.buildResponse("200", "Statistiques récupérées avec succès", statsDTO);
    }

}
