package stream.notesapp;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class ImageWidget extends AppWidgetProvider {

    private static final String ADD_IMAGE = "ADD_IMAGE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; ++i) {
            Intent intent = new Intent(context, ImageWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);

            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.activity_main);
            rv.setRemoteAdapter(R.id.image_gridview, intent);

            rv.setEmptyView(R.id.image_gridview, R.id.image_button);

            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        // See the dimensions and
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        // Get min width and height.
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        Intent intent = new Intent(context, ImageWidgetService.class);
        // Add the app widget ID to the intent extras.
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews remoteViews = getRemoteViews(context, minWidth, minHeight);
        remoteViews.setRemoteAdapter(R.id.image_gridview, intent);
//        remoteViews.setEmptyView(R.id.image_gridview, R.id.image_button);
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
        // Now you changing layout base on you column count
        // In this code from 1 column to 4
        // you can make code for more columns on your own.
        switch (columns) {
            case 1:
                Log.d("Widget Columns", "1");
                return new RemoteViews(context.getPackageName(), R.layout.widget_image_1column);
            case 2:
                Log.d("Widget Columns", "2");
                return new RemoteViews(context.getPackageName(), R.layout.widget_image_2column);
            case 3:
                Log.d("Widget Columns", "3");
                return new RemoteViews(context.getPackageName(), R.layout.widget_image_3column);
            case 4:
                Log.d("Widget Columns", "4");
                return new RemoteViews(context.getPackageName(), R.layout.widget_image_4column);
            default:
                Log.d("Widget Columns", "default");
                return new RemoteViews(context.getPackageName(), R.layout.widget_image_4column);
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
        if (intent.getAction().equals(ADD_IMAGE)) {
            Log.d("onReceive", ADD_IMAGE);
        } else {
            Log.d("onReceive", "Clicked");
            super.onReceive(context, intent);
        }
    }
}
