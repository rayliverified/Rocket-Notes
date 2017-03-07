package stream.rocketnotes;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PopupActivity extends Activity {

    private boolean titleCreated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = getLayoutInflater().inflate(R.layout.activity_popup, null);
        setContentView(view);

        //Configure floating window
        Window window = getWindow();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(window.getAttributes()); //Inherit transparent window attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; //Floating window WRAPS_CONTENT by default. This forces full width window.
        lp.gravity = Gravity.TOP; //Pins floating window to top of screen
        getWindow().setAttributes(lp);

        //Flag allows window to overlap status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        //FLAG_NOT_TOUCH_MODAL passes through touch events to objects underneath view
        //FLAG_WATCH_OUTSIDE_TOUCH dismisses window when outside touch is detected
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH|WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Automatically opens keyboard for immediate input
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        //Focus defaults to editText, set again just in case
        final EditText editText = (EditText) findViewById(R.id.edit_edit);
        editText.requestFocus();
        //OnEditorActionListener and OnKeyListener to detect keypresses DO NOT WORK on softkeyboards

        LinearLayout editLayout = (LinearLayout) findViewById(R.id.edit_layout);
        TextView editDetails = (TextView) findViewById(R.id.edit_details);
        final TextView editTitle = (TextView) findViewById(R.id.edit_title);
        final TextView editBody = (TextView) findViewById(R.id.edit_body);
        ImageButton editSubmit = (ImageButton) findViewById(R.id.edit_submit);

        if (getIntent().getAction().equals(Constants.NEW_NOTE))
        {
            editText.setHint("Enter new note...");
            editDetails.setText("New Note • now");
            editTitle.setText("Note Title");
            editBody.setText("Note Body");
            editText.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {

                    String[] noteText = s.toString().split("\n", 2);
                    Log.d("Note Length", String.valueOf(noteText.length));
                    if (noteText.length == 2)
                    {
                        Log.d("Note Body", noteText[1]);
                        if (!TextUtils.isEmpty(noteText[1]))
                        {
                            editBody.setText(noteText[1]);
                        }
                        else
                        {
                            editBody.setText("Note Body");
                        }
                    }
                    else
                    {
                        editBody.setText("Note Body");
                        editBody.setVisibility(View.GONE);
                        titleCreated = false;
                    }
                    editTitle.setText(noteText[0]);

                    //Reset Note Title when EditText is empty
                    if (TextUtils.isEmpty(s.toString()))
                    {
                        Log.d("Note Empty", "True");
                        editTitle.setText("Note Title");
                    }
                    Log.d("Note Title", noteText[0]);
                }

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    if (s.length() < 1 || start >= s.length() || start < 0)
                    {
                        return;
                    }

                    //Detect enter key presses
                    if (s.subSequence(start, start + 1).toString().equalsIgnoreCase("\n")) {
                        Log.d("Key", "Enter");

                        if (start == 0)
                        {
                            finish();
                        }

                        if (titleCreated == false)
                        {
                            editBody.setVisibility(View.VISIBLE);
                            titleCreated = true;
                        }
                        else
                        {
                            //Save note and close activity
                            Intent saveNote = new Intent(getApplicationContext(), SaveNoteService.class);
                            saveNote.putExtra(Constants.BODY, editText.getText().toString().trim());
                            saveNote.setAction(Constants.NEW_NOTE);
                            getApplicationContext().startService(saveNote);
                            finish();
                        }
                    }
                }
            });
            editSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TextUtils.isEmpty(editText.getText().toString().trim()))
                    {
                        //Save note and close activity
                        Intent saveNote = new Intent(getApplicationContext(), SaveNoteService.class);
                        saveNote.putExtra(Constants.BODY, editText.getText().toString().trim());
                        saveNote.setAction(Constants.NEW_NOTE);
                        getApplicationContext().startService(saveNote);
                    }
                    finish();
                }
            });
        }
        else if (getIntent().getAction().equals(Constants.OPEN_NOTE))
        {
            final Integer noteID = getIntent().getIntExtra("NOTEID", 0);
            Log.d("Received Note ID", String.valueOf(noteID));

            DatabaseHelper dbHelper = new DatabaseHelper(this);
            NotesItem note = dbHelper.GetNote(noteID);
            String noteTime = stream.rocketnotes.Utils.TextUtils.getTimeStamp(note.getNotesDate());

            editText.setHint("Add to note...");
            editDetails.setText("Update Note • " + noteTime);
            final String noteTextRaw = note.getNotesNote();
            ArrayList<String> noteText = NoteHelper.getNote(noteTextRaw);
            editTitle.setText(noteText.get(0));
            if (!TextUtils.isEmpty(noteText.get(1)))
            {
                editBody.setText(noteText.get(1));
                editBody.setVisibility(View.VISIBLE);
            }
            else
            {
                editBody.setVisibility(View.GONE);
            }
            editText.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {

                }

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    if (s.length() < 1 || start >= s.length() || start < 0)
                    {
                        return;
                    }

                    //Detect enter key presses
                    if (s.subSequence(start, start + 1).toString().equalsIgnoreCase("\n")) {
                        Log.d("Key", "Enter");

                        if (start == 0)
                        {
                            finish();
                        }
                        else
                        {
                            //Save note and close activity
                            Intent saveNote = new Intent(getApplicationContext(), SaveNoteService.class);
                            saveNote.putExtra(Constants.ID, noteID);
                            saveNote.putExtra(Constants.BODY, noteTextRaw + "\n" + editText.getText().toString().trim());
                            saveNote.setAction(Constants.UPDATE_NOTE);
                            getApplicationContext().startService(saveNote);
                            finish();
                        }
                    }
                }
            });
            editSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!TextUtils.isEmpty(editText.getText().toString().trim()))
                    {
                        //Save note and close activity
                        Intent saveNote = new Intent(getApplicationContext(), SaveNoteService.class);
                        saveNote.putExtra(Constants.ID, noteID);
                        saveNote.putExtra(Constants.BODY, noteTextRaw + "\n" + editText.getText().toString().trim());
                        saveNote.setAction(Constants.UPDATE_NOTE);
                        getApplicationContext().startService(saveNote);
                    }
                    finish();
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        Log.d("Key", "Back");
        finish();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If we've received a touch notification that the user has touched
        // outside the app, finish the activity.
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            finish();
            return true;
        }

        // Delegate everything else to Activity.
        return super.onTouchEvent(event);
    }
}
