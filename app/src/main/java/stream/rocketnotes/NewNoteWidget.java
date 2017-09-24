package stream.rocketnotes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class NewNoteWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int i = 0; i < appWidgetIds.length; ++i) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_newnote);

            //Register an onClickListener
            Intent newNoteIntent = new Intent(context, NewNoteWidget.class);
            newNoteIntent.setAction(Constants.QUICK_NOTE);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, newNoteIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.image_button, pendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Constants.QUICK_NOTE)) {
            Log.d("onReceive", Constants.QUICK_NOTE);
            intent = new Intent(context, EditActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.setAction(Constants.NEW_NOTE);
            context.startActivity(intent);
        } else {
            Log.d("onReceive", "Clicked");
            super.onReceive(context, intent);
        }
    }
}
