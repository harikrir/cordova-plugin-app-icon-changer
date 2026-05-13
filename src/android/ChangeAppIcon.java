package com.cordova.changeappicon;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
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
            iconName = args.getJSONObject(0).optString("iconName", "light");
        } else {
            iconName = args.getString(0);
        }

        changeIcon(iconName, callbackContext);
        return true;
    }

    private void changeIcon(final String iconName, final CallbackContext callbackContext) {

        cordova.getActivity().runOnUiThread(() -> {
            try {
                Context context = cordova.getActivity();
                PackageManager pm = context.getPackageManager();
                String pkg = context.getPackageName();

                String selected = iconName.toLowerCase();

                switch (selected) {

                    case "dark":

                        // ✅ Enable target FIRST
                        pm.setComponentEnabledSetting(
                                new ComponentName(pkg, pkg + ".Dark"),
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP
                        );

                        // ✅ Disable others
                        pm.setComponentEnabledSetting(
                                new ComponentName(pkg, pkg + ".Light"),
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP
                        );

                        pm.setComponentEnabledSetting(
                                new ComponentName(pkg, pkg + ".Private"),
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP
                        );

                        break;


                    case "private":

                        pm.setComponentEnabledSetting(
                                new ComponentName(pkg, pkg + ".Private"),
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP
                        );

                        pm.setComponentEnabledSetting(
                                new ComponentName(pkg, pkg + ".Light"),
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP
                        );

                        pm.setComponentEnabledSetting(
                                new ComponentName(pkg, pkg + ".Dark"),
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP
                        );

                        break;


                    case "light":
                    default:

                        pm.setComponentEnabledSetting(
                                new ComponentName(pkg, pkg + ".Light"),
                                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                                PackageManager.DONT_KILL_APP
                        );

                        pm.setComponentEnabledSetting(
                                new ComponentName(pkg, pkg + ".Dark"),
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP
                        );

                        pm.setComponentEnabledSetting(
                                new ComponentName(pkg, pkg + ".Private"),
                                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                                PackageManager.DONT_KILL_APP
                        );

                        break;
                }

                // ✅ Delay helps launcher update cleanly
                new android.os.Handler().postDelayed(() -> {

                    callbackContext.success("Icon changed to " + iconName);

                    // ✅ Refresh launcher (avoid restart)
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                }, 800);

            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
        });
    }
}
