package ru.vsu.csf.mynotes.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class GetNoteResponse {

    private Long id;
    private String name;
    private Boolean isAttached;

    private List<Long> attachments;

}
