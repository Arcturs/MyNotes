package ru.vsu.csf.mynotes.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.configuration.property.ApplicationProperties;
import ru.vsu.csf.mynotes.dictionary.FileExtension;
import ru.vsu.csf.mynotes.exception.BadRequestException;
import ru.vsu.csf.mynotes.exception.NotFoundException;
import ru.vsu.csf.mynotes.model.entity.Attachment;
import ru.vsu.csf.mynotes.repository.AttachmentRepository;
import ru.vsu.csf.mynotes.util.FilePartUtils;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    private static final Duration RESPONSE_TIMEOUT = Duration.ofMillis(5_000);
    private static final Integer MAX_FILE_AMOUNT = 2;
    private static final Integer MAX_FILE_SIZE_MB = 12;

    @InjectMocks
    private AttachmentService attachmentService;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private FilePart filePart;

    @Mock
    private DataBuffer dataBuffer;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Test
    void saveAttachmentsSuccessTest() {
        when(applicationProperties.getMaxFileAmount()).thenReturn(MAX_FILE_AMOUNT);
        when(applicationProperties.getMaxFileSizeMb()).thenReturn(MAX_FILE_SIZE_MB);

        when(filePart.filename()).thenReturn("image.png");
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));
        when(dataBuffer.capacity()).thenReturn(10*1024*1024);

        final var dataBufferBytes = "bytes".getBytes();
        try (MockedStatic<FilePartUtils> utilities = Mockito.mockStatic(FilePartUtils.class)) {
            utilities.when(() -> FilePartUtils.getByteArray(filePart))
                    .thenReturn(Mono.just(dataBufferBytes));
            when(attachmentRepository.save(new Attachment()
                    .setFile(dataBufferBytes)
                    .setExtension(FileExtension.PNG)))
                    .thenReturn(Mono.empty());

            assertDoesNotThrow(() -> attachmentService.saveAttachments(List.of(filePart))
                    .collectList()
                    .block(RESPONSE_TIMEOUT));

            verify(attachmentRepository, atMostOnce()).save(any());
        }
    }

    @Test
    void saveAttachmentThrowsExceptionWhenTooMuchFilesTest() {
        when(applicationProperties.getMaxFileAmount()).thenReturn(MAX_FILE_AMOUNT);

        assertThatThrownBy(() -> attachmentService.saveAttachments(List.of(filePart, filePart, filePart))
                    .collectList()
                    .block(RESPONSE_TIMEOUT))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Количество файлов превышает заданный лимит в количестве 2 файлов");

        verify(attachmentRepository, never()).save(any());
    }

    @Test
    void saveAttachmentThrowsExceptionWhenFileDoesNotHaveExtensionTest() {
        when(applicationProperties.getMaxFileAmount()).thenReturn(MAX_FILE_AMOUNT);
        when(filePart.filename()).thenReturn("image");

        assertThatThrownBy(() -> attachmentService.saveAttachments(List.of(filePart))
                    .collectList()
                    .block(RESPONSE_TIMEOUT))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Файл не имеет расширения");

        verify(attachmentRepository, never()).save(any());
    }

    @Test
    void saveAttachmentThrowsExceptionWhenFileHasInvalidExtensionTest() {
        when(applicationProperties.getMaxFileAmount()).thenReturn(MAX_FILE_AMOUNT);
        when(filePart.filename()).thenReturn("image.mp4");

        assertThatThrownBy(() -> attachmentService.saveAttachments(List.of(filePart))
                    .collectList()
                    .block(RESPONSE_TIMEOUT))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Формат вложения mp4 не поддерживается");

        verify(attachmentRepository, never()).save(any());
    }

    @Test
    void saveAttachmentThrowsExceptionWhenFileTooLargeTest() {
        when(applicationProperties.getMaxFileAmount()).thenReturn(MAX_FILE_AMOUNT);
        when(applicationProperties.getMaxFileSizeMb()).thenReturn(MAX_FILE_SIZE_MB);

        when(filePart.filename()).thenReturn("image.mp3");
        when(filePart.content()).thenReturn(Flux.just(dataBuffer));
        when(dataBuffer.capacity()).thenReturn(30*1024*1024);

        assertThatThrownBy(() -> attachmentService.saveAttachments(List.of(filePart))
                    .collectList()
                    .block(RESPONSE_TIMEOUT))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Размер файла превышает максимальное число Мбайт: ");

        verify(attachmentRepository, never()).save(any());
    }

    @Test
    void deleteAttachmentsSuccessTest() {
        when(attachmentRepository.findById(1L)).thenReturn(Mono.just(new Attachment()));
        when(attachmentRepository.deleteById(1L)).thenReturn(Mono.empty());

        assertDoesNotThrow(() -> attachmentService.deleteAttachments(List.of(1L)).block(RESPONSE_TIMEOUT));

        verify(attachmentRepository, atMostOnce()).deleteById(1L);
    }

    @Test
    void deleteAttachmentsThrowsNotFoundExceptionTest() {
        when(attachmentRepository.findById(1L)).thenReturn(Mono.empty());

        assertThatThrownBy(() -> attachmentService.deleteAttachments(List.of(1L)).block(RESPONSE_TIMEOUT))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Не существует вложения с ИД 1");

        verify(attachmentRepository, never()).deleteById(1L);
    }

}
