package fr.karspa.hiker_thinker.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class LoginResponseDTO {

    private String userId;
    private String email;

    private String jwt;
}