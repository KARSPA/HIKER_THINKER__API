package fr.karspa.hiker_thinker.services.impl;

import fr.karspa.hiker_thinker.dtos.responses.LoginResponseDTO;
import fr.karspa.hiker_thinker.model.User;
import fr.karspa.hiker_thinker.repository.AuthUserRepository;
import fr.karspa.hiker_thinker.services.UserService;
import fr.karspa.hiker_thinker.services.auth.JwtTokenProvider;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private JwtTokenProvider jwtTokenProvider;
    private AuthUserRepository userRepository;

    @Override
    public ResponseModel<LoginResponseDTO> getUserFromToken(String token) {
        String userId = jwtTokenProvider.getUserId(token);
        User user = userRepository.findById(userId).orElse(null);

        if(user == null){
            return ResponseModel.buildResponse("404", "Utilisateur non trouvé.", null);
        }

        LoginResponseDTO loginResponseDTO = LoginResponseDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();

        return ResponseModel.buildResponse("200", "Informations de l'utilisateur trouvé avec succès.", loginResponseDTO);

    }
}
