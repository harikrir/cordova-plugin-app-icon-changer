package com.cordova.changeappicon;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

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

        String iconName = args.getString(0);
        changeIcon(iconName, callbackContext);
        return true;
    }

    private void changeIcon(String iconName, CallbackContext callbackContext) {
        try {
            Context context = cordova.getActivity();
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();

            // Disable MAIN activity
            ComponentName mainActivity =
                new ComponentName(packageName, packageName + ".MainActivity");

            pm.setComponentEnabledSetting(
                mainActivity,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
            );

            setAlias(pm, packageName, "Light", "light".equals(iconName));
            setAlias(pm, packageName, "Dark", "dark".equals(iconName));
            setAlias(pm, packageName, "Private", "private".equals(iconName));

            callbackContext.success("Icon changed to " + iconName);

        } catch (Exception e) {
            callbackContext.error(e.getMessage());
        }
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
