package ru.vsu.csf.mynotes.configuration.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.exception.UnauthorizedException;
import ru.vsu.csf.mynotes.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsService implements ReactiveUserDetailsService {

    public static final String BAD_CREDENTIALS_MESSAGE = "Неправильные почта или пароль";

    private final UserRepository userRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByEmail(username)
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new UnauthorizedException(BAD_CREDENTIALS_MESSAGE))))
                .map(user -> user);
    }

}
