package fr.karspa.hiker_thinker.config.filters;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.karspa.hiker_thinker.services.auth.CustomUserDetailsService;
import fr.karspa.hiker_thinker.services.auth.JwtTokenProvider;
import fr.karspa.hiker_thinker.utils.ResponseModel;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private JwtTokenProvider tokenProvider;
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = getTokenFromRequest(request);
        try{
            if(StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
                String email = jwtTokenProvider.getUsername(token);
                String userId = jwtTokenProvider.getUserId(token);

                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                //Détails personnalisés dans le authToken de l'application pour la suite de cette requête.
                Map<String, Object> customDetails = new HashMap<>();
                customDetails.put("userId", userId);
                customDetails.put("requestInfo", new WebAuthenticationDetailsSource().buildDetails(request));

                authToken.setDetails(customDetails);

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        }catch(ExpiredJwtException | SignatureException ex){

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ResponseModel<Object> res = ResponseModel.buildResponse("800", "Authentification échouée. Veuillez vous reconnecter.", null);
            new ObjectMapper().writeValue(response.getWriter(), res);
        }

    }


    private String getTokenFromRequest(HttpServletRequest request){
        String bearerToken = request.getHeader("Authorization");

        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7);
        }

        return null;
    }
}
