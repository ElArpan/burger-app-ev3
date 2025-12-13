package com.B.carrasco.burgerapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "BurgerAppSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_GUEST_MODE = "guestMode";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String username, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_USER_ROLE, role);
        editor.putBoolean(KEY_GUEST_MODE, false);
        editor.commit();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }

    public String getUserRole() {
        return pref.getString(KEY_USER_ROLE, "client");
    }

    public void setGuestMode(boolean isGuest) {
        editor.putBoolean(KEY_GUEST_MODE, isGuest);
        editor.commit();
    }

    public boolean isGuestMode() {
        return pref.getBoolean(KEY_GUEST_MODE, false);
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();
    }

    public void setUserId(int userId) {
        editor.putInt(KEY_USER_ID, userId);
        editor.commit();
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }
}