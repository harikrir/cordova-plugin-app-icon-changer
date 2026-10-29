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

            try {

                PackageManager pm = context.getPackageManager();

                String pkg = context.getPackageName();

                ComponentName mainActivity = new ComponentName(pkg, pkg + ".MainActivity");

                pm.setComponentEnabledSetting(

                        mainActivity,

                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,

                        PackageManager.DONT_KILL_APP

                );

                String activeAlias = null;

                for (String alias : ALIASES) {

                    ComponentName comp = new ComponentName(pkg, pkg + "." + alias);

                    if (pm.getComponentEnabledSetting(comp) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {

                        activeAlias = alias;

                        break;

                    }

                }

                if (activeAlias == null) {

                    activeAlias = "Light";

                }

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

            } catch (Exception e) {

                Log.e(TAG, "Error during upgrade icon normalization: " + e.getMessage(), e);

            }

        }

    }

}
 
