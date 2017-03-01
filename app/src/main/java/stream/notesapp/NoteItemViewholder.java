package stream.notesapp;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;

public class NoteItemViewholder extends AbstractFlexibleItem<NoteItemViewholder.MyViewHolder> {

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
     *   is unique (read more in the wiki page: "Setting Up Advanced").
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

    /**
     * The Adapter is provided to be forwarded to the ImageViewHolder.
     * The unique instance of the LayoutInflater is also provided to simplify the
     * creation of the VH.
     */
    @Override
    public MyViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater,
                                         ViewGroup parent) {
        return new MyViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    /**
     * The Adapter and the Payload are provided to get more specific information from it.
     */
    @Override
    public void bindViewHolder(FlexibleAdapter adapter, MyViewHolder holder, int position,
                               List payloads) {
        ArrayList<String> note = NoteHelper.getNote(title);
        holder.noteTitle.setText(note.get(0));
        if (!TextUtils.isEmpty(note.get(1)))
        {
            holder.noteBody.setText(note.get(1));
            holder.noteBody.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.noteBody.setVisibility(View.GONE);
        }
    }

    /**
     * The ViewHolder used by this item.
     * Extending from FlexibleViewHolder is recommended especially when you will use
     * more advanced features.
     */
    public class MyViewHolder extends FlexibleViewHolder {

        public TextView noteTitle;
        public TextView noteBody;

        public MyViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            noteTitle = (TextView) view.findViewById(R.id.item_note_title);
            noteBody = (TextView) view.findViewById(R.id.item_note_note);
        }
    }
}
