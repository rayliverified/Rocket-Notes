package stream.rocketnotes;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.uxcam.UXCam;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.viewholders.FlexibleViewHolder;
import me.xdrop.fuzzywuzzy.FuzzySearch;

public class NoteItemViewholder extends AbstractFlexibleItem<NoteItemViewholder.MyViewHolder> implements IFilterable {

    private String id;
    private String title;

    public NoteItemViewholder(String id, String title) {
        this.id = id;
        this.title = title;
    }

    /**
     * When an item is equals to another?
     * Write your own concept of equals, mandatory to implement.
     * This will be explained in the "Item interfaces" Wiki page.
     */
    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof NoteItemViewholder) {
            NoteItemViewholder inItem = (NoteItemViewholder) inObject;
            return this.id.equals(inItem.id);
        }
        return false;
    }

    /**
     * You should implement also this method if equals() is implemented.
     * This method, if implemented, has several implications that Adapter handles better:
     * - The Hash increases performance in big list during Update & Filter operations.
     * - Collapsing many expandable items is much faster.
     * - You might want to activate stable ids via Constructor for RV, if your id
     * is unique (read more in the wiki page: "Setting Up Advanced").
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * For the item type we need an int value: the layoutResID is sufficient.
     */
    @Override
    public int getLayoutRes() {
        return R.layout.item_notes;
    }

    @Override
    public MyViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new MyViewHolder(view, adapter);
    }

    /**
     * The Adapter and the Payload are provided to get more specific information from it.
     */
    @Override
    public void bindViewHolder(FlexibleAdapter adapter, MyViewHolder holder, int position,
                               List payloads) {
        final Context context = holder.itemView.getContext();
        UXCam.occludeSensitiveView(holder.noteLayout);

        ArrayList<String> note = NoteHelper.getNote(stream.rocketnotes.utils.TextUtils.Compatibility(title));
        holder.noteTitle.setText(stream.rocketnotes.utils.TextUtils.fromHtml(note.get(0)));
        if (!TextUtils.isEmpty(note.get(1))) {
            holder.noteBody.setText(stream.rocketnotes.utils.TextUtils.fromHtml(note.get(1)));
            holder.noteBody.setVisibility(View.VISIBLE);
        } else {
            holder.noteBody.setVisibility(View.GONE);
        }

        holder.noteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, EditActivity.class);
                intent.setAction(Constants.OPEN_NOTE);
                intent.putExtra(Constants.ID, Integer.valueOf(id));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public boolean filter(String constraint) {
        Integer fuzzyRatio = FuzzySearch.partialRatio(title.toLowerCase(), constraint.toLowerCase());
        Log.d("Fuzzy Search Ratio", String.valueOf(fuzzyRatio));
        return fuzzyRatio >= 70 || title.toLowerCase().trim().contains(constraint);
    }

    /**
     * The ViewHolder used by this item.
     * Extending from FlexibleViewHolder is recommended especially when you will use
     * more advanced features.
     */
    public class MyViewHolder extends FlexibleViewHolder {

        public LinearLayout noteLayout;
        public TextView noteTitle;
        public TextView noteBody;

        public MyViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            noteLayout = view.findViewById(R.id.item_note);
            noteTitle = view.findViewById(R.id.item_note_title);
            noteBody = view.findViewById(R.id.item_note_note);
        }
    }
}
