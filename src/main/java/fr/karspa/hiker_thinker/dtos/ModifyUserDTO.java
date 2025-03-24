package fr.karspa.hiker_thinker.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ModifyUserDTO {

    private String userId;

    private String jwt;

    private String password;
    private String newPassword;

    private String firstName;
    private String lastName;
}
