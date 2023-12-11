package ru.vsu.csf.mynotes.service;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.exception.BadRequestException;
import ru.vsu.csf.mynotes.exception.NotFoundException;
import ru.vsu.csf.mynotes.model.entity.Note;
import ru.vsu.csf.mynotes.model.entity.NoteAttachments;
import ru.vsu.csf.mynotes.repository.NoteAttachmentsRepository;
import ru.vsu.csf.mynotes.repository.NoteRepository;
import ru.vsu.csf.mynotes.util.FilePartUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private static final String DEFAULT_NOTE_NAME = "Новая_заметка";

    private final NoteRepository noteRepository;
    private final NoteAttachmentsRepository noteAttachmentsRepository;
    private final AttachmentService attachmentService;

    public Mono<Long> createNote() {
        return noteRepository.save(new Note().setName(DEFAULT_NOTE_NAME))
                .map(Note::getId);
    }

    public Mono<Long> changeNoteName(Long id, String name) {
        if (name.isBlank()) {
            throw new BadRequestException("Имя заметки не может быть пустым");
        }
        return findById(id)
                .flatMap(note -> noteRepository.save(note.setName(name)))
                .map(Note::getId);
    }

    public Mono<Long> changeNoteText(Long id, FilePart text) {
        return Mono.zip(findById(id), FilePartUtils.getByteArray(text))
                .flatMap(tuple -> noteRepository.save(tuple.getT1().setText(tuple.getT2())))
                .map(Note::getId);
    }

    public Mono<Long> addAttachmentsToNote(Long id, List<FilePart> attachments) {
        return Mono.zip(findById(id), attachmentService.saveAttachments(attachments).collectList())
                .flatMap(tuple -> Flux.fromIterable(tuple.getT2())
                            .flatMap(attachmentId -> noteAttachmentsRepository.save(
                                    new NoteAttachments()
                                            .setNoteId(tuple.getT1().getId())
                                            .setAttachmentId(attachmentId)))
                        .then())
                .thenReturn(id);
    }

    public Mono<Void> deleteAttachments(Long id, List<Long> attachmentIds) {
        return findById(id)
                .flatMapMany(ignored -> attachmentService.deleteAttachments(attachmentIds)
                        .thenMany(Flux.fromIterable(attachmentIds)))
                .flatMap(attachmentId -> noteAttachmentsRepository.deleteByNoteIdAndAttachmentId(id, attachmentId))
                .then();
    }

    private Mono<Note> findById(Long id) {
        return noteRepository.findById(id)
                .switchIfEmpty(
                        Mono.error(new NotFoundException("Не удалось найти заметку с ИД " + id)));
    }

}
