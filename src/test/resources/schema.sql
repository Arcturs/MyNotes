CREATE TABLE note(
    id INTEGER PRIMARY KEY,
    name VARCHAR(100),
    text BYTEA
);

CREATE TABLE attachment(
    id INTEGER PRIMARY KEY,
    file BYTEA
);

CREATE TABLE note_attachments(
    id INTEGER PRIMARY KEY,
    note_id INTEGER NOT NULL REFERENCES note(id),
    attachment_id INTEGER NOT NULL REFERENCES attachment(id)
);