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


         Log.i(TAG, "ChanageToIcon: "+ iconname);
        final Context ct = this.cordova.getActivity().getApplicationContext();
        //final Context ct = cordova.getActivity();
        PackageManager pm = ct.getPackageManager();
        switch (iconname){
            case "dark":

                pm.setComponentEnabledSetting(this.cordova.getActivity().getComponentName() , PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(new ComponentName(ct, packagenameval+".dark"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(new ComponentName(ct, packagenameval+".private"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(new ComponentName(ct, packagenameval+".light"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
               
                
                break;
            case "private":
				pm.setComponentEnabledSetting(this.cordova.getActivity().getComponentName() , PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(new ComponentName(ct, packagenameval+".dark"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(new ComponentName(ct, packagenameval+".private"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(new ComponentName(ct, packagenameval+".light"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
             
                break;
            case "light":
				pm.setComponentEnabledSetting(this.cordova.getActivity().getComponentName() , PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(new ComponentName(ct, packagenameval+".Icon1"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(new ComponentName(ct, packagenameval+".private"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                pm.setComponentEnabledSetting(new ComponentName(ct, packagenameval+".light"), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
              
                break;
     
            default:
              
                break;
        }

        callbackContext.success("Plugin Success");


        
       
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
