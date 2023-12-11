package ru.vsu.csf.mynotes.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
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
