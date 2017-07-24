package stream.rocketnotes;

import android.app.Activity;
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
import android.support.design.internal.NavigationMenu;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
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
import com.flurry.android.FlurryAgent;
import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;
import com.pyze.android.Pyze;
import com.pyze.android.PyzeEvents;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollStaggeredLayoutManager;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.flexibleadapter.items.IFlexible;
import io.github.yavski.fabspeeddial.FabSpeedDial;
import io.github.yavski.fabspeeddial.SimpleMenuListenerAdapter;
import stream.rocketnotes.filter.FilterMaterialSearchView;
import stream.rocketnotes.filter.model.Filter;
import stream.rocketnotes.utils.AnalyticsUtils;
import stream.rocketnotes.utils.FileUtils;

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
    FilterMaterialSearchView mFilterView;
    private Integer mNoteCount;
    private Integer mImageCount;
    private String mActivity = "MainActivity";
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        initializeAnalytics();
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
//        checkVoiceRecognition();
        mFilterView = (FilterMaterialSearchView) findViewById(R.id.sv);
        setupSearchBar();
        setupFAB();
        if (getIntent().getAction() != null && getIntent().getAction() != Intent.ACTION_MAIN)
        {
            Log.d("MainActivity", getIntent().getAction());
            if (getIntent().getAction().equals(Constants.STICKY))
            {

            }
        }
        else
        {
            Log.d("MainActivity", "Clear Sticky");
            UpdateMainEvent stickyEvent = EventBus.getDefault().getStickyEvent(UpdateMainEvent.class);
            if(stickyEvent != null) {
                EventBus.getDefault().removeStickyEvent(stickyEvent);
            }
        }
        sessionDetails();
        Log.d("MainActivity", "onCreate");
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
    }

    private void setupSearchBar() {

        mSearchView = (FloatingSearchView) findViewById(R.id.floating_search_view);
//        mActionVoice = (MenuItem) findViewById(R.id.action_voice);
        mActionCamera = (MenuItem) findViewById(R.id.action_camera);

        mSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {

            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {

                if (!oldQuery.equals("") && newQuery.equals("")) {
                    mSearchView.clearSuggestions();
                    FilterReset(true);
                } else {
                    if (mAdapter.hasNewSearchText(newQuery)) {
                        Log.d(TAG, "onQueryTextChange newText: " + newQuery);
                        mAdapter.setSearchText(newQuery);
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

//                if (item.getItemId() == R.id.action_voice)
//                {
//                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//                    //... put other settings in the Intent
//                    startActivityForResult(intent, 0);
//                }
                if (item.getItemId() == R.id.action_camera)
                {
                    AnalyticsUtils.AnalyticEvent(mActivity, "Click", "Camera");
                    Intent intent = new Intent(mContext, CameraActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
                else if (item.getItemId() == R.id.filter_image)
                {
                    AnalyticsUtils.AnalyticEvent(mActivity, "Click", "Filter Image");
                    FilterImages();
                }
                else if (item.getItemId() == R.id.filter_text)
                {
                    AnalyticsUtils.AnalyticEvent(mActivity, "Click", "Filter Text");
                    FilterText();
                }
                else if (item.getItemId() == R.id.action_backup_sql)
                {
                    AnalyticsUtils.AnalyticEvent(mActivity, "Click", "Backup Database");
                    DialogProperties properties = new DialogProperties();
                    properties.selection_mode = DialogConfigs.SINGLE_MODE;
                    properties.selection_type = DialogConfigs.DIR_SELECT;
                    properties.root = new File("/mnt/sdcard/");
                    properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                    properties.offset = new File(DialogConfigs.DEFAULT_DIR);
                    properties.extensions = null;
                    FilePickerDialog dialog = new FilePickerDialog(MainActivity.this, properties);
                    dialog.setTitle("Choose Backup Location");
                    dialog.setDialogSelectionListener(new DialogSelectionListener() {
                        @Override
                        public void onSelectedFilePaths(String[] files) {
                            if (files.length >= 1)
                            {
                                BackupDatabase(files[0]);
                            }
                            else
                            {
                                Toasty.error(mContext, "No Location Selected", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    dialog.show();
                }
                else if (item.getItemId() == R.id.action_restore_sql)
                {
                    AnalyticsUtils.AnalyticEvent(mActivity, "Click", "Restore Database");
                    DialogProperties properties = new DialogProperties();
                    properties.selection_mode = DialogConfigs.SINGLE_MODE;
                    properties.selection_type = DialogConfigs.FILE_SELECT;
                    properties.root = new File("/mnt/sdcard/");
                    properties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
                    properties.offset = new File(DialogConfigs.DEFAULT_DIR);
                    properties.extensions = new String[]{"zip"};
                    FilePickerDialog dialog = new FilePickerDialog(MainActivity.this, properties);
                    dialog.setTitle("Choose NotesDB Backup");
                    dialog.setDialogSelectionListener(new DialogSelectionListener() {
                        @Override
                        public void onSelectedFilePaths(String[] files) {
                            if (files.length >= 1)
                            {
                                RestoreDatabase(files[0]);
                            }
                            else
                            {
                                Toasty.error(mContext, "No File Selected", Toast.LENGTH_SHORT).show();
                            }
                            for (String filePath : files)
                            {
                                Log.d("File Path", filePath);
                            }
                        }
                    });
                    dialog.show();
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

    public void setupFAB()
    {
        FabSpeedDial fabSpeedDial = (FabSpeedDial) findViewById(R.id.main_fab);
        fabSpeedDial.setMenuListener(new SimpleMenuListenerAdapter() {
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
                }
                else if (menuItem.getItemId() == R.id.action_camera) {
                    Log.d("FAB", "Camera");
                    AnalyticsUtils.AnalyticEvent(mActivity, "Click", "New Image");
                    Intent intent = new Intent(mContext, CameraActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                    return true;
                }
                return false;
            }
            @Override
            public boolean onPrepareMenu(NavigationMenu navigationMenu) {
                return true;
            }
        });
    }

    public void FilterImages()
    {
        Filter filter = new Filter(1, "Image", 0, R.drawable.icon_picture_image, ContextCompat.getColor(mContext, R.color.colorPrimary));
        mFilterView.setVisibility(View.VISIBLE);
        mFilterView.addFilter(filter);
        List<IFlexible> list = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        ArrayList<NotesItem> notesItems = dbHelper.GetImageNotes();
        Log.d("NotesItem Size", String.valueOf(notesItems.size()));
        for (NotesItem note : notesItems)
        {
            list.add(new ImageItemViewholder(Integer.toString(note.getNotesID()), note.getNotesImage()));
        }
        mAdapter.updateDataSet(list);
        RemoveSticky();
        Log.d("Filter", "Images");
    }

    public void FilterText()
    {
        Filter filter = new Filter(1, "Text", 0, R.drawable.icon_rocket_image, ContextCompat.getColor(mContext, R.color.colorPrimary));
        mFilterView.setVisibility(View.VISIBLE);
        mFilterView.addFilter(filter);
        List<IFlexible> list = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        ArrayList<NotesItem> notesItems = dbHelper.GetTextNotes();
        Log.d("NotesItem Size", String.valueOf(notesItems.size()));
        for (NotesItem note : notesItems)
        {
            list.add(new NoteItemViewholder(Integer.toString(note.getNotesID()), note.getNotesNote()));
        }
        mAdapter.updateDataSet(list);
        Log.d("Filter", "Text");
    }

    public void FilterReset(boolean clearSticky)
    {
        List<IFlexible> list = getDatabaseList();
        mAdapter.updateDataSet(list);
        //Do not clear sticky if filtering image notes
        if (clearSticky)
            RemoveSticky();
        Log.d("Filter", "Reset");
    }

    public void BackupDatabase(String savePath)
    {
        try {
            //Make sure Pictures folder exists. User could have no picture notes.
            FileUtils.InitializePicturesFolder(mContext);

            //Copy notes database to Pictures folder.
            final String inFileName = mContext.getDatabasePath("NotesDB").getPath();
            File dbFile = new File(inFileName);
            FileInputStream fis = new FileInputStream(dbFile);
            String outFileName = getFilesDir() + "/" + ".Pictures/" + "NotesDB.db";
            // Open the empty db as the output stream
            OutputStream output = new FileOutputStream(outFileName);
            // Transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer))>0){
                output.write(buffer, 0, length);
            }
            // Close the streams
            output.flush();
            output.close();
            fis.close();

            //Zip Pictures folder and save to user specified location.
            File storageDir = new File(getFilesDir(), ".Pictures");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String currentDate = sdf.format(new Date());
            String saveFilePath = savePath + "/" + "RocketNotes_" + currentDate + ".zip";
            ZipUtil.pack(storageDir, new File(saveFilePath));

            Toasty.success(mContext, "Backup Successful", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toasty.error(mContext, "Backup Failed", Toast.LENGTH_SHORT).show();
        }
    }

    public void RestoreDatabase(String restorePath)
    {
        //Make sure Pictures folder exists. User could have no picture notes.
        FileUtils.InitializePicturesFolder(mContext);
        //Restoring backup requires NotesDB. If no NotesDB found, backup file is not valid.
        boolean validBackup = ZipUtil.containsEntry(new File(restorePath), "NotesDB.db");
        if (validBackup)
        {
            ZipUtil.unpackEntry(new File(restorePath), "NotesDB.db", new File(mContext.getDatabasePath("NotesDB").getPath()));
            ZipUtil.unpack(new File(restorePath), new File(getFilesDir(), ".Pictures"));
            File file = new File(getFilesDir(), ".Pictures/NotesDB.db");
            file.delete();
            Toasty.success(mContext, "Backup Restored", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toasty.error(mContext, "Invalid Backup File", Toast.LENGTH_SHORT).show();
        }

        FilterReset(true);
    }

    public void UpdateOnAdd(UpdateMainEvent event)
    {
        Integer noteID = event.getID();
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        NotesItem note = dbHelper.GetNote(noteID);
        AbstractFlexibleItem item = null;
        if (note.getNotesImage() != null)
        {
            Log.d("Image View Holder", note.getNotesImage());
            item = new ImageItemViewholder(Integer.toString(noteID), note.getNotesImage());
        }
        else if (note.getNotesNote() != null)
        {
            Log.d("Note", "Note Item");
            item = new NoteItemViewholder(Integer.toString(noteID), note.getNotesNote());
        }
        mAdapter.addItem(0, item);
        mStaggeredLayoutManager.scrollToPosition(0);
        RemoveSticky();
        Log.d("Broadcast Receiver", Constants.RECEIVED);
    }

    public void UpdateOnUpdate(UpdateMainEvent event)
    {
        Integer noteID = event.getID();
        DatabaseHelper dbHelper = new DatabaseHelper(getApplicationContext());
        NotesItem note = dbHelper.GetNote(noteID);
        AbstractFlexibleItem item = null;
        if (note.getNotesImage() != null)
        {
            Log.d("Image View Holder", note.getNotesImage());
            item = new ImageItemViewholder(Integer.toString(noteID), note.getNotesImage());
        }
        else if (note.getNotesNote() != null)
        {
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

    public void UpdateOnDelete(UpdateMainEvent event)
    {
        NotesItem note = event.getNotesItem();
        AbstractFlexibleItem item = null;
        if (note.getNotesImage() != null)
        {
            Log.d("Image View Holder", note.getNotesImage());
            item = new ImageItemViewholder(Integer.toString(note.getNotesID()), note.getNotesImage());
        }
        else if (note.getNotesNote() != null)
        {
            Log.d("Note", "Note Item");
            item = new NoteItemViewholder(Integer.toString(note.getNotesID()), note.getNotesNote());
        }
        Integer position = mAdapter.getGlobalPositionOf(item);
        Log.d("Item Position", String.valueOf(position));
        mAdapter.removeItem(position);
        RemoveSticky();
        Log.d("Broadcast Receiver", Constants.DELETE_NOTE);
    }


    public void RemoveSticky()
    {
        UpdateMainEvent stickyEvent = EventBus.getDefault().getStickyEvent(UpdateMainEvent.class);
        if(stickyEvent != null) {
            EventBus.getDefault().removeStickyEvent(stickyEvent);
        }
    }

    public void UpdateFilter(UpdateMainEvent event)
    {
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
        if (event.getAction().equals(Constants.RECEIVED)) {
            UpdateOnAdd(event);
        }
        else if (event.getAction().equals(Constants.UPDATE_NOTE))
        {
            UpdateOnUpdate(event);
        }
        else if (event.getAction().equals(Constants.DELETE_NOTE))
        {
            UpdateOnDelete(event);
        }
        else if (event.getAction().equals(Constants.FILTER))
        {
            UpdateFilter(event);
        }
        else if (event.getAction().equals(Constants.FILTER_IMAGES))
        {
            FilterImages();
        }
    }

    @Override
    protected void onResume() {
        //Listen for new messages received
        Log.d("LocalBroadcastManager", "onResume");
        //Sometimes, multiple changes are made to mAdapter and the entire view should be refreshed.
        if (sharedPref.getBoolean(Constants.REFRESH, false)) {
            if (mAdapter != null)
            {
                Log.d("MainActivity", "Refresh");
                FilterReset(false);
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
        mNoteCount = 0;
        mImageCount = 0;
        Log.d("NotesItem Size", String.valueOf(notesItems.size()));
        for (NotesItem note : notesItems)
        {
            if (note.getNotesNote() != null)
            {
                mNoteCount += 1;
                list.add(new NoteItemViewholder(Integer.toString(note.getNotesID()), note.getNotesNote()));
            }
            else if (note.getNotesImage() != null)
            {
                mImageCount += 1;
                list.add(new ImageItemViewholder(Integer.toString(note.getNotesID()), note.getNotesImage()));
            }
        }
        return list;
    }

    public void sessionDetails()
    {
        Map<String, String> params = new HashMap<String, String>();
        HashMap <String, String> attributes = new HashMap<String, String>();

        int ids[] = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, ImageWidget.class));
        if (ids.length > 0) {
            params.put("Image Widget", String.valueOf(true));
            attributes.put("Image Widget", String.valueOf(true));
        } else {
            params.put("Image Widget", String.valueOf(false));
            attributes.put("Image Widget", String.valueOf(false));
        }
        ids = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, NotesWidget.class));
        if (ids.length > 0) {
            params.put("Notes Widget", String.valueOf(true));
            attributes.put("Notes Widget", String.valueOf(true));
        } else {
            params.put("Notes Widget", String.valueOf(false));
            attributes.put("Notes Widget", String.valueOf(false));
        }

        //Flurry
        params.put("Note Count", String.valueOf(mNoteCount));
        params.put("Image Count", String.valueOf(mImageCount));
        FlurryAgent.logEvent(mActivity, params);
        //Pyze
        attributes.put("Note Count", String.valueOf(mNoteCount));
        attributes.put("Image Count", String.valueOf(mImageCount));
        PyzeEvents.postCustomEventWithAttributes(mActivity, attributes);
    }

    public void initializeAnalytics()
    {
        if (!FlurryAgent.isSessionActive())
        {
            new FlurryAgent.Builder()
                    .withLogEnabled(true)
                    .build(this, Constants.FLURRY_API_KEY);
        };
        Pyze.initialize(getApplication());
//        UXCam.startWithKey(Constants.UXCAM_API_KEY);
    }
}
