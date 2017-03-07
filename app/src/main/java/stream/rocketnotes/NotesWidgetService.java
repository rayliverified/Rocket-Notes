package stream.rocketnotes;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;

public class NotesWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new NotesRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
class NotesRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private static int mCount = 0;
    private ArrayList<NotesItem> mNotesItems = new ArrayList<NotesItem>();
    private Context mContext;
    private int mAppWidgetId;
    public NotesRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }
    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
//        mNotesItems = lastNotesModified();
        DatabaseHelper dbHelper = new DatabaseHelper(mContext);
        mNotesItems = dbHelper.GetTextNotes();
        Log.d("Note Size", String.valueOf(mNotesItems.size()));
        mCount = mNotesItems.size();
    }
    //    public static ArrayList<File> recentImages() {
//
//        return imageItems;
//    }
    public void onDestroy() {
        // In onDestroy() you should tear down anything that was setup for your data source,
        // eg. cursors, connections, etc.
//        mNotesItems.clear();
    }
    public int getCount() {
        return mCount;
    }
    @Override
    public RemoteViews getViewAt(int position) {

        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.item_notes_widget);
        if (position < mNotesItems.size()) {
            NotesItem note = mNotesItems.get(position);
            ArrayList<String> noteText = NoteHelper.getNote(note.getNotesNote());
            rv.setTextViewText(R.id.item_note_title, noteText.get(0));
            rv.setTextViewText(R.id.item_note_date, stream.rocketnotes.Utils.TextUtils.getTimeStampShort(note.getNotesDate()));
            if (!TextUtils.isEmpty(noteText.get(1)))
            {
                rv.setTextViewText(R.id.item_note_note, noteText.get(1).replaceAll("\n", " "));
                rv.setViewVisibility(R.id.item_note_note, View.VISIBLE);
            }
            else
            {
                rv.setViewVisibility(R.id.item_note_note, View.GONE);
            }
        }
        Bundle extras = new Bundle();
        Intent fillInIntent = new Intent();
        if (position < mNotesItems.size()) {
            extras.putInt("EXTRA_ITEM", mNotesItems.get(position).getNotesID());
            Log.d("Set ID", String.valueOf(mNotesItems.get(position).getNotesID()));
            fillInIntent.putExtras(extras);
        }
        rv.setOnClickFillInIntent(R.id.item_note, fillInIntent);

        return rv;
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
