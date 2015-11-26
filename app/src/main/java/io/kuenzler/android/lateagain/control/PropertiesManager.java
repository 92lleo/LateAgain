package io.kuenzler.android.lateagain.control;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * @author Leonhard Künzler
 * @version 0.2
 * @date 26.11.15 12:30
 */
public class PropertiesManager {

    SharedPreferences prefs;
    Activity main;

    /**
     * Manages SharedPreferences
     */
    public PropertiesManager(Activity main) {
        this.main = main;
        initPrefs();
    }

    private void initPrefs() {
        prefs = main.getSharedPreferences("location", 0);

    }

    /**
     * @return the SharedPreferences object
     */
    public SharedPreferences getPrefs() {
        return prefs;
    }

    /**
     * Returns value from preferences from given key
     *
     * @param key key for value
     * @return value as string
     */
    public String getFromKey(String key) {
        return prefs.getString(key, "");
    }

    /**
     * Sets new value by key
     *
     * @param key   key for value
     * @param value value to set
     */
    public void setFromKey(String key, String value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
