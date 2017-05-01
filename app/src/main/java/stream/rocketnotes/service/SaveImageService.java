package stream.rocketnotes.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;

import es.dmoral.toasty.Toasty;
import stream.rocketnotes.Constants;
import stream.rocketnotes.R;

public class SaveImageService extends Service {

    private Context context = this;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        new SaveFileTask(context, intent).execute();
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    private class SaveFileTask extends AsyncTask<Intent, Void, String> {
        final Context mContext;
        final Intent mIntent;
        final Handler mHandler;

        public SaveFileTask(final Context context, Intent intent) {
            mContext = context;
            mIntent = intent;
            mHandler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message message) {
                    Toasty.custom(context, message.obj.toString(), null, ContextCompat.getColor(context, R.color.blackTranslucent), Toast.LENGTH_SHORT, false, false).show();
                }
            };
        }

        public void execute() {
            execute(mIntent);
        }

        @Override
        protected String doInBackground(Intent... params) {

            Bundle extras = params[0].getExtras();
            String sourcePath = extras.getString(Constants.SOURCE_PATH);
            String savePath = extras.getString(Constants.SAVE_PATH);
            String sourceName = "";
            File imageFile = null;
            try {
                imageFile = new File(new URI(sourcePath));
                sourcePath = imageFile.getAbsolutePath();
                sourceName = imageFile.getName();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            File dir = new File(savePath);
            if (!dir.exists()) {
                dir.mkdirs();
                Log.d("Directory", "Created");
            }
            else
            {
                Log.d("Directory", "Exists");
            }

            Log.d("Source Path", sourcePath);
            savePath = savePath + "/" + sourceName;
            Log.d("Save Path", savePath);
            File f = new File(savePath);
            if (!f.exists())
            {
                try {
                    f.createNewFile();
                    copyFile(new File(sourcePath), f);
                    return savePath;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Log.d("Save File", "File Exists");
                Message message = mHandler.obtainMessage(0, "Already Saved");
                message.sendToTarget();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String savedImagePath) {
            super.onPostExecute(savedImagePath);

            if (savedImagePath != null)
            {
                savedImagePath = "file://" + savedImagePath;
                Intent saveNote = new Intent(getApplicationContext(), SaveNoteService.class);
                saveNote.putExtra(Constants.IMAGE, savedImagePath);
                saveNote.setAction(Constants.NEW_NOTE);
                getApplicationContext().startService(saveNote);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }

    private boolean copyFile(File sourceFile, File destFile) throws IOException {

        FileChannel source = null;
        FileChannel destination = null;
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        if (destination != null && source != null) {
            destination.transferFrom(source, 0, source.size());
        }
        if (source != null) {
            source.close();
        }
        if (destination != null) {
            destination.close();
        }
        Log.d("Save File", "File Saved");

        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
