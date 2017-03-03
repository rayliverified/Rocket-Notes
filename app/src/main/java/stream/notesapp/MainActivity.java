package stream.notesapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollStaggeredLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;

public class MainActivity extends Activity implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "Search";
    private SharedPreferences sharedPref;
    private RecyclerView mRecyclerView;
    private FlexibleAdapter<IFlexible> mAdapter;
    private StaggeredGridLayoutManager mStaggeredLayoutManager;
    private FloatingSearchView mSearchView;
    private AppBarLayout mAppBar;
    private MenuItem mActionVoice;
    private MenuItem mActionCamera;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //Checks for first launch
        if (sharedPref.getBoolean("prefs_first_start", true)) {

            DatabaseHelper dbMessage = new DatabaseHelper(mContext);

            //Start sequence finished
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("prefs_tutorial_intro", false);
            editor.apply();
        }

        mAppBar = (AppBarLayout) findViewById(R.id.app_bar);
        mAppBar.addOnOffsetChangedListener(this);

        initializeRecyclerView(savedInstanceState);
    }

    private void initializeRecyclerView(Bundle savedInstanceState) {

        // Optional but strongly recommended: Compose the initial list
        List<IFlexible> myItems = getDatabaseList();

        // Initialize the Adapter
        mAdapter = new FlexibleAdapter<IFlexible>(myItems);
        mStaggeredLayoutManager = createNewStaggeredGridLayoutManager();

        // Prepare the RecyclerView and attach the Adapter to it
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(mStaggeredLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        
        setupSearchBar();
        checkVoiceRecognition();

        UpdateMainEvent stickyEvent = EventBus.getDefault().getStickyEvent(UpdateMainEvent.class);
        if(stickyEvent != null) {
            EventBus.getDefault().removeStickyEvent(stickyEvent);
        }
        Log.d("MainActivity", "onCreate");
    }

    private void setupSearchBar() {

        mActionVoice = (MenuItem) findViewById(R.id.action_voice);
        mActionCamera = (MenuItem) findViewById(R.id.action_camera);

        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {

            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {

                if (!oldQuery.equals("") && newQuery.equals("")) {
                    mSearchView.clearSuggestions();
                } else {

                    //this shows the top left circular progress
                    //you can call it where ever you want, but
                    //it makes sense to do it when loading something in
                    //the background.

//                    //simulates a query call to a data source
//                    //with a new query.
//                    DataHelper.findSuggestions(getActivity(), newQuery, 5,
//                            FIND_SUGGESTION_SIMULATED_DELAY, new DataHelper.OnFindSuggestionsListener() {
//
//                                @Override
//                                public void onResults(List<ColorSuggestion> results) {
//
//                                    //this will swap the data and
//                                    //render the collapse/expand animations as necessary
//                                    mSearchView.swapSuggestions(results);
//
//                                    //let the users know that the background
//                                    //process has completed
//                                    mSearchView.hideProgress();
//                                }
//                            });
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

                Log.d(TAG, "onFocus()");
            }

            @Override
            public void onFocusCleared() {

//                //set the title of the bar so that when focus is returned a new query begins
//                mSearchView.setSearchBarTitle(mLastQuery);

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

                if (item.getItemId() == R.id.action_voice)
                {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    //... put other settings in the Intent
                    startActivityForResult(intent, 0);
                }
                else if (item.getItemId() == R.id.action_camera)
                {
                    Intent intent = new Intent(mContext, CameraActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
                else
                {

                }

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

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMessageEvent(UpdateMainEvent event) {
        Log.d("MainActivity", "Update Received");
        UpdateOnAdd(event);
    }

    public void UpdateOnAdd(UpdateMainEvent event)
    {
        if (event.getAction().equals(Constants.RECEIVED)) {

            Integer noteID = event.getID();
            DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
            NotesItem note = dbHelper.GetNote(noteID);
            AbstractFlexibleItem item = null;
            if (note.getNotesNote() != null)
            {
                Log.d("Note", "Note Item");
                item = new NoteItemViewholder(Integer.toString(noteID), note.getNotesNote());
            }
            else if (note.getNotesImage() != null)
            {
                Log.d("Image View Holder", note.getNotesImage());
                item = new ImageItemViewholder(Integer.toString(noteID), note.getNotesImage());
            }
            mAdapter.addItem(0, item);
            mStaggeredLayoutManager.scrollToPosition(0);
            UpdateMainEvent stickyEvent = EventBus.getDefault().getStickyEvent(UpdateMainEvent.class);
            if(stickyEvent != null) {
                EventBus.getDefault().removeStickyEvent(stickyEvent);
            }
            Log.d("Broadcast Receiver", Constants.RECEIVED);
        }
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

    @Override
    protected void onResume() {
        //Listen for new messages received
        Log.d("LocalBroadcastManager", "onResume");
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == 0 && resultCode == RESULT_OK) {
            ArrayList<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            mSearchView.setSearchFocused(true);
            mSearchView.setSearchText(results.get(0));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        mSearchView.setTranslationY(verticalOffset);
    }

    protected StaggeredGridLayoutManager createNewStaggeredGridLayoutManager() {
        return new SmoothScrollStaggeredLayoutManager(this, 2);
    }

    public List<IFlexible> getDatabaseList() {
        List<IFlexible> list = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        ArrayList<NotesItem> notesItems = dbHelper.GetNotesDate();
        Log.d("NotesItem Size", String.valueOf(notesItems.size()));
        for (NotesItem note : notesItems)
        {
            if (note.getNotesNote() != null)
            {
                Log.d("Note", "Note Item");
                list.add(new NoteItemViewholder(Integer.toString(note.getNotesID()), note.getNotesNote()));
            }
            else if (note.getNotesImage() != null)
            {
                Log.d("Image View Holder", note.getNotesImage());
                list.add(new ImageItemViewholder(Integer.toString(note.getNotesID()), note.getNotesImage()));
            }
        }
//        for (int i = 0; i < 50; i++)
//        {
//            list.add(new NoteItemViewholder(Integer.toString(i), "Wow, this is a great gig."));
//        }
        return list;
    }
}
