package ru.vsu.csf.mynotes.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {

    @NotBlank(message = "Поле Логин должно быть заполнено")
    @Size(max = 100, message = "Максимальный размер поля Логин составляет 100 символов")
    private String login;

    @Email(message = "Поле Почта неверного формата")
    @NotBlank(message = "Поле Почта должно быть заполнено")
    @Size(max = 200, message = "Максимальный размер поля Почта составляет 200 символов")
    private String email;

    @NotBlank(message = "Поле Пароль должно быть заполнено")
    @Size(max = 20, min = 10, message = "Размер поля Пароль должно быть не менее 10 и не более 20 символов")
    private String password;

    @NotBlank(message = "Поле Повторить Пароль должно быть заполнено")
    @Size(max = 20, min = 10, message = "Размер поля Повторить Пароль должно быть не менее 10 и не более 20 символов")
    private String repeatPassword;

}
