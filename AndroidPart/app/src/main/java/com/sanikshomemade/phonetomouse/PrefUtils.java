package com.sanikshomemade.phonetomouse;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import java.util.Locale;

public class PrefUtils {

    private static final String PREFS_FILE = "com.sanikshomemade.phonetomouse.PREFS_FILE";
    public static final String LAST_PAIRED_MAC = "last_paired_mac";
    public  static final String LAST_PAIRED_DEVICE_NAME = "last_paired_name";
    public static final String THEME_OPTION = "theme";
    public static final String PREFERRED_CURSOR_SCALE = "pref_cr_scale";
    public  static final String PREFERRED_LANGUAGE = "pref_lang";
    public static final String PREFERRED_BT_RECONNECT = "pref_reconn";
    public static final String CONNECT_SOUND_KEY = "pref_sound1";
    public static final String LONG_PRESS_SOUND_KEY = "pref_sound2";

    private static SharedPreferences getPrefsFile(Context ct) {
        return ct.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    }

    static public void SaveDeviceInfo(Context ct, String deviceName, String macValue)
    {
        SharedPreferences.Editor editor = getPrefsFile(ct).edit();
        editor.putString(LAST_PAIRED_DEVICE_NAME, deviceName);
        editor.putString(LAST_PAIRED_MAC, macValue);
        editor.apply();
    }

    static public <T> T getValueFromPrefs(Context ct, String key, T defValue)
    {
        if(defValue instanceof String) {
            return (T)getPrefsFile(ct).getString(key, "");
        }
        else if(defValue instanceof Integer) {
            return (T)((Integer)getPrefsFile(ct).getInt(key, (int)defValue));
        }
        else { //bool
            return (T)((Boolean)getPrefsFile(ct).getBoolean(key, (boolean)defValue));
        }
    }

    static public SharedPreferences.Editor getEditor(Context ct) {
        return getPrefsFile(ct).edit();
    }

    static public String GetLanguageCodeToOverrideTo(Context app) {
        int langIndexFromPrefs = getValueFromPrefs(app, PREFERRED_LANGUAGE, -1);
        if(langIndexFromPrefs <0) { return  ""; }


        String crtContextLang = MyApp.MyMultilangCompatActivity.getLocaleFromRes(app.getResources()).getLanguage();
        String langCodePrefs = app.getResources().getStringArray(R.array.languages)[langIndexFromPrefs].toLowerCase();

        return crtContextLang.equals(langCodePrefs)? "" : langCodePrefs;
    }

    static public Context GetOverriddenLanguageContext(String lang, Context context) {
        Locale myLocale = new Locale(lang);
        Locale.setDefault(myLocale);

        Configuration conf = context.getResources().getConfiguration();
        conf.setLocale(myLocale);

        return context.createConfigurationContext(conf);
    }
}
