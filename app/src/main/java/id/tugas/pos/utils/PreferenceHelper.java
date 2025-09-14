package id.tugas.pos.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {
    private static final String PREF_NAME = "pos_preferences";
    private static final String KEY_CURRENT_STORE_ID = "current_store_id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_USER_EMAIL = "user_email";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void setCurrentStoreId(Context context, int storeId) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(KEY_CURRENT_STORE_ID, storeId);
        editor.apply();
    }

    public static int getCurrentStoreId(Context context) {
        return getPreferences(context).getInt(KEY_CURRENT_STORE_ID, 1); // Default to store ID 1
    }

    public static void setUserId(Context context, int userId) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
    }

    public static int getUserId(Context context) {
        return getPreferences(context).getInt(KEY_USER_ID, -1);
    }

    public static void setUserRole(Context context, String role) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    public static String getUserRole(Context context) {
        return getPreferences(context).getString(KEY_USER_ROLE, "user");
    }

    public static void setUserEmail(Context context, String email) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.putString(KEY_USER_EMAIL, email);
        editor.apply();
    }

    public static String getUserEmail(Context context) {
        return getPreferences(context).getString(KEY_USER_EMAIL, "");
    }

    public static void clearUserSession(Context context) {
        SharedPreferences.Editor editor = getPreferences(context).edit();
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_ROLE);
        editor.remove(KEY_USER_EMAIL);
        editor.apply();
    }

    public static boolean isUserLoggedIn(Context context) {
        return getUserId(context) != -1;
    }
}
