package ru.vsu.csf.mynotes.dictionary;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public enum FileExtension {

    JPG("jpg"), PNG("png"), JPEG("jpeg"), MP3("mp3"), GIF("gif");

    @Getter
    private final String value;

    public static List<String> getValues() {
        return FileExtension.values()
    }

}
