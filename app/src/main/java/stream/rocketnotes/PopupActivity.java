package stream.rocketnotes;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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

import com.flurry.android.FlurryAgent;
import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.pyze.android.Pyze;
import com.pyze.android.PyzeEvents;
import com.uxcam.UXCam;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PopupActivity extends Activity {

    private boolean titleCreated = false;
    private EditText editText;
    private String noteStatus;
    private String noteTextRaw;
    private Integer noteID;
    private boolean savedNote = false;
    private MixpanelAPI mixpanel;
    private String mActivity = "PopupActivity";
    private Context mContext;

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

        mContext = getApplicationContext();
        initializeAnalytics();
        noteStatus = getIntent().getAction();

        //Focus defaults to editText, set again just in case
        editText = (EditText) findViewById(R.id.edit_edit);
        editText.requestFocus();
        //OnEditorActionListener and OnKeyListener to detect keypresses DO NOT WORK on softkeyboards

        LinearLayout editNote = (LinearLayout) findViewById(R.id.edit_note);
        final LinearLayout editEditLayout = (LinearLayout) findViewById(R.id.edit_edit_layout);
        TextView editDetails = (TextView) findViewById(R.id.edit_details);
        final TextView editTitle = (TextView) findViewById(R.id.edit_title);
        final TextView editBody = (TextView) findViewById(R.id.edit_body);
        ImageButton editSubmit = (ImageButton) findViewById(R.id.edit_submit);
        final TextView editHelper = (TextView) findViewById(R.id.edit_helper);

//        UXCam.occludeSensitiveView(editNote);
//        UXCam.occludeSensitiveView(editText);

        if (getIntent().getAction().equals(Constants.NEW_NOTE))
        {
            AnalyticEvent("Note Type", Constants.NEW_NOTE);

            editText.setHint(R.string.edit_hint);
            editDetails.setText("New Note • now");
            editTitle.setText("Note Title");
            editBody.setText("Note Body");
            editNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    savedNote = true;
                    Intent editIntent = new Intent(mContext, EditActivity.class);
                    editIntent.putExtra(Constants.BODY, editText.getText().toString());
                    editIntent.setAction(Constants.NEW_NOTE);
                    editIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.startActivity(editIntent);
                }
            });
            editText.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {

                    String[] noteText = s.toString().split("\n", 2);
                    Log.d("Note Length", String.valueOf(noteText.length));
                    if (s.length() > 20 && s.length() < Constants.TITLE_LENGTH && noteText.length == 1)
                    {
                        String helperText = "(" + String.valueOf(Constants.TITLE_LENGTH - s.length()) + " characters remaining)";
                        editHelper.setText(helperText);
                    }
                    else
                    {
                        editHelper.setText(R.string.edit_helper_default);
                    }
                    if (noteText.length == 2)
                    {
                        editHelper.setText(R.string.edit_helper_save);
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
                        editHelper.setVisibility(View.GONE);
                        float scale = getResources().getDisplayMetrics().density;
                        int dpAsPixels = (int) (12*scale + 0.5f);
                        editEditLayout.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, dpAsPixels);
                    }
                    Log.d("Note Title", noteText[0]);
                }

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    if (s.length() >= 1)
                    {
                        float scale = getResources().getDisplayMetrics().density;
                        int dpAsPixels = (int) (12*scale + 0.5f);
                        editEditLayout.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, 0);
                        editHelper.setVisibility(View.VISIBLE);
                    }

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
                            savedNote = true;
                            saveNote();
                            finish();
                        }
                    }
                }
            });
            editSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    savedNote = true;
                    saveNote();
                    finish();
                }
            });
        }
        else if (getIntent().getAction().equals(Constants.OPEN_NOTE))
        {
            AnalyticEvent("Note Type", Constants.OPEN_NOTE);

            noteID = getIntent().getIntExtra(Constants.ID, -1);
            Log.d("Received Note ID", String.valueOf(noteID));

            DatabaseHelper dbHelper = new DatabaseHelper(this);
            NotesItem note = dbHelper.GetNote(noteID);
            String noteTime = stream.rocketnotes.utils.TextUtils.getTimeStamp(note.getNotesDate());

            editText.setHint("Add to note...");
            editDetails.setText("Update Note • " + noteTime);
            noteTextRaw = note.getNotesNote();
            ArrayList<String> noteText = NoteHelper.getNote(stream.rocketnotes.utils.TextUtils.Compatibility(noteTextRaw));
            editTitle.setText(stream.rocketnotes.utils.TextUtils.fromHtml(noteText.get(0)));
            editNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    savedNote = true;
                    Intent editIntent = new Intent(mContext, EditActivity.class);
                    editIntent.putExtra(Constants.ID, noteID);
                    editIntent.putExtra(Constants.BODY, editText.getText().toString());
                    editIntent.setAction(Constants.OPEN_NOTE);
                    editIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    mContext.startActivity(editIntent);
                }
            });
            if (!TextUtils.isEmpty(noteText.get(1)))
            {
                editBody.setText(stream.rocketnotes.utils.TextUtils.fromHtml(noteText.get(1)));
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
                            savedNote = true;
                            saveNote();
                            finish();
                        }
                    }
                }
            });
            editSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    savedNote = true;
                    saveNote();
                    finish();
                }
            });
        }
    }

    private void saveNote()
    {
        //Save note and close activity
        if (!TextUtils.isEmpty(editText.getText().toString().trim()))
        {
            Intent saveNote = new Intent(mContext, SaveNoteService.class);
            if (noteStatus.equals(Constants.NEW_NOTE))
            {
                saveNote.putExtra(Constants.BODY, editText.getText().toString().trim());
                saveNote.setAction(Constants.NEW_NOTE);
                mContext.startService(saveNote);
            }
            else if (noteStatus.equals(Constants.OPEN_NOTE))
            {
                saveNote.putExtra(Constants.ID, noteID);
                saveNote.putExtra(Constants.BODY, noteTextRaw + "<br>" + editText.getText().toString().trim());
                saveNote.setAction(Constants.UPDATE_NOTE);
                mContext.startService(saveNote);
            }
        }
    }

    @Override
    protected void onPause() {
        if (!savedNote)
        {
            //Autosave note when window loses focus
            Log.d("Popup", "Autosaved");
            savedNote = true;
            saveNote();
        }
        super.onPause();
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
        UXCam.addVerificationListener(new UXCam.OnVerificationListener() {
            @Override
            public void onVerificationSuccess() {
                //Tag Mixpanel events with UXCam recording URLS. Example:
                JSONObject eventProperties = new JSONObject();
                try {
                    eventProperties.put("UXCam: Session Recording link", UXCam.urlForCurrentSession());
                } catch (JSONException exception) {
                }
                mixpanel.track("UXCam Session URL", eventProperties);
                //Tag Mixpanel profile with UXCam user URLS. Example:
                mixpanel.getPeople().set("UXCam User URL", UXCam.urlForCurrentUser());
            }
            @Override
            public void onVerificationFailed(String errorMessage) {
            }
        });
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
