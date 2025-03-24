package fr.karspa.hiker_thinker.services.auth;

import fr.karspa.hiker_thinker.dtos.LoginDTO;
import fr.karspa.hiker_thinker.dtos.ModifyUserDTO;
import fr.karspa.hiker_thinker.dtos.RegisterDTO;
import fr.karspa.hiker_thinker.dtos.responses.LoginResponseDTO;
import fr.karspa.hiker_thinker.dtos.responses.RegisterResponseDTO;
import fr.karspa.hiker_thinker.utils.ResponseModel;

public interface AuthService {
    ResponseModel<LoginResponseDTO> login(LoginDTO loginDTO);
    ResponseModel<RegisterResponseDTO> register(RegisterDTO registerDTO);

    ResponseModel<LoginResponseDTO> verifyConnected(String token);
    ResponseModel<LoginResponseDTO> getUser(String userId, String token);
    ResponseModel<LoginResponseDTO> modifyUser(ModifyUserDTO dto);
}
