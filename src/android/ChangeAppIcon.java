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
                String packageName = context.getPackageName();

                String selected = iconName.toLowerCase();

                String targetAlias;

                switch (selected) {
                    case "dark":
                        targetAlias = "Dark";
                        break;
                    case "private":
                        targetAlias = "Private";
                        break;
                    case "light":
                    default:
                        targetAlias = "Light";
                        break;
                }

                // ✅ STEP 1: ENABLE target FIRST (VERY IMPORTANT)
                enableAlias(pm, packageName, targetAlias);

                // ✅ STEP 2: DELAY → let launcher register change
                new android.os.Handler().postDelayed(() -> {

                    // ✅ STEP 3: DISABLE others AFTER
                    disableOthers(pm, packageName, targetAlias);

                    callbackContext.success("Icon changed to " + iconName);

                    // ✅ STEP 4: Refresh launcher (better than restart)
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.addCategory(Intent.CATEGORY_HOME);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);

                }, 800); // optimal delay

            } catch (Exception e) {
                callbackContext.error(e.getMessage());
            }
        });
    }

    // ✅ Enable selected alias
    private void enableAlias(PackageManager pm, String pkg, String alias) {
        ComponentName component = new ComponentName(pkg, pkg + "." + alias);

        pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );
    }

    // ✅ Disable all others AFTER enabling
    private void disableOthers(PackageManager pm, String pkg, String activeAlias) {
        String[] aliases = {"Light", "Dark", "Private"};

        for (String alias : aliases) {
            if (!alias.equals(activeAlias)) {
                ComponentName component = new ComponentName(pkg, pkg + "." + alias);

                pm.setComponentEnabledSetting(
                        component,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                );
            }
        }
    }
}
