package fr.karspa.hiker_thinker.services.impl;

import fr.karspa.hiker_thinker.dtos.UserStatisticsDTO;
import fr.karspa.hiker_thinker.repository.StatisticsRepository;
import fr.karspa.hiker_thinker.services.StatisticsService;
import fr.karspa.hiker_thinker.services.auth.JwtTokenProvider;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import fr.karspa.hiker_thinker.utils.TokenUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private StatisticsRepository statisticsRepository;
    private TokenUtils tokenUtils;


    @Override
    public ResponseModel<UserStatisticsDTO> getUserStatistics(String userId) {

        // Appeler la méthode du repo qui construit les statistiques de l'utilisateur.
        UserStatisticsDTO statsDTO = this.statisticsRepository.getUserStatistics(userId);

        return ResponseModel.buildResponse("200", "Statistiques récupérées avec succès", statsDTO);
    }

}
