package com.cordova.changeappicon;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
public class SmartRedirectActivity extends Activity {
   private static final String TAG = "SmartRedirectActivity";
   private static final String[] ALIASES = {"Dark", "Private"};
   @Override
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       try {
           Intent originalIntent = getIntent();
           Uri data = originalIntent != null ? originalIntent.getData() : null;
           PackageManager pm = getPackageManager();
           String pkg = getPackageName();
           // 1. Detect which component is active right now
           String activeTarget = pkg + ".MainActivity"; // default fallback
           for (String alias : ALIASES) {
               ComponentName comp = new ComponentName(pkg, pkg + "." + alias);
               if (pm.getComponentEnabledSetting(comp) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                   activeTarget = pkg + "." + alias;
                   break;
               }
           }
           Log.d(TAG, "Routing deep link directly to active component: " + activeTarget);
           // 2. Build the launch intent directed to the ACTIVE component
           Intent launchIntent = new Intent();
           launchIntent.setComponent(new ComponentName(pkg, activeTarget));
           launchIntent.setAction(Intent.ACTION_VIEW);
           if (data != null) {
               launchIntent.setData(data);
           }
           // Copy forward any extras/flags passed by OAuth/DeepLink
           if (originalIntent != null && originalIntent.getExtras() != null) {
               launchIntent.putExtras(originalIntent.getExtras());
           }
           launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
           // 3. Launch the active component smoothly
           startActivity(launchIntent);
       } catch (Exception e) {
           Log.e(TAG, "Failed to route redirect intent: " + e.getMessage(), e);
       } finally {
           // Close the redirect activity trampoline
           finish();
       }
   }
}
