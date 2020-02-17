package com.imsajib02.passwordmanager;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import static com.imsajib02.passwordmanager.DBHelper.*;
import static com.imsajib02.passwordmanager.MainActivity.*;
import static com.imsajib02.passwordmanager.MyMethods.*;
import static com.imsajib02.passwordmanager.Support.*;
import static com.imsajib02.passwordmanager.Local.*;

public class ScheduleBackupService extends IntentService {

    Context context;
    public StorageReference storageRef;
    public NotificationHandler notificationHandler;
    public boolean aBoolean;
    public DBHelper dbHelper;

    public String notification_title, notification_msg;

    public ScheduleBackupService() {
        super("AutoBackup");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        CheckNetwork checkNetwork = new CheckNetwork();
        context = getApplicationContext();
        notificationHandler = new NotificationHandler(context);
        dbHelper = new DBHelper(context, PRIMARY_DATABASE);

        aBoolean = intent.getBooleanExtra(FAILED_BACKUP, false);

        if(dbHelper.HasData(context))
        {
            if(getPreferenceBoolean(context, isBACKUPNEEDED))
            {
                if(isInternetAvailable)
                {
                    Uri file = Uri.fromFile(new File(context.getDatabasePath(PRIMARY_DATABASE).getAbsolutePath()));

                    String accountName = getPreferenceString(context, ACCOUNTNAME);
                    StorageReference fileRef = storageRef.child(accountName+ "/" +file.getLastPathSegment());

                    UploadTask uploadTask = fileRef.putFile(file);

                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {

                            Log.d("AutoBackup ", exception.getMessage().toString());

                            // push notification
                            if(!aBoolean)
                            {
                                notification_title = "Backup failed";
                                notification_msg = "Could not backup data to server.";

                                notificationHandler.CreateNotification(notification_title, notification_msg);
                            }

                            setPreferenceBoolean(context, isScheduledBackupFailed, true);
                            ScheduleFailedBackup(context);

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            Log.d("AutoBackup ", "Successfully uploaded database on server. Write account name and get download URL.");

                            setPreferenceBoolean(context, isBACKUPNEEDED, false);
                            setPreferenceBoolean(context, isRESTORENEEDED, false);
                            setPreferenceBoolean(context, isScheduledBackupFailed, false);

                            //push notification
                            notification_title = "Backup successful";
                            notification_msg = "Data is successfully backed up.";

                            notificationHandler.CreateNotification(notification_title, notification_msg);
                        }
                    });
                }
                else
                {
                    //push notification
                    if(!aBoolean)
                    {
                        notification_title = "Backup failed.";
                        notification_msg = "No internet connection available.";

                        notificationHandler.CreateNotification(notification_title, notification_msg);
                    }

                    setPreferenceBoolean(context, isScheduledBackupFailed, true);
                    ScheduleFailedBackup(context);
                }
            }
            else
            {
                setPreferenceBoolean(context, isScheduledBackupFailed, false);

                // push notification
                notification_title = "No backup required";
                notification_msg = "You already have the latest backup.";

                notificationHandler.CreateNotification(notification_title, notification_msg);
            }
        }
    }
}
