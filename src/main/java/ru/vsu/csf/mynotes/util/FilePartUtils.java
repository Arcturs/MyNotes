package ru.vsu.csf.mynotes.util;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.vsu.csf.mynotes.exception.InternalErrorException;

@UtilityClass
public class FilePartUtils {

    public static Mono<byte[]> getByteArray(FilePart filePart) {
        return DataBufferUtils.join(filePart.content())
                .publishOn(Schedulers.boundedElastic())
                .map(dataBuffer -> {
                    try {
                        return dataBuffer.asInputStream().readAllBytes();
                    } catch (Exception e) {
                        throw new InternalErrorException("Произошла ошибка при чтении файла " + filePart.filename());
                    }
                });
    }

}
