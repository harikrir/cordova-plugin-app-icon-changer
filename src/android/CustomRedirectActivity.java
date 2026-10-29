package com.cordova.changeappicon;

import android.content.ComponentName;

import android.content.pm.PackageManager;

import android.os.Bundle;

import android.util.Log;

// Inherits from OutSystems RedirectActivity

public class CustomRedirectActivity extends com.outsystems.plugins.auth.RedirectActivity {

    private static final String TAG = "CustomRedirectActivity";

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        // Synchronously re-enable MainActivity BEFORE OutSystems tries to launch it!

        try {

            PackageManager pm = getPackageManager();

            String pkg = getPackageName();

            ComponentName mainActivity = new ComponentName(pkg, pkg + ".MainActivity");

            pm.setComponentEnabledSetting(

                    mainActivity,

                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,

                    PackageManager.DONT_KILL_APP

            );

            Log.d(TAG, "MainActivity successfully enabled before redirect execution.");

        } catch (Exception e) {

            Log.e(TAG, "Error enabling MainActivity in RedirectActivity: " + e.getMessage());

        }

        // OutSystems executes its normal explicit intent to MainActivity safely

        super.onCreate(savedInstanceState);

    }

}
 
