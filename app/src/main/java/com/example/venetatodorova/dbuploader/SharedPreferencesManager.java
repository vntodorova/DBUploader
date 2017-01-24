package com.example.venetatodorova.dbuploader;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

class SharedPreferencesManager {
    private static SharedPreferences mSharedPref;
    private static final String SP_REMAINING_FILES_KEY = "Remaining files";
    private static final String SP_NAME_KEY = "My shared preferences";

    static void init(Context context)
    {
        if(mSharedPref == null)
            mSharedPref = context.getSharedPreferences(SP_NAME_KEY, Activity.MODE_PRIVATE);
    }

    static ArrayList<String> read() {
        Set<String> remainingSet = mSharedPref.getStringSet(SP_REMAINING_FILES_KEY, new HashSet<String>());
        ArrayList<String> remainingList = new ArrayList<>();
        remainingList.addAll(remainingSet);
        return remainingList;
    }

    static Integer read(String key, int defValue) {
        return mSharedPref.getInt(key, defValue);
    }

    static void write(String key, Integer value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putInt(key, value).apply();
    }

    static void write(ArrayList<String> filesList) {
        Set<String> remainingSet = new HashSet<>();
        remainingSet.addAll(filesList);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putStringSet(SP_REMAINING_FILES_KEY, remainingSet).apply();
    }

    static void remove(String currentFilePath) {
        Set<String> sharedPrefSet = mSharedPref.getStringSet(SP_REMAINING_FILES_KEY, new HashSet<String>());
        sharedPrefSet.remove(currentFilePath);
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.remove(SP_REMAINING_FILES_KEY);
        editor.putStringSet(SP_REMAINING_FILES_KEY, sharedPrefSet).apply();
    }

    static void clear() {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.clear().apply();
    }
}
