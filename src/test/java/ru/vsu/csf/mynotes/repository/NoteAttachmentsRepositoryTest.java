package ru.vsu.csf.mynotes.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import ru.vsu.csf.mynotes.model.entity.Attachment;
import ru.vsu.csf.mynotes.model.entity.Note;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class NoteAttachmentsRepositoryTest {

    private static final Duration RESPONSE_TIMEOUT = Duration.ofMillis(1_000_000);

    @Autowired
    private NoteAttachmentsRepository noteAttachmentsRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private AttachmentRepository attachmentRepository;

    @BeforeEach
    @AfterEach
    void setUp() {
        noteAttachmentsRepository.deleteAll()
                .then(noteRepository.deleteAll())
                .then(attachmentRepository.deleteAll())
                .block(RESPONSE_TIMEOUT);
    }

    @Test
    void deleteByNoteIdAndAttachmentIdSuccess() {
        noteRepository.save(new Note())
                .then(attachmentRepository.save(new Attachment()));

        assertDoesNotThrow(() -> noteAttachmentsRepository.deleteByNoteIdAndAttachmentId(1L, 1L)
                .block(RESPONSE_TIMEOUT));

        assertTrue(noteAttachmentsRepository.findAll()
                .collectList()
                .block(RESPONSE_TIMEOUT)
                .isEmpty());
    }

}