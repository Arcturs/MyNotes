package ru.vsu.csf.mynotes.service;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.exception.ForbiddenException;
import ru.vsu.csf.mynotes.model.entity.User;
import ru.vsu.csf.mynotes.repository.NoteRepository;
import ru.vsu.csf.mynotes.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public Mono<Void> checkUsersPermission(@NotNull String email, @NotNull Long noteId) {
        return userRepository.findByEmail(email)
                .flatMap(user -> noteRepository.findByIdAndUserId(noteId, user.getId()))
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new ForbiddenException("Запрещен доступ к заметке"))))
                .then();
    }

    public Mono<Long> getUserId(@NotNull String email) {
        return userRepository.findByEmail(email)
                .map(User::getId);
    }

}
