package stream.notesapp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class MainActivity extends Activity {

    private SharedPreferences sharedPref;
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
    }
}
