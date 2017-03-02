package stream.notesapp;

public class UpdateMainEvent {

    public String action;
    public Integer id;

    public UpdateMainEvent(String action) {
        this.action = action;
    }

    public UpdateMainEvent(String action, Integer id) {
        this.action = action;
        this.id = id;
    }

    public String getAction() { return this.action; }

    public Integer getID() { return this.id; }
}
