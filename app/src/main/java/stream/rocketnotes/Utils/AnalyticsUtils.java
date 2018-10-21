package stream.rocketnotes.utils;

import android.app.Application;

import com.pyze.android.Pyze;
import com.pyze.android.PyzeEvents;

import java.util.HashMap;

import stream.rocketnotes.Constants;

public class AnalyticsUtils {

    public static void InitializeAnalytics(Application application) {
        if (Constants.ANALYTICS_ENABLED) {
            Pyze.initialize(application);
//        UXCam.startWithKey(Constants.UXCAM_API_KEY);
//        UXCam.occludeSensitiveScreen(true);
        }
    }

    public static void AnalyticEvent(String event) {
        if (Constants.ANALYTICS_ENABLED) {
            //UXCam
//            UXCam.addTagWithProperties(event);
            //Pyze
            PyzeEvents.postCustomEvent(event);
        }
    }

    public static void AnalyticEvent(String activity, String object, String value) {
        if (Constants.ANALYTICS_ENABLED) {
            //UXCam
//            UXCam.addTagWithProperties(activity, params);
            //Pyze
            HashMap<String, Object> attributes = new HashMap<>();
            attributes.put(object, String.valueOf(value));
            PyzeEvents.postCustomEventWithAttributes(activity, attributes);
        }
    }
}
