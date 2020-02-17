package com.imsajib02.passwordmanager;

import android.content.Context;

import static com.imsajib02.passwordmanager.Local.*;
import static com.imsajib02.passwordmanager.MainActivity.*;
import static com.imsajib02.passwordmanager.BackupActivity.*;

public class Auth {

    public static void GetUserCredentials(Context context)
    {
        GET_PIN = getPreferenceInt(context, PINCODE);
        GET_QUESTION = getPreferenceString(context, QUESTION);
        GET_ANSWER = getPreferenceString(context, ANSWER);
        //HAS_DENIED_TWICE = getPreferencePermission(context);
    }
}
