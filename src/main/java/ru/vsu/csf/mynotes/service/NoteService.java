package ru.vsu.csf.mynotes.service;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.exception.BadRequestException;
import ru.vsu.csf.mynotes.exception.NotFoundException;
import ru.vsu.csf.mynotes.model.dto.GetNoteResponse;
import ru.vsu.csf.mynotes.model.dto.GetNotesResponse;
import ru.vsu.csf.mynotes.model.entity.Attachment;
import ru.vsu.csf.mynotes.model.entity.Note;
import ru.vsu.csf.mynotes.model.entity.NoteAttachments;
import ru.vsu.csf.mynotes.repository.NoteAttachmentsRepository;
import ru.vsu.csf.mynotes.repository.NoteRepository;
import ru.vsu.csf.mynotes.util.FilePartUtils;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@Service
@RequiredArgsConstructor
public class NoteService {

    private static final String DEFAULT_NOTE_NAME = "Новая_заметка";

    private final NoteRepository noteRepository;
    private final NoteAttachmentsRepository noteAttachmentsRepository;
    private final AttachmentService attachmentService;

    public Mono<Long> createNote(@NotNull Long userId) {
        return noteRepository.save(
                new Note().setName(DEFAULT_NOTE_NAME)
                        .setUserId(userId))
                .map(Note::getId);
    }

    public Mono<Long> changeNoteName(@NotNull Long id, @NotNull String name) {
        if (name.isBlank()) {
            throw new BadRequestException("Имя заметки не может быть пустым");
        }
        return findById(id)
                .flatMap(note -> noteRepository.save(note.setName(name)))
                .map(Note::getId);
    }

    public Mono<Long> changeNoteText(@NotNull Long id, @NotNull FilePart text) {
        return Mono.zip(findById(id), FilePartUtils.getByteArray(text))
                .flatMap(tuple -> noteRepository.save(tuple.getT1().setText(tuple.getT2())))
                .map(Note::getId);
    }

    public Flux<Long> addAttachmentsToNote(@NotNull Long id, @NotNull List<FilePart> attachments) {
        return Mono.zip(findById(id), attachmentService.saveAttachments(attachments).collectList())
                .flatMapMany(tuple -> Flux.fromIterable(tuple.getT2())
                            .flatMap(attachmentId -> noteAttachmentsRepository.save(
                                    new NoteAttachments()
                                            .setNoteId(tuple.getT1().getId())
                                            .setAttachmentId(attachmentId))))
                .map(NoteAttachments::getAttachmentId);
    }

    public Mono<Void> deleteAttachments(@NotNull Long id, @NotNull List<Long> attachmentIds) {
        return findById(id)
                .flatMap(ignored -> noteAttachmentsRepository.deleteByNoteIdAndAttachmentIdIn(id, attachmentIds))
                .flatMapMany(ignored -> attachmentService.deleteAttachments(attachmentIds))
                .then();
    }

    private Mono<Note> findById(Long id) {
        return noteRepository.findById(id)
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new NotFoundException("Не удалось найти заметку с ИД " + id))));
    }

    public Mono<GetNoteResponse> getNoteById(@NotNull Long id) {
        return Mono.zip(findById(id),
                        noteAttachmentsRepository.findAllByNoteId(id)
                                .map(NoteAttachments::getAttachmentId)
                                .collectList())
                .map(tuple -> mapToGetNoteResponse(tuple.getT1(), tuple.getT2()));
    }

    public Mono<GetNotesResponse> getNotes(@NotNull Long userId) {
        return noteRepository.findByUserId(userId)
                .map(NoteService::mapToGetNoteResponse)
                .collectList()
                .map(notes -> new GetNotesResponse().setNotes(notes));
    }

    public Mono<Void> getNoteText(@NotNull Long id, @NotNull ServerHttpResponse response) {
        return findById(id)
                .map(Note::getText)
                .flatMap(text -> {
                    response.setStatusCode(OK);
                    response.getHeaders().setContentType(MediaType.TEXT_HTML);
                    response.getHeaders().setContentDisposition(
                            ContentDisposition.attachment()
                                    .filename("note_%d_text".formatted(id))
                                    .build()
                    );
                    return response.writeWith(Mono.just(response.bufferFactory().wrap(text)));
                });
    }

    public Mono<Void> getNoteAttachment(
            @NotNull Long id,
            @NotNull Long attachmentId,
            @NotNull ServerHttpResponse response) {

        return findById(id)
                .flatMap(ignored -> attachmentService.getAttachmentById(attachmentId))
                .flatMap(attachment -> noteAttachmentsRepository.findByNoteIdAndAttachmentId(id, attachmentId)
                        .switchIfEmpty(Mono.defer(() ->
                                Mono.error(
                                        new NotFoundException(
                                                "У заметки с ИД %d нет файла с ИД %d".formatted(id, attachmentId)))))
                        .thenReturn(attachment))
                .flatMap(attachment -> {
                    response.setStatusCode(OK);
                    response.getHeaders().set("Content-Type", attachment.getExtension().getContentType());
                    response.getHeaders().setContentDisposition(
                            ContentDisposition.attachment()
                                    .filename("note_%d_file".formatted(id))
                                    .build()
                    );
                    return response.writeWith(Mono.just(response.bufferFactory().wrap(attachment.getFile())));
                });
    }

    public Mono<Long> attachNote(@NotNull Long id) {
        return findById(id)
                .flatMap(note -> noteRepository.save(note.setAttached(true)))
                .map(Note::getId);
    }

    private static GetNoteResponse mapToGetNoteResponse(Note note) {
        return new GetNoteResponse()
                .setId(note.getId())
                .setIsAttached(note.isAttached())
                .setName(note.getName());
    }

    private static GetNoteResponse mapToGetNoteResponse(Note note, List<Long> attachmentIds) {
        return mapToGetNoteResponse(note)
                .setAttachments(attachmentIds);
    }

}
