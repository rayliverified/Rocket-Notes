package stream.notesapp;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class ImageWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ImageRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ImageRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static final int mCount = 8;
    private ArrayList<String> mImageItems = new ArrayList<String>();
    private Context mContext;
    private int mAppWidgetId;

    public ImageRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        Log.d("Image Widget", "Created");
        mImageItems = lastFileModified(mContext);
//        // We sleep for 3 seconds here to show how the empty view appears in the interim.
//        // The empty view is set in the StackWidgetProvider and should be a sibling of the
//        // collection view.
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    public static ArrayList<String> lastFileModified(Context context) {

        DatabaseHelper dbHelper = new DatabaseHelper(context);
        ArrayList<NotesItem> notesItems = dbHelper.GetRecentImages();
        Log.d("Image Items", String.valueOf(notesItems.size()));

        ArrayList<String> imageItems = new ArrayList<String>();
        for (NotesItem note : notesItems)
        {
            File imageFile = null;
            try {
                imageFile = new File(new URI(note.getNotesImage()));
                imageItems.add(imageFile.getAbsolutePath());
                Log.d("Absolute Path", imageFile.getAbsolutePath());
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        return imageItems;
    }

    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
//        mImageItems.clear();
    }

    public int getCount() {
        return mCount;
    }

    @Override
    public RemoteViews getViewAt(int position) {

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.item_image_widget);
        Log.d("Size mImageItems", String.valueOf(mImageItems.size()));

        if (position < mImageItems.size())
        {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = 16;
            bmOptions.inPurgeable = false;
            String imagePath = mImageItems.get(position);
            Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
            Matrix matrix = getOrientation(imagePath);
            imageBitmap = crop(imageBitmap, matrix);
//            Log.d("Image URI", String.valueOf(Uri.fromFile(mImageItems.get(position))));
//            long id = ContentUris.parseId(Uri.fromFile(mImageItems.get(position)));
//            Bitmap imageBitmap = MediaStore.Images.Thumbnails.getThumbnail(
//                    mContext.getContentResolver(), id,
//                    MediaStore.Images.Thumbnails.MICRO_KIND, null);
            Log.d("Image Width", String.valueOf(imageBitmap.getWidth()));
            Log.d("Image Height", String.valueOf(imageBitmap.getHeight()));
            rv.setImageViewBitmap(R.id.item_image, imageBitmap);
        }
        else
        {
            rv.setImageViewResource(R.id.item_image, R.drawable.icon_picture);
        }

        Bundle extras = new Bundle();
        Intent fillInIntent = new Intent();
        if (position < mImageItems.size())
        {
            extras.putInt("EXTRA_ITEM", position);
            fillInIntent.putExtras(extras);
        }
        rv.setOnClickFillInIntent(R.id.item_image, fillInIntent);

        return rv;
    }

    public static Matrix getOrientation(String photoPath) {
        final Matrix matrix = new Matrix();
        try {
            ExifInterface exif = new ExifInterface(photoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            switch (orientation) {
                case 1:
                case 2:
                    break;
                case 3:
                    matrix.postRotate(180);
                    break;
                case 4:
                case 5:
                    break;
                case 6:
                    matrix.postRotate(90);
                    break;
                case 7:
                    break;
                case 8:
                    matrix.postRotate(-90);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return matrix;
    }

    private static Bitmap crop(Bitmap srcBmp, Matrix matrix) {
        Bitmap dstBmp;
        Log.d("Width", String.valueOf(srcBmp.getWidth()));
        Log.d("Height", String.valueOf(srcBmp.getHeight()));
        if (srcBmp.getWidth() >= srcBmp.getHeight()) {

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    0,
                    srcBmp.getHeight(),
                    srcBmp.getHeight(),
                    matrix,
                    true
            );

        } else {

            dstBmp = Bitmap.createBitmap(
                    srcBmp,
                    0,
                    0,
                    srcBmp.getWidth(),
                    srcBmp.getWidth(),
                    matrix,
                    true
            );
        }

        Log.d("Width", String.valueOf(dstBmp.getWidth()));
        Log.d("Height", String.valueOf(dstBmp.getHeight()));
        return dstBmp;
    }

    public RemoteViews getLoadingView() {
        // You can create a custom loading view (for instance when getViewAt() is slow.) If you
        // return null here, you will get the default loading view.
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        Log.d("Widget Updated", "Data Changed");
        onCreate();
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
    }
}