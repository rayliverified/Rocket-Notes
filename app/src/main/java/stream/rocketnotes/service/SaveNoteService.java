package stream.rocketnotes.service;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnFailureListener;
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
import stream.rocketnotes.interfaces.FirestoreInterface;
import stream.rocketnotes.interfaces.UpdateMainEvent;
import stream.rocketnotes.repository.FirestoreRepository;
import stream.rocketnotes.utils.FileUtils;

public class SaveNoteService extends Service {

    private final String TAG = this.getClass().getSimpleName();

    private NotesItem notesItem = new NotesItem();
    private Integer noteID;
    private String body;
    private String image;
    private String action;
    private String type;

    private Context mContext = this;
    private DatabaseHelper dbHelper;
    private Calendar calendar;
    private FirestoreRepository firestoreRepository;

    //TODO Logic to handle onStartCommand being called multiple times does not exist.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        dbHelper = new DatabaseHelper(mContext);
        calendar = Calendar.getInstance();
        Log.d(TAG, String.valueOf(notesItem));

        getData(intent);

        switch (action) {
            case Constants.NEW_NOTE: {
                Log.d("SaveNoteService", Constants.NEW_NOTE);
                SaveNote();

                if (type.equals(Constants.IMAGE)) {
                    //If image size is greater than 400Kb, generate image thumbnail for faster loading.
                    Uri imageUri = Uri.parse(image);
                    if (imageUri.getPath() != null) {
                        File file = new File(imageUri.getPath());
                        Log.d("File Size", String.valueOf(file.length()));
                        if (file.length() > 400000)
                        {
                            SaveImageThumbnail(imageUri);
                        }
                        else
                        {
                            SaveNoteCloud();
                        }
                    }
                }
                else {
                    SaveNoteCloud();
                }
            }
            case Constants.UPDATE_NOTE: {
                Log.d("SaveNoteService", Constants.UPDATE_NOTE);

                if (noteID != 0) {
                    notesItem.setID(noteID);
                    notesItem.setDate(getCurrentTime());
                    notesItem.setNote(body);

                    dbHelper.UpdateNote(notesItem);
                    UpdateSender(notesItem);
                    UpdateNoteWidget();
                    SaveNoteCloud();
                }
            }
        }

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    private void SaveNote() {
        notesItem = dbHelper.AddNewNote(body, getCurrentTime(), image, null);
        NotificationSender(notesItem);
        switch (type) {
            case Constants.TEXT: {
                UpdateNoteWidget();
            }
            case Constants.IMAGE: {
                UpdateImageWidget();
            }
        }
    }

    private void SaveImageThumbnail(Uri imageUri) {
        //Generate thumbnail name.
        String imageName = FileUtils.GetFileName(image);
        imageName = FileUtils.GetFileNameNoExtension(imageName) + "_Compressed.jpg";
        Log.d("Outfile", getFilesDir() + "/.Pictures/" + imageName);
        //Set thumbnail compression options.
        Tiny.FileCompressOptions options = new Tiny.FileCompressOptions();
        options.size = 200000;
        options.isKeepSampling = false;
        options.overrideSource = false;
        options.outfile = getFilesDir() + "/.Pictures/" + imageName;
        Tiny.getInstance().source(imageUri.getPath()).asFile().withOptions(options).compress(new FileCallback() {
            @Override
            public void callback(boolean isSuccess, String outfile, Throwable t) {
                //Return the compressed file path.
                if (isSuccess)
                {
                    Log.d("Compressed Path", outfile);
                    notesItem.setImagePreview("file://" + outfile);
                    dbHelper.UpdateImagePreview(image, "file://" + outfile);
                    UpdateImageWidget();
                    SaveNoteCloud();
                }
                else
                {
                    Toasty.error(mContext, "Error, please refresh image cache", Toast.LENGTH_SHORT, false).show();
                }
            }
        });
    }

    private void SaveNoteCloud() {
        Log.d(TAG, "SaveNoteCloud");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String userID = sharedPref.getString(Constants.FIRESTORE_USER_ID, "");
        if (!userID.equals("")) {
            firestoreRepository = new FirestoreRepository(mContext, userID, dbHelper);
            FirestoreInterface firestoreInterface = new FirestoreInterface() {
                @Override
                public void onSuccess() {
                    Log.d(TAG, "Success!");
                }

                @Override
                public OnFailureListener getFailureListener() {
                    return new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error writing document", e);
                        }
                    };
                }
            };
            firestoreRepository.SaveNoteCloud(notesItem, firestoreInterface);
        }
    }

    private void NotificationSender(NotesItem note) {
        Toasty.custom(mContext, "Saved", null, ContextCompat.getColor(mContext, R.color.blackTranslucent), Toast.LENGTH_SHORT, false, false).show();
        EventBus.getDefault().postSticky(new UpdateMainEvent(Constants.RECEIVED, note.getID()));
        Log.d("SaveNoteService", String.valueOf(note.getID()));
    }

    private void UpdateSender(NotesItem note) {
        Toasty.custom(mContext, "Saved", null, ContextCompat.getColor(mContext, R.color.blackTranslucent), Toast.LENGTH_SHORT, false, false).show();
        EventBus.getDefault().postSticky(new UpdateMainEvent(Constants.UPDATE_NOTE, note.getID()));
        Log.d("SaveNoteService", String.valueOf(note.getID()));
    }

    private void UpdateNoteWidget()
    {
        int[] widgetIDs = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), NotesWidget.class));
        for (int id : widgetIDs) {
            AppWidgetManager.getInstance(getApplication()).notifyAppWidgetViewDataChanged(id, R.id.notes_listview);
        }
    }

    private void UpdateImageWidget()
    {
        int[] imageWidgetIDs = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), ImageWidget.class));
        for (int id : imageWidgetIDs) {
            AppWidgetManager.getInstance(getApplication()).notifyAppWidgetViewDataChanged(id, R.id.image_gridview);
        }
    }

    private void getData(Intent intent) {
        if (intent.getAction() != null) {
            action = intent.getAction();
            if (intent.getExtras() != null) {
                Bundle extras = intent.getExtras();
                noteID = extras.getInt(Constants.ID);
                body = extras.getString(Constants.BODY);
                image = extras.getString(Constants.IMAGE);
                if (image != null) {
                    type = Constants.IMAGE;
                }
                else
                {
                    type = Constants.TEXT;
                }
            }
            if (action.equals(Constants.UPDATE_NOTE)) {
                type = Constants.TEXT; //Currently, only text notes can be updated.
            }
        }
    }

    private Long getCurrentTime() {
        return calendar.getTimeInMillis();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
