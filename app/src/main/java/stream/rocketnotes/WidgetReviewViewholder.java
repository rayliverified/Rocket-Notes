package stream.rocketnotes;

import android.animation.Animator;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.suke.widget.SwitchButton;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.helpers.AnimatorHelper;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;
import stream.customalert.CustomAlertDialogue;
import stream.rocketnotes.utils.AnalyticsUtils;
import stream.rocketnotes.utils.AnimUtils;
import stream.rocketnotes.utils.PermissionUtils;

public class WidgetReviewViewholder extends AbstractFlexibleItem<WidgetReviewViewholder.MyViewHolder> {

    private String id;
    private boolean widgetReview;
    private String mActivity = this.getClass().getSimpleName();
    private Activity activity;

    public WidgetReviewViewholder(String id, Activity activity) {
        this.id = id;
        this.activity = activity;
    }

    /**
     * When an item is equals to another?
     * Write your own concept of equals, mandatory to implement.
     * This will be explained in the "Item interfaces" Wiki page.
     */
    @Override
    public boolean equals(Object inObject) {
        if (inObject instanceof WidgetReviewViewholder) {
            WidgetReviewViewholder inItem = (WidgetReviewViewholder) inObject;
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
        return R.layout.item_widgetreview;
    }

    @Override
    public MyViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new MyViewHolder(view, adapter);
    }

