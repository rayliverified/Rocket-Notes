package stream.rocketnotes.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.zxy.tiny.Tiny;
import com.zxy.tiny.callback.FileCallback;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Calendar;

import es.dmoral.toasty.Toasty;
import stream.rocketnotes.Constants;
import stream.rocketnotes.DatabaseHelper;
import stream.rocketnotes.ImageWidget;
import stream.rocketnotes.NotesItem;
import stream.rocketnotes.NotesWidget;
import stream.rocketnotes.R;
import stream.rocketnotes.UpdateMainEvent;
import stream.rocketnotes.utils.FileUtils;
import stream.rocketnotes.utils.TextUtils;

public class SaveNoteService extends Service {
    private final String TAG = "SaveNoteService";

    private String body;
    private String image;

    private Context mContext = this;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent.getAction().equals(Constants.NEW_NOTE)) {
            Log.d("SaveNoteService", Constants.NEW_NOTE);
            Bundle extras = intent.getExtras();
            body = extras.getString(Constants.BODY);
            image = extras.getString(Constants.IMAGE);
            if (image != null)
            {
                Uri imageUri = Uri.parse(image);
                String imageName = FileUtils.GetFileName(image);
                imageName = FileUtils.GetFileNameNoExtension(imageName) + "_Compressed.jpg";
                Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
                options.size = 200;
                options.isKeepSampling = false;
                options.overrideSource = false;
                options.outfile = getFilesDir() + "/.Pictures/" + imageName;
                Log.d("Outfile", getFilesDir() + "/.Pictures/" + imageName);
                File file = new File(imageUri.getPath());
                Log.d("File Size", String.valueOf(file.length()));
                if (file.length() > 500000)
                {
                    Tiny.getInstance().source(imageUri.getPath()).asFile().withOptions(options).compress(new FileCallback() {
                        @Override
                        public void callback(boolean isSuccess, String outfile, Throwable t) {
                            //Return the compressed file path
                            if (isSuccess)
                            {
                                Log.d("Compressed Path", outfile);
                                Calendar calendar = Calendar.getInstance();
                                Long currentTime = calendar.getTimeInMillis();
                                DatabaseHelper dbHelper = new DatabaseHelper(mContext);
                                NotesItem savedNote = dbHelper.AddNewNote(body, currentTime, image, "file://" + outfile);
                                NotificationSender(savedNote);
                                UpdateImageWidget();
                            }
                            else
                            {
                                Toasty.error(mContext, "Error", Toast.LENGTH_SHORT, false).show();
                            }
                        }
                    });
                }
                else
                {
                    Calendar calendar = Calendar.getInstance();
                    Long currentTime = calendar.getTimeInMillis();
                    DatabaseHelper dbHelper = new DatabaseHelper(mContext);
                    NotesItem savedNote = dbHelper.AddNewNote(body, currentTime, image, null);
                    NotificationSender(savedNote);
                }
            }
            else
            {
                Calendar calendar = Calendar.getInstance();
                Long currentTime = calendar.getTimeInMillis();
                DatabaseHelper dbHelper = new DatabaseHelper(mContext);
                NotesItem savedNote = dbHelper.AddNewNote(body, currentTime, null, null);
                NotificationSender(savedNote);
                UpdateNoteWidget();
            }
        } else if (intent.getAction().equals(Constants.UPDATE_NOTE)) {
            Log.d("SaveNoteService", Constants.UPDATE_NOTE);
            Bundle extras = intent.getExtras();
            Integer noteID = extras.getInt(Constants.ID);
            String body = extras.getString(Constants.BODY);
            Calendar calendar = Calendar.getInstance();
            Long currentTime = calendar.getTimeInMillis();

            NotesItem note = new NotesItem();
            note.setNotesID(noteID);
            note.setNotesDate(currentTime);
            note.setNotesNote(body);

            DatabaseHelper dbHelper = new DatabaseHelper(mContext);
            dbHelper.UpdateNote(note);
            UpdateSender(note);

            UpdateNoteWidget();
        }
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    public void NotificationSender(NotesItem note) {
        Toasty.custom(mContext, "Saved", null, ContextCompat.getColor(mContext, R.color.blackTranslucent), Toast.LENGTH_SHORT, false, false).show();
        EventBus.getDefault().postSticky(new UpdateMainEvent(Constants.RECEIVED, note.getNotesID()));
        Log.d("SaveNoteService", String.valueOf(note.getNotesID()));
    }

    public void UpdateSender(NotesItem note) {
        Toasty.custom(mContext, "Saved", null, ContextCompat.getColor(mContext, R.color.blackTranslucent), Toast.LENGTH_SHORT, false, false).show();
        EventBus.getDefault().postSticky(new UpdateMainEvent(Constants.UPDATE_NOTE, note.getNotesID()));
        Log.d("SaveNoteService", String.valueOf(note.getNotesID()));
    }

    public void UpdateNoteWidget()
    {
        int widgetIDs[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), NotesWidget.class));
        for (int id : widgetIDs) {
            AppWidgetManager.getInstance(getApplication()).notifyAppWidgetViewDataChanged(id, R.id.notes_listview);
        }
    }

    public void UpdateImageWidget()
    {
        int imageWidgetIDs[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ImageWidget.class));
        for (int id : imageWidgetIDs) {
            AppWidgetManager.getInstance(getApplication()).notifyAppWidgetViewDataChanged(id, R.id.image_gridview);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
