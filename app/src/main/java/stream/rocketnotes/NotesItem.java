package stream.rocketnotes;

import android.content.Context;

public class NotesItem {

    Context mContext;

    Integer _id = null;
    Long date = null;
    String note = null;
    String image = null;
    String imagePreview = null;
    String cloudId = null;
    boolean shared = false;

    public NotesItem() { }
    public NotesItem(Context mContext) {
        this.mContext = mContext;
    }
    public NotesItem(Integer id, Long date, String note, String image, String imagePreview, String cloudId) {
        this._id = id;
        this.date = date;
        this.note = note;
        this.image = image;
        this.imagePreview = imagePreview;
        this.cloudId = cloudId;
    }
    public NotesItem getNotes() {
        return this;
    }
    public void setID(Integer _id) {
        this._id = _id;
    }
    public void setNote(String note) {
        this.note = note;
    }
    public void setDate(Long date) {
        this.date = date;
    }
    public void setImage(String image) {
        this.image = image;
    }
    public void setImagePreview(String imagePreview) { this.imagePreview = imagePreview; }
    public void setCloudId(String cloudId) { this.cloudId = cloudId; }
    public void setShared(boolean shared) {
        this.shared = shared;
    }

    public boolean getShared() {
        return shared;
    }
    public Integer getID() {
        return this._id;
    }
    public String getNote() {
        return this.note;
    }
    public Long getDate() {
        return this.date;
    }
    public String getImage() {
        return this.image;
    }
    public String getImagePreview() { return imagePreview; }
    public String getCloudId() { return cloudId; }
}

