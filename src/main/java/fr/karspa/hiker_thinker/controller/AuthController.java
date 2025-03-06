package fr.karspa.hiker_thinker.controller;

import fr.karspa.hiker_thinker.dtos.LoginDTO;
import fr.karspa.hiker_thinker.dtos.RegisterDTO;
import fr.karspa.hiker_thinker.dtos.responses.LoginResponseDTO;
import fr.karspa.hiker_thinker.dtos.responses.RegisterResponseDTO;
import fr.karspa.hiker_thinker.repository.AuthUserRepository;
import fr.karspa.hiker_thinker.services.auth.AuthService;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    @PostMapping("/login")
    public ResponseEntity<ResponseModel<LoginResponseDTO>> login(@RequestBody LoginDTO loginDTO) {

        ResponseModel<LoginResponseDTO> response = authService.login(loginDTO);

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseModel<RegisterResponseDTO>> register(@RequestBody RegisterDTO registerDTO) {
        ResponseModel<RegisterResponseDTO> response = authService.register(registerDTO);

        if(response.getCode().equals("201")){
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
