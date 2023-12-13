package ru.vsu.csf.mynotes.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.model.entity.NoteAttachments;

@Repository
public interface NoteAttachmentsRepository extends R2dbcRepository<NoteAttachments, Long> {

    Mono<Void> deleteByNoteIdAndAttachmentId(Long noteId, Long attachmentId);

    Flux<NoteAttachments> findAllByNoteId(Long noteId);

    Mono<NoteAttachments> findByNoteIdAndAttachmentId(Long noteId, Long attachmentId);

}
