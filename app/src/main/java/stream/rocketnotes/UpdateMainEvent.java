package stream.rocketnotes;

public class UpdateMainEvent {

    public String action;
    public Integer id;
    public NotesItem notesItem;

    public UpdateMainEvent(String action) {
        this.action = action;
    }

    public UpdateMainEvent(String action, Integer id) {
        this.action = action;
        this.id = id;
    }

    public UpdateMainEvent(String action, NotesItem note) {
        this.action = action;
        this.notesItem = note;
    }

    public String getAction() { return this.action; }

    public Integer getID() { return this.id; }

    public NotesItem getNotesItem() { return this.notesItem; }
}
