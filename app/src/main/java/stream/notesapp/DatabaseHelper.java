package stream.notesapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper implements Constants {

    private static final int DBVersion = 1;
    private static final String DBName = "NotesDB";

    private static final String TABLE_NOTES = "notes";
    private static final String KEY_ID = "_id";
    private static final String KEY_NOTE = "note";
    private static final String KEY_DATE = "date";
    private static final String KEY_IMAGE = "image";

    public DatabaseHelper(Context context) {
        super(context, DBName, null, DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Table Query.
        String notesTable = "CREATE TABLE IF NOT EXISTS notes (_id INTEGER PRIMARY KEY AUTOINCREMENT, note TEXT, date INTEGER, image TEXT);";
        //Execute Query
        db.execSQL(notesTable);
        Log.d("SQLite", "Tables created");
    }

    public void resetDatabase(Context context) {

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);

        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS notes");
        onCreate(db);
    }

    public void initiateContent() {
        //Initiate note sequence
        for (int i = 0; i < 20; i++)
        {
            NotesItem note = new NotesItem();
            note.setNotesNote("Note content: " + i);
            note.setNotesDate(1487520000000L + i);
            AddNote(note);
        }
    }

    // Adding a new id to database.
    public long AddNote(NotesItem note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_ID, note.getNotesID());
        values.put(KEY_NOTE, note.getNotesNote());
        values.put(KEY_DATE, note.getNotesDate());
        values.put(KEY_IMAGE, note.getNotesImage());

        long id = db.insert(TABLE_NOTES, null, values);
        Log.d("Saved ID", String.valueOf(id));
        db.close();
        return id;
    }

    // Adding a new id to database.
    public NotesItem AddNewNote(String note, Long date, String image) {
        NotesItem notesItem = new NotesItem();
        notesItem.setNotesNote(note);
        notesItem.setNotesDate(date);
        notesItem.setNotesImage(image);
        notesItem.setNotesID((int) AddNote(notesItem));

        return notesItem;
    }

    //Update note
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

    //Return notes sorted by last id date
    public ArrayList<NotesItem> GetNotesDate() {
        ArrayList<NotesItem> notes = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTES + " ORDER BY " + KEY_DATE + " DESC LIMIT 20";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                NotesItem note = new NotesItem();
                if (c.getString(c.getColumnIndexOrThrow("_id")) != null) {
                    note.setNotesID(c.getInt(c.getColumnIndexOrThrow("_id")));
                }
                if (c.getString(c.getColumnIndexOrThrow("note")) != null) {
                    note.setNotesNote(c.getString(c.getColumnIndexOrThrow("note")));
                }
                if (c.getString(c.getColumnIndexOrThrow("date")) != null) {
                    note.setNotesDate(Long.valueOf(c.getString(c.getColumnIndexOrThrow("date"))));
                }
                if (c.getString(c.getColumnIndexOrThrow("image")) != null) {
                    note.setNotesImage(c.getString(c.getColumnIndexOrThrow("image")));
                }
                notes.add(note);
            } while (c.moveToNext());
        }
        c.close();
        return notes;
    }

    //Return note
    public NotesItem GetNote(Integer id) {
        ArrayList<NotesItem> notes = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + KEY_ID + " = '" + id + "'";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        NotesItem note = new NotesItem();
        if (c.moveToFirst()) {
            if (c.getString(c.getColumnIndexOrThrow("_id")) != null) {
                note.setNotesID(c.getInt(c.getColumnIndexOrThrow("_id")));
            }
            if (c.getString(c.getColumnIndexOrThrow("note")) != null) {
                note.setNotesNote(c.getString(c.getColumnIndexOrThrow("note")));
            }
            if (c.getString(c.getColumnIndexOrThrow("date")) != null) {
                note.setNotesDate(Long.valueOf(c.getString(c.getColumnIndexOrThrow("date"))));
            }
            if (c.getString(c.getColumnIndexOrThrow("image")) != null) {
                note.setNotesImage(c.getString(c.getColumnIndexOrThrow("image")));
            }
        }
        c.close();
        return note;
    }

    //Return recent text notes
    public ArrayList<NotesItem> GetTextNotes() {
        ArrayList<NotesItem> notes = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + KEY_NOTE + " NOT NULL ORDER BY " + KEY_DATE + " DESC LIMIT 20";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                NotesItem note = new NotesItem();
                if (c.getString(c.getColumnIndexOrThrow("_id")) != null) {
                    note.setNotesID(c.getInt(c.getColumnIndexOrThrow("_id")));
                }
                if (c.getString(c.getColumnIndexOrThrow("note")) != null) {
                    note.setNotesNote(c.getString(c.getColumnIndexOrThrow("note")));
                }
                if (c.getString(c.getColumnIndexOrThrow("date")) != null) {
                    note.setNotesDate(Long.valueOf(c.getString(c.getColumnIndexOrThrow("date"))));
                }
                notes.add(note);
            } while (c.moveToNext());
        }
        c.close();
        return notes;
    }

    //Return recent images
    public ArrayList<NotesItem> GetRecentImages() {
        ArrayList<NotesItem> notes = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTES + " WHERE " + KEY_IMAGE + " NOT NULL ORDER BY " + KEY_DATE + " DESC LIMIT " + Constants.RECENT_IMAGES;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                NotesItem note = new NotesItem();
                if (c.getString(c.getColumnIndexOrThrow("_id")) != null) {
                    note.setNotesID(c.getInt(c.getColumnIndexOrThrow("_id")));
                }
                if (c.getString(c.getColumnIndexOrThrow("note")) != null) {
                    note.setNotesNote(c.getString(c.getColumnIndexOrThrow("note")));
                }
                if (c.getString(c.getColumnIndexOrThrow("date")) != null) {
                    note.setNotesDate(Long.valueOf(c.getString(c.getColumnIndexOrThrow("date"))));
                }
                if (c.getString(c.getColumnIndexOrThrow("image")) != null) {
                    note.setNotesImage(c.getString(c.getColumnIndexOrThrow("image")));
                }
                notes.add(note);
            } while (c.moveToNext());
        }
        c.close();
        return notes;
    }
}
