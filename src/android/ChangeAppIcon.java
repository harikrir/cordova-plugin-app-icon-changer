package com.cordova.changeappicon;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChangeAppIcon extends CordovaPlugin {

    private static final String TAG = "ChangeAppIcon";
    private static final String ACTION_CHANGE_ICON = "changeIcon";
    private static final String[] ALIASES = {"Light", "Dark", "Private"};

    @Override
    protected void pluginInitialize() {
        super.pluginInitialize();
        // Self-heal: ensure MainActivity is enabled if it was disabled by an earlier release
        ensureMainActivityEnabled();
    }

    private void ensureMainActivityEnabled() {
        try {
            Context context = cordova.getActivity();
            PackageManager pm = context.getPackageManager();
            String pkg = context.getPackageName();
            ComponentName mainActivity = new ComponentName(pkg, pkg + ".MainActivity");

            int state = pm.getComponentEnabledSetting(mainActivity);
            if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                pm.setComponentEnabledSetting(
                        mainActivity,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP
                );
                Log.d(TAG, "MainActivity healed from DISABLED to ENABLED.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking/healing MainActivity: " + e.getMessage());
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

    private void changeIcon(String iconName, CallbackContext callbackContext) {
        try {
            Context context = cordova.getActivity();
            PackageManager pm = context.getPackageManager();
            String pkg = context.getPackageName();

            String selectedIcon = capitalize(iconName);

            // Toggle ONLY aliases — leave MainActivity untouched!
            for (String alias : ALIASES) {
                if (alias.equalsIgnoreCase(selectedIcon)) {
                    enable(pm, pkg, alias);
                } else {
                    disable(pm, pkg, alias);
                }
            }

            callbackContext.success("Icon changed to " + selectedIcon);

        } catch (Exception e) {
            callbackContext.error("Error changing icon: " + e.getMessage());
        }
    }

    private void enable(PackageManager pm, String pkg, String name) {
        pm.setComponentEnabledSetting(
                new ComponentName(pkg, pkg + "." + name),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );
    }

    private void disable(PackageManager pm, String pkg, String name) {
        pm.setComponentEnabledSetting(
                new ComponentName(pkg, pkg + "." + name),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
    }

    private String capitalize(String value) {
        if (value == null || value.length() == 0) return value;
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }
}
