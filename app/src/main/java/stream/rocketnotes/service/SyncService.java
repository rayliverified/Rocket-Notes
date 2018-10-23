package stream.rocketnotes.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import org.greenrobot.eventbus.EventBus;

import androidx.preference.PreferenceManager;
import stream.rocketnotes.Constants;
import stream.rocketnotes.interfaces.UpdateMainEvent;
import stream.rocketnotes.viewholder.SyncHeaderViewholder;

public class SyncService extends Service {
    private final String TAG = this.getClass().getSimpleName();

    SharedPreferences sharedPref;
    private Context mContext = this;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (!sharedPref.getString(Constants.FIREBASE_USER_ID, "").equals("")) {
            //Check if notes need to be syncronized.

        } else {
            EventBus.getDefault().post(new UpdateMainEvent(SyncHeaderViewholder.SYNC_STATE_LOGGEDOUT));
        }

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
