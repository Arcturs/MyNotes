package ru.vsu.csf.mynotes.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.exception.BadRequestException;
import ru.vsu.csf.mynotes.exception.NotFoundException;
import ru.vsu.csf.mynotes.model.entity.Note;
import ru.vsu.csf.mynotes.repository.NoteAttachmentsRepository;
import ru.vsu.csf.mynotes.repository.NoteRepository;
import ru.vsu.csf.mynotes.util.FilePartUtils;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    private static final Duration RESPONSE_TIMEOUT = Duration.ofMillis(5_000);
    private static final Long NOTE_ID = 1L;
    private static final Long ATTACHMENT_ID1 = 20L;
    private static final Long ATTACHMENT_ID2 = 21L;
    private static final Long USER_ID = 1L;

    @InjectMocks
    private NoteService noteService;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private FilePart filePart;

    @Mock
    private AttachmentService attachmentService;

    @Mock
    private NoteAttachmentsRepository noteAttachmentsRepository;

    @Test
    void createNoteSuccessTest() {
        when(noteRepository.save(any())).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> noteService.createNote(USER_ID).block(RESPONSE_TIMEOUT));

        verify(noteRepository, atMostOnce()).save(any());
    }

    @Test
    void changeNoteNameSuccessTest() {
        final var note = createTestNote();
        when(noteRepository.findById(NOTE_ID)).thenReturn(Mono.just(note));
        when(noteRepository.save(any())).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> noteService.changeNoteName(NOTE_ID, "name1").block(RESPONSE_TIMEOUT));

        verify(noteRepository, atMostOnce()).save(any());
    }

    @Test
    void changeNoteNameThrowsBadRequestExceptionTest() {
        assertThatThrownBy(() -> noteService.changeNoteName(NOTE_ID, "").block(RESPONSE_TIMEOUT))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Имя заметки не может быть пустым");

        verify(noteRepository, never()).save(any());
    }

    @Test
    void changeNoteNameThrowsNotFoundExceptionTest() {
        when(noteRepository.findById(NOTE_ID)).thenReturn(Mono.empty());

        assertThatThrownBy(() -> noteService.changeNoteName(NOTE_ID, "name1").block(RESPONSE_TIMEOUT))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Не удалось найти заметку с ИД 1");

        verify(noteRepository, never()).save(any());
    }

    @Test
    void changeNoteTextSuccessTest() {
        final var note = createTestNote();
        final var dataBufferBytes = "bytes".getBytes();
        when(noteRepository.findById(NOTE_ID)).thenReturn(Mono.just(note));
        try (MockedStatic<FilePartUtils> utilities = Mockito.mockStatic(FilePartUtils.class)) {
            utilities.when(() -> FilePartUtils.getByteArray(filePart))
                    .thenReturn(Mono.just(dataBufferBytes));
            when(noteRepository.save(note.setText(dataBufferBytes))).thenReturn(Mono.empty());

            assertDoesNotThrow(() -> noteService.changeNoteText(NOTE_ID, filePart).block(RESPONSE_TIMEOUT));

            verify(noteRepository, atMostOnce()).save(any());
        }
    }

    @Test
    void changeNoteTextThrowsNotFoundExceptionTest() {
        final var dataBufferBytes = "bytes".getBytes();
        when(noteRepository.findById(NOTE_ID)).thenReturn(Mono.empty());
        try (MockedStatic<FilePartUtils> utilities = Mockito.mockStatic(FilePartUtils.class)) {
            utilities.when(() -> FilePartUtils.getByteArray(filePart))
                    .thenReturn(Mono.just(dataBufferBytes));

            assertThatThrownBy(() -> noteService.changeNoteText(NOTE_ID, filePart).block(RESPONSE_TIMEOUT))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Не удалось найти заметку с ИД 1");

            verify(noteRepository, never()).save(any());
        }
    }

    @Test
    void addAttachmentsToNoteSuccessTest() {
        final var note = createTestNote();
        when(noteRepository.findById(NOTE_ID)).thenReturn(Mono.just(note));
        when(attachmentService.saveAttachments(List.of(filePart)))
                .thenReturn(Flux.fromIterable(List.of(ATTACHMENT_ID1, ATTACHMENT_ID2)));
        when(noteAttachmentsRepository.save(any())).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> noteService.addAttachmentsToNote(NOTE_ID, List.of(filePart)).block(RESPONSE_TIMEOUT));

        verify(noteAttachmentsRepository, times(2)).save(any());
    }

    @Test
    void addAttachmentsToNoteThrowsNotFoundExceptionTest() {
        when(noteRepository.findById(NOTE_ID)).thenReturn(Mono.empty());
        when(attachmentService.saveAttachments(List.of(filePart)))
                .thenReturn(Flux.fromIterable(List.of(ATTACHMENT_ID1, ATTACHMENT_ID2)));

        assertThatThrownBy(() -> noteService.addAttachmentsToNote(NOTE_ID, List.of(filePart)).block(RESPONSE_TIMEOUT))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Не удалось найти заметку с ИД 1");

        verify(noteAttachmentsRepository, never()).save(any());
    }

    @Test
    void deleteAttachmentsSuccessTest() {
        final var note = createTestNote();
        when(noteRepository.findById(NOTE_ID)).thenReturn(Mono.just(note));
        when(attachmentService.deleteAttachments(List.of(ATTACHMENT_ID1, ATTACHMENT_ID2)))
                .thenReturn(Mono.empty());
        when(noteAttachmentsRepository.deleteByNoteIdAndAttachmentIdIn(anyLong(), anyList()))
                .thenReturn(Mono.empty());

        assertDoesNotThrow(() -> noteService.deleteAttachments(NOTE_ID, List.of(ATTACHMENT_ID1, ATTACHMENT_ID2))
                .block(RESPONSE_TIMEOUT));

        verify(noteAttachmentsRepository, times(2))
                .deleteByNoteIdAndAttachmentIdIn(anyLong(), anyList());
    }

    @Test
    void deleteAttachmentsThrowsNotFoundExceptionTest() {
        when(noteRepository.findById(NOTE_ID)).thenReturn(Mono.empty());

        assertThatThrownBy(() -> noteService.deleteAttachments(NOTE_ID, List.of(ATTACHMENT_ID1, ATTACHMENT_ID2))
                    .block(RESPONSE_TIMEOUT))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Не удалось найти заметку с ИД 1");

        verify(noteAttachmentsRepository, never())
                .deleteByNoteIdAndAttachmentIdIn(anyLong(), anyList());
    }

    private static Note createTestNote() {
        return new Note()
                .setId(1L)
                .setName("name")
                .setText("text".getBytes());
    }

}