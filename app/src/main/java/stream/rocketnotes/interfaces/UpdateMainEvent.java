package stream.rocketnotes.interfaces;

import stream.rocketnotes.NotesItem;

public class UpdateMainEvent {

    public String action;
    public Integer id;
    public NotesItem notesItem;
    public String noteText;

    public UpdateMainEvent(String action) {
        this.action = action;
    }

    public UpdateMainEvent(String action, String noteText) {
        this.action = action;
        this.noteText = noteText;
    }

    public UpdateMainEvent(String action, Integer id) {
        this.action = action;
        this.id = id;
    }

    public UpdateMainEvent(String action, NotesItem note) {
        this.action = action;
        this.notesItem = note;
    }

    public UpdateMainEvent(String action, Integer id, String noteText) {
        this.action = action;
        this.id = id;
        this.noteText = noteText;
    }

    public String getAction() {
        return this.action;
    }

    public Integer getID() {
        return this.id;
    }

    public NotesItem getNotesItem() {
        return this.notesItem;
    }

    public String getNoteText() {
        return noteText;
    }
}
