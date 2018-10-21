package stream.rocketnotes;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import stream.rocketnotes.interfaces.ShareAdapterInterface;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ShareAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context mContext;
    ArrayList<NotesItem> notesList;

    public ShareAdapter(Context context, ArrayList<NotesItem> notesList) {
        this.mContext = context;
        this.notesList = notesList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_share_add, parent, false);
        return new ShareAddViewholder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder genericHolder, final int position) {
        NotesItem note = notesList.get(position);
        ShareAddViewholder holder = (ShareAddViewholder) genericHolder;
        holder.setShareAdapterInterface(new ShareAdapterInterface() {
            @Override
            public void ShareNote(Integer position) {

            }

            @Override
            public void ShareNote(Integer position, boolean shared) {
                Log.d("SharedNote", String.valueOf(shared));
                notesList.get(position).setShared(shared);
            }
        });
        holder.setNote(note);
    }

    @Override
    public int getItemCount() {
        return notesList.size();
    }

    public NotesItem getNote(int position) {
        return notesList.get(position);
    }
}