package com.cordova.changeappicon;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

public class UpgradeReceiver extends BroadcastReceiver {
    private static final String TAG = "AppIconUpgradeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            Log.d(TAG, "Package upgraded. Ensuring MainActivity is enabled...");
            try {
                PackageManager pm = context.getPackageManager();
                String pkg = context.getPackageName();

                // Explicitly re-enable MainActivity
                ComponentName mainActivity = new ComponentName(pkg, pkg + ".MainActivity");
                pm.setComponentEnabledSetting(
                        mainActivity,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                );
                Log.d(TAG, "MainActivity successfully re-enabled.");
            } catch (Exception e) {
                Log.e(TAG, "Failed to re-enable MainActivity on upgrade: " + e.getMessage());
            }
        }
    }
}
