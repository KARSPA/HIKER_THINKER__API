package fr.karspa.hiker_thinker.controller;

import fr.karspa.hiker_thinker.dtos.LoginDTO;
import fr.karspa.hiker_thinker.dtos.RegisterDTO;
import fr.karspa.hiker_thinker.dtos.responses.LoginResponseDTO;
import fr.karspa.hiker_thinker.dtos.responses.RegisterResponseDTO;
import fr.karspa.hiker_thinker.services.auth.AuthService;
import fr.karspa.hiker_thinker.services.auth.AuthServiceImpl;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/login")
    public ResponseEntity<ResponseModel<LoginResponseDTO>> login(@RequestBody LoginDTO loginDTO) {
        log.info("POST /auth/login => Tentative de connexion de : {}", loginDTO.getEmail());

        ResponseModel<LoginResponseDTO> response = authService.login(loginDTO);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseModel<RegisterResponseDTO>> register(@RequestBody RegisterDTO registerDTO) {
        log.info("POST /auth/register => Tentative de création de compte de : {}", registerDTO.getEmail());

        ResponseModel<RegisterResponseDTO> response = authService.register(registerDTO);

        if(response.getCode().equals("201")){
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
