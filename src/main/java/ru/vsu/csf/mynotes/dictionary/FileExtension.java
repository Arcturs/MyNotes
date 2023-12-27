package ru.vsu.csf.mynotes.dictionary;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
public enum FileExtension {

    JPG(MediaType.IMAGE_JPEG_VALUE),
    PNG(MediaType.IMAGE_PNG_VALUE),
    JPEG(MediaType.IMAGE_JPEG_VALUE),
    MP3("audio/mp3"),
    GIF(MediaType.IMAGE_GIF_VALUE);

    @Getter
    private final String contentType;

    public static List<String> getValues() {
        return Arrays.stream(FileExtension.values())
                .map(el -> el.name().toLowerCase())
                .toList();
    }

}
