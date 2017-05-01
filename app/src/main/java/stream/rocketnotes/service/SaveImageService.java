package stream.rocketnotes.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
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
            String sourceName = "";
            //Create name for saved image
            if (sourcePath != null)
            {
                //Format file name with proper encoding so image locations are stored correctly
                try {
                    sourceName = URLDecoder.decode(sourcePath.substring(sourcePath.lastIndexOf("/") + 1), "UTF-8");
                    sourceName = sourceName.replaceAll(" ", "_").trim();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    sourceName = "Image_" + System.currentTimeMillis();
                }
            }

            //Verify that Pictures directory exists
            File dir = new File(getFilesDir() + "/.Pictures/");
            if (!dir.exists()) {
                dir.mkdirs();
                Log.d("Directory", "Created");
            }
            else
            {
                Log.d("Directory", "Exists");
            }

            //Use InputStream method for file saving to ensure compatibility with all content schemes.
            InputStream inputStream = null;
            try {
                inputStream = getContentResolver().openInputStream(Uri.parse(sourcePath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            //Save file in Pictures folder.
            String outputfile = getFilesDir() + "/.Pictures/" + sourceName;
            Log.d("OutputFile", outputfile);
            File f = new File(outputfile);
            if (!f.exists())
            {
                try {
                    f.setWritable(true, false);
                    OutputStream outputStream = new FileOutputStream(f);
                    byte buffer[] = new byte[1024];
                    int length = 0;

                    while((length = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer,0,length);
                    }

                    outputStream.close();
                    inputStream.close();

                    return outputfile;
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
