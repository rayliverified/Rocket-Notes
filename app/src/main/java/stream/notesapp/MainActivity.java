package stream.notesapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

import java.util.ArrayList;
import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollStaggeredLayoutManager;
import eu.davidea.flexibleadapter.items.IFlexible;

public class MainActivity extends Activity implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "Search";
    private SharedPreferences sharedPref;
    private RecyclerView mRecyclerView;
    private FloatingSearchView mSearchView;
    private AppBarLayout mAppBar;
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
        FlexibleAdapter<IFlexible> adapter = new FlexibleAdapter<IFlexible>(myItems);

        // Prepare the RecyclerView and attach the Adapter to it
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(createNewStaggeredGridLayoutManager());
        mRecyclerView.setAdapter(adapter);

        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
        setupSearchBar();
    }

    private void setupSearchBar() {
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
                    mSearchView.showProgress();

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

//                if (item.getItemId() == R.id.action_change_colors) {
//
//                    mIsDarkSearchTheme = true;
//
//                    //demonstrate setting colors for items
//                    mSearchView.setBackgroundColor(Color.parseColor("#787878"));
//                    mSearchView.setViewTextColor(Color.parseColor("#e9e9e9"));
//                    mSearchView.setHintTextColor(Color.parseColor("#e9e9e9"));
//                    mSearchView.setActionMenuOverflowColor(Color.parseColor("#e9e9e9"));
//                    mSearchView.setMenuItemIconColor(Color.parseColor("#e9e9e9"));
//                    mSearchView.setLeftActionIconColor(Color.parseColor("#e9e9e9"));
//                    mSearchView.setClearBtnColor(Color.parseColor("#e9e9e9"));
//                    mSearchView.setDividerColor(Color.parseColor("#BEBEBE"));
//                    mSearchView.setLeftActionIconColor(Color.parseColor("#e9e9e9"));
//                } else {
//
//                    //just print action
//                    Toast.makeText(getActivity().getApplicationContext(), item.getTitle(),
//                            Toast.LENGTH_SHORT).show();
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
        for (NotesItem note : notesItems)
        {
            list.add(new NoteItemViewholder(Integer.toString(note.getNotesID()), note.getNotesNote()));
        }
        for (int i = 0; i < 100; i++)
        {
            list.add(new NoteItemViewholder(Integer.toString(i), "Wow, this is a great gig."));
        }
        return list;
    }
}
