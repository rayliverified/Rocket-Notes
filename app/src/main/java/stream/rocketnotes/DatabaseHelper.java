package stream.rocketnotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper implements Constants {

    private static final int DBVersion = 2;
    private static final String DBName = "NotesDB";

    private static final String TABLE_NOTES = "notes";
    private static final String KEY_ID = "_id";
    private static final String KEY_NOTE = "note";
    private static final String KEY_DATE = "date";
    private static final String KEY_IMAGE = "image";
    private static final String KEY_IMAGEPREVIEW = "imagepreview";

    private static final String DATABASE_V2 = "ALTER TABLE " + TABLE_NOTES + " ADD COLUMN imagepreview TEXT";

    public DatabaseHelper(Context context) {
        super(context, DBName, null, DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Table Query.
        String notesTable = "CREATE TABLE IF NOT EXISTS notes (_id INTEGER PRIMARY KEY AUTOINCREMENT, note TEXT, date INTEGER, image TEXT, imagepreview TEXT);";
        //Execute Query
        db.execSQL(notesTable);
        Log.d("SQLite", "Tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            db.execSQL(DATABASE_V2);
        }
    }

    public void resetDatabase(Context context) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);

        db.close();
    }

    public void initiateContent() {
        //Generate sample notes data.
        for (int i = 0; i < 20; i++) {
            NotesItem note = new NotesItem();
            note.setNotesNote("Note content: " + i);
            note.setNotesDate(1487520000000L + i);
            AddNote(note);
        }
    }

    //Method to add a note directly to database.
    public long AddNote(NotesItem note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_ID, note.getNotesID());
        values.put(KEY_NOTE, note.getNotesNote());
        values.put(KEY_DATE, note.getNotesDate());
        values.put(KEY_IMAGE, note.getNotesImage());

        long id = db.insert(TABLE_NOTES, null, values);
        Log.d("Database Saved ID", String.valueOf(id));
        db.close();
        return id;
    }

    //Add a note to database and return the added note object.
    public NotesItem AddNewNote(String note, Long date, String image) {
        NotesItem notesItem = new NotesItem();
        notesItem.setNotesNote(note);
        notesItem.setNotesDate(date);
        notesItem.setNotesImage(image);
        notesItem.setNotesID((int) AddNote(notesItem));

        return notesItem;
    }

    //Update existing text note.
    public long UpdateNote(NotesItem note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        String selection = Integer.toString(note.getNotesID());
        selection = "_id=" + selection;

        if (note.getNotesDate() != null) {
            values.put(KEY_DATE, note.getNotesDate());
        }
        if (note.getNotesNote() != null) {
            values.put(KEY_NOTE, note.getNotesNote());
        }

        return db.update(TABLE_NOTES, values, selection, null);
    }

    //Delete note
    public long DeleteNote(Integer id) {

        SQLiteDatabase db = this.getWritableDatabase();
        String selection = KEY_ID + " = ?";
        String[] selectionArgs = new String[]{Integer.toString(id)};

        return db.delete(TABLE_NOTES, selection, selectionArgs);
    }

    //Return notes sorted by last note date. Used in MainActivity to return all notes.
    public ArrayList<NotesItem> GetNotesDate() {
        ArrayList<NotesItem> notes = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTES + " ORDER BY " + KEY_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                NotesItem item = new NotesItem();
                item = GetNoteItem(item, c);
                notes.add(item);
            } while (c.moveToNext());
        }
        c.close();
        return notes;
    }

    //Return single note.
    public NotesItem GetNote(Integer id) {
        String selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + KEY_ID + " = '" + id + "'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        NotesItem item = new NotesItem();
        if (c.moveToFirst()) {
            item = GetNoteItem(item, c);
        }
        c.close();
        return item;
    }

    //Return text notes. Used in MainActivity to filter text notes.
    public ArrayList<NotesItem> GetTextNotes() {
        ArrayList<NotesItem> notes = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + KEY_NOTE + " NOT NULL ORDER BY " + KEY_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                NotesItem item = new NotesItem();
                item = GetNoteItem(item, c);
                notes.add(item);
            } while (c.moveToNext());
        }
        c.close();
        return notes;
    }

    //Return specified number of recent text notes. Used in RecentNotesWidget and ShareActivity.
    public ArrayList<NotesItem> GetTextNotes(Integer limit) {
        ArrayList<NotesItem> notes = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + KEY_IMAGE + " IS NULL ORDER BY " + KEY_DATE + " DESC LIMIT " + limit;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                NotesItem item = new NotesItem();
                item = GetNoteItem(item, c);
                notes.add(item);
            } while (c.moveToNext());
        }
        c.close();
        return notes;
    }

    //Return latest noteID. Used in EditActivity to keep track of autosaved note and delete if necessary.
    public Integer GetLatestID() {
        String selectQuery = "SELECT * FROM " + TABLE_NOTES + " ORDER BY " + KEY_ID + " DESC LIMIT 1";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        Integer id = -1;
        if (c.moveToFirst()) {
            id = c.getInt(c.getColumnIndexOrThrow("_id"));
        }
        c.close();
        return id;
    }

    //Return image notes. Used in MainActivity to filter image notes.
    public ArrayList<NotesItem> GetImageNotes() {
        ArrayList<NotesItem> notes = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + KEY_IMAGE + " NOT NULL ORDER BY " + KEY_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                NotesItem item = new NotesItem();
                item = GetNoteItem(item, c);
                notes.add(item);
            } while (c.moveToNext());
        }
        c.close();
        return notes;
    }

    //Return recent images. Used in RecentImagesWidget.
    public ArrayList<NotesItem> GetRecentImages() {
        ArrayList<NotesItem> notes = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + KEY_IMAGE + " NOT NULL ORDER BY " + KEY_DATE + " DESC LIMIT " + Constants.RECENT_IMAGES;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                NotesItem item = new NotesItem();
                item = GetNoteItem(item, c);
                notes.add(item);
            } while (c.moveToNext());
        }
        c.close();
        return notes;
    }

    public NotesItem GetNoteItem(NotesItem item, Cursor c) {
        item.setNotesID(c.getInt(c.getColumnIndexOrThrow(KEY_ID)));
        item.setNotesNote(c.getString(c.getColumnIndexOrThrow(KEY_NOTE)));
        item.setNotesDate(Long.valueOf(c.getString(c.getColumnIndexOrThrow(KEY_DATE))));
        item.setNotesImage(c.getString(c.getColumnIndexOrThrow(KEY_IMAGE)));
        item.setNotesImagePreview(c.getString(c.getColumnIndexOrThrow(KEY_IMAGEPREVIEW)));

        return item;
    }
}
