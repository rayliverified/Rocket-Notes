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

public class NotesWidget extends AppWidgetProvider {

    private static String KEY_TEXT_REPLY = "key_text_reply";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; ++i) {
            Intent intent = new Intent(context, NotesWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);

            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_notes_listview);
            rv.setRemoteAdapter(R.id.notes_listview, intent);

//            rv.setEmptyView(R.id.notes_listview, R.id.rocket_icon);

            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

//            // Register an onClickListener
//            Intent noteAddIntent = new Intent(context, NotesWidget.class);
//            noteAddIntent.setAction(NEW_NOTE);
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, noteAddIntent, 0);
//            rv.setOnClickPendingIntent(R.id.new_note, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        //Get widget object details
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        //Get min width and height.
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        Intent intent = new Intent(context, NotesWidgetService.class);
        //Add the app widget ID to the intent extras.
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews remoteViews = getRemoteViews(context, minWidth, minHeight);
        remoteViews.setRemoteAdapter(R.id.notes_listview, intent);
//        remoteViews.setEmptyView(R.id.image_listview, R.id.image_button);

        //Register widget onClickListeners
        Intent noteAddIntent = new Intent(context, NotesWidget.class);
        noteAddIntent.setAction(Constants.NEW_NOTE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, noteAddIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.new_note, pendingIntent);

        noteAddIntent = new Intent(context, NotesWidget.class);
        noteAddIntent.setAction(Constants.OPEN_APP);
        pendingIntent = PendingIntent.getBroadcast(context, 0, noteAddIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.rocket_icon, pendingIntent);

        Intent noteOpenIntent = new Intent(context, NotesWidget.class);
        noteOpenIntent.setAction(Constants.OPEN_NOTE);
        noteOpenIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        pendingIntent = PendingIntent.getBroadcast(context, 0, noteOpenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.notes_listview, pendingIntent);

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
                return new RemoteViews(context.getPackageName(), R.layout.widget_notes_listview);
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
        if (intent.getAction().equals(Constants.NEW_NOTE)) {
            Log.d("onReceive", Constants.NEW_NOTE);

            intent = new Intent(context, PopupActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setAction(Constants.NEW_NOTE);
            context.startActivity(intent);
        }
        else if (intent.getAction().equals(Constants.OPEN_NOTE))
        {
            Log.d("onReceive", Constants.OPEN_NOTE);
            try {
//                Log.d("File Path", intent.getStringExtra("EXTRA_ITEM"));
                Integer noteID = intent.getIntExtra("EXTRA_ITEM", -1);
                if (noteID != -1)
                {
                    Log.d("Note ID", String.valueOf(noteID));
                    intent = new Intent(context, PopupActivity.class);
                    intent.putExtra("NOTEID", noteID);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.setAction(Constants.OPEN_NOTE);
                    context.startActivity(intent);
                }
            }
            catch (ActivityNotFoundException e) {

            }
        }
        else if (intent.getAction().equals(Constants.OPEN_APP))
        {
            Intent openIntent = new Intent(context, MainActivity.class);
            openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(openIntent);
        }
        else {
            Log.d("onReceive", "Clicked");
            super.onReceive(context, intent);
        }
    }
}
