package fr.karspa.hiker_thinker.services.auth;

import fr.karspa.hiker_thinker.dtos.LoginDTO;
import fr.karspa.hiker_thinker.dtos.RegisterDTO;

public interface AuthService {
    String login(LoginDTO loginDTO);
    String register(RegisterDTO registerDTO);
}
