package com.imsajib02.passwordmanager;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static com.imsajib02.passwordmanager.Local.*;
import static com.imsajib02.passwordmanager.MyMethods.*;
import static com.imsajib02.passwordmanager.Support.FAILED_BACKUP;

public class BootBroadcastReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;

        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
        {
            if(getPreferenceBoolean(context, isBACKUPSCHEDULED))
            {
                if(getPreferenceBoolean(context, isScheduledBackupFailed))
                {
                    ScheduleFailedBackup();
                }

                ScheduleBackup();
            }
        }
    }

    public void ScheduleBackup()
    {
        Intent intent = new Intent(context, MyAlarmReceiver.class);
        intent.putExtra(FAILED_BACKUP, false);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, MyAlarmReceiver.REQUEST_CODE1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long millis = GetScheduledTime();

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarm.setRepeating(AlarmManager.RTC_WAKEUP, millis,
                AlarmManager.INTERVAL_DAY, pIntent);
    }

    public void ScheduleFailedBackup()
    {
        //Scheduling backup after 5 minutes
        long millis = System.currentTimeMillis() + 300000;

        Intent intent = new Intent(context, MyAlarmReceiver.class);
        intent.putExtra(FAILED_BACKUP, true);

        final PendingIntent pIntent = PendingIntent.getBroadcast(context, MyAlarmReceiver.REQUEST_CODE2,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarm.set(AlarmManager.RTC_WAKEUP, millis, pIntent);
    }

    public long GetScheduledTime()
    {
        long millis = 0;

        String hour = String.valueOf(getPreferenceInt(context, HOURSofSCHEDULE));
        String minute = String.valueOf(getPreferenceInt(context, MINUTESofSCHEDULE));

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(hour));
        calendar.set(Calendar.MINUTE, Integer.valueOf(minute));
        calendar.set(Calendar.SECOND, 0);

        millis = calendar.getTimeInMillis();

        return millis;
    }
}
