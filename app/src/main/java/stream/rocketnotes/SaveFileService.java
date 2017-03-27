package stream.rocketnotes;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
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

public class SaveFileService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Bundle extras = intent.getExtras();
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

        Log.d("Source Path", sourcePath);
        savePath = savePath + "/" + sourceName;
        Log.d("Save Path", savePath);
        File f = new File(savePath);
        if (!f.exists())
        {
            try {
                f.createNewFile();
                copyFile(new File(sourcePath), f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Log.d("Save File", "File Exists");
            Toasty.custom(this, "Already Saved", null, ContextCompat.getColor(this, R.color.white), ContextCompat.getColor(this, R.color.blackTranslucent), Toast.LENGTH_SHORT, false, true).show();
        }

        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    private void copyFile(File sourceFile, File destFile) throws IOException {
        if (!sourceFile.exists()) {
            Log.d("Save File", "File Exists");
            Toasty.custom(this, "Already Saved", null, ContextCompat.getColor(this, R.color.white), ContextCompat.getColor(this, R.color.blackTranslucent), Toast.LENGTH_SHORT, false, true).show();
            return;
        }

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
        Toasty.custom(this, "Saved", null, ContextCompat.getColor(this, R.color.white), ContextCompat.getColor(this, R.color.blackTranslucent), Toast.LENGTH_SHORT, false, true).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
