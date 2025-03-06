package fr.karspa.hiker_thinker.services.auth;

import fr.karspa.hiker_thinker.model.AuthUser;
import fr.karspa.hiker_thinker.repository.AuthUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private AuthUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AuthUser user = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé."));

        return new User(user.getEmail(),
                user.getPassword(),
                user.getRoles()
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()));
    }
}
