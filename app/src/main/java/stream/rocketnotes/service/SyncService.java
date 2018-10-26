package stream.rocketnotes.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import stream.rocketnotes.Constants;
import stream.rocketnotes.DatabaseHelper;
import stream.rocketnotes.NotesItem;
import stream.rocketnotes.interfaces.FirestoreInterface;
import stream.rocketnotes.interfaces.UpdateMainEvent;
import stream.rocketnotes.repository.FirestoreRepository;
import stream.rocketnotes.viewholder.SyncHeaderViewholder;

public class SyncService extends Service {
    private final String TAG = this.getClass().getSimpleName();
    private ArrayList<NotesItem> mNotes;
    private Integer totalSize = 0;

    DatabaseHelper dbHelper;
    private FirestoreRepository firestoreRepository;

    SharedPreferences sharedPref;
    private Context mContext = this;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        dbHelper = new DatabaseHelper(mContext);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String userID = sharedPref.getString(Constants.FIREBASE_USER_ID, "");
        if (!userID.equals("")) {
            firestoreRepository = new FirestoreRepository(mContext, userID);
            //Check if notes need to be syncronized.
            mNotes = dbHelper.GetUnsyncedNotes();
            totalSize = mNotes.size();
            Log.d("Unsynced Notes", String.valueOf(mNotes.size()));
            if (mNotes.size() > 0) {
                EventBus.getDefault().post(new UpdateMainEvent(SyncHeaderViewholder.SYNC_STATE_BACKINGUP));
                for (int i = 0; i < mNotes.size(); i++) {
                    SaveNoteCloud(mNotes.get(i));
                }
            }
        } else {
            EventBus.getDefault().post(new UpdateMainEvent(SyncHeaderViewholder.SYNC_STATE_LOGGEDOUT));
        }

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    public void SaveNoteCloud(final NotesItem notesItem) {
        FirestoreInterface firestoreInterface = new FirestoreInterface() {
            @Override
            public void onSuccess() {
                mNotes.remove(notesItem);
                Log.d("Notes Left to Sync", String.valueOf(mNotes.size()));
                if (mNotes.size() == 0) {
                    EventBus.getDefault().post(new UpdateMainEvent(SyncHeaderViewholder.SYNC_STATE_BACKEDUP));
                } else {
                    EventBus.getDefault().post(new UpdateMainEvent(SyncHeaderViewholder.SYNC_STATE_BACKINGUP, totalSize - mNotes.size() + "/" + totalSize));
                }
            }

            @Override
            public OnFailureListener getFailureListener() {
                return new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                };
            }
        };
        firestoreRepository.AddNote(notesItem, firestoreInterface);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
