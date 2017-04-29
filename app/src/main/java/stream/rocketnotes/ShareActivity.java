package stream.rocketnotes;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

public class ShareActivity extends Activity {

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

        View view = getLayoutInflater().inflate(R.layout.activity_share, null);
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
//        initializeAnalytics();
        noteStatus = getIntent().getAction();

//        //Focus defaults to editText, set again just in case
//        editText = (EditText) findViewById(R.id.edit_edit);
//        editText.requestFocus();
//        //OnEditorActionListener and OnKeyListener to detect keypresses DO NOT WORK on softkeyboards
//
//        LinearLayout editNote = (LinearLayout) findViewById(R.id.edit_note);
//        final LinearLayout editEditLayout = (LinearLayout) findViewById(R.id.edit_edit_layout);
//        TextView editDetails = (TextView) findViewById(R.id.edit_details);
//        final TextView editTitle = (TextView) findViewById(R.id.edit_title);
//        final TextView editBody = (TextView) findViewById(R.id.edit_body);
//        ImageButton editSubmit = (ImageButton) findViewById(R.id.edit_submit);
//        final TextView editHelper = (TextView) findViewById(R.id.edit_helper);
    }
}
