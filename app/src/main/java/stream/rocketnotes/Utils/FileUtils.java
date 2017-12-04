package stream.rocketnotes.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

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


    /**
     * @param path File path.
     * @return Filename.
     */
    public static String GetFileNameFromPath(String path) {
        if (path != null) {
            return path.substring(path.lastIndexOf("/") + 1);
        } else {
            return "";
        }
    }

    /**
     * @param fileName Filename with extension.
     * @return Filename without extension.
     */
    public static String GetFileNameNoExtension(String fileName) {
        if (fileName.contains(".")) {
            return fileName.substring(0, fileName.lastIndexOf("."));
        } else {
            return fileName;
        }
    }

    public static String GetFileName(String filename) {
        try {
            int cut = filename.lastIndexOf('/');
            if (cut != -1) {
                filename = filename.substring(cut + 1);
            }
        } catch (Exception e) {
            Log.d("GetFileNameFromUri", e.getMessage());
        }
        return filename;
    }

    /**
     * Returns the content type by reading Content Resolver for content:// URIs.
     * Otherwise, uses MimeTypeMap getSingleton to obtain MimeType from extension.
     */
    public static String GetMimeTypeFromUri(Context context, Uri uri)
    {
        Log.d("URI Scheme", uri.getScheme());
        String extension;
        //Check uri format to avoid null
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //If scheme is a content
            extension = context.getContentResolver().getType(uri);
        } else {
            String fileExtension = getExtension(uri.toString());
            Log.d("File Extension", fileExtension);
            if (fileExtension.length() > 0)
                extension = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.substring(1));
            else
                extension = null;
        }
        return extension;
    }

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     *         null if uri was null.
     */
    public static String getExtension(String uri) {
        if (uri == null) {
            return null;
        }

        int dot = uri.lastIndexOf(".");
        if (dot >= 0) {
            return uri.substring(dot);
        } else {
            // No extension.
            return "";
        }
    }

    public static String GetFileNameFromUri(Context context, Uri imageUri)
    {
        String result = null;
        try {
            if (imageUri.getScheme().equals("content")) {
                Cursor cursor = context.getContentResolver().query(imageUri, null, null, null, null);
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            }
            if (result == null) {
                result = imageUri.getPath();
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        catch (Exception e)
        {
            Log.d("GetFileNameFromUri", e.getMessage());
        }
        return result;
    }
}
