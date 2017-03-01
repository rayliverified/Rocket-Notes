package stream.notesapp;

import android.content.Context;

public class NotesItem {


    Context mContext;

    Integer _id = null;
    String note = null;
    Long date = null;
    String image = null;

    public NotesItem()
    {

    }

    public NotesItem (Context mContext)
    {
        this.mContext = mContext;
    }

    public NotesItem getNotes()
    {
        return this;
    }

    public void setNotesID (Integer _id)
    {
        this._id = _id;
    }

    public void setNotesNote(String note)
    {
        this.note = note;
    }

    public void setNotesDate(Long date)
    {
        this.date = date;
    }

    public void setNotesImage(String image)
    {
        this.image = image;
    }

    public Integer getNotesID ()
    {
        return this._id;
    }

    public String getNotesNote() { return this.note; }

    public Long getNotesDate() { return this.date; }

    public String getNotesImage()
    {
        return this.image;
    }
}

