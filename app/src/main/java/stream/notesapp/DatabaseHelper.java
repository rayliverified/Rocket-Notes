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

    public DatabaseHelper(Context context) {
        super(context, DBName, null, DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Table Query.
        String notesTable = "CREATE TABLE IF NOT EXISTS notes (_id INTEGER PRIMARY KEY AUTOINCREMENT, note TEXT, date INTEGER);";
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

    // Adding a new message to database.
    public long AddNote(NotesItem note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_ID, note.getNotesID());
        values.put(KEY_NOTE, note.getNotesNote());
        values.put(KEY_DATE, note.getNotesDate());

        long id = db.insert(TABLE_NOTES, null, values);
        Log.d("Saved ID", String.valueOf(id));
        db.close();
        return id;
    }

    // Adding a new message to database.
    public NotesItem AddNewNote(String note, Long date) {
        NotesItem notesItem = new NotesItem();
        notesItem.setNotesNote(note);
        notesItem.setNotesDate(date);
        notesItem.setNotesID((int) AddNote(notesItem));

        return notesItem;
    }


    //Return contacts sorted by last message date
    public ArrayList<NotesItem> GetNotesDate() {
        ArrayList<NotesItem> notes = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTES + " ORDER BY " + KEY_DATE + " DESC LIMIT 20";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                NotesItem note = new NotesItem();
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
}
