
package com.cordova.changeappicon;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;

public class UpgradeReceiver extends BroadcastReceiver {

    private static final String TAG = "AppIconUpgradeReceiver";

    private static final String PREFS_NAME = "AppIconChanger";
    private static final String PREF_ACTIVE_ICON = "active_icon";

    private static final String[] ICON_ALIASES = {
        ".Light",
        ".Dark",
        ".Private"
    };

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent == null ||
            !Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            return;
        }

        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();

        try {
            // 1. Always enable MainActivity for OutSystems deep links.
            ComponentName mainActivity = new ComponentName(
                packageName,
                packageName + ".MainActivity"
            );

            pm.setComponentEnabledSetting(
                mainActivity,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            );

            // 2. Restore the previously selected icon.
            SharedPreferences prefs = context.getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            );

            String selectedAlias = prefs.getString(
                PREF_ACTIVE_ICON,
                null
            );

            // 3. For existing customers without saved preferences,
            // detect which alias was explicitly enabled in the old app.
            if (!isValidAlias(selectedAlias)) {
                selectedAlias = getPreviouslyEnabledAlias(pm, packageName);
            }

            if (!isValidAlias(selectedAlias)) {
                selectedAlias = ".Light";
            }

            // 4. Enable selected alias first.
            ComponentName selectedComponent = new ComponentName(
                packageName,
                packageName + selectedAlias
            );

            pm.setComponentEnabledSetting(
                selectedComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            );

            // 5. Disable the other aliases.
            for (String alias : ICON_ALIASES) {

                if (alias.equals(selectedAlias)) {
                    continue;
                }

                ComponentName component = new ComponentName(
                    packageName,
                    packageName + alias
                );

                pm.setComponentEnabledSetting(
                    component,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
                );
            }

            // 6. Save the selected alias for future upgrades.
            prefs.edit()
                .putString(PREF_ACTIVE_ICON, selectedAlias)
                .apply();

            Log.i(TAG, "Upgrade completed. MainActivity enabled. Icon: "
                    + selectedAlias);

        } catch (Exception e) {
            Log.e(TAG, "Error restoring app components after upgrade", e);
        }
    }

    private String getPreviouslyEnabledAlias(
            PackageManager pm, String packageName) {

        for (String alias : ICON_ALIASES) {

            ComponentName component = new ComponentName(
                packageName,
                packageName + alias
            );

            int state = pm.getComponentEnabledSetting(component);

            if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                return alias;
            }
        }

        return ".Light";
    }

    private boolean isValidAlias(String alias) {

        if (alias == null) {
            return false;
        }

        for (String item : ICON_ALIASES) {
            if (item.equals(alias)) {
                return true;
            }
        }

        return false;
    }
}
