package com.cordova.changeappicon;

import android.content.ContentProvider;

import android.content.ContentValues;

import android.content.ComponentName;

import android.content.Context;

import android.content.pm.PackageManager;

import android.database.Cursor;

import android.net.Uri;

import android.util.Log;

public class AppIconInitProvider extends ContentProvider {

    private static final String TAG = "AppIconInitProvider";

    @Override

    public boolean onCreate() {

        Context context = getContext();

        if (context != null) {

            try {

                PackageManager pm = context.getPackageManager();

                String pkg = context.getPackageName();

                ComponentName mainActivity = new ComponentName(pkg, pkg + ".MainActivity");

                // ContentProvider runs BEFORE RedirectActivity!

                // If MainActivity was disabled, enable it now so RedirectActivity won't crash!

                if (pm.getComponentEnabledSetting(mainActivity) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {

                    pm.setComponentEnabledSetting(

                            mainActivity,

                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,

                            PackageManager.DONT_KILL_APP

                    );

                    Log.d(TAG, "MainActivity resurrected by ContentProvider before any Activity started.");

                }

            } catch (Exception e) {

                Log.e(TAG, "Error in AppIconInitProvider: " + e.getMessage());

            }

        }

        return true;

    }

    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) { return null; }

    @Override public String getType(Uri uri) { return null; }

    @Override public Uri insert(Uri uri, ContentValues values) { return null; }

    @Override public int delete(Uri uri, String selection, String[] selectionArgs) { return 0; }

    @Override public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) { return 0; }

}
 
