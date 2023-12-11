package ru.vsu.csf.mynotes.model.entity;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "note")
@Accessors(chain = true)
public class Note {

    @Id
    private Long id;

    private String name;

    private byte[] text;

}
