package stream.rocketnotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;

import es.dmoral.toasty.Toasty;
import stream.rocketnotes.utils.FileUtils;

public class DatabaseHelper extends SQLiteOpenHelper implements Constants {

    private static final int DBVersion = 3;
    private static final String DBName = "NotesDB";

    public static final String TABLE_NOTES = "notes";
    public static final String KEY_ID = "_id";
    public static final String KEY_DATE = "date";
    public static final String KEY_NOTE = "note";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_IMAGEPREVIEW = "imagepreview";
    public static final String KEY_CLOUDID = "cloudid";

    private static final String DATABASE_V2 = "ALTER TABLE " + TABLE_NOTES + " ADD COLUMN imagepreview TEXT";
    private static final String DATABASE_V3 = "ALTER TABLE " + TABLE_NOTES + " ADD COLUMN cloudid TEXT";

    public DatabaseHelper(Context context) {
        super(context, DBName, null, DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Table Query.
        String notesTable = "CREATE TABLE IF NOT EXISTS notes (_id INTEGER PRIMARY KEY AUTOINCREMENT, date INTEGER, note TEXT, image TEXT, imagepreview TEXT);";
        //Execute Query
        db.execSQL(notesTable);
        Log.d("SQLite", "Tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            db.execSQL(DATABASE_V2);
        }
        if (oldVersion < 3) {
            db.execSQL(DATABASE_V3);
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
        values.put(KEY_IMAGEPREVIEW, note.getNotesImagePreview());
        values.put(KEY_CLOUDID, note.getCloudId());

        long id = db.insert(TABLE_NOTES, null, values);
        Log.d("Database Saved ID", String.valueOf(id));
        db.close();
        return id;
    }

    //Add a note to database and return the added note object.
    public NotesItem AddNewNote(String note, Long date, String image, String imagePreview) {
        NotesItem notesItem = new NotesItem();
        notesItem.setNotesNote(note);
        notesItem.setNotesDate(date);
        notesItem.setNotesImage(image);
        notesItem.setNotesImagePreview(imagePreview);
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
        item.setCloudId(c.getString(c.getColumnIndexOrThrow(KEY_CLOUDID)));

        return item;
    }

    public void DeleteImagePreview()
    {
        String selectQuery = "SELECT " + KEY_IMAGE + ", " + KEY_IMAGEPREVIEW + " FROM " + TABLE_NOTES + " WHERE " + KEY_IMAGEPREVIEW + " NOT NULL";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                try {
                    String imagePreview = c.getString(c.getColumnIndexOrThrow(KEY_IMAGEPREVIEW));
                    Log.d("Image Preview", imagePreview);
                    File imageFile = new File(new URI(imagePreview));
                    imageFile.delete();
                    UpdateImagePreview(c.getString(c.getColumnIndexOrThrow(KEY_IMAGE)), null);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } while (c.moveToNext());
        }
        c.close();
    }

    public void CreateImagePreview(Context context) {
        String selectQuery = "SELECT " + KEY_IMAGE + " FROM " + TABLE_NOTES + " WHERE " + KEY_IMAGE + " NOT NULL";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        if (c.moveToFirst()) {
            do {
                Log.d("Image", c.getString(c.getColumnIndexOrThrow(KEY_IMAGE)));
                final String image = c.getString(c.getColumnIndexOrThrow(KEY_IMAGE));
                Uri imageUri = Uri.parse(image);
                String imageName = FileUtils.GetFileName(image);
                imageName = FileUtils.GetFileNameNoExtension(imageName) + "_Compressed.jpg";
                Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
                options.size = 200000;
                options.isKeepSampling = false;
                options.overrideSource = false;
                options.outfile = context.getFilesDir() + "/.Pictures/" + imageName;
                Log.d("Outfile", context.getFilesDir() + "/.Pictures/" + imageName);
                File file = new File(imageUri.getPath());
                Log.d("File Size", String.valueOf(file.length()));
                if (file.length() > 400000) {
                    Tiny.getInstance().source(imageUri.getPath()).asFile().withOptions(options).compress(new FileCallback() {
                        @Override
                        public void callback(boolean isSuccess, String outfile, Throwable t) {
                            //Return the compressed file path
                            if (isSuccess) {
                                Log.d("Compressed Path", outfile);
                                UpdateImagePreview(image, "file://" + outfile);
                            }
                        }
                    });
                }
            }
            while (c.moveToNext());
        }
        c.close();
    }

    public void UpdateImagePreview(String image, String imagepreview)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_IMAGEPREVIEW, imagepreview);
        db.update(TABLE_NOTES, values, KEY_IMAGE + "=?", new String[]{image});
    }
}
