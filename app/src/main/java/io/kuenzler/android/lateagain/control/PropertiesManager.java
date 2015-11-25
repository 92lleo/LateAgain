package io.kuenzler.android.lateagain.control;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * @author Leonhard Künzler
 * @version 0.1
 * @date 25.11.15 23:00
 */
public class PropertiesManager {

    SharedPreferences prefs;
    Activity main;

    /**
     *
     */
    public PropertiesManager(Activity main) {
        this.main = main;
        initPrefs();
    }

    private void initPrefs() {
        prefs = main.getSharedPreferences("location", 0);

    }

    /**
     * @return
     */
    public SharedPreferences getPrefs() {
        return prefs;
    }

    public String getFromKey(String key) {
        return prefs.getString(key, "penis");
    }

    /**
     * @param key
     * @param value
     */
    public void setFromKey(String key, String value) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
