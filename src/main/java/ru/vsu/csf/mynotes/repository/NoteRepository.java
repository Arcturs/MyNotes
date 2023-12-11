package ru.vsu.csf.mynotes.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.vsu.csf.mynotes.model.entity.Note;

@Repository
public interface NoteRepository extends R2dbcRepository<Note, Long> {
}
