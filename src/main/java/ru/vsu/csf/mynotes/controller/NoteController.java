package ru.vsu.csf.mynotes.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.model.dto.ChangeNoteNameRequest;
import ru.vsu.csf.mynotes.model.dto.GetNoteResponse;
import ru.vsu.csf.mynotes.model.dto.GetNotesResponse;
import ru.vsu.csf.mynotes.model.dto.RemoveAttachmentsRequest;
import ru.vsu.csf.mynotes.service.AuthService;
import ru.vsu.csf.mynotes.service.NoteService;

import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Note Controller")
@RequestMapping("api/v1/notes")
public class NoteController {

    private final NoteService noteService;
    private final AuthService authService;

    @GetMapping("")
    @Operation(summary = "Показывает все заметки пользователя")
    public Mono<GetNotesResponse> getNotes(@AuthenticationPrincipal Principal principal) {
        return authService.getUserId(principal.getName())
                .flatMap(noteService::getNotes);
    }

    @PostMapping("")
    @Operation(summary = "Создает новую заметку")
    public Mono<Long> createNote(@AuthenticationPrincipal Principal principal) {
        return authService.getUserId(principal.getName())
                .flatMap(noteService::createNote);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Показывает заметку пользователя")
    public Mono<GetNoteResponse> getNoteById(@PathVariable Long id, @AuthenticationPrincipal Principal principal) {
        return authService.checkUsersPermission(principal.getName(), id)
                .then(noteService.getNoteById(id));
    }

    @PostMapping("/{id}/attach")
    @Operation(summary = "Прикрепляет заметку")
    public Mono<Long> attachNote(@PathVariable Long id, @AuthenticationPrincipal Principal principal) {
        return authService.checkUsersPermission(principal.getName(), id)
                .then(noteService.attachNote(id));
    }

    @PostMapping("/{id}/name")
    @Operation(summary = "Изменяет название заметки")
    public Mono<Long> changeNotesName(
            @PathVariable Long id,
            @RequestBody ChangeNoteNameRequest request,
            @AuthenticationPrincipal Principal principal) {

        return authService.checkUsersPermission(principal.getName(), id)
                .then(noteService.changeNoteName(id, request.getName()));
    }

    @GetMapping("/{id}/text")
    @Operation(summary = "Показывает текст заметки")
    public Mono<Void> getNotesText(
            @PathVariable Long id,
            @AuthenticationPrincipal Principal principal,
            ServerHttpResponse response) {

        return authService.checkUsersPermission(principal.getName(), id)
                .then(noteService.getNoteText(id, response));
    }

    @PostMapping(path = "/{id}/text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Изменяет текст заметки")
    public Mono<Long> changeNotesText(
            @PathVariable Long id,
            @RequestPart Mono<FilePart> text,
            @AuthenticationPrincipal Principal principal) {

        return authService.checkUsersPermission(principal.getName(), id)
                .then(text.flatMap(it -> noteService.changeNoteText(id, it)));
    }

    @PostMapping(path = "/{id}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Добавляет вложения в заметку")
    public Mono<List<Long>> addNotesAttachment(
            @PathVariable Long id,
            @RequestPart Flux<FilePart> attachments,
            @AuthenticationPrincipal Principal principal) {

        return authService.checkUsersPermission(principal.getName(), id)
                .then(attachments.collectList()
                        .flatMapMany(it -> noteService.addAttachmentsToNote(id, it))
                        .collectList());
    }

    @DeleteMapping("/{id}/attachment")
    @Operation(summary = "Удаляет вложения из заметки")
    public Mono<Void> removeAttachmentsFromNote(
            @PathVariable Long id,
            @RequestBody RemoveAttachmentsRequest request,
            @AuthenticationPrincipal Principal principal) {

        return authService.checkUsersPermission(principal.getName(), id)
                .then(noteService.deleteAttachments(id, request.getAttachments()));
    }

    @GetMapping("/{id}/attachment/{attachmentId}")
    @Operation(summary = "Показывает вложение заметки")
    public Mono<Void> getNotesAttachment(
            @PathVariable Long id,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal Principal principal,
            ServerHttpResponse response) {

        return authService.checkUsersPermission(principal.getName(), id)
                .then(noteService.getNoteAttachment(id, attachmentId, response));
    }

}
