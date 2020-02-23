package id.digilabyte.nibchat.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.quickblox.videochat.webrtc.QBRTCSession;

public class UserPreferences {

    private final static String PREFS_FILENAME = "NIBCHAT";
    private final static String PREFS_USER = "user";
    private final static String PREFS_PASS = "pass";
    private final static String PREFS_FIRST = "isFirst";

    private Context context;
    private SharedPreferences sharedPreferences;

    public UserPreferences(Context context) {
        this.context = context;
        sharedPreferences = this.context.getSharedPreferences(PREFS_FILENAME, 0);
    }

    public void setUser (String string) {
        sharedPreferences.edit().putString(PREFS_USER, string).apply();
    }

    public void clear() {
        sharedPreferences.edit().clear().apply();
        setFirst(true);
        setUser("");
        setPass("");
    }

    public String getUser () {
        String user = sharedPreferences.getString(PREFS_USER, "");
        return user;
    }

    public void setFirst (Boolean string) {
        sharedPreferences.edit().putBoolean(PREFS_FIRST, string).apply();
    }

    public Boolean getFirst () {
        Boolean isFirst = sharedPreferences.getBoolean(PREFS_FIRST, true);
        return isFirst;
    }

    public void setPass (String string) {
        sharedPreferences.edit().putString(PREFS_PASS, string).apply();
    }

    public String getPass () {
        String pass = sharedPreferences.getString(PREFS_PASS, "");
        return pass;
    }

}
