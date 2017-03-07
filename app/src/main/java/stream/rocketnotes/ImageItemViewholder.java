package stream.rocketnotes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;

public class ImageItemViewholder extends AbstractFlexibleItem<ImageItemViewholder.ImageViewHolder> {

    private String id;
    private String image;

    public ImageItemViewholder(String id, String image) {
        this.id = id;
        this.image = image;
    }

    /**
     * When an item is equals to another?
     * Write your own concept of equals, mandatory to implement.
     * This will be explained in the "Item interfaces" Wiki page.
     */
    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof ImageItemViewholder) {
            ImageItemViewholder inItem = (ImageItemViewholder) inObject;
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
        return R.layout.item_image;
    }

    /**
     * The Adapter is provided to be forwarded to the ImageViewHolder.
     * The unique instance of the LayoutInflater is also provided to simplify the
     * creation of the VH.
     */
    @Override
    public ImageViewHolder createViewHolder(FlexibleAdapter adapter, LayoutInflater inflater,
                                            ViewGroup parent) {
        return new ImageViewHolder(inflater.inflate(getLayoutRes(), parent, false), adapter);
    }

    /**
     * The Adapter and the Payload are provided to get more specific information from it.
     */
    @Override
    public void bindViewHolder(FlexibleAdapter adapter, ImageViewHolder holder, final int position,
                               List payloads) {
        final Context context = holder.itemView.getContext();

        Picasso.with(context).load(image).transform(ImageTransformer.getTransformation(holder.noteImage)).placeholder(R.drawable.icon_picture_full).into(holder.noteImage);

        holder.noteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ImageViewerActivity.class);
                intent.setAction(Constants.OPEN_IMAGE_SINGLE);
                intent.putExtra(Constants.ID, Integer.valueOf(id));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    /**
     * The ViewHolder used by this item.
     * Extending from FlexibleViewHolder is recommended especially when you will use
     * more advanced features.
     */
    public class ImageViewHolder extends FlexibleViewHolder {

        public ImageView noteImage;

        public ImageViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            noteImage = (ImageView) view.findViewById(R.id.item_image);
        }
    }
}
