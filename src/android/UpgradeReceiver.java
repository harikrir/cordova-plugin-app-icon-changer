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

            Log.d(TAG, "Application upgraded. Restoring MainActivity and normalizing aliases...");

            try {

                PackageManager pm = context.getPackageManager();

                String pkg = context.getPackageName();

                // 1. Always keep MainActivity ENABLED so OutSystems cache manifest & RedirectActivity work

                ComponentName mainActivity = new ComponentName(pkg, pkg + ".MainActivity");

                pm.setComponentEnabledSetting(

                        mainActivity,

                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,

                        PackageManager.DONT_KILL_APP

                );

                // 2. Check which alias the user was using previously

                String activeAlias = null;

                for (String alias : ALIASES) {

                    ComponentName comp = new ComponentName(pkg, pkg + "." + alias);

                    if (pm.getComponentEnabledSetting(comp) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {

                        activeAlias = alias;

                        break;

                    }

                }

                // Fallback to Light if none was explicitly enabled

                if (activeAlias == null) {

                    activeAlias = "Light";

                }

                // 3. Enable ONLY the active alias and disable the other two

                for (String alias : ALIASES) {

                    ComponentName comp = new ComponentName(pkg, pkg + "." + alias);

                    pm.setComponentEnabledSetting(

                            comp,

                            alias.equalsIgnoreCase(activeAlias)

                                    ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED

                                    : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,

                            PackageManager.DONT_KILL_APP

                    );

                }

                Log.d(TAG, "Upgrade complete. Active icon: " + activeAlias);

            } catch (Exception e) {

                Log.e(TAG, "Error during upgrade icon normalization: " + e.getMessage(), e);

            }

        }

    }

}
 
