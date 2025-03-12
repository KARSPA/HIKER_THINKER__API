package fr.karspa.hiker_thinker.services;

import fr.karspa.hiker_thinker.dtos.responses.LoginResponseDTO;
import fr.karspa.hiker_thinker.utils.ResponseModel;

public interface UserService {

    ResponseModel<LoginResponseDTO> getUserFromToken(String token);
}
