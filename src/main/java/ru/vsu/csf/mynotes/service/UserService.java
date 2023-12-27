package ru.vsu.csf.mynotes.service;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.exception.BadRequestException;
import ru.vsu.csf.mynotes.exception.ConflictException;
import ru.vsu.csf.mynotes.model.dto.LoginUserRequest;
import ru.vsu.csf.mynotes.model.dto.RegisterUserRequest;
import ru.vsu.csf.mynotes.model.entity.User;
import ru.vsu.csf.mynotes.repository.UserRepository;

import static ru.vsu.csf.mynotes.configuration.security.UserDetailsService.BAD_CREDENTIALS_MESSAGE;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Mono<Long> registerUser(@NotNull RegisterUserRequest request) {
        return findByEmail(request.getEmail())
                .defaultIfEmpty(mapRequestToUser(request))
                .doOnNext(user -> {
                    if (user.getId() != null) {
                        throw new ConflictException(
                                "Пользователь с почтой %s уже существует".formatted(user.getEmail()));
                    }
                    if (!request.getPassword().equals(request.getRepeatPassword())) {
                        throw new BadRequestException("Пароли не совпадают");
                    }
                })
                .flatMap(userRepository::save)
                .map(User::getId);
    }

    private Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Mono<Long> loginUser(@NotNull LoginUserRequest request) {
        return findByEmail(request.getEmail())
                .switchIfEmpty(Mono.error(
                        new BadRequestException(BAD_CREDENTIALS_MESSAGE)))
                .doOnNext(user -> {
                    if (!BCrypt.checkpw(request.getPassword(), user.getPasswordHash())) {
                        throw new BadRequestException(BAD_CREDENTIALS_MESSAGE);
                    }
                })
                .map(User::getId);
    }

    private static User mapRequestToUser(RegisterUserRequest request) {
        return new User()
                .setEmail(request.getEmail())
                .setLogin(request.getLogin())
                .setPasswordHash(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
    }

}
