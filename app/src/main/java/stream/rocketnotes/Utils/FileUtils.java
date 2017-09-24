package stream.rocketnotes.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtils {

    //Check if noMedia file has been placed in Pictures folder
    public static boolean NoMediaExists(Context context) {
        File imagePath = new File(context.getFilesDir(), ".Pictures");
        File file = new File(imagePath, ".nomedia");
        if (file != null) {
            return file.exists();
        }
        return false;
    }

    //Create noMedia file in Pictures folder
    public static void CreateNoMediaFile(Context context) throws IOException {
        File imagePath = new File(context.getFilesDir(), ".Pictures");
        File noMediaFile = new File(imagePath, ".nomedia");
        noMediaFile.createNewFile();
        Log.d("No Media", "Created");
    }

    //Create a new internal folder for storing pictures if no folder exists
    public static void InitializePicturesFolder(Context context) {
        File dir = new File(context.getFilesDir() + "/.Pictures");
        if (!dir.exists()) {
            dir.mkdirs();
            Log.d("Directory", "Created");
        } else {
            Log.d("Directory", "Exists");
        }
        if (!NoMediaExists(context)) {
            try {
                CreateNoMediaFile(context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.d("No Media", "Exists");
        }
    }

    public static String GetFileUriFromContentUri(Context context, Uri uri) {
        String fileUri = null;
        if (uri != null && "content".equals(uri.getScheme())) {
            Cursor cursor = context.getContentResolver().query(uri, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
            cursor.moveToFirst();
            fileUri = "file://" + cursor.getString(0);
            cursor.close();
        } else {
            fileUri = uri.toString();
        }

        return fileUri;
    }

    public static void CopyFile(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }
}
