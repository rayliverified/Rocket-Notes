package stream.notesapp;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class NoteHelper {

    public NoteHelper ()
    {

    }

    public static ArrayList<String> getNote(String noteRaw)
    {
        ArrayList<String> note = new ArrayList<String>();
        String[] noteText = noteRaw.split("\n", 2);
        if (noteText.length == 2)
        {
            note.add(noteText[0]);
            note.add(noteText[1]);
        }
        else
        {
            if (noteRaw.length() > Constants.TITLE_LENGTH)
            {
                note.add(noteRaw.substring(0, Constants.TITLE_LENGTH));
                note.add(noteRaw.substring(Constants.TITLE_LENGTH, noteRaw.length()));
            }
            else
            {
                note.add(noteRaw);
                note.add("");
            }
        }
        return note;
    }
}
