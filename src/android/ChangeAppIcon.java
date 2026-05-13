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

        // ✅ Support object input: { iconName: "dark" }
        String iconName;
        if (args.get(0) instanceof JSONObject) {
            iconName = args.getJSONObject(0).optString("iconName", "light");
        } else {
            iconName = args.getString(0);
        }

        changeIcon(iconName, callbackContext);
        return true;
    }

    private void changeIcon(final String iconName, final CallbackContext callbackContext) {

        cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Context context = cordova.getActivity();
                    PackageManager pm = context.getPackageManager();
                    String packageName = context.getPackageName();

                    // ✅ NEVER disable MainActivity ❌ (FIXED)

                    // ✅ Always ensure ONE alias is enabled
                    boolean isLight = "light".equalsIgnoreCase(iconName);
                    boolean isDark = "dark".equalsIgnoreCase(iconName);
                    boolean isPrivate = "private".equalsIgnoreCase(iconName);

                    // ✅ fallback → Light if invalid input
                    if (!isLight && !isDark && !isPrivate) {
                        isLight = true;
                    }

                    setAlias(pm, packageName, "Light", isLight);
                    setAlias(pm, packageName, "Dark", isDark);
                    setAlias(pm, packageName, "Private", isPrivate);

                    callbackContext.success("Icon changed to " + iconName);

                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    private void setAlias(PackageManager pm, String packageName, String alias, boolean enable) {
        ComponentName component =
                new ComponentName(packageName, packageName + "." + alias);

        pm.setComponentEnabledSetting(
                component,
                enable
                        ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
    }
}
