package ru.vsu.csf.mynotes.service;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vsu.csf.mynotes.configuration.property.ApplicationProperties;
import ru.vsu.csf.mynotes.dictionary.FileExtension;
import ru.vsu.csf.mynotes.exception.BadRequestException;
import ru.vsu.csf.mynotes.exception.NotFoundException;
import ru.vsu.csf.mynotes.model.entity.Attachment;
import ru.vsu.csf.mynotes.repository.AttachmentRepository;
import ru.vsu.csf.mynotes.util.FilePartUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;
    private final ApplicationProperties applicationProperties;

    public Flux<Long> saveAttachments(@NotNull List<FilePart> attachments) {
        return Mono.just(attachments)
                .flatMap(it -> validateAttachments(it).thenReturn(it))
                .flatMapIterable(it -> it)
                .flatMap(this::addAttachment);
    }

    private Mono<Void> validateAttachments(List<FilePart> attachments) {
        if (attachments.size() > applicationProperties.getMaxFileAmount()) {
            throw new BadRequestException("Количество файлов превышает заданный лимит в количестве %d файлов"
                    .formatted(applicationProperties.getMaxFileAmount()));
        }
        return Flux.fromIterable(attachments)
                .flatMap(this::validateAttachment)
                .then();
    }

    private Mono<Void> validateAttachment(FilePart attachment) {
        var fileNameParts = attachment.filename().split("\\.");
        if (fileNameParts.length < 2) {
            throw new BadRequestException("Файл не имеет расширения");
        }
        if (!FileExtension.getValues().contains(fileNameParts[1])) {
            throw new BadRequestException("Формат вложения %s не поддерживается".formatted(fileNameParts[1]));
        }
        return Mono.just(attachment)
                .flatMapMany(Part::content)
                .map(DataBuffer::capacity)
                .collectList()
                .doOnNext(capacity -> {
                    var fileSize = capacity.stream()
                            .reduce(0, Integer::sum);
                    if (fromBytesToMegaBytes(fileSize) > applicationProperties.getMaxFileSizeMb()) {
                        throw new BadRequestException(
                                "Размер файла превышает максимальное число Мбайт: %s (в файле %s)"
                                        .formatted(applicationProperties.getMaxFileSizeMb(), fileSize));
                    }
                })
                .then();
    }

    private Mono<Long> addAttachment(FilePart attachment) {
        return FilePartUtils.getByteArray(attachment)
                .flatMap(bytes -> attachmentRepository.save(
                        new Attachment().setFile(bytes)
                                .setExtension(
                                        FileExtension.valueOf(attachment.filename()
                                                .split("\\.")[1]
                                                .toUpperCase()))))
                .map(Attachment::getId);
    }

    public Mono<Void> deleteAttachments(@NotNull List<Long> ids) {
        return Flux.fromIterable(ids)
                .flatMap(this::deleteAttachment)
                .then();
    }

    private Mono<Void> deleteAttachment(Long id) {
        return findById(id)
                .flatMap(ignored -> attachmentRepository.deleteById(id));
    }

    private Mono<Attachment> findById(Long id) {
        return attachmentRepository.findById(id)
                .switchIfEmpty(Mono.defer(() ->
                        Mono.error(new NotFoundException("Не существует вложения с ИД " + id))));
    }

    public Mono<Attachment> getAttachmentById(@NotNull Long id) {
        return findById(id);
    }

    private static int fromBytesToMegaBytes(int bytes) {
        return bytes / 1024 / 1024;
    }

}
