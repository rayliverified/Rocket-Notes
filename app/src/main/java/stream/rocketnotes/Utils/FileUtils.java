package stream.rocketnotes.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

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
    public static void CreateNoMediaFile(Context context) throws IOException
    {
        File imagePath = new File(context.getFilesDir(), ".Pictures");
        File noMediaFile = new File(imagePath, ".nomedia");
        noMediaFile.createNewFile();
        Log.d("No Media", "Created");
    }

    //Create a new internal folder for storing pictures if no folder exists
    public static void InitializePicturesFolder(Context context)
    {
        File dir = new File(context.getFilesDir() + "/.Pictures");
        if (!dir.exists()) {
            dir.mkdirs();
            Log.d("Directory", "Created");
        }
        else
        {
            Log.d("Directory", "Exists");
        }
        if (!NoMediaExists(context))
        {
            try {
                CreateNoMediaFile(context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            Log.d("No Media", "Exists");
        }
    }
}
