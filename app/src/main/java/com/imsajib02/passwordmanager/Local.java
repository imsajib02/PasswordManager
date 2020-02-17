package com.imsajib02.passwordmanager;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.List;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public class Local {

    public static final String MY_PREFS_NAME = "PasswordManager";
    public static final String PINCODE = "UserPin";
    public static final String QUESTION = "Question";
    public static final String ANSWER = "Answer";
    public static final String PERMISSION = "ReadPermission";
    public static final String DENIEDTWICE = "DeniedTwice";
    public static final String isTERMSANDPOLICYAGREED = "TermsAndPolicyAgreed";
    public static final String ACCOUNTNAME = "AccountName";
    public static final String isBACKUPSCHEDULED = "BackupScheduled";
    public static final String TYPEofSCHEDULE = "ScheduleType";
    public static final String HOURSofSCHEDULE = "ScheduleHour";
    public static final String MINUTESofSCHEDULE = "ScheduleMinute";
    public static final String DAYofSCHEDULE = "ScheduleDay";
    public static final String isBACKUPNEEDED = "NeedBackup";
    public static final String isRESTORENEEDED = "NeedRestore";
    public static final String isScheduledBackupFailed = "ScheduledBackupFailed";

    public static void setPreferenceInt(Context context, String key, int value)
    {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public static int getPreferenceInt(Context context, String key)
    {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        return prefs.getInt(key, 0);
    }

    public static void setPreferenceString(Context context, String Key, String value)
    {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString(Key, value);
        editor.apply();
    }

    public static String getPreferenceString(Context context, String key)
    {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(key, null);
    }

    public static void setPreferenceBoolean(Context context, String key, boolean value)
    {
        SharedPreferences.Editor editor = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getPreferenceBoolean(Context context, String key)
    {
        SharedPreferences prefs = context.getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(key, false);
    }
}
