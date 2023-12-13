package ru.vsu.csf.mynotes.model.entity;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import ru.vsu.csf.mynotes.dictionary.FileExtension;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "attachment")
public class Attachment {

    @Id
    private Long id;

    private byte[] file;

    private FileExtension extension;

}
