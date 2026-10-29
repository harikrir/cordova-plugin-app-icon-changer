package com.cordova.changeappicon;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

public class ChangeAppIcon {

    private static final String TAG = "ChangeAppIcon";

    private static final String PREFS_NAME = "AppIconChanger";
    private static final String PREF_ACTIVE_ICON = "active_icon";

    private static final String MAIN_ACTIVITY = ".MainActivity";

    private static final String[] ICON_ALIASES = {
        ".Light",
        ".Dark",
        ".Private"
    };

    public static void changeIcon(Context context, String iconName) {

        if (context == null || iconName == null) {
            Log.e(TAG, "Context or icon name is null");
            return;
        }

        String selectedAlias = getAlias(iconName);

        if (selectedAlias == null) {
            Log.e(TAG, "Invalid icon name: " + iconName);
            return;
        }

        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        try {
            // MainActivity must always remain enabled.
            ComponentName mainActivity = new ComponentName(
                packageName,
                packageName + MAIN_ACTIVITY
            );

            packageManager.setComponentEnabledSetting(
                mainActivity,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            );

            // Disable all aliases except the selected alias.
            for (String alias : ICON_ALIASES) {

                ComponentName component = new ComponentName(
                    packageName,
                    packageName + alias
                );

                int state = alias.equals(selectedAlias)
                    ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;

                packageManager.setComponentEnabledSetting(
                    component,
                    state,
                    PackageManager.DONT_KILL_APP
                );
            }

            // Save selected icon for upgrade recovery.
            SharedPreferences preferences = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            );

            preferences.edit()
                .putString(PREF_ACTIVE_ICON, selectedAlias)
                .apply();

            Log.i(TAG, "Icon changed successfully: " + selectedAlias);

        } catch (Exception e) {
            Log.e(TAG, "Error changing app icon", e);
        }
    }

    private static String getAlias(String iconName) {

        switch (iconName.toLowerCase()) {

            case "light":
                return ".Light";

            case "dark":
                return ".Dark";

            case "private":
                return ".Private";

            default:
                return null;
        }
    }
}
