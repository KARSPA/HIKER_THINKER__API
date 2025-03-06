package fr.karspa.hiker_thinker.controller;

import fr.karspa.hiker_thinker.model.AuthUser;
import fr.karspa.hiker_thinker.repository.AuthUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class UserController {

    private AuthUserRepository userRepository;
    private PasswordEncoder passwordEncoder;


    @PostMapping("/register")
    public ResponseEntity registerUser(@RequestBody AuthUser user){
        try{
            if(userRepository.findByEmail(user.getEmail()).isPresent())
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Email déjà pris.");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            AuthUser savedUser = userRepository.save(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
        } catch(Exception e){
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
