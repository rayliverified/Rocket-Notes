package stream.notesapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;

public class NewNoteWidget extends AppWidgetProvider {

    private static final String ACTION_CLICK = "ACTION_CLICK";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Get all ids
        ComponentName thisWidget = new ComponentName(context,
                NewNoteWidget.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetID : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_newnote);

            // Register an onClickListener
            Intent intent = new Intent(context, NewNoteWidget.class);
            intent.setAction(ACTION_CLICK);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.image_button, pendingIntent);

            appWidgetManager.updateAppWidget(widgetID, remoteViews);
        }
    }

    private static final String KEY_TEXT_REPLY = "key_text_reply";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_CLICK)) {
            Log.d("onReceive", ACTION_CLICK);

            // Key for the string that's delivered in the action's intent.
            String replyLabel = "Reply";
            RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                    .setLabel(replyLabel)
                    .build();

            Intent replyintent = new Intent(context, NewNoteWidget.class);
            replyintent.setAction(ACTION_CLICK);
            PendingIntent replyPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            // Create the reply action and add the remote input.
            NotificationCompat.Action action =
                    new NotificationCompat.Action.Builder(R.drawable.new_message_icon,
                            replyLabel, replyPendingIntent)
                            .addRemoteInput(remoteInput)
                            .build();

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.new_message_icon)
                            .setContentTitle("Test notification")
                            .setContentText("Message body...text.")
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
        } else {
            Log.d("onReceive", "Clicked");
            super.onReceive(context, intent);
        }
    }
}
