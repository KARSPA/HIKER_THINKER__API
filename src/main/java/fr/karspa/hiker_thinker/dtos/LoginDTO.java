package fr.karspa.hiker_thinker.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class LoginDTO {

    private String email;

    private String password;
}
