package fr.karspa.hiker_thinker.services.auth;

import fr.karspa.hiker_thinker.dtos.LoginDTO;
import fr.karspa.hiker_thinker.dtos.RegisterDTO;
import fr.karspa.hiker_thinker.dtos.responses.LoginResponseDTO;
import fr.karspa.hiker_thinker.dtos.responses.RegisterResponseDTO;
import fr.karspa.hiker_thinker.model.User;
import fr.karspa.hiker_thinker.model.Equipment;
import fr.karspa.hiker_thinker.repository.AuthUserRepository;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private AuthUserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Override
    public ResponseModel<LoginResponseDTO> login(LoginDTO loginDTO) {

        try{
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            User user = userRepository.findByEmailForAuth(loginDTO.getEmail()).get(); //Ne peut être nul car vérifier juste au dessus dans l'authentification


            String token = jwtTokenProvider.generateToken(authentication, user.getId());

            LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .jwt(token)
                    .build();

            return ResponseModel.buildResponse("200", "Connexion réussie.", loginResponseDTO);


        }catch(AuthenticationException e){
            return ResponseModel.buildResponse("703", "Erreur d'authentification. Pseudo ou mot de passe incorrect.", null);
        }

    }

    @Override
    public ResponseModel<RegisterResponseDTO> register(RegisterDTO registerDTO) {

        //TODO : VALIDATION DU DTO ??

        User user = User.builder()
            .email(registerDTO.getEmail())
            .password(passwordEncoder.encode(registerDTO.getPassword()))
            .firstName(registerDTO.getFirstName())
            .lastName(registerDTO.getLastName())
            .roles(List.of("ROLE_USER"))
            .active(true)
            .inventory(new HashMap<String, List<Equipment>>())
            .build();


        if(userRepository.findByEmailForAuth(user.getEmail()).isPresent())
            return ResponseModel.buildResponse("409", "Email non disponible.", null);

        User savedUser = userRepository.save(user);

        RegisterResponseDTO registerResponseDTO = new RegisterResponseDTO(savedUser.getId(), savedUser.getEmail());

        return ResponseModel.buildResponse("201", "Utilisateur créé avec succès.", registerResponseDTO);

    }
}
