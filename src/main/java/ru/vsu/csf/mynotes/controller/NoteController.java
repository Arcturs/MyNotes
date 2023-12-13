package ru.vsu.csf.mynotes.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.model.dto.ChangeNoteNameRequest;
import ru.vsu.csf.mynotes.model.dto.GetNoteResponse;
import ru.vsu.csf.mynotes.model.dto.GetNotesResponse;
import ru.vsu.csf.mynotes.model.dto.RemoveAttachmentsRequest;
import ru.vsu.csf.mynotes.service.NoteService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notes")
public class NoteController {

    private final NoteService noteService;

    @GetMapping("")
    public Mono<GetNotesResponse> getNotes() {
        return noteService.getNotes();
    }

    @PostMapping("")
    public Mono<Long> createNote() {
        return noteService.createNote();
    }

    @GetMapping("/{id}")
    public Mono<GetNoteResponse> getNoteById(@PathVariable Long id) {
        return noteService.getNoteById(id);
    }

    @PostMapping("/{id}/name")
    public Mono<Long> changeNotesName(@PathVariable Long id, @RequestBody ChangeNoteNameRequest request) {
        return noteService.changeNoteName(id, request.getName());
    }

    @GetMapping("/{id}/text")
    public Mono<Void> getNotesText(@PathVariable Long id, ServerHttpResponse response) {
        return noteService.getNoteText(id, response);
    }

    @PostMapping("/{id}/text")
    public Mono<Long> changeNotesText(@PathVariable Long id, Mono<FilePart> text) {
        return text.flatMap(it -> noteService.changeNoteText(id, it));
    }

    @GetMapping("/{id}/attachment")
    public Mono<Long> addNotesAttachment(@PathVariable Long id, Flux<FilePart> attachments) {
        return attachments.collectList()
                .flatMap(it -> noteService.addAttachmentsToNote(id, it));
    }

    @DeleteMapping("/{id}/attachment")
    public Mono<Void> removeAttachmentsFromNote(@PathVariable Long id, @RequestBody RemoveAttachmentsRequest request) {
        return noteService.deleteAttachments(id, request.getAttachments());
    }

    @GetMapping("/{id}/attachment/{attachmentId}")
    public Mono<Void> getNotesAttachment(
            @PathVariable Long id, @PathVariable Long attachmentId, ServerHttpResponse response) {
        return noteService.getNoteAttachment(id, attachmentId, response);
    }

}
