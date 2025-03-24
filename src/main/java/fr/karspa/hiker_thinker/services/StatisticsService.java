package fr.karspa.hiker_thinker.services;

import fr.karspa.hiker_thinker.dtos.UserStatisticsDTO;
import fr.karspa.hiker_thinker.utils.ResponseModel;

public interface StatisticsService {

    ResponseModel<UserStatisticsDTO> getUserStatistics(String userId);
}
