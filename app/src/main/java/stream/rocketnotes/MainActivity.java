package stream.rocketnotes;

import android.Manifest;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.material.appbar.AppBarLayout;
import com.pyze.android.Pyze;
import com.pyze.android.PyzeEvents;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import eu.davidea.fastscroller.FastScroller;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollStaggeredLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import stream.custompermissionsdialogue.PermissionsDialogue;
import stream.custompermissionsdialogue.utils.PermissionUtils;
import stream.rocketnotes.filter.FilterMaterialSearchView;
import stream.rocketnotes.filter.model.Filter;
import stream.rocketnotes.utils.AnalyticsUtils;

public class MainActivity extends AppCompatActivity {

    private Integer mNoteCount;
    private Integer mImageCount;

    RecyclerView mRecyclerView;
    FlexibleAdapter<IFlexible> mAdapter;
    StaggeredGridLayoutManager mStaggeredLayoutManager;
    FastScroller mFastScroller;
    DatabaseHelper dbHelper;

    AppBarLayout mAppBar;
    FloatingSearchView mSearchView;
    FilterMaterialSearchView mFilterView;
    MenuItem mActionVoice;
    MenuItem mActionCamera;
    FabSpeedDial mFAB;

    Context mContext;
    private String mActivity = this.getClass().getSimpleName();
    SharedPreferences sharedPref;
    private final String TAG = "Search";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            PermissionsDialogue.Builder alertPermissions = new PermissionsDialogue.Builder(MainActivity.this)
                    .setCancelable(false)
                    .setMessage(getString(R.string.app_name) + " requires the following permissions to save notes: ")
                    .setIcon(R.mipmap.ic_launcher)
                    .setRequireStorage(PermissionsDialogue.REQUIRED)
                    .setOnContinueClicked(new PermissionsDialogue.OnContinueClicked() {
                        @Override
                        public void OnClick(View view, Dialog dialog) {
                            dialog.dismiss();
                        }
                    })
                    .setDecorView(getWindow().getDecorView())
                    .build();
            alertPermissions.show();
        }
        InitializeAnalytics();
        Pyze.showInAppNotificationUI(this, null);
        dbHelper = new DatabaseHelper(mContext);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //Checks for first launch
//        if (sharedPref.getBoolean("prefs_first_start", true)) {
//
//            //Start sequence finished
//            SharedPreferences.Editor editor = sharedPref.edit();
//            editor.putBoolean("prefs_tutorial_intro", false);
//            editor.apply();
//        }

