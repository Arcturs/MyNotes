package ru.vsu.csf.mynotes.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginUserRequest {

    @NotBlank(message = "Поле Почта не должно быть пустым")
    private String email;

    @NotBlank(message = "Поле Пароль не должно быть пустым")
    private String password;

}
