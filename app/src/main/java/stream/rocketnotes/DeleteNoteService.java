package stream.rocketnotes;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;

public class DeleteNoteService extends Service {
    private final String TAG = "SaveNoteService";

    private Context mContext = this;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(Constants.DELETE_NOTE)) {
            Bundle extras = intent.getExtras();
            Integer noteID = extras.getInt(Constants.ID);
            DatabaseHelper dbHelper = new DatabaseHelper(mContext);
            NotesItem note = dbHelper.GetNote(noteID);
            if (note.getNotesImage() != null)
            {
                try {
                    File imageFile = new File(new URI(note.getNotesImage()));
                    imageFile.delete();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }
            else
            {

            }
            dbHelper.DeleteNote(noteID);
            Log.d("Deleted Note", String.valueOf(noteID));
            NotificationSender(note);

            int widgetIDs[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), NotesWidget.class));
            for (int id : widgetIDs) {
                AppWidgetManager.getInstance(getApplication()).notifyAppWidgetViewDataChanged(id, R.id.notes_listview);
            }

            int imageWidgetIDs[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ImageWidget.class));
            for (int id : imageWidgetIDs) {
                AppWidgetManager.getInstance(getApplication()).notifyAppWidgetViewDataChanged(id, R.id.image_gridview);
            }
        }
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    public void NotificationSender(NotesItem note)
    {
        EventBus.getDefault().postSticky(new UpdateMainEvent(Constants.DELETE_NOTE, note));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
