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
   public static final String[] ALIASES = {"Light", "Dark", "Private"};
   @Override
   protected void pluginInitialize() {
       super.pluginInitialize();
       // Guarantee MainActivity is ALWAYS enabled so cache manifest and deep links never fail
       ensureMainActivityEnabled();
   }
   private void ensureMainActivityEnabled() {
       try {
           Context context = cordova.getActivity();
           PackageManager pm = context.getPackageManager();
           String pkg = context.getPackageName();
           ComponentName mainActivity = new ComponentName(pkg, pkg + ".MainActivity");
           if (pm.getComponentEnabledSetting(mainActivity) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
               pm.setComponentEnabledSetting(
                       mainActivity,
                       PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                       PackageManager.DONT_KILL_APP
               );
           }
       } catch (Exception e) {
           Log.e(TAG, "Failed to ensure MainActivity enabled: " + e.getMessage());
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
           if ("Default".equalsIgnoreCase(selectedIcon) || selectedIcon.isEmpty()) {
               selectedIcon = "Light";
           }
           // 1. Ensure MainActivity stays ENABLED permanently
           ensureMainActivityEnabled();
           ComponentName targetAlias = new ComponentName(pkg, pkg + "." + selectedIcon);
           // If already active, exit without restarting
           if (pm.getComponentEnabledSetting(targetAlias) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
               callbackContext.success("Icon already set to " + selectedIcon);
               return;
           }
           // 2. Enable target alias first
           pm.setComponentEnabledSetting(
                   targetAlias,
                   PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                   PackageManager.DONT_KILL_APP
           );
           // 3. Disable the other two aliases
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
           callbackContext.success("Icon changed to " + selectedIcon);
           // 4. Close cleanly after 400ms to refresh launcher cache in-place
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
           }, 400);
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
