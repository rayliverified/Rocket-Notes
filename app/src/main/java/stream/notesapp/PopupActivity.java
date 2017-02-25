package stream.notesapp;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class PopupActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View view = getLayoutInflater().inflate(R.layout.activity_popup, null);
        setContentView(view);

        Window window = getWindow();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(window.getAttributes()); //Inherit transparent window attributes
        lp.width = WindowManager.LayoutParams.MATCH_PARENT; //Floating window WRAPS_CONTENT by default. This forces full width window.
        lp.gravity = Gravity.TOP; //Pins floating window to top of screen
        getWindow().setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Flag allows window to overlap status bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        }

        //FLAG_NOT_TOUCH_MODAL passes through touch events to objects underneath view
        //FLAG_WATCH_OUTSIDE_TOUCH dismisses window when outside touch is detected
        window.addFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
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
