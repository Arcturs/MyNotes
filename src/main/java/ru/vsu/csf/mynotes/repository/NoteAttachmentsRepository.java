package ru.vsu.csf.mynotes.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.model.entity.NoteAttachments;

import java.util.List;

@Repository
public interface NoteAttachmentsRepository extends R2dbcRepository<NoteAttachments, Long> {

    Mono<Void> deleteByNoteIdAndAttachmentIdIn(Long noteId, List<Long> attachmentId);

    Flux<NoteAttachments> findAllByNoteId(Long noteId);

    Mono<NoteAttachments> findByNoteIdAndAttachmentId(Long noteId, Long attachmentId);

}
