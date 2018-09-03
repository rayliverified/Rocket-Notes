package stream.rocketnotes.utils;

import com.pyze.android.PyzeEvents;

import java.util.HashMap;

public class AnalyticsUtils {

    public static void AnalyticEvent(String event) {
        //UXCam
//        UXCam.addTagWithProperties(event);
        //Pyze
        PyzeEvents.postCustomEvent(event);
    }

    public static void AnalyticEvent(String activity, String object, String value) {
        //UXCam
//        UXCam.addTagWithProperties(activity, params);
        //Pyze
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(object, String.valueOf(value));
        PyzeEvents.postCustomEventWithAttributes(activity, attributes);
    }
}
