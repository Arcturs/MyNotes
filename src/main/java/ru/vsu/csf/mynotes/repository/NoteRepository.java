package ru.vsu.csf.mynotes.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.model.entity.Note;

@Repository
public interface NoteRepository extends R2dbcRepository<Note, Long> {

    Mono<Note> findByIdAndUserId(Long id, Long userId);

    Flux<Note> findByUserId(Long userId);

}
