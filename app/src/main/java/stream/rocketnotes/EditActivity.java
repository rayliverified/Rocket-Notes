package stream.rocketnotes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.pyze.android.Pyze;

import es.dmoral.toasty.Toasty;
import jp.wasabeef.richeditor.RichEditor;
import stream.rocketnotes.service.DeleteNoteService;
import stream.rocketnotes.service.SaveNoteService;
import stream.rocketnotes.utils.AnalyticsUtils;

public class EditActivity extends AppCompatActivity {

    private RichEditor mEditor;
    private String noteStatus; //OPEN_NOTE or NEW_NOTE flag.
    private Integer noteID;
    private String noteTextRaw;
    private boolean originalNew = false; //Current note is new and just created
    private boolean deletedNote = false;
    private boolean savedNote = false; //Has note already been saved? If so, treat as OPEN_NOTE
    private boolean overrideExit = false; //Skip onPause autosave and exit cleanly.

    private String mActivity = this.getClass().getSimpleName();
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        ActionBar();
        Window window = getWindow();
        mContext = getApplicationContext();
        InitializeAnalytics();
        noteStatus = getIntent().getAction();
        Log.d("Intent Action", noteStatus);

        mEditor = findViewById(R.id.editor);
        mEditor.setPadding(12, 12, 12, 12);
        noteTextRaw = "";
        noteID = -1;

