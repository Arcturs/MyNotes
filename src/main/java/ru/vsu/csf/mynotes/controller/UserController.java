package ru.vsu.csf.mynotes.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.model.dto.LoginUserRequest;
import ru.vsu.csf.mynotes.model.dto.RegisterUserRequest;
import ru.vsu.csf.mynotes.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/user")
@Tag(name = "User Controller")
public class UserController {

    private final UserService userService;

    @SecurityRequirements
    @PostMapping("/register")
    @Operation(summary = "Регистрация пользователя")
    public Mono<Long> registerUser(@RequestBody RegisterUserRequest request) {
        return userService.registerUser(request);
    }

    @SecurityRequirements
    @PostMapping("/login")
    @Operation(summary = "Вход пользователя в систему")
    public Mono<Long> loginUser(@RequestBody LoginUserRequest request) {
        return userService.loginUser(request);
    }

}
