package com.cordova.changeappicon;

import android.content.BroadcastReceiver;

import android.content.ComponentName;

import android.content.Context;

import android.content.Intent;

import android.content.pm.PackageManager;

import android.util.Log;

public class UpgradeReceiver extends BroadcastReceiver {

    private static final String TAG = "AppIconUpgradeReceiver";

    private static final String[] ALIASES = {"Light", "Dark", "Private"};

    @Override

    public void onReceive(Context context, Intent intent) {

        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {

            Log.d(TAG, "Application package upgraded. Sanitizing MainActivity and Aliases...");

            try {

                PackageManager pm = context.getPackageManager();

                String pkg = context.getPackageName();

                // 1. Explicitly re-enable MainActivity so RedirectActivity and deep links work

                ComponentName mainActivity = new ComponentName(pkg, pkg + ".MainActivity");

                pm.setComponentEnabledSetting(

                        mainActivity,

                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,

                        PackageManager.DONT_KILL_APP

                );

                Log.d(TAG, "MainActivity restored to ENABLED.");

                // 2. Identify which alias the user was currently using

                String activeAlias = null;

                for (String alias : ALIASES) {

                    ComponentName comp = new ComponentName(pkg, pkg + "." + alias);

                    int state = pm.getComponentEnabledSetting(comp);

                    if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {

                        activeAlias = alias;

                        break;

                    }

                }

                // If no alias was explicitly enabled, fallback to default Light

                if (activeAlias == null) {

                    activeAlias = "Light";

                }

                // 3. Strictly enforce ONLY ONE enabled alias and disable all others

                for (String alias : ALIASES) {

                    ComponentName comp = new ComponentName(pkg, pkg + "." + alias);

                    if (alias.equalsIgnoreCase(activeAlias)) {

                        pm.setComponentEnabledSetting(

                                comp,

                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,

                                PackageManager.DONT_KILL_APP

                        );

                    } else {

                        pm.setComponentEnabledSetting(

                                comp,

                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,

                                PackageManager.DONT_KILL_APP

                        );

                    }

                }

                Log.d(TAG, "Cleanup completed successfully. Single active launcher alias: " + activeAlias);

            } catch (Exception e) {

                Log.e(TAG, "Failed to sanitize components on upgrade: " + e.getMessage(), e);

            }

        }

    }

}
 