        if (noteStatus.equals(Constants.OPEN_NOTE)) {
            AnalyticsUtils.AnalyticEvent(mActivity, "Note Type", Constants.OPEN_NOTE);

            savedNote = true;

            mEditor.clearFocus(); //Clear default focus so keyboard does not appear automatically.
            noteID = getIntent().getIntExtra(Constants.ID, -1);
            Log.d("Received Note ID", String.valueOf(noteID));
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            NotesItem note = dbHelper.GetNote(noteID);
            noteTextRaw = stream.rocketnotes.utils.TextUtils.Compatibility(note.getNotesNote());

            //Intent contains extra string if note opened from PopupActivity and user has typed text.
            if (!TextUtils.isEmpty(getIntent().getStringExtra(Constants.BODY))) {
                //Append what the user was typing to existing note.
                String editAdd = getIntent().getStringExtra(Constants.BODY);
                editAdd = "<br>" + editAdd;
                mEditor.setHtml(noteTextRaw + editAdd);
                //Automatically opens keyboard for immediate input
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                //User was adding to note so open keyboard for additional input.
                mEditor.focusEditor();
            } else {
                mEditor.setHtml(noteTextRaw);
            }
        } else {
            AnalyticsUtils.AnalyticEvent(mActivity, "Note Type", Constants.NEW_NOTE);

            originalNew = true;

            //Intent contains extra string if note opened from PopupActivity and user started typing new note.
            if (!TextUtils.isEmpty(getIntent().getStringExtra(Constants.BODY))) {
                noteTextRaw = stream.rocketnotes.utils.TextUtils.Compatibility(getIntent().getStringExtra(Constants.BODY));
                mEditor.setHtml(noteTextRaw);
            }

            //Automatically opens keyboard for immediate input
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            mEditor.focusEditor();
            mEditor.setOnTextChangeListener(new RichEditor.OnTextChangeListener() {

                @Override
                public void onTextChange(String text) {

                }
            });
        }
    }

    @Override
    protected void onPause() {
        //Do not automatically save note if user deletes note or exits properly.
        if (!overrideExit) {
            if (!deletedNote && !savedNote) {
                //Note has not been deleted and never saved before.
                //Autosave note when window loses focus
                Log.d("Edit Text", "Autosaved");
                Log.d("onPause", String.valueOf(savedNote));
                saveNote();
            } else if (!deletedNote && savedNote == true) {
                //Update existing note in database.
                DatabaseHelper dbHelper = new DatabaseHelper(this);
                noteStatus = Constants.OPEN_NOTE;
                noteID = dbHelper.GetLatestID(); //New note always has the latest ID.
                Log.d("Autosaved Note ID", String.valueOf(noteID));
                saveNote();
            }
        }
        super.onPause();
    }

    @Override
    protected void onStart() {
        //Update noteTextRaw to newest saved value
        if (mEditor.getHtml() != null) {
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
            case R.id.action_share:
                Log.d("Edit Text", "Share");
                openShareIntent();
                break;
            case R.id.action_copy:
                Log.d("Edit Text", "Share");
                openCopyIntent();
                break;
            case R.id.action_delete:
                Log.d("Edit Text", "Delete");
                overrideExit = true;
                openDeleteIntent();
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

    private void saveNote() {
        //Save note and close activity
        if (!TextUtils.isEmpty(stream.rocketnotes.utils.TextUtils.Clean(mEditor.getHtml())) && !noteTextRaw.equals(stream.rocketnotes.utils.TextUtils.Clean(mEditor.getHtml()))) {
            Intent saveNote = new Intent(mContext, SaveNoteService.class);
            saveNote.putExtra(Constants.BODY, stream.rocketnotes.utils.TextUtils.Clean(mEditor.getHtml()));
            if (noteStatus.equals(Constants.OPEN_NOTE)) {
                Log.d(mActivity, Constants.UPDATE_NOTE);
                saveNote.setAction(Constants.UPDATE_NOTE);
                saveNote.putExtra(Constants.ID, noteID);
                mContext.startService(saveNote);
            } else {
                Log.d(mActivity, Constants.NEW_NOTE);
                saveNote.setAction(Constants.NEW_NOTE);
                mContext.startService(saveNote);
                savedNote = true;
            }
        } else if (TextUtils.isEmpty(stream.rocketnotes.utils.TextUtils.Clean(mEditor.getHtml()))) {
            //Note is empty. Delete note if saved in database.
            openDeleteIntent();
        }
    }

    private void openShareIntent() {

        if (!TextUtils.isEmpty(stream.rocketnotes.utils.TextUtils.Clean(mEditor.getHtml()))) {
            stream.rocketnotes.utils.TextUtils.Share(mContext, mEditor.getHtml());
        } else {
            Toasty.error(mContext, "Note Empty", Toast.LENGTH_SHORT, true).show();
        }
    }

    private void openCopyIntent() {

        if (!TextUtils.isEmpty(stream.rocketnotes.utils.TextUtils.Clean(mEditor.getHtml()))) {
            stream.rocketnotes.utils.TextUtils.CopyText(mContext, mEditor.getHtml());
            Toasty.normal(mContext, "Copied", Toast.LENGTH_SHORT).show();
        } else {
            Toasty.error(mContext, "Note Empty", Toast.LENGTH_SHORT, true).show();
        }
    }

    private void openDeleteIntent() {
        if (savedNote == false && originalNew == true) {
            //Note has not been saved and no database methods need to be called. Finish activity.
            Log.d(mActivity, "Delete Unsaved Note");
        } else if (savedNote == true && originalNew == true) {
            //New note has been saved. Must get saved ID and pass to DeleteNote.
            Log.d(mActivity, "Delete New Note");
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            noteID = dbHelper.GetLatestID();
            DeleteNote();
        } else if (savedNote == true) {
            //Delete existing note.
            DeleteNote();
        }
        finish();
    }

    public void DeleteNote() {
        Intent deleteNote = new Intent(mContext, DeleteNoteService.class);
        deleteNote.putExtra(Constants.ID, noteID);
        deleteNote.setAction(Constants.DELETE_NOTE);
        mContext.startService(deleteNote);
        deletedNote = true;
        Log.d("Notification", Constants.DELETE_NOTE);
    }

    private void ActionBar() {
        ActionBar toolBar = getSupportActionBar();
        if (toolBar != null) {
            toolBar.setDisplayHomeAsUpEnabled(true);
            toolBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            toolBar.setCustomView(R.layout.toolbar_edit);
            toolBar.setElevation(0);
            Toolbar parent = (Toolbar) toolBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0, 0);
        }

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.editpage_title);

        ImageView toolbarBack = findViewById(R.id.toolbar_save);
        toolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(mActivity, "Back");
                overrideExit = true;
                saveNote();
                finish();
            }
        });
    }

    public void InitializeAnalytics() {
        if (FlurryAgent.isSessionActive() == false) {
            new FlurryAgent.Builder()
                    .withLogEnabled(true)
                    .build(this, Constants.FLURRY_API_KEY);
        }
        Pyze.initialize(getApplication());
//        UXCam.startWithKey(Constants.UXCAM_API_KEY);
//        UXCam.occludeSensitiveScreen(true);
    }
}
