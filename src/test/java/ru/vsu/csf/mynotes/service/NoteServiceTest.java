package ru.vsu.csf.mynotes.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.exception.BadRequestException;
import ru.vsu.csf.mynotes.exception.NotFoundException;
import ru.vsu.csf.mynotes.model.entity.Note;
import ru.vsu.csf.mynotes.repository.NoteRepository;
import ru.vsu.csf.mynotes.util.FilePartUtils;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    private static final Duration RESPONSE_TIMEOUT = Duration.ofMillis(5_000);

    @InjectMocks
    private NoteService noteService;

    @Mock
    private NoteRepository noteRepository;

    @Test
    void createNoteSuccessTest() {
        when(noteRepository.save(any())).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> noteService.createNote().block(RESPONSE_TIMEOUT));

        verify(noteRepository, atMostOnce()).save(any());
    }

    @Test
    void changeNoteNameSuccessTest() {
        var note = createTestNote();
        when(noteRepository.findById(1L)).thenReturn(Mono.just(note));
        when(noteRepository.save(any())).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> noteService.changeNoteName(1L, "name1").block(RESPONSE_TIMEOUT));

        verify(noteRepository, atMostOnce()).save(any());
    }

    @Test
    void changeNoteNameThrowsBadRequestExceptionTest() {
        assertThatThrownBy(() -> noteService.changeNoteName(1L, "").block(RESPONSE_TIMEOUT))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Имя заметки не может быть пустым");

        verify(noteRepository, never()).save(any());
    }

    @Test
    void changeNoteNameThrowsNotFoundExceptionTest() {
        when(noteRepository.findById(1L)).thenReturn(Mono.empty());

        assertThatThrownBy(() -> noteService.changeNoteName(1L, "name1").block(RESPONSE_TIMEOUT))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Не удалось найти заметку с ИД 1");

        verify(noteRepository, never()).save(any());
    }

    @Test
    void changeNoteTextSuccessTest() {
        var note = createTestNote();
        when(noteRepository.findById(1L)).thenReturn(Mono.just(note));
    }

    private static Note createTestNote() {
        return new Note()
                .setId(1L)
                .setName("name")
                .setText("text".getBytes());
    }

}