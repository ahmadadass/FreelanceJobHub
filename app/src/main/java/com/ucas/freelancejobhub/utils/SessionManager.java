package com.ucas.freelancejobhub.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS_NAME = "freelance_job_hub_session";
    private static final String KEY_USER_ID = "active_user_id";
    private static final String KEY_REMEMBER = "remember_session";

    private final SharedPreferences preferences;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void startSession(long userId, boolean remember) {
        preferences.edit()
                .putLong(KEY_USER_ID, userId)
                .putBoolean(KEY_REMEMBER, remember)
                .apply();
    }

    public long getUserId() {
        return preferences.getLong(KEY_USER_ID, -1L);
    }

    public boolean isLoggedIn() {
        return getUserId() > 0;
    }

    public boolean isRemembered() {
        return preferences.getBoolean(KEY_REMEMBER, false);
    }

    public void clear() {
        preferences.edit().clear().apply();
    }
}
