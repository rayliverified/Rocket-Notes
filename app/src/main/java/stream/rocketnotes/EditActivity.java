package stream.rocketnotes;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;

public class EditActivity extends AppCompatActivity {

    private EditText editText;
    private String noteStatus;
    private Integer noteID;
    private String noteTextRaw;
    private boolean originalNew = false;
    private boolean deletedNote = false;
    private boolean savedNote = false;
    private boolean overrideExit = false; //Skip onPause autosave and exit cleanly.
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        ActionBar();
        Window window = getWindow();
        mContext = getApplicationContext();

        noteStatus = getIntent().getAction();
        Log.d("Intent Action", noteStatus);

        //Focus defaults to editText, set again just in case
        editText = (EditText) findViewById(R.id.edit_edit);
        editText.requestFocus();
        noteTextRaw = "";
        noteID = -1;

        if (getIntent().getAction().equals(Constants.OPEN_NOTE))
        {
            noteID = getIntent().getIntExtra(Constants.ID, -1);
            Log.d("Received Note ID", String.valueOf(noteID));
            DatabaseHelper dbHelper = new DatabaseHelper(this);
            NotesItem note = dbHelper.GetNote(noteID);
            noteTextRaw = note.getNotesNote();

            String editAdd = "";
            if (!TextUtils.isEmpty(getIntent().getStringExtra(Constants.BODY)))
            {
                editAdd = getIntent().getStringExtra(Constants.BODY);
            }
            editText.setText(noteTextRaw + "\n" + editAdd);
            editText.clearFocus();
//            editText.addTextChangedListener(new TextWatcher() {
//
//                public void afterTextChanged(Editable s) {
//
//                }
//
//                public void beforeTextChanged(CharSequence s, int start,
//                                              int count, int after) {
//                }
//
//                public void onTextChanged(CharSequence s, int start,
//                                          int before, int count) {
//                    if (s.length() < 1 || start >= s.length() || start < 0)
//                    {
//                        return;
//                    }
//
//                    //Detect enter key presses
//                    if (s.subSequence(start, start + 1).toString().equalsIgnoreCase("\n")) {
//                        Log.d("Key", "Enter");
//
//                        if (start == 0)
//                        {
//                            finish();
//                        }
//                        else
//                        {
//                            //Save note and close activity
////                        Intent saveNote = new Intent(getApplicationContext(), SaveNoteService.class);
////                        saveNote.putExtra(Constants.ID, noteID);
////                        saveNote.putExtra(Constants.BODY, noteTextRaw + "\n" + editText.getText().toString().trim());
////                        saveNote.setAction(Constants.UPDATE_NOTE);
////                        getApplicationContext().startService(saveNote);
////                        finish();
//                        }
//                    }
//                }
//            });
//        editSubmit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!TextUtils.isEmpty(editText.getText().toString().trim()))
//                {
//                    //Save note and close activity
//                    Intent saveNote = new Intent(getApplicationContext(), SaveNoteService.class);
//                    saveNote.putExtra(Constants.ID, noteID);
//                    saveNote.putExtra(Constants.BODY, noteTextRaw + "\n" + editText.getText().toString().trim());
//                    saveNote.setAction(Constants.UPDATE_NOTE);
//                    getApplicationContext().startService(saveNote);
//                }
//                finish();
//            }
//        });
        }
        else
        {
            originalNew = true;

            //Automatically opens keyboard for immediate input
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            if (!TextUtils.isEmpty(getIntent().getStringExtra(Constants.BODY)))
            {
                editText.setText(getIntent().getStringExtra(Constants.BODY));
            }
            editText.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {

//                    String[] noteText = s.toString().split("\n", 2);
//                    if (noteText.length == 2)
//                    {
//                        Log.d("Note Body", noteText[1]);
//                        if (!TextUtils.isEmpty(noteText[1]))
//                        {
//                            editText.setText(noteText[0] + noteText[1]);
//                        }
//                        else
//                        {
//                            editText.setText(noteText[0]);
//                        }
//                    }
//                    else
//                    {
//                        if (!TextUtils.isEmpty(s.toString()))
//                        {
//                            editText.setText(noteText[0]);
//                        }
//                        else
//                        {
//                            //Reset Note Title when EditText is empty
//                            Log.d("Note Empty", "True");
//                            editText.setHint("Note Title");
//                        }
//                    }
                }

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
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
        noteTextRaw = editText.getText().toString().trim();

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
        if (!TextUtils.isEmpty(editText.getText().toString().trim()) && !noteTextRaw.equals(editText.getText().toString().trim()))
        {
            Intent saveNote = new Intent(mContext, SaveNoteService.class);
            if (noteStatus.equals(Constants.OPEN_NOTE))
            {
                Log.d("Edit Activity", Constants.UPDATE_NOTE);
                saveNote.putExtra(Constants.ID, noteID);
                saveNote.putExtra(Constants.BODY, editText.getText().toString().trim());
                saveNote.setAction(Constants.UPDATE_NOTE);
                mContext.startService(saveNote);
            }
            else
            {
                Log.d("Edit Activity", Constants.NEW_NOTE);
                saveNote.putExtra(Constants.BODY, editText.getText().toString().trim());
                saveNote.setAction(Constants.NEW_NOTE);
                mContext.startService(saveNote);
                savedNote = true;
            }
        }
        else if (TextUtils.isEmpty(editText.getText().toString().trim()))
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

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html){
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html,Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
    }
}
