package com.cordova.changeappicon;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
public class UpgradeReceiver extends BroadcastReceiver {
   private static final String TAG = "AppIconUpgradeReceiver";
   private static final String[] ALIASES = {"Light", "Dark", "Private"};
   @Override
   public void onReceive(Context context, Intent intent) {
       if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
           Log.d(TAG, "Application package upgraded. Sanitizing components...");
           try {
               PackageManager pm = context.getPackageManager();
               String pkg = context.getPackageName();
               // 1. Identify if customer was previously running a custom icon
               String activeAlias = null;
               for (String alias : ALIASES) {
                   ComponentName comp = new ComponentName(pkg, pkg + "." + alias);
                   if (pm.getComponentEnabledSetting(comp) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                       activeAlias = alias;
                       break;
                   }
               }
               ComponentName mainActivity = new ComponentName(pkg, pkg + ".MainActivity");
               if (activeAlias != null) {
                   // Customer was on Light, Dark, or Private: Keep only that alias enabled
                   for (String alias : ALIASES) {
                       ComponentName comp = new ComponentName(pkg, pkg + "." + alias);
                       pm.setComponentEnabledSetting(
                               comp,
                               alias.equalsIgnoreCase(activeAlias)
                                       ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                                       : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                               PackageManager.DONT_KILL_APP
                       );
                   }
                   pm.setComponentEnabledSetting(
                           mainActivity,
                           PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                           PackageManager.DONT_KILL_APP
                   );
               } else {
                   // Customer was on OutSystems default icon: Keep MainActivity enabled
                   pm.setComponentEnabledSetting(
                           mainActivity,
                           PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                           PackageManager.DONT_KILL_APP
                   );
                   for (String alias : ALIASES) {
                       ComponentName comp = new ComponentName(pkg, pkg + "." + alias);
                       pm.setComponentEnabledSetting(
                               comp,
                               PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                               PackageManager.DONT_KILL_APP
                       );
                   }
               }
               Log.d(TAG, "Upgrade reconciliation complete.");
           } catch (Exception e) {
               Log.e(TAG, "Failed to sanitize components on upgrade: " + e.getMessage(), e);
           }
       }
   }
}
