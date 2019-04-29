package stream.rocketnotes.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.google.android.gms.tasks.OnFailureListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import stream.rocketnotes.Constants;
import stream.rocketnotes.DatabaseHelper;
import stream.rocketnotes.NotesItem;
import stream.rocketnotes.interfaces.FirestoreInterface;
import stream.rocketnotes.interfaces.UpdateMainEvent;
import stream.rocketnotes.repository.FirestoreRepository;
import stream.rocketnotes.utils.CloudUtils;
import stream.rocketnotes.viewholder.SyncHeaderViewholder;

public class SyncService extends Service {

    private final String TAG = this.getClass().getSimpleName();

    public static final String SYNC_STATE_LOGGED_OUT = "SYNC_STATE_LOGGED_OUT";
    public static final String SYNC_STATE_STARTING = "SYNC_STATE_STARTING";
    public static final String SYNC_STATE_SYNCING = "SYNC_STATE_SYNCING";
    public static final String SYNC_STATE_COMPLETED = "SYNC_STATE_COMPLETED";
    public static final String SYNC_STATE_ERROR = "SYNC_STATE_ERROR";
    public static final String SYNC_STATE_ERROR_CONNECTION = "SYNC_STATE_ERROR_CONNECTION";
    private static final int SYNC_LIMIT = 100;

    private String state;
    private ArrayList<NotesItem> mNotes;
    private String userID = "";
    private Integer totalSize = 0;
    private Integer totalSynced = 0;

    DatabaseHelper dbHelper;
    private FirestoreRepository firestoreRepository;

    SharedPreferences sharedPref;
    private Context mContext = this;

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        dbHelper = new DatabaseHelper(mContext);
        userID = sharedPref.getString(Constants.FIRESTORE_USER_ID, "");
        state = SYNC_STATE_STARTING;
        totalSize = dbHelper.GetUnsyncedNotesCount();
        totalSynced = 0;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!CloudUtils.isConnected(mContext)) {
            state = SYNC_STATE_ERROR_CONNECTION;
            stopSelf();
        }
        else if (userID.equals("")) {
            state = SYNC_STATE_LOGGED_OUT;
            EventBus.getDefault().post(new UpdateMainEvent(SyncHeaderViewholder.SYNC_STATE_LOGGEDOUT));
            stopSelf();
        }
        else if (totalSize == 0) {
            state = SYNC_STATE_COMPLETED;
            EventBus.getDefault().post(new UpdateMainEvent(SYNC_STATE_COMPLETED));
            stopSelf();
        } else if (state.equals(SYNC_STATE_SYNCING) && mNotes.size() > 0) {
            //Do nothing because notes are being synced already.
        }
        else {
            //Sync notes!
            state = SYNC_STATE_SYNCING;
            EventBus.getDefault().post(new UpdateMainEvent(SyncHeaderViewholder.SYNC_STATE_BACKINGUP));
            firestoreRepository = new FirestoreRepository(mContext, userID, dbHelper);
            //Check if notes need to be syncronized.
            mNotes = dbHelper.GetUnsyncedNotes(SYNC_LIMIT);
            if (mNotes.size() > 0) {
                for (int i = 0; i < mNotes.size(); i++) {
                    SaveNoteCloud(mNotes.get(i));
                }
            }
            else {
                totalSize = 0;
                state = SYNC_STATE_COMPLETED;
                EventBus.getDefault().post(new UpdateMainEvent(SyncHeaderViewholder.SYNC_STATE_BACKEDUP));
                stopSelf();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void SaveNoteCloud(final NotesItem notesItem) {
        FirestoreInterface firestoreInterface = new FirestoreInterface() {
            @Override
            public void onSuccess() {
                mNotes.remove(notesItem);
                totalSynced += 1;
                if (state.equals(SYNC_STATE_SYNCING)) {
                    if (totalSynced >= totalSize) {
                        state = SYNC_STATE_COMPLETED;
                        EventBus.getDefault().post(new UpdateMainEvent(SyncHeaderViewholder.SYNC_STATE_BACKEDUP));
                        stopSelf();
                    } else {
                        if (mNotes.size() == 0) {
                            Intent intent = new Intent(mContext, SyncService.class);
                            startService(intent);
                        }
                        EventBus.getDefault().post(new UpdateMainEvent(SyncHeaderViewholder.SYNC_STATE_BACKINGUP, totalSynced + "/" + totalSize));
                    }
                }
            }

            @Override
            public OnFailureListener getFailureListener() {
                return new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failure: " + e);
                        state = SYNC_STATE_ERROR;
                        EventBus.getDefault().post(new UpdateMainEvent(SyncHeaderViewholder.SYNC_STATE_ERROR));
                        stopSelf();
                    }
                };
            }
        };
        firestoreRepository.SaveNoteCloud(notesItem, firestoreInterface);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
