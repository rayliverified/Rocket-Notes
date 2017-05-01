package stream.rocketnotes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.pyze.android.Pyze;
import com.pyze.android.PyzeEvents;
import com.uxcam.UXCam;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.richeditor.RichEditor;
import stream.rocketnotes.service.DeleteNoteService;
import stream.rocketnotes.service.SaveNoteService;

public class EditActivity extends AppCompatActivity {

    private RichEditor mEditor;
    private String noteStatus;
    private Integer noteID;
    private String noteTextRaw;
    private boolean originalNew = false;
    private boolean deletedNote = false;
    private boolean savedNote = false;
    private boolean overrideExit = false; //Skip onPause autosave and exit cleanly.
    private MixpanelAPI mixpanel;
    private String mActivity = "EditActivity";
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        ActionBar();
        Window window = getWindow();
        mContext = getApplicationContext();
        initializeAnalytics();
        noteStatus = getIntent().getAction();
        Log.d("Intent Action", noteStatus);

        //Focus defaults to editText, set again just in case
        mEditor = (RichEditor) findViewById(R.id.editor);
        mEditor.setPadding(12, 12, 12, 12);
        noteTextRaw = "";
        noteID = -1;

        if (getIntent().getAction().equals(Constants.OPEN_NOTE))
        {
            AnalyticEvent("Note Type", Constants.OPEN_NOTE);

            mEditor.clearFocus();
            noteID = getIntent().getIntExtra(Constants.ID, -1);
            Log.d("Received Note ID", String.valueOf(noteID));
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            NotesItem note = dbHelper.GetNote(noteID);
            noteTextRaw = stream.rocketnotes.utils.TextUtils.Compatibility(note.getNotesNote());

            String editAdd = "";
            if (!TextUtils.isEmpty(getIntent().getStringExtra(Constants.BODY)))
            {
                editAdd = getIntent().getStringExtra(Constants.BODY);
                editAdd = "<br>" + editAdd;
                mEditor.setHtml(noteTextRaw + editAdd);
                //Automatically opens keyboard for immediate input
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                mEditor.focusEditor();
            }
            else
            {
                mEditor.setHtml(noteTextRaw + editAdd);
            }
        }
        else
        {
            AnalyticEvent("Note Type", Constants.NEW_NOTE);

            originalNew = true;

            if (!TextUtils.isEmpty(getIntent().getStringExtra(Constants.BODY)))
            {
                noteTextRaw = stream.rocketnotes.utils.TextUtils.Compatibility(getIntent().getStringExtra(Constants.BODY));
                mEditor.setHtml(noteTextRaw);
            }

            //Automatically opens keyboard for immediate input
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            mEditor.focusEditor();
            mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener(){

                @Override
                public void onTextChange(String text) {

                }
            });
        }
    }

    @Override
    protected void onPause() {
        if (!overrideExit)
        {
            if (!deletedNote && !savedNote)
            {
                //Autosave note when window loses focus
                Log.d("Edit Text", "Autosaved");
                Log.d("onPause", String.valueOf(savedNote));
                saveNote();
            }
            else if (!deletedNote && savedNote == true)
            {
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                noteStatus = Constants.OPEN_NOTE;
                noteID = dbHelper.GetLatestID();
                Log.d("Autosaved Note ID", String.valueOf(noteID));
                saveNote();
            }
        }
        super.onPause();
    }

    @Override
    protected void onStart() {
        //Update noteTextRaw to newest saved value
        if (mEditor.getHtml() != null)
        {
            noteTextRaw = mEditor.getHtml();
        }

        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_undo:
                Log.d("Edit Text", "Undo");
                mEditor.undo();
                break;
            case R.id.action_redo:
                Log.d("Edit Text", "Redo");
                mEditor.redo();
                break;
            case R.id.action_delete:
                Log.d("Edit Text", "Delete");
                overrideExit = true;
                openDeleteIntent();
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        overrideExit = true;
        saveNote();
        super.onBackPressed();
    }

    private void saveNote()
    {
        //Save note and close activity
        if (!TextUtils.isEmpty(stream.rocketnotes.utils.TextUtils.Clean(mEditor.getHtml())) && !noteTextRaw.equals(stream.rocketnotes.utils.TextUtils.Clean(mEditor.getHtml())))
        {
            Intent saveNote = new Intent(mContext, SaveNoteService.class);
            if (noteStatus.equals(Constants.OPEN_NOTE))
            {
                Log.d("Edit Activity", Constants.UPDATE_NOTE);
                saveNote.putExtra(Constants.ID, noteID);
                saveNote.putExtra(Constants.BODY, stream.rocketnotes.utils.TextUtils.Clean(mEditor.getHtml()));
                saveNote.setAction(Constants.UPDATE_NOTE);
                mContext.startService(saveNote);
            }
            else
            {
                Log.d("Edit Activity", Constants.NEW_NOTE);
                saveNote.putExtra(Constants.BODY, stream.rocketnotes.utils.TextUtils.Clean(mEditor.getHtml()));
                saveNote.setAction(Constants.NEW_NOTE);
                mContext.startService(saveNote);
                savedNote = true;
            }
        }
        else if (TextUtils.isEmpty(stream.rocketnotes.utils.TextUtils.Clean(mEditor.getHtml())))
        {
            openDeleteIntent();
        }
    }

    private void openDeleteIntent()
    {
        if (savedNote == false && originalNew == true)
        {
            return;
        }
        else if (savedNote == true && originalNew == true)
        {
            Log.d("Edit Activity", "Delete New Note");
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            noteID = dbHelper.GetLatestID();
        }
        DeleteNote();
    }

    public void DeleteNote()
    {
        Intent deleteNote = new Intent(mContext, DeleteNoteService.class);
        deleteNote.putExtra(Constants.ID, noteID);
        deleteNote.setAction(Constants.DELETE_NOTE);
        mContext.startService(deleteNote);
        deletedNote = true;
        EventBus.getDefault().postSticky(new UpdateMainEvent(Constants.DELETE_NOTE));
        Log.d("Notification", Constants.DELETE_NOTE);
    }

    private void ActionBar() {
        ActionBar toolBar = getSupportActionBar();
        if (toolBar != null) {
            toolBar.setDisplayHomeAsUpEnabled(true);
            toolBar.setDisplayOptions(toolBar.DISPLAY_SHOW_CUSTOM);
            toolBar.setCustomView(R.layout.toolbar_default);
            toolBar.setElevation(0);
            Toolbar parent = (Toolbar) toolBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0,0);
        }

        TextView toolbarTitle = (TextView) findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.editpage_title);

        ImageView toolbarBack = (ImageView) findViewById(R.id.toolbar_save);
        toolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Edit Text", "Back");
                overrideExit = true;
                saveNote();
                finish();
            }
        });
    }

    public void initializeAnalytics()
    {
        if (FlurryAgent.isSessionActive() == false)
        {
            new FlurryAgent.Builder()
                    .withLogEnabled(true)
                    .build(this, Constants.FLURRY_API_KEY);
        }
        mixpanel = MixpanelAPI.getInstance(this, Constants.MIXPANEL_API_KEY);
        mixpanel.getPeople().identify(mixpanel.getDistinctId());
        Pyze.initialize(getApplication());
        UXCam.startWithKey(Constants.UXCAM_API_KEY);
        UXCam.occludeSensitiveScreen(true);
    }

    public void AnalyticEvent(String object, String value)
    {
        try {
            JSONObject mixObject = new JSONObject();
            mixObject.put(object, value);
            mixpanel.track(mActivity, mixObject);
        } catch (JSONException e) {
            Log.e(Constants.APP_NAME, "Unable to add properties to JSONObject", e);
        }
        //Flurry
        Map<String, String> params = new HashMap<String, String>();
        params.put(object, value);
        FlurryAgent.logEvent(mActivity, params);
        //UXCam
        UXCam.addTagWithProperties(mActivity, params);
        //Pyze
        HashMap <String, String> attributes = new HashMap<String, String>();
        attributes.put(object, String.valueOf(value));
        PyzeEvents.postCustomEventWithAttributes(mActivity, attributes);
    }
}
