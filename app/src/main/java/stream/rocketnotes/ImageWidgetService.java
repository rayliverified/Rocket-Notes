package stream.rocketnotes;

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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ImageWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ImageRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ImageRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static int mCount = 8;
    private ArrayList<NotesItem> mNotesItems = new ArrayList<NotesItem>();
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
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        mNotesItems = dbHelper.GetRecentImages();
        Log.d("Image Widget", "Created");
        Log.d("Image Items Size", String.valueOf(mNotesItems.size()));
    }

    @Override
    public RemoteViews getViewAt(int position) {

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.item_image_widget);
        if (position < mNotesItems.size()) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = 8;
            bmOptions.inPurgeable = false;
            String imageURI;
            if (mNotesItems.get(position).getImagePreview() != null)
            {
                imageURI = mNotesItems.get(position).getImagePreview();
            }
            else
            {
                imageURI = mNotesItems.get(position).getImage();
            }
            Log.d("ImagePath", imageURI);
            File imageFile = null;
            String imagePath = "";
            try {
                imageFile = new File(new URI(imageURI));
                imagePath = imageFile.getAbsolutePath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
                imageFile = null;
            }
            if (imageFile != null) {
                Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
                Matrix matrix = getOrientation(imagePath);
                imageBitmap = crop(imageBitmap, matrix);
                Log.d("Image Width", String.valueOf(imageBitmap.getWidth()));
                Log.d("Image Height", String.valueOf(imageBitmap.getHeight()));
                rv.setImageViewBitmap(R.id.item_image, imageBitmap);
            } else {
                rv.setImageViewResource(R.id.item_image, R.drawable.image_picture_placeholder);
            }
        } else {
            rv.setImageViewResource(R.id.item_image, R.drawable.image_picture_placeholder);
        }

        Bundle extras = new Bundle();
        Intent fillInIntent = new Intent();
        if (position < mNotesItems.size()) {
            extras.putInt(Constants.ID, mNotesItems.get(position).getID());
            extras.putInt(Constants.IMAGE, position);
            fillInIntent.putExtras(extras);
        }
        rv.setOnClickFillInIntent(R.id.item_image, fillInIntent);

        return rv;
    }

    public static String getImagePath(String notesImage) {

        File imageFile = null;
        String imagePath = null;
        try {
            imageFile = new File(new URI(notesImage));
            imagePath = imageFile.getAbsolutePath();
            Log.d("Absolute Path", imagePath);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return imagePath;
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
                    srcBmp.getWidth() / 2 - srcBmp.getHeight() / 2,
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
                    srcBmp.getHeight() / 2 - srcBmp.getWidth() / 2,
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

    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
//        mImageItems.clear();
    }

    public int getCount() {
        return mCount;
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
        Log.d("Image Widget", "Data Changed");
        onCreate();
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
    }
}