//        checkVoiceRecognition();
        mAppBar = findViewById(R.id.app_bar);
        mFilterView = findViewById(R.id.sv);
        SetupSearchBar();
        InitializeRecyclerView();
        SetupFAB();
        if (getIntent().getAction() != null && getIntent().getAction() != Intent.ACTION_MAIN) {
            Log.d("MainActivity", getIntent().getAction());
            if (getIntent().getAction().equals(Constants.STICKY)) {

            }
        } else {
            Log.d("MainActivity", "Clear Sticky");
            UpdateMainEvent stickyEvent = EventBus.getDefault().getStickyEvent(UpdateMainEvent.class);
            if (stickyEvent != null) {
                EventBus.getDefault().removeStickyEvent(stickyEvent);
            }
        }
        SessionDetails();
        Log.d("MainActivity", "onCreate");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void InitializeRecyclerView() {

        // Optional but strongly recommended: Compose the initial list
        List<IFlexible> myItems = getDatabaseList();

        // Initialize the Adapter
        mAdapter = new FlexibleAdapter<>(myItems);
        mAdapter.setAnimationOnForwardScrolling(true)
                .setAnimationOnReverseScrolling(true)
                .setAnimationEntryStep(true)
                .setAnimationInterpolator(new DecelerateInterpolator())
                .setAnimationDuration(300L);
        mStaggeredLayoutManager = createNewStaggeredGridLayoutManager();

        // Prepare the RecyclerView and attach the Adapter to it
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setItemViewCacheSize(0); //Setting ViewCache to 0 (default=2) will animate items better while scrolling down+up with LinearLayout
        mRecyclerView.setLayoutManager(mStaggeredLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        if (sharedPref.getBoolean("enable_fastscroller", false))
        {
            //Create FastScroller.
            mFastScroller = findViewById(R.id.fast_scroller);
            mFastScroller.setAutoHideEnabled(true);             //true is the default value
            mFastScroller.setAutoHideDelayInMillis(500L);      //1000ms is the default value
            mFastScroller.setIgnoreTouchesOutsideHandle(false); //false is the default value
            //0 pixel is the default value. When > 0 it mimics the fling gesture
            mFastScroller.setMinimumScrollThreshold(100);
            mFastScroller.setBubbleAndHandleColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
            mFastScroller.addOnScrollStateChangeListener(new FastScroller.OnScrollStateChangeListener() {
                @Override
                public void onFastScrollerStateChange(boolean scrolling) {
                    if (scrolling)
                    {
                        mFAB.hide();
                    }
                    else
                    {
                        mFAB.show();
                    }
                }
            });
            mAdapter.setFastScroller(mFastScroller);
        }
        else
        {
            if (mAdapter.getFastScroller() != null)
            {
                mAdapter.getFastScroller().hideScrollbar();
            }
        }

//        mRecyclerView.setY(Units.dpToPx(mContext, 56));
//        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                switch (motionEvent.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        break;
//                    case MotionEvent.ACTION_UP:
//                        break;
//                    case MotionEvent.ACTION_POINTER_DOWN:
//                        break;
//                    case MotionEvent.ACTION_POINTER_UP:
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        mRecyclerView.get()
//                        break;
//                }
//                ret
//                urn false;
//            }
//        });
//        mRecyclerView.addOnScrollListener(new MoveViewOnScrollListener(mRecyclerView));
    }

    private void SetupSearchBar() {

        mSearchView = findViewById(R.id.floating_search_view);
//        mActionVoice = (MenuItem) findViewById(R.id.action_voice);
        mActionCamera = findViewById(R.id.action_camera);

        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {

            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {

                if (!oldQuery.equals("") && newQuery.equals("")) {
                    mSearchView.clearSuggestions();
                    FilterReset(true);
                } else {
                    if (mAdapter.hasNewFilter(newQuery)) {
                        Log.d(TAG, "onQueryTextChange newText: " + newQuery);
                        mAdapter.setFilter(newQuery);
                        // Fill and Filter mItems with your custom list and automatically animate the changes
                        // Watch out! The original list must be a copy
                        mAdapter.filterItems(getDatabaseList(), 200);
                    }
                }

                Log.d(TAG, "onSearchTextChanged()");
            }
        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(final SearchSuggestion searchSuggestion) {

//                ColorSuggestion colorSuggestion = (ColorSuggestion) searchSuggestion;
//                DataHelper.findColors(getActivity(), colorSuggestion.getBody(),
//                        new DataHelper.OnFindColorsListener() {
//
//                            @Override
//                            public void onResults(List<ColorWrapper> results) {
//                                //show search results
//                            }
//
//                        });
//                Log.d(TAG, "onSuggestionClicked()");
//
//                mLastQuery = searchSuggestion.getBody();
            }

            @Override
            public void onSearchAction(String query) {
//                mLastQuery = query;
//
//                DataHelper.findColors(getActivity(), query,
//                        new DataHelper.OnFindColorsListener() {
//
//                            @Override
//                            public void onResults(List<ColorWrapper> results) {
//                                //show search results
//                            }
//
//                        });
                Log.d(TAG, "onSearchAction()");
            }
        });

        mSearchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
//                //show suggestions when search bar gains focus (typically history suggestions)
//                mSearchView.swapSuggestions(DataHelper.getHistory(getActivity(), 3));
                AnalyticsUtils.AnalyticEvent(mActivity, "Click", "Search");
                mFilterView.setVisibility(View.GONE);
                Log.d(TAG, "onFocus()");
            }

            @Override
            public void onFocusCleared() {

//                //set the title of the bar so that when focus is returned a new query begins
                mSearchView.setSearchBarTitle("Rocket Notes");
                FilterReset(true);

                //you can also set setSearchText(...) to make keep the query there when not focused and when focus returns
                //mSearchView.setSearchText(searchSuggestion.getBody());

                Log.d(TAG, "onFocusCleared()");
            }
        });


        //handle menu clicks the same way as you would
        //in a regular activity
        mSearchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_camera:
                        AnalyticsUtils.AnalyticEvent(mActivity, "Click", "Camera");
                        Intent intent = new Intent(mContext, CameraActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        break;
                    case R.id.filter_image:
                        AnalyticsUtils.AnalyticEvent(mActivity, "Click", "Filter Image");
                        FilterImages();
                        break;
                    case R.id.filter_text:
                        AnalyticsUtils.AnalyticEvent(mActivity, "Click", "Filter Text");
                        FilterText();
                        break;
                    case R.id.action_settings:
                        intent = new Intent(mContext, SettingsActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                        break;
                    default:
                        break;
                }

//                if (item.getItemId() == R.id.action_voice)
//                {
//                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//                    //... put other settings in the Intent
//                    startActivityForResult(intent, 0);
//                }
            }
        });

        //use this listener to listen to menu clicks when app:floatingSearch_leftAction="showHome"
        mSearchView.setOnHomeActionClickListener(new FloatingSearchView.OnHomeActionClickListener() {
            @Override
            public void onHomeClicked() {

                Log.d(TAG, "onHomeClicked()");
            }
        });

        /*
         * Here you have access to the left icon and the text of a given suggestion
         * item after as it is bound to the suggestion list. You can utilize this
         * callback to change some properties of the left icon and the text. For example, you
         * can load the left icon images using your favorite image loading library, or change text color.
         *
         *
         * Important:
         * Keep in mind that the suggestion list is a RecyclerView, so views are reused for different
         * items in the list.
         */
        mSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon,
                                         TextView textView, SearchSuggestion item, int itemPosition) {
//                ColorSuggestion colorSuggestion = (ColorSuggestion) item;
//
//                String textColor = mIsDarkSearchTheme ? "#ffffff" : "#000000";
//                String textLight = mIsDarkSearchTheme ? "#bfbfbf" : "#787878";
//
//                if (colorSuggestion.getIsHistory()) {
//                    leftIcon.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
//                            R.drawable.ic_history_black_24dp, null));
//
//                    Util.setIconColor(leftIcon, Color.parseColor(textColor));
//                    leftIcon.setAlpha(.36f);
//                } else {
//                    leftIcon.setAlpha(0.0f);
//                    leftIcon.setImageDrawable(null);
//                }
//
//                textView.setTextColor(Color.parseColor(textColor));
//                String text = colorSuggestion.getBody()
//                        .replaceFirst(mSearchView.getQuery(),
//                                "<font color=\"" + textLight + "\">" + mSearchView.getQuery() + "</font>");
//                textView.setText(Html.fromHtml(text));
            }

        });
    }

    public void SetupFAB() {
        mFAB = findViewById(R.id.main_fab);
        mFAB.setMenuListener(new SimpleMenuListenerAdapter() {
            @Override
            public boolean onMenuItemSelected(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_text) {
                    Log.d("FAB", "Text");
                    AnalyticsUtils.AnalyticEvent(mActivity, "Click", "New Note");
                    Intent intent = new Intent(mContext, EditActivity.class);
                    intent.setAction(Constants.NEW_NOTE);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    return true;
                } else if (menuItem.getItemId() == R.id.action_camera) {
                    Log.d("FAB", "Camera");
                    AnalyticsUtils.AnalyticEvent(mActivity, "Click", "New Image");
                    Intent intent = new Intent(mContext, CameraActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    return true;
                }
                return false;
            }
        });
    }

    public void FilterImages() {
        Filter filter = new Filter(1, "Image", 0, R.drawable.icon_camera_shortcut, ContextCompat.getColor(mContext, R.color.colorPrimary));
        mFilterView.setVisibility(View.VISIBLE);
        mFilterView.addFilter(filter);
        List<IFlexible> list = new ArrayList<>();
        ArrayList<NotesItem> notesItems = dbHelper.GetImageNotes();
        Log.d("NotesItem Size", String.valueOf(notesItems.size()));
        for (NotesItem note : notesItems) {
            if (note.getNotesImagePreview() != null)
            {
                list.add(new ImageItemViewholder(Integer.toString(note.getNotesID()), note.getNotesImagePreview()));
            }
            else
            {
                list.add(new ImageItemViewholder(Integer.toString(note.getNotesID()), note.getNotesImage()));
            }
        }
        mAdapter.updateDataSet(list);
        RemoveSticky();
        Log.d("Filter", "Images");
    }

    public void FilterText() {
        Filter filter = new Filter(1, "Text", 0, R.drawable.icon_edit_shortcut, ContextCompat.getColor(mContext, R.color.colorPrimary));
        mFilterView.setVisibility(View.VISIBLE);
        mFilterView.addFilter(filter);
        List<IFlexible> list = new ArrayList<>();
        ArrayList<NotesItem> notesItems = dbHelper.GetTextNotes();
        Log.d("NotesItem Size", String.valueOf(notesItems.size()));
        for (NotesItem note : notesItems) {
            list.add(new NoteItemViewholder(Integer.toString(note.getNotesID()), note.getNotesNote()));
        }
        mAdapter.updateDataSet(list);
        Log.d("Filter", "Text");
    }

    public void FilterReset(boolean clearSticky) {
        List<IFlexible> list = getDatabaseList();
        mAdapter.updateDataSet(list);
        //Do not clear sticky if filtering image notes
        if (clearSticky)
            RemoveSticky();
        Log.d("Filter", "Reset");
    }

    public void UpdateOnAdd(UpdateMainEvent event) {
        Integer noteID = event.getID();
        NotesItem note = dbHelper.GetNote(noteID);
        AbstractFlexibleItem item = null;
        if (note.getNotesImage() != null) {
            Log.d("Image View Holder", note.getNotesImage());
            item = new ImageItemViewholder(Integer.toString(noteID), note.getNotesImage());
        } else if (note.getNotesNote() != null) {
            Log.d("Note", "Note Item");
            item = new NoteItemViewholder(Integer.toString(noteID), note.getNotesNote());
        }
        mAdapter.addItem(0, item);
        mStaggeredLayoutManager.scrollToPosition(0);
        RemoveSticky();
        Log.d("Broadcast Receiver", Constants.RECEIVED);
    }

    public void UpdateOnUpdate(UpdateMainEvent event) {
        Integer noteID = event.getID();
        NotesItem note = dbHelper.GetNote(noteID);
        AbstractFlexibleItem item = null;
        if (note.getNotesImage() != null) {
            Log.d("Image View Holder", note.getNotesImage());
            item = new ImageItemViewholder(Integer.toString(noteID), note.getNotesImage());
        } else if (note.getNotesNote() != null) {
            Log.d("Note", "Note Item");
            item = new NoteItemViewholder(Integer.toString(noteID), note.getNotesNote());
        }
        Integer currentPosition = mAdapter.getGlobalPositionOf(item);
        mAdapter.updateItem(item, null);
        mAdapter.moveItem(currentPosition, 0);
        mStaggeredLayoutManager.scrollToPosition(1);
        RemoveSticky();
        Log.d("Broadcast Receiver", Constants.UPDATE_NOTE);
    }

    public void UpdateOnDelete(UpdateMainEvent event) {
        NotesItem note = event.getNotesItem();
        AbstractFlexibleItem item = null;
        if (note.getNotesImage() != null) {
            Log.d("Image View Holder", note.getNotesImage());
            item = new ImageItemViewholder(Integer.toString(note.getNotesID()), note.getNotesImage());
        } else if (note.getNotesNote() != null) {
            Log.d("Note", "Note Item");
            item = new NoteItemViewholder(Integer.toString(note.getNotesID()), note.getNotesNote());
        }
        Integer position = mAdapter.getGlobalPositionOf(item);
        Log.d("Item Position", String.valueOf(position));
        mAdapter.removeItem(position);
        RemoveSticky();
        Log.d("Broadcast Receiver", Constants.DELETE_NOTE);
    }

    public void UpdateOnHide(UpdateMainEvent event) {
        AbstractFlexibleItem item = null;
        item = new WidgetReviewViewholder("Review", MainActivity.this);
        Integer position = mAdapter.getGlobalPositionOf(item);
        Log.d("Item Position", String.valueOf(position));
        mAdapter.removeItem(position);
        RemoveSticky();
        Log.d("Broadcast Receiver", Constants.HIDE_REVIEW);
    }

    public void RemoveSticky() {
        UpdateMainEvent stickyEvent = EventBus.getDefault().getStickyEvent(UpdateMainEvent.class);
        if (stickyEvent != null) {
            EventBus.getDefault().removeStickyEvent(stickyEvent);
        }
    }

    public void UpdateFilter(UpdateMainEvent event) {
        FilterReset(true);
    }

    public void checkVoiceRecognition() {
        // Check if voice recognition is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0) {
            Toast.makeText(this, "Voice recognizer not present",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateMainEvent event) {
        Log.d("MainActivity", event.getAction());
        switch (event.getAction()) {
            case Constants.RECEIVED:
                UpdateOnAdd(event);
                break;
            case Constants.UPDATE_NOTE:
                UpdateOnUpdate(event);
                break;
            case Constants.DELETE_NOTE:
                UpdateOnDelete(event);
                break;
            case Constants.HIDE_REVIEW:
                UpdateOnHide(event);
                break;
            case Constants.FILTER:
                UpdateFilter(event);
                break;
            case Constants.FILTER_IMAGES:
                FilterImages();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        //Listen for new messages received
        Log.d("LocalBroadcastManager", "onResume");
        //Sometimes, multiple changes are made to mAdapter and the entire view should be refreshed.
        if (sharedPref.getBoolean(Constants.REFRESH, false)) {
            if (mAdapter != null) {
                Log.d("MainActivity", "Refresh");
                InitializeRecyclerView();
            }
            //Reset REFRESH flag
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Constants.REFRESH, false);
            editor.apply();
        }
        //Get events after feed is refreshed.
        EventBus.getDefault().register(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d("LocalBroadcastManager", "onPause");
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            mSearchView.setSearchFocused(true);
            mSearchView.setSearchText(results.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected StaggeredGridLayoutManager createNewStaggeredGridLayoutManager() {
        return new SmoothScrollStaggeredLayoutManager(this, 2);
    }

    public List<IFlexible> getDatabaseList() {
        List<IFlexible> list = new ArrayList<>();
        ArrayList<NotesItem> notesItems = dbHelper.GetNotesDate();
        mNoteCount = 0;
        mImageCount = 0;
        Log.d("NotesItem Size", String.valueOf(notesItems.size()));
        for (NotesItem note : notesItems) {
            if (note.getNotesNote() != null) {
                mNoteCount += 1;
                list.add(new NoteItemViewholder(Integer.toString(note.getNotesID()), note.getNotesNote()));
            } else if (note.getNotesImage() != null) {
                mImageCount += 1;
                if (note.getNotesImagePreview() != null)
                {
                    list.add(new ImageItemViewholder(Integer.toString(note.getNotesID()), note.getNotesImagePreview()));
                }
                else
                {
                    list.add(new ImageItemViewholder(Integer.toString(note.getNotesID()), note.getNotesImage()));
                }
            }
        }
        //Display prompt for review if user has created 3 or more notes and not hidden widget.
        SharedPreferences prefs = mContext.getSharedPreferences("prefs", 0);
        boolean hideReview = prefs.getBoolean(Constants.WIDGET_REVIEW_HIDE, false);
        if (mNoteCount + mImageCount >= 3 && !hideReview) {
            list.add(new WidgetReviewViewholder("Review", MainActivity.this));
        }
        return list;
    }

    public void SessionDetails() {
        HashMap<String, Object> attributes = new HashMap<>();

        int ids[] = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, ImageWidget.class));
        if (ids.length > 0) {
            attributes.put("Image Widget", String.valueOf(true));
        } else {
            attributes.put("Image Widget", String.valueOf(false));
        }
        ids = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, NotesWidget.class));
        if (ids.length > 0) {
            attributes.put("Notes Widget", String.valueOf(true));
        } else {
            attributes.put("Notes Widget", String.valueOf(false));
        }

        //Pyze
        attributes.put("Note Count", String.valueOf(mNoteCount));
        attributes.put("Image Count", String.valueOf(mImageCount));
        PyzeEvents.postCustomEventWithAttributes(mActivity, attributes);
    }

    public void InitializeAnalytics() {
        Pyze.initialize(getApplication());
//        UXCam.startWithKey(Constants.UXCAM_API_KEY);
    }
}
