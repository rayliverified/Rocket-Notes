package stream.rocketnotes.utils;

import android.text.format.DateFormat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TextUtils {

    public TextUtils()
    {

    }

    public static String getTimeStamp(Long time) {

        String timestamp = "";

        SimpleDateFormat format;
        Calendar noteTime = Calendar.getInstance();
        noteTime.setTimeInMillis(time);
        Calendar now = Calendar.getInstance();
        String timeFormatString = "h:mm aa";
        long diff = now.getTimeInMillis() - time;
        long minutes = diff / 60000;
        long hours = minutes / 60;
        if (minutes <= 1)
            return "now";
        else if (hours < 1)
            return String.valueOf(minutes) + " minutes ago";
        else if (now.get(Calendar.YEAR) == noteTime.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == noteTime.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == noteTime.get(Calendar.DATE))
        {
            return "Today " + DateFormat.format(timeFormatString, noteTime);
        }
        else if (now.get(Calendar.YEAR) == noteTime.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == noteTime.get(Calendar.MONTH)
                && now.get(Calendar.DATE) - noteTime.get(Calendar.DATE) == 1)
        {
            return "Yesterday " + DateFormat.format(timeFormatString, noteTime);
        }

        format = new SimpleDateFormat("MMMM dd, h:mm a");
        format.setTimeZone(TimeZone.getDefault());
        timestamp = format.format(new Date(time));

        return timestamp;
    }

    public static String getTimeStampShort(Long time) {

        String timestamp = "";

        SimpleDateFormat format;
        Calendar noteTime = Calendar.getInstance();
        noteTime.setTimeInMillis(time);
        Calendar now = Calendar.getInstance();
        String timeFormatString = "h:mm aa";
        if (now.get(Calendar.YEAR) == noteTime.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == noteTime.get(Calendar.MONTH)
                && now.get(Calendar.DATE) == noteTime.get(Calendar.DATE))
        {
            return (String) DateFormat.format(timeFormatString, noteTime);
        }
        else if (now.get(Calendar.YEAR) == noteTime.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == noteTime.get(Calendar.MONTH)
                && now.get(Calendar.DATE) - noteTime.get(Calendar.DATE) == 1)
        {
            return "Yesterday";
        }

        format = new SimpleDateFormat("MMMM dd");
        format.setTimeZone(TimeZone.getDefault());
        timestamp = format.format(new Date(time));

        return timestamp;
    }
}
