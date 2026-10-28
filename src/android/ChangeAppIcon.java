package com.cordova.changeappicon;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChangeAppIcon extends CordovaPlugin {

    private static final String ACTION_CHANGE_ICON = "changeIcon";

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

            // Normalize icon name (light -> Light)
            String selectedIcon = capitalize(iconName);

            // ✅ IMPORTANT: Disable MAIN ACTIVITY (same as your first plugin)
            pm.setComponentEnabledSetting(
                    cordova.getActivity().getComponentName(),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP
            );

            // ✅ Disable ALL launcher aliases (brute force)
            disable(pm, pkg, "Light");
            disable(pm, pkg, "Dark");
            disable(pm, pkg, "Private");

            // ✅ Enable ONLY selected icon
            enable(pm, pkg, selectedIcon);

            callbackContext.success("Icon changed to " + selectedIcon);

        } catch (Exception e) {
            callbackContext.error("Error changing icon: " + e.getMessage());
        }
    }

    // ✅ Enable alias
    private void enable(PackageManager pm, String pkg, String name) {
        pm.setComponentEnabledSetting(
                new ComponentName(pkg, pkg + "." + name),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );
    }

    // ✅ Disable alias
    private void disable(PackageManager pm, String pkg, String name) {
        pm.setComponentEnabledSetting(
                new ComponentName(pkg, pkg + "." + name),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
    }

    // ✅ Helper: capitalize first letter
    private String capitalize(String value) {
        if (value == null || value.length() == 0) return value;
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }
}
