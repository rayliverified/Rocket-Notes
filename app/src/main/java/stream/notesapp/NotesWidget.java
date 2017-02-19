package stream.notesapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;

public class NotesWidget extends AppWidgetProvider {

    private static final String NEW_NOTE = "NEW_NOTE";
    private static final String OPEN_NOTE = "OPEN_NOTE";
    private static final String NEW_REPLY = "NEW_REPLY";
    private static String KEY_TEXT_REPLY = "key_text_reply";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
//        DatabaseHelper dbHelper = new DatabaseHelper(context);
        for (int i = 0; i < appWidgetIds.length; ++i) {
            Intent intent = new Intent(context, NotesWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);

            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_notes_listview);
            rv.setRemoteAdapter(R.id.notes_listview, intent);

            rv.setEmptyView(R.id.notes_listview, R.id.new_note);

            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            // Register an onClickListener
            Intent noteAddIntent = new Intent(context, NotesWidget.class);
            noteAddIntent.setAction(NEW_NOTE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, noteAddIntent, 0);
            rv.setOnClickPendingIntent(R.id.new_note, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }

//        dbHelper.close();

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        // See the dimensions and
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);

        // Get min width and height.
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        Intent intent = new Intent(context, NotesWidgetService.class);
        // Add the app widget ID to the intent extras.
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        RemoteViews remoteViews = getRemoteViews(context, minWidth, minHeight);
        remoteViews.setRemoteAdapter(R.id.notes_listview, intent);
//        remoteViews.setEmptyView(R.id.image_listview, R.id.image_button);

        // Register an onClickListener
        Intent noteAddIntent = new Intent(context, NotesWidget.class);
        noteAddIntent.setAction(NEW_NOTE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, noteAddIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.new_note, pendingIntent);

        Intent noteOpenIntent = new Intent(context, NotesWidget.class);
        noteOpenIntent.setAction(OPEN_NOTE);
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
        if (intent.getAction().equals(NEW_NOTE)) {
            Log.d("onReceive", NEW_NOTE);
//            DatabaseHelper dbHelper = new DatabaseHelper(context);
//            dbHelper.initiateContent();

            // Key for the string that's delivered in the action's intent.
            String replyLabel = "Reply";
            RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                    .setLabel(replyLabel)
                    .build();

            Intent replyintent = new Intent(context, NotesWidget.class);
            replyintent.setAction(NEW_REPLY);
            PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, 0, replyintent, 0);

            // Create the reply action and add the remote input.
            NotificationCompat.Action action =
                    new NotificationCompat.Action.Builder(R.drawable.new_message_icon,
                            replyLabel, replyPendingIntent)
                            .addRemoteInput(remoteInput)
                            .build();

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.new_message_icon)
                            .setContentTitle("New Note")
//                            .setContentText("Message body...text.")
                            .setPriority(Notification.PRIORITY_MAX)
                            .addAction(action);
            if (Build.VERSION.SDK_INT >= 21) mBuilder.setVibrate(new long[0]);
            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(context, ResultActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(ResultActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            // mId (placeholder 0) allows you to update the notification later on.
            mNotificationManager.notify(0, mBuilder.build());

//            intent = new Intent(context, CameraActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(intent);
        }
        else if (intent.getAction().equals(NEW_REPLY))
        {
            String newNote = getMessageText(intent);
            if (!newNote.equals(""))
            {
                Calendar calendar = Calendar.getInstance();
                Long currentTime = calendar.getTimeInMillis();
                DatabaseHelper dbHelper = new DatabaseHelper(context);
                dbHelper.AddNewNote(newNote, currentTime);
                int widgetIDs[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, NotesWidget.class));

                for (int id : widgetIDs)
                {
                    AppWidgetManager.getInstance(context).notifyAppWidgetViewDataChanged(id, R.id.notes_listview);
                }
            }
        }
        else if (intent.getAction().equals(OPEN_NOTE))
        {
            Log.d("onReceive", OPEN_NOTE);
        }
        else {
            Log.d("onReceive", "Clicked");
            super.onReceive(context, intent);
        }
    }

    private String getMessageText(Intent intent) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return remoteInput.getString(KEY_TEXT_REPLY);
        }
        return null;
    }
}
