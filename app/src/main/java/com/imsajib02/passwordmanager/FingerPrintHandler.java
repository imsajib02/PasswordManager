package com.imsajib02.passwordmanager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.util.Log;

import static android.hardware.fingerprint.FingerprintManager.FINGERPRINT_ERROR_CANCELED;
import static com.imsajib02.passwordmanager.MyMethods.*;
import static com.imsajib02.passwordmanager.MainActivity.*;

@TargetApi(Build.VERSION_CODES.M)
public class FingerPrintHandler extends FingerprintManager.AuthenticationCallback{

    public Context context;
    public static CancellationSignal cancellationSignal;

    public FingerPrintHandler(Context context)
    {
        this.context = context;
    }

    public void StartAuthentication(FingerprintManager fingerprintManager, FingerprintManager.CryptoObject cryptoObject)
    {
        cancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {

        //cancellationSignal.cancel();
        if(errorCode != FINGERPRINT_ERROR_CANCELED)
        {
            StopAuthentication();
            ShowSnackbar((Activity) context, R.id.viewflipper, String.valueOf(errString));
        }
        //super.onAuthenticationError(errorCode, errString);
    }

    @Override
    public void onAuthenticationFailed() {

        snackbar_msg = "Authentication failed";
        ShowSnackbar((Activity) context, R.id.viewflipper, snackbar_msg);
        //super.onAuthenticationFailed();
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {

        ShowSnackbar((Activity) context, R.id.viewflipper, String.valueOf(helpString));
        //super.onAuthenticationHelp(helpCode, helpString);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {

        Log.d("FingerPrint ", "User authenticated using fingerprint.");
        imageview.setImageResource(R.drawable.fingerprint_2);
        StopAuthentication();

        Intent intent = new Intent(context, HomeActivity.class);
        context.startActivity(intent);
        ((Activity) context).finish();
        //super.onAuthenticationSucceeded(result);
    }

    public static void StopAuthentication()
    {
        if(cancellationSignal != null && !cancellationSignal.isCanceled())
        {
            cancellationSignal.cancel();
        }
    }
}
