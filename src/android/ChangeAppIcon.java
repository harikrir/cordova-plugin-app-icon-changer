package com.cordova.changeappicon;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
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
   public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
       if (!ACTION_CHANGE_ICON.equals(action)) {
           callbackContext.error("Unsupported action");
           return false;
       }
       String iconName = args.optJSONObject(0) != null ? args.optJSONObject(0).optString("iconName", "Light") : args.getString(0);
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
               // Revert to Light: Enable MainActivity, Disable all aliases
               pm.setComponentEnabledSetting(mainActivity, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
               for (String alias : ALIASES) {
                   pm.setComponentEnabledSetting(new ComponentName(pkg, pkg + "." + alias), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
               }
           } else {
               // Switch to Dark/Private: Enable Target Alias, Disable MainActivity
               for (String alias : ALIASES) {
                   pm.setComponentEnabledSetting(new ComponentName(pkg, pkg + "." + alias),
                       alias.equalsIgnoreCase(selectedIcon) ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                       PackageManager.DONT_KILL_APP);
               }
               pm.setComponentEnabledSetting(mainActivity, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
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
