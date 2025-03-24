package fr.karspa.hiker_thinker.controller;

import fr.karspa.hiker_thinker.dtos.LoginDTO;
import fr.karspa.hiker_thinker.dtos.ModifyUserDTO;
import fr.karspa.hiker_thinker.dtos.responses.LoginResponseDTO;
import fr.karspa.hiker_thinker.services.UserService;
import fr.karspa.hiker_thinker.services.auth.AuthService;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import fr.karspa.hiker_thinker.utils.TokenUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private AuthService authService;
    private TokenUtils tokenUtils;


    public UserController(AuthService authService, TokenUtils tokenUtils) {
        this.authService = authService;
        this.tokenUtils = tokenUtils;
    }

    @GetMapping("/verify")
    public ResponseEntity<ResponseModel<LoginResponseDTO>> verifyTokenValidity(@RequestHeader(name = "Authorization") String token) {
        ResponseModel<LoginResponseDTO> response = authService.verifyConnected(token.substring(7));

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ResponseModel<LoginResponseDTO>> getUser(@PathVariable("userId") String userId,
                                                                   @RequestHeader(name = "Authorization") String token) {

        ResponseModel<LoginResponseDTO> response = authService.getUser(userId, token.substring(7));

        if(response.getCode().equals("200")){
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<ResponseModel<LoginResponseDTO>> modifyUser(@RequestHeader(name = "Authorization") String token,
                                                                      @PathVariable String userId,
                                                                      @RequestBody ModifyUserDTO dto) {
        dto.setUserId(userId);
        dto.setJwt(token.substring(7));
        ResponseModel<LoginResponseDTO> response = authService.modifyUser(dto);

        if (response.getCode().equals("200")) {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

    }
}
