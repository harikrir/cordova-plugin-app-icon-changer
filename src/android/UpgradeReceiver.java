package com.cordova.changeappicon;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

public class UpgradeReceiver extends BroadcastReceiver {

    private static final String TAG = "AppIconUpgradeReceiver";

    private static final String PREFS_NAME = "AppIconChanger";
    private static final String PREF_ACTIVE_ICON = "active_icon";

    private static final String MAIN_ACTIVITY = ".MainActivity";

    private static final String[] ICON_ALIASES = {
        ".Light",
        ".Dark",
        ".Private"
    };

    @Override
    public void onReceive(Context context, android.content.Intent intent) {

        if (intent == null ||
            !android.content.Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            return;
        }

        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();

        try {
            // Always keep MainActivity enabled for deep-link handling.
            ComponentName mainActivity = new ComponentName(
                packageName,
                packageName + MAIN_ACTIVITY
            );

            packageManager.setComponentEnabledSetting(
                mainActivity,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            );

            SharedPreferences preferences = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            );

            String selectedAlias = preferences.getString(
                PREF_ACTIVE_ICON,
                ".Light"
            );

            // Validate the saved alias.
            if (!isValidAlias(selectedAlias)) {
                selectedAlias = ".Light";
            }

            // Enable the selected alias and disable the others.
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

            Log.i(TAG, "App icon restored: " + selectedAlias);

        } catch (Exception e) {
            Log.e(TAG, "Error restoring app icon after upgrade", e);
        }
    }

    private boolean isValidAlias(String alias) {

        for (String item : ICON_ALIASES) {
            if (item.equals(alias)) {
                return true;
            }
        }

        return false;
    }
}
