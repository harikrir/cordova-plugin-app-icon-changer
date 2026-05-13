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

        // ✅ Support both formats:
        // { iconName: "dark" } OR "dark"
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

                    String selected = iconName.toLowerCase();

                    // ✅ STEP 1: Disable ALL aliases first (prevents duplicates)
                    disableAlias(pm, packageName, "Light");
                    disableAlias(pm, packageName, "Dark");
                    disableAlias(pm, packageName, "Private");

                    // ✅ STEP 2: Decide which one to enable
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

                    // ✅ STEP 3: Enable ONLY selected alias
                    enableAlias(pm, packageName, targetAlias);

                    // ✅ STEP 4: Delay slightly (important for launcher refresh)
              new android.os.Handler().postDelayed(new Runnable() {
    @Override
    public void run() {

        callbackContext.success("Icon changed to " + iconName);

        // ✅ Fix: go to home instead of restarting app
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

    }
}, 1200); // slightly higher delay improves stability

                } catch (Exception e) {
                    callbackContext.error(e.getMessage());
                }
            }
        });
    }

    // ✅ Enable alias
    private void enableAlias(PackageManager pm, String pkg, String alias) {
        ComponentName component = new ComponentName(pkg, pkg + "." + alias);

        pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );
    }

    // ✅ Disable alias
    private void disableAlias(PackageManager pm, String pkg, String alias) {
        ComponentName component = new ComponentName(pkg, pkg + "." + alias);

        pm.setComponentEnabledSetting(
                component,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );
    }

    // ✅ Restart app (critical to avoid duplicate icons)
    private void restartApp(Context context) {

        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());

        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }

        // Kill current process cleanly
        Runtime.getRuntime().exit(0);
    }
}
``
