package stream.rocketnotes.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class PermissionUtils {

    public static boolean IsPermissionEnabled(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= 23) {
            return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public static boolean IsPermissionsEnabled(Context context, String[] permissionList) {
        for (String permission : permissionList) {
            if (!IsPermissionEnabled(context, permission)) {
                return false;
            }
        }

        return true;
    }

    public static Boolean isAppInstalled(Context context, String appName) {
        PackageManager pm = context.getPackageManager();
        boolean installed;
        try {
            pm.getPackageInfo(appName, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }
}
