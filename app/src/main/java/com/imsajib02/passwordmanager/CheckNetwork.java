package com.imsajib02.passwordmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import static com.imsajib02.passwordmanager.Support.*;

public class CheckNetwork extends BroadcastReceiver{

    public static final int NOCONNECTION = 0;
    public static final int WIFI = 1;
    public static final int MOBILE = 2;

    public static int getConnectivityStatus(Context context) {

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null) {

            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {

                return WIFI;
            }
            else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {

                return MOBILE;
            }
        }

        return NOCONNECTION;
    }

    public static String getConnectivityStatusString(Context context) {

        int connectionType = CheckNetwork.getConnectivityStatus(context);

        String connectionStatus = null;

        if(connectionType == CheckNetwork.WIFI) {

            connectionStatus = "WIFI";
        }
        else if (connectionType == CheckNetwork.MOBILE) {

            connectionStatus = "MOBILE";
        }
        else {

            connectionStatus = "No Connection";
        }

        return connectionStatus;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if(networkInfo != null)
        {
            if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI)
            {
                isConnected = true;
                CheckInternetAvailability();
            }
            else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE)
            {
                isConnected = true;
                CheckInternetAvailability();
            }
        }
        else
        {
            isConnected = false;
        }
    }

    public void CheckInternetAvailability()
    {
        new InternetCheck(new InternetCheck.Consumer() {
            @Override
            public void accept(Boolean internet) {

                if(internet)
                {
                    isInternetAvailable = true;
                }
                else
                {
                    isInternetAvailable = false;
                }
            }
        });
    }
}
