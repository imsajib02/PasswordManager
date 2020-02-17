package com.imsajib02.passwordmanager;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import static com.imsajib02.passwordmanager.Local.*;
import static com.imsajib02.passwordmanager.MyMethods.*;

public class NotificationHandler extends NotificationCompat {

    Context context;
    public String CHANNEL_ID = "PasswordManager";
    public int notificationId = 4674;

    //NotificationManagerCompat notificationManager;
    public NotificationManager notificationManager;


    public NotificationHandler(Context context) {

        this.context = context;
        //notificationManager = NotificationManagerCompat.from(context);
        notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
    }

    public void CreateNotification(String title, String content)
    {
        long millis = System.currentTimeMillis();

        Builder builder = new Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setAutoCancel(true)
                .setLights(Color.BLUE, 1000, 1000)
                .setVibrate(new long[]{500, 500})
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setVisibility(VISIBILITY_PRIVATE)
                .setWhen(millis)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Backup", importance);

            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            channel.setVibrationPattern(new long[]{500, 500});

            notificationManager.createNotificationChannel(channel);
        }

        WakeUpScreen();

        Vibrator v = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
        v.vibrate(new long[]{0, 500, 0, 500}, -1);
        //v.vibrate(1000);

        notificationManager.notify(notificationId, builder.build());
    }

    public void DeleteNotification()
    {
        notificationManager.cancel(notificationId);
    }

    private void WakeUpScreen()
    {
        PowerManager pm = (PowerManager) context.getSystemService(context.POWER_SERVICE);

        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, "SMOK Komunal");
        wl.acquire(10000);
    }
}
