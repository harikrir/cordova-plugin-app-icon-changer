package com.cordova.changeappicon;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

public class SmartRedirectActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        
        if (intent.getData() != null) {
            try {
                String pkg = getPackageName();
                PackageManager pm = getPackageManager();
                
                // 1. Determine which component is currently active. 
                // Default to MainActivity if no alias is found.
                String activeComponentName = pkg + ".MainActivity"; 
                String[] aliases = {"Light", "Dark", "Private"};
                
                for (String alias : aliases) {
                    ComponentName comp = new ComponentName(pkg, pkg + "." + alias);
                    if (pm.getComponentEnabledSetting(comp) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                        activeComponentName = pkg + "." + alias;
                        break;
                    }
                }

                // 2. Route directly to the active component (Alias OR MainActivity)
                // We use setComponent instead of Class.forName because aliases do not have Java classes.
                Intent launchIntent = new Intent();
                launchIntent.setComponent(new ComponentName(pkg, activeComponentName));
                launchIntent.setAction("com.outsystems.HANDLE_DEEPLINK");
                launchIntent.setData(intent.getData());
                launchIntent.setFlags(603979776); // FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TOP
                
                if (intent.getExtras() != null) {
                    launchIntent.putExtras(intent.getExtras());
                }
                
                startActivity(launchIntent);
                
            } catch (Exception e) { 
                e.printStackTrace();
            }
        }
        finish();
    }
}
