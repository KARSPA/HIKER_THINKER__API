package fr.karspa.hiker_thinker.services.auth;

import fr.karspa.hiker_thinker.dtos.LoginDTO;
import fr.karspa.hiker_thinker.dtos.RegisterDTO;
import fr.karspa.hiker_thinker.model.AuthUser;
import fr.karspa.hiker_thinker.repository.AuthUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private AuthenticationManager authenticationManager;
    private JwtTokenProvider jwtTokenProvider;
    private AuthUserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Override
    public String login(LoginDTO loginDTO) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(), loginDTO.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return jwtTokenProvider.generateToken(authentication);
    }

    @Override
    public String register(RegisterDTO registerDTO) {

        //TODO : VALIDATION DU DTO ??

        AuthUser user = AuthUser.builder()
            .email(registerDTO.getEmail())
            .password(passwordEncoder.encode(registerDTO.getPassword()))
            .firstName(registerDTO.getFirstName())
            .lastName(registerDTO.getLastName())
            .roles(List.of("ROLE_USER"))
            .build();


        try{
            if(userRepository.findByEmail(user.getEmail()).isPresent())
                return "ERREUR";
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            AuthUser savedUser = userRepository.save(user);
            return "SAVED USER";
        } catch(Exception e){
            return "ERREUR";
        }
    }
}
