
package com.cordova.changeappicon;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class ChangeAppIcon extends CordovaPlugin {

    private static final String ACTION_CHANGE_ICON = "changeIcon";

    private static final String[] ICONS = {
        "Light",
        "Dark",
        "Private"
    };

    @Override
    public boolean execute(
            String action,
            JSONArray args,
            CallbackContext callbackContext) throws JSONException {

        if (!ACTION_CHANGE_ICON.equals(action)) {
            callbackContext.error("Unsupported action: " + action);
            return false;
        }

        if (args == null || args.length() == 0) {
            callbackContext.error("Missing icon name");
            return true;
        }

        String iconName;

        if (args.opt(0) instanceof JSONObject) {
            iconName = args.getJSONObject(0).optString("iconName", "");
        } else {
            iconName = args.optString(0, "");
        }

        if (iconName == null || iconName.trim().isEmpty()) {
            callbackContext.error("Icon name cannot be empty");
            return true;
        }

        String selectedIcon = normalizeIconName(iconName);

        if (selectedIcon == null) {
            callbackContext.error(
                "Invalid icon name. Use Light, Dark, or Private."
            );
            return true;
        }

        changeIcon(selectedIcon, callbackContext);
        return true;
    }

    private void changeIcon(
            String selectedIcon,
            CallbackContext callbackContext) {

        try {
            Context context = cordova.getActivity();
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();

            /*
             * IMPORTANT:
             * Do not disable MainActivity.
             * Deep links and RedirectActivity depend on it.
             */

            // Enable the selected alias first.
            setAliasState(
                pm,
                packageName,
                selectedIcon,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            );

            // Disable the remaining launcher aliases.
            for (String icon : ICONS) {
                if (!icon.equals(selectedIcon)) {
                    setAliasState(
                        pm,
                        packageName,
                        icon,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                    );
                }
            }

            callbackContext.success(
                "Icon changed to " + selectedIcon
            );

        } catch (Exception e) {
            callbackContext.error(
                "Error changing icon: " + e.getMessage()
            );
        }
    }

    private void setAliasState(
            PackageManager pm,
            String packageName,
            String aliasName,
            int state) {

        ComponentName component = new ComponentName(
            packageName,
            packageName + "." + aliasName
        );

        pm.setComponentEnabledSetting(
            component,
            state,
            PackageManager.DONT_KILL_APP
        );
    }

    private String normalizeIconName(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        switch (normalized) {
            case "light":
                return "Light";

            case "dark":
                return "Dark";

            case "private":
                return "Private";

            default:
                return null;
        }
    }
}
