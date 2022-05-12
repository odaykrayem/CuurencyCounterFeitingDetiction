package com.mrmindteam.cuurencycounterfeitingdetiction;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPrefManager {

    private static final String KEY_ID = "keyid";
    private static final String SHARED_PREF_NAME = "generalFile";
    private static final String KEY_IMAGE_ID = "keyimageid";


    private static SharedPrefManager mInstance;
    private static Context context;

    private SharedPrefManager(Context context) {
        SharedPrefManager.context = context;
    }
    public static synchronized SharedPrefManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }
        return mInstance;
    }

    public void setImageId(int imageId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_IMAGE_ID,imageId);

        editor.apply();
    }

    //this method will check whether user is already logged in or not
    public int getImageId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(KEY_IMAGE_ID, -1);
    }

    //this method will logout the user
    public void deleteImageId() {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().commit();
    }

}
