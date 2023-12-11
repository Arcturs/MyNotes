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
@Accessors(chain = true)
@Table(name = "note_attachments")
public class NoteAttachments {

    @Id
    private Long id;

    private Long noteId;

    private Long attachmentId;

}
