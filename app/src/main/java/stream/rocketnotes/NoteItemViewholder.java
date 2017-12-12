package stream.rocketnotes;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFilterable;
import eu.davidea.viewholders.FlexibleViewHolder;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import stream.rocketnotes.service.DeleteNoteService;

public class NoteItemViewholder extends AbstractFlexibleItem<NoteItemViewholder.MyViewHolder> implements IFilterable {

    private String id;
    private String noteText;
    private boolean showQuickActions = true;

    private final String mActivity = this.getClass().getSimpleName();

    public NoteItemViewholder(String id, String noteText) {
        this.id = id;
        this.noteText = noteText;
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
//        UXCam.occludeSensitiveView(holder.noteLayout);

        ArrayList<String> note = NoteHelper.getNote(stream.rocketnotes.utils.TextUtils.Compatibility(noteText));
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

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        showQuickActions = sharedPrefs.getBoolean("show_quickactions", true);

        if (!showQuickActions) {
            holder.menuContainer.setVisibility(View.GONE);
        } else {
            holder.menuContainer.setVisibility(View.VISIBLE);
            holder.mBtnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, PopupActivity.class);
                    intent.putExtra(Constants.ID, Integer.valueOf(id));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(Constants.OPEN_NOTE);
                    context.startActivity(intent);
                }
            });

            holder.mBtnMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new BottomSheet.Builder(context)
                            .setSheet(R.menu.menu_card_view)
                            .setListener(new BottomSheetListener() {
                                @Override
                                public void onSheetShown(@NonNull BottomSheet bottomSheet) {

                                }

                                @Override
                                public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem menuItem) {
                                    switch (menuItem.getItemId()) {
                                        case R.id.action_share:
                                            stream.rocketnotes.utils.TextUtils.Share(context, noteText);
                                            Log.d(mActivity, "Share");
                                            break;
                                        case R.id.action_copy:
                                            stream.rocketnotes.utils.TextUtils.CopyText(context, noteText);
                                            Toasty.normal(context, "Copied", Toast.LENGTH_SHORT).show();
                                            Log.d(mActivity, "Copy");
                                            break;
                                        case R.id.action_delete:
                                            Intent deleteNote = new Intent(context, DeleteNoteService.class);
                                            deleteNote.putExtra(Constants.ID, Integer.valueOf(id));
                                            deleteNote.setAction(Constants.DELETE_NOTE);
                                            context.startService(deleteNote);
                                            Log.d(mActivity, Constants.DELETE_NOTE);
                                            break;
                                        default:
                                            break;
                                    }
                                }

                                @Override
                                public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @DismissEvent int i) {

                                }
                            })
                            .show();
                }
            });
        }
    }

    @Override
    public boolean filter(String constraint) {
        Integer fuzzyRatio = FuzzySearch.partialRatio(noteText.toLowerCase(), constraint.toLowerCase());
        Log.d("Fuzzy Search Ratio", String.valueOf(fuzzyRatio));
        return fuzzyRatio >= 70 || noteText.toLowerCase().trim().contains(constraint);
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
        public RelativeLayout menuContainer;
        public ImageView mBtnAdd;
        public ImageView mBtnMore;

        public MyViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            noteLayout = view.findViewById(R.id.item_note);
            noteTitle = view.findViewById(R.id.item_note_title);
            noteBody = view.findViewById(R.id.item_note_note);
            menuContainer = view.findViewById(R.id.menu_container);
            mBtnAdd = view.findViewById(R.id.btn_add);
            mBtnMore = view.findViewById(R.id.btn_more);
        }

        @Override
        public void scrollAnimators(@NonNull List<Animator> animators, int position, boolean isForward) {
            if (mAdapter.getRecyclerView().getLayoutManager() instanceof GridLayoutManager) {
                if (position % 2 != 0)
                    AnimatorHelper.slideInFromRightAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
                else
                    AnimatorHelper.slideInFromLeftAnimator(animators, itemView, mAdapter.getRecyclerView(), 0.5f);
            } else {
                if (isForward)
                    AnimatorHelper.slideInFromBottomAnimator(animators, itemView, mAdapter.getRecyclerView());
                else
                    AnimatorHelper.slideInFromTopAnimator(animators, itemView, mAdapter.getRecyclerView());
            }
        }
    }
}
