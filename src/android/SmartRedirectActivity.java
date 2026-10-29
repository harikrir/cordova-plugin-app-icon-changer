package com.cordova.changeappicon;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
public class SmartRedirectActivity extends Activity {
   private static final String TAG = "SmartRedirectActivity";
   private static final String[] ALIASES = {"Dark", "Private"};
   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       try {
           PackageManager pm = getPackageManager();
           String pkg = getPackageName();
           // 1. Forcefully re-enable MainActivity to prevent OutSystems crash
           ComponentName mainActivity = new ComponentName(pkg, pkg + ".MainActivity");
           pm.setComponentEnabledSetting(
                   mainActivity,
                   PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                   PackageManager.DONT_KILL_APP
           );
           // 2. Disable aliases to prevent duplicates
           for (String alias : ALIASES) {
               ComponentName comp = new ComponentName(pkg, pkg + "." + alias);
               pm.setComponentEnabledSetting(
                       comp,
                       PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                       PackageManager.DONT_KILL_APP
               );
           }
           Log.d(TAG, "MainActivity restored. Forwarding deep link intent.");
           // 3. Forward the exact deep link payload into the OutSystems app
           Intent originalIntent = getIntent();
           Intent launchIntent = new Intent(this, Class.forName(pkg + ".MainActivity"));
           launchIntent.setAction(Intent.ACTION_VIEW);
           if (originalIntent != null) {
               if (originalIntent.getData() != null) {
                   launchIntent.setData(originalIntent.getData());
               }
               if (originalIntent.getExtras() != null) {
                   launchIntent.putExtras(originalIntent.getExtras());
               }
           }
           launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
           startActivity(launchIntent);
       } catch (Exception e) {
           Log.e(TAG, "Failed to route redirect intent: " + e.getMessage(), e);
       } finally {
           finish();
       }
   }
}
