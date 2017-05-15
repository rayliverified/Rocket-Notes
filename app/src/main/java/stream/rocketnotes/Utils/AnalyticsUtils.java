package stream.rocketnotes.utils;

import com.flurry.android.FlurryAgent;
import com.pyze.android.PyzeEvents;
import com.uxcam.UXCam;

import java.util.HashMap;
import java.util.Map;

public class AnalyticsUtils {

    public static void AnalyticEvent(String event)
    {
        //Flurry
        FlurryAgent.logEvent(event);
        //UXCam
//        UXCam.addTagWithProperties(event);
        //Pyze
        PyzeEvents.postCustomEvent(event);
    }

    public static void AnalyticEvent(String activity, String object, String value)
    {
        //Flurry
        Map<String, String> params = new HashMap<String, String>();
        params.put(object, value);
        FlurryAgent.logEvent(activity, params);
        //UXCam
//        UXCam.addTagWithProperties(activity, params);
        //Pyze
        HashMap <String, String> attributes = new HashMap<String, String>();
        attributes.put(object, String.valueOf(value));
        PyzeEvents.postCustomEventWithAttributes(activity, attributes);
    }
}
