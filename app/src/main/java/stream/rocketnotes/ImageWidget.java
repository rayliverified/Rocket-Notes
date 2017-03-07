package stream.rocketnotes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class ImageWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; ++i) {
            Intent intent = new Intent(context, ImageWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_image_gallery);
            remoteViews.setRemoteAdapter(R.id.image_gridview, intent);
//            rv.setEmptyView(R.id.image_gridview, R.id.image_button);

            //Register an onClickListener
            Intent imageAddIntent = new Intent(context, ImageWidget.class);
            imageAddIntent.setAction(Constants.ADD_IMAGE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, imageAddIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.image_button, pendingIntent);

            Intent imageOpenintent = new Intent(context, ImageWidget.class);
            imageOpenintent.setAction(Constants.OPEN_IMAGE);
            imageOpenintent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            pendingIntent = PendingIntent.getBroadcast(context, 0, imageOpenintent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.image_gridview, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        //See the dimensions and
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        //Get min width and height.
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        //Add the app widget ID to the intent extras.
        Intent intent = new Intent(context, ImageWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews remoteViews = getRemoteViews(context, minWidth, minHeight);
        remoteViews.setRemoteAdapter(R.id.image_gridview, intent);
//        remoteViews.setEmptyView(R.id.image_gridview, R.id.image_button);

        //Register an onClickListener
        Intent imageAddIntent = new Intent(context, ImageWidget.class);
        imageAddIntent.setAction(Constants.ADD_IMAGE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, imageAddIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.image_button, pendingIntent);

        Intent imageOpenintent = new Intent(context, ImageWidget.class);
        imageOpenintent.setAction(Constants.OPEN_IMAGE);
        imageOpenintent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        pendingIntent = PendingIntent.getBroadcast(context, 0, imageOpenintent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.image_gridview, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    /**
     * Determine appropriate view based on row or column provided.
     *
     * @param minWidth
     * @param minHeight
     * @return
     */
    private RemoteViews getRemoteViews(Context context, int minWidth, int minHeight) {
        // First find out rows and columns based on width provided.
        int rows = getCellsForSize(minHeight);
        int columns = getCellsForSize(minWidth);
        // Change layout based on column count
        switch (columns) {
            default:
                Log.d("Widget Columns", "default");
                return new RemoteViews(context.getPackageName(), R.layout.widget_image_gallery);
        }
    }

    /**
     * Returns number of cells needed for given size of the widget.
     *
     * @param size Widget size in dp.
     * @return Size in number of cells.
     */
    private static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < size) {
            ++n;
        }
        return n - 1;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Constants.ADD_IMAGE)) {
            Log.d("onReceive", Constants.ADD_IMAGE);
            intent = new Intent(context, CameraActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
        else if (intent.getAction().equals(Constants.OPEN_IMAGE))
        {
            Log.d("onReceive", Constants.OPEN_IMAGE);
            try {
                Integer position = intent.getIntExtra(Constants.IMAGE, -1);
                Integer noteID = intent.getIntExtra(Constants.ID, -1);
                if (position != -1)
                {
                    intent = new Intent(context, ImageViewerActivity.class);
                    intent.setAction(Constants.OPEN_IMAGE);
                    intent.putExtra(Constants.IMAGE, position);
                    intent.putExtra(Constants.ID, noteID);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                }
            }
            catch (ActivityNotFoundException e) {

            }
        }
        else {
            Log.d("onReceive", "Clicked");
            super.onReceive(context, intent);
        }
    }
}