    /**
     * The Adapter and the Payload are provided to get more specific information from it.
     */
    @Override
    public void bindViewHolder(final FlexibleAdapter adapter, final MyViewHolder holder, final int position,
                               List payloads) {
        final Context context = holder.itemView.getContext();
        GetReviewStatus(context);
        final String appName = context.getPackageName();
        if (!widgetReview) {
            holder.rateYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AnalyticsUtils.AnalyticEvent(mActivity, "Click", "Review");
                    holder.rateYes.startAnimation(AnimUtils.Bounce(context, R.anim.anim_bounce, 600));
                    SetReviewStatus(context, true);
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    if (PermissionUtils.isAppInstalled(context, "com.android.vending")) {
                        intent.setData(Uri.parse("market://details?id=" + appName));
                        try {
                            context.startActivity(intent);
                        } catch (android.content.ActivityNotFoundException ex) {
                            intent.setData(Uri.parse(String.format("https://play.google.com/store/apps/details?id=%s", BuildConfig.APPLICATION_ID)));
                            context.startActivity(intent);
                        }
                    } else if (PermissionUtils.isAppInstalled(context, "com.amazon.venezia")) {
                        intent.setData(Uri.parse("amzn://apps/android?p=" + appName));
                        try {
                            context.startActivity(intent);
                        } catch (android.content.ActivityNotFoundException ex) {
                            intent.setData(Uri.parse("https://www.amazon.com/Stream-Inc-Blank-Icon/dp/B06XDVL38F"));
                            context.startActivity(intent);
                        }
                    } else {
                        intent.setData(Uri.parse(String.format("https://play.google.com/store/apps/details?id=%s", BuildConfig.APPLICATION_ID)));
                        context.startActivity(intent);
                    }
                }
            });
            holder.rateNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AnalyticsUtils.AnalyticEvent(mActivity, "Click", "Feedback");
                    holder.rateNo.startAnimation(AnimUtils.Bounce(context, R.anim.anim_bounce, 600));

                    ArrayList<String> boxHint = new ArrayList<>();
                    boxHint.add("Message");

                    CustomAlertDialogue.Builder alert = new CustomAlertDialogue.Builder(context)
                            .setStyle(CustomAlertDialogue.Style.INPUT)
                            .setTitle("Why so sad :(")
                            .setMessage("Please send us your feedback so we can make this app better!")
                            .setPositiveText("Submit")
                            .setPositiveColor(R.color.positive)
                            .setPositiveTypeface(Typeface.DEFAULT_BOLD)
                            .setOnInputClicked(new CustomAlertDialogue.OnInputClicked() {
                                @Override
                                public void OnClick(View view, Dialog dialog, ArrayList<String> inputList) {
                                    AnalyticsUtils.AnalyticEvent(mActivity, "Feedback", "Submit");
                                    Intent intent = new Intent(Intent.ACTION_SEND);
                                    intent.setType("message/rfc822");
                                    intent.setType("vnd.android.cursor.item/email");
                                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{context.getString(R.string.email_mailto)});
                                    intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.email_subject));
                                    intent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.email_message) + inputList.get(0));
                                    try {
                                        Toasty.normal(context, "Send via email", Toast.LENGTH_SHORT).show();
                                        context.startActivity(Intent.createChooser(intent, "Send email using..."));
                                    } catch (android.content.ActivityNotFoundException ex) {
                                        Toasty.normal(context, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeText("Close")
                            .setNegativeColor(R.color.negative)
                            .setOnNegativeClicked(new CustomAlertDialogue.OnNegativeClicked() {
                                @Override
                                public void OnClick(View view, Dialog dialog) {
                                    dialog.dismiss();
                                    AnalyticsUtils.AnalyticEvent(mActivity, "Feedback", "Cancel");
                                }
                            })
                            .setBoxInputHint(boxHint)
                            .setDecorView(activity.getWindow().getDecorView())
                            .build();
                    alert.show();
                }
            });
        } else {
            holder.rateNoLayout.setVisibility(View.GONE);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(dpToPx(context, 120), dpToPx(context, 120));
            layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
            holder.rateYes.setLayoutParams(layoutParams);
            holder.body.setText("Thanks for the great review.");
            holder.rateYesText.setText("You're the BEST!");
            holder.rateYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.rateYes.startAnimation(AnimUtils.Bounce(context, R.anim.anim_bounce, 600));
                    SetReviewStatus(context, false);
                }
            });
            holder.hideLayout.setVisibility(View.VISIBLE);
            holder.switchButton.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                    if (!isChecked) {
                        AnalyticsUtils.AnalyticEvent(mActivity, "Click", "Hide");
                        SetHideStatus(context, true);
                        EventBus.getDefault().post(new UpdateMainEvent(Constants.HIDE_REVIEW));
                        Log.d("Notification", Constants.HIDE_REVIEW);
                    }
                }
            });
        }
    }

    /**
     * The ViewHolder used by this item.
     * Extending from FlexibleViewHolder is recommended especially when you will use
     * more advanced features.
     */
    public class MyViewHolder extends FlexibleViewHolder {

        RelativeLayout widgetReviewLayout;
        LinearLayout rateYesLayout;
        LinearLayout rateNoLayout;
        LinearLayout hideLayout;
        ImageButton rateYes;
        ImageButton rateNo;
        TextView body;
        TextView rateYesText;
        SwitchButton switchButton;

        public MyViewHolder(View view, FlexibleAdapter adapter) {
            super(view, adapter);
            widgetReviewLayout = view.findViewById(R.id.item_widgetreview);
            rateYes = view.findViewById(R.id.smiley_yes);
            rateNo = view.findViewById(R.id.smiley_no);
            rateYesLayout = view.findViewById(R.id.smiley_yes_layout);
            rateNoLayout = view.findViewById(R.id.smiley_no_layout);
            hideLayout = view.findViewById(R.id.hide_container);
            body = view.findViewById(R.id.item_widgetreview_body);
            rateYesText = view.findViewById(R.id.smiley_yes_text);
            switchButton = view.findViewById(R.id.hide_switch);
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

    private void SetReviewStatus(Context context, boolean review) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.WIDGET_REVIEW, review);
        editor.apply();
    }

    private void GetReviewStatus(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", 0);
        widgetReview = prefs.getBoolean(Constants.WIDGET_REVIEW, false);
    }

    private void SetHideStatus(Context context, boolean hide) {
        SharedPreferences prefs = context.getSharedPreferences("prefs", 0);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(Constants.WIDGET_REVIEW_HIDE, hide);
        editor.apply();
    }

    /**
     * Converts dp to pixels.
     */
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}
