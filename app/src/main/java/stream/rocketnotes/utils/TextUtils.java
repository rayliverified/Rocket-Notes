package stream.rocketnotes.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateFormat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TextUtils {

    public TextUtils() {

    }

    public static String Compatibility(String raw) {
        if (raw == null || raw == "") {
            return null;
        }
        String output = "";
        output = raw.replaceAll("\n", "<br>");
        return output;
    }

    public static String Clean(String raw) {
        if (raw == null || raw == "") {
            return null;
        }
        String output = "";
        output = raw.trim();
        output = output.replaceAll("&nbsp;", " ");
        if (output.length() >= 4) {
            if (output.substring(0, 4).equals("<br>")) {
                Log.d("Trimed", "Beginning");
                output = output.substring(4, output.length());
            }
        }
        if (output.length() >= 8) {
            if (output.substring(output.length() - 8, output.length()).equals("<br><br>")) {
                Log.d("Trimed", "End");
                output = output.substring(0, output.length() - 8);
            }
        }
        Log.d("Output", output);
        return output.trim();
    }

    public static String CleanShare(String raw) {
        String output = "";
        output = raw.replaceAll("<br>", "\n");
        return output;
    }

    public static void Share(Context context, String text) {
        String shareText = Clean(text);
        shareText = CleanShare(text);
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
        context.startActivity(Intent.createChooser(sharingIntent, "Share Note"));
    }

    public static void CopyText(Context context, String text) {
        String shareText = Clean(text);
        shareText = CleanShare(text);
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("text/plain", shareText);
        clipboard.setPrimaryClip(clip);
    }

    @SuppressWarnings("deprecation")
    public static Spanned fromHtml(String html) {
        Spanned result;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            result = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            result = Html.fromHtml(html);
        }
        return result;
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
                && now.get(Calendar.DATE) == noteTime.get(Calendar.DATE)) {
            return "Today " + DateFormat.format(timeFormatString, noteTime);
        } else if (now.get(Calendar.YEAR) == noteTime.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == noteTime.get(Calendar.MONTH)
                && now.get(Calendar.DATE) - noteTime.get(Calendar.DATE) == 1) {
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
                && now.get(Calendar.DATE) == noteTime.get(Calendar.DATE)) {
            return (String) DateFormat.format(timeFormatString, noteTime);
        } else if (now.get(Calendar.YEAR) == noteTime.get(Calendar.YEAR)
                && now.get(Calendar.MONTH) == noteTime.get(Calendar.MONTH)
                && now.get(Calendar.DATE) - noteTime.get(Calendar.DATE) == 1) {
            return "Yesterday";
        }

        format = new SimpleDateFormat("MMMM dd");
        format.setTimeZone(TimeZone.getDefault());
        timestamp = format.format(new Date(time));

        return timestamp;
    }
}
