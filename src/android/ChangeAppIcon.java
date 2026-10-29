package com.cordova.changeappicon;

import android.app.Activity;

import android.app.Application;

import android.content.ComponentName;

import android.content.Context;

import android.content.pm.PackageManager;

import android.os.Bundle;

import android.os.Handler;

import android.os.Looper;

import android.util.Log;

import org.apache.cordova.CordovaPlugin;

import org.apache.cordova.CallbackContext;

import org.json.JSONArray;

import org.json.JSONException;

import org.json.JSONObject;

public class ChangeAppIcon extends CordovaPlugin {

    private static final String TAG = "ChangeAppIcon";

    private static final String ACTION_CHANGE_ICON = "changeIcon";

    public static final String[] ALIASES = {"Dark", "Private"};

    @Override

    protected void pluginInitialize() {

        super.pluginInitialize();

        registerResumeCleanup();

    }

    /**

     * When MainActivity resumes after a deep link, check if an alias (Dark/Private)

     * is active. If so, re-disable MainActivity so the second icon disappears.

     */

    private void registerResumeCleanup() {

        try {

            Application app = cordova.getActivity().getApplication();

            app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {

                @Override

                public void onActivityResumed(Activity activity) {

                    if (activity.getClass().getName().contains("MainActivity")) {

                        sanitizeMainActivityLauncher(activity);

                    }

                }

                @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

                @Override public void onActivityStarted(Activity activity) {}

                @Override public void onActivityPaused(Activity activity) {}

                @Override public void onActivityStopped(Activity activity) {}

                @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

                @Override public void onActivityDestroyed(Activity activity) {}

            });

        } catch (Exception e) {

            Log.e(TAG, "Error registering resume listener: " + e.getMessage());

        }

    }

    private void sanitizeMainActivityLauncher(Context context) {

        try {

            PackageManager pm = context.getPackageManager();

            String pkg = context.getPackageName();

            // Check if Dark or Private is currently active

            boolean isAliasActive = false;

            for (String alias : ALIASES) {

                ComponentName comp = new ComponentName(pkg, pkg + "." + alias);

                if (pm.getComponentEnabledSetting(comp) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {

                    isAliasActive = true;

                    break;

                }

            }

            // If user is on Dark or Private, hide MainActivity again so only 1 icon remains

            if (isAliasActive) {

                ComponentName mainActivity = new ComponentName(pkg, pkg + ".MainActivity");

                pm.setComponentEnabledSetting(

                        mainActivity,

                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,

                        PackageManager.DONT_KILL_APP

                );

                Log.d(TAG, "MainActivity suppressed after deep link resume. Single icon maintained.");

            }

        } catch (Exception e) {

            Log.e(TAG, "Error sanitizing launcher: " + e.getMessage());

        }

    }

    @Override

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext)

            throws JSONException {

        if (!ACTION_CHANGE_ICON.equals(action)) {

            callbackContext.error("Unsupported action");

            return false;

        }

        if (args == null || args.length() == 0) {

            callbackContext.error("Missing icon name");

            return true;

        }

        String iconName;

        if (args.get(0) instanceof JSONObject) {

            iconName = args.getJSONObject(0).optString("iconName", "Light");

        } else {

            iconName = args.getString(0);

        }

        changeIcon(iconName, callbackContext);

        return true;

    }

    private synchronized void changeIcon(String iconName, CallbackContext callbackContext) {

        try {

            Activity activity = cordova.getActivity();

            Context context = activity.getApplicationContext();

            PackageManager pm = context.getPackageManager();

            String pkg = context.getPackageName();

            String selectedIcon = capitalize(iconName);

            ComponentName mainActivity = new ComponentName(pkg, pkg + ".MainActivity");

            if ("Light".equalsIgnoreCase(selectedIcon) || "Default".equalsIgnoreCase(selectedIcon)) {

                // LIGHT (OutSystems Default)

                pm.setComponentEnabledSetting(

                        mainActivity,

                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,

                        PackageManager.DONT_KILL_APP

                );

                for (String alias : ALIASES) {

                    ComponentName comp = new ComponentName(pkg, pkg + "." + alias);

                    pm.setComponentEnabledSetting(

                            comp,

                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,

                            PackageManager.DONT_KILL_APP

                    );

                }

                selectedIcon = "Light";

            } else {

                // DARK or PRIVATE

                pm.setComponentEnabledSetting(

                        mainActivity,

                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,

                        PackageManager.DONT_KILL_APP

                );

                for (String alias : ALIASES) {

                    if (!alias.equalsIgnoreCase(selectedIcon)) {

                        ComponentName comp = new ComponentName(pkg, pkg + "." + alias);

                        pm.setComponentEnabledSetting(

                                comp,

                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,

                                PackageManager.DONT_KILL_APP

                        );

                    }

                }

                ComponentName targetAlias = new ComponentName(pkg, pkg + "." + selectedIcon);

                pm.setComponentEnabledSetting(

                        targetAlias,

                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,

                        PackageManager.DONT_KILL_APP

                );

            }

            callbackContext.success("Icon changed to " + selectedIcon);

            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {

                @Override

                public void run() {

                    try {

                        if (activity != null && !activity.isFinishing()) {

                            activity.finishAffinity();

                        }

                        android.os.Process.killProcess(android.os.Process.myPid());

                        System.exit(0);

                    } catch (Exception ex) {

                        Log.e(TAG, "Error closing app: " + ex.getMessage());

                    }

                }

            }, 300);

        } catch (Exception e) {

            Log.e(TAG, "Error changing icon: " + e.getMessage(), e);

            callbackContext.error("Error changing icon: " + e.getMessage());

        }

    }

    private String capitalize(String value) {

        if (value == null || value.length() == 0) return value;

        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();

    }

}
 
