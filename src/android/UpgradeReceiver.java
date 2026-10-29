package com.cordova.changeappicon;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
public class UpgradeReceiver extends BroadcastReceiver {
   private static final String TAG = "AppIconUpgradeReceiver";
   @Override
   public void onReceive(Context context, Intent intent) {
       if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
           Log.d(TAG, "Application updated. Re-enabling MainActivity...");
           try {
               PackageManager pm = context.getPackageManager();
               String pkg = context.getPackageName();
               // Explicitly re-enable MainActivity so explicit intents / deep links work immediately
               ComponentName mainActivity = new ComponentName(pkg, pkg + ".MainActivity");
               pm.setComponentEnabledSetting(
                       mainActivity,
                       PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                       PackageManager.DONT_KILL_APP
               );
               Log.d(TAG, "MainActivity successfully enabled after upgrade.");
           } catch (Exception e) {
               Log.e(TAG, "Failed to re-enable MainActivity on upgrade: " + e.getMessage(), e);
           }
       }
   }
}
