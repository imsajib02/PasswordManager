package com.imsajib02.passwordmanager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.androidstudy.networkmanager.Monitor;
import com.androidstudy.networkmanager.Tovuti;
import com.github.informramiz.daypickerlibrary.views.DayPickerDialog;
import com.github.informramiz.daypickerlibrary.views.DayPickerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.accounts.AccountManager.newChooseAccountIntent;
import static com.imsajib02.passwordmanager.Local.*;
import static com.imsajib02.passwordmanager.MainActivity.*;
import static com.imsajib02.passwordmanager.MyMethods.*;
import static com.imsajib02.passwordmanager.DBHelper.*;
import static com.imsajib02.passwordmanager.Support.NORMAL_REQUEST;
import static com.imsajib02.passwordmanager.Support.databaseReference;
import static com.imsajib02.passwordmanager.Support.fileDownloadTask;
import static com.imsajib02.passwordmanager.Support.isConnected;
import static com.imsajib02.passwordmanager.Support.isInternetAvailable;
import static com.imsajib02.passwordmanager.Support.progressDialog;
import static com.imsajib02.passwordmanager.Support.storageRef;
import static com.imsajib02.passwordmanager.Support.user_account_context;
import static com.imsajib02.passwordmanager.Support.user_account_resId;
import static com.imsajib02.passwordmanager.Support.weekdays;

public class BackupActivity extends AppCompatActivity {

    public static TextView backup, restore, schedule;
    public static RelativeLayout backuppagerelativeLayout;
    AccountManager accountManager;
    Account[] accounts;

    boolean[] intialdays = {true, false, false, false, false, false, false};
    int hour_Schedule, minute_Schedule;
    String day_Schedule;
    int SHARED_PREFERENCE_TIME_REQUEST = 757, PICK_TIME_REQUEST = 257;

    public Dialog dialog;

    public int REQUEST_READ_CONTACTS = 0;
    public int DENY_COUNT = 0;
    public String accountName;
    public static boolean HAS_DENIED_TWICE;
    public static int BACKUP_REQUEST = 111;
    public static int RESTORE_REQUEST = 123;
    public static int AUTO_BACKUP_REQUEST = 132;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    UploadTask uploadTask;
    DBHelper dbHelper;
    CheckNetwork checkNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        backup = (TextView) findViewById(R.id.backup);
        restore = (TextView) findViewById(R.id.restore);
        schedule = (TextView) findViewById(R.id.schedule);
        backuppagerelativeLayout = (RelativeLayout) findViewById(R.id.BackupRelativeLayout);

        accountManager = AccountManager.get(this);

        firebaseAuth = FirebaseAuth.getInstance();
        InitializeDatabaseStorage(BackupActivity.this, R.id.BackupRelativeLayout);
        firebaseUser = firebaseAuth.getCurrentUser();
        checkNetwork = new CheckNetwork();

        InitializeProgressDialog(BackupActivity.this);

        accountName = getPreferenceString(this, ACCOUNTNAME);

        backup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);
                dbHelper = new DBHelper(v.getContext(), PRIMARY_DATABASE);

                if(dbHelper.HasData(v.getContext()))
                {
                    if(getPreferenceBoolean(BackupActivity.this, isBACKUPNEEDED))
                    {
                        if(ThereIsInternet(v.getContext(), R.id.BackupRelativeLayout, NORMAL_REQUEST))
                        {
                            //RequestPermission();
                            Log.d("Backup ", "Backup requested by user.");

                            if(getPreferenceBoolean(v.getContext(), isTERMSANDPOLICYAGREED))
                            {
                                Log.d("Backup ", "Terms and Policy agreed by the user.");

                                if(TextUtils.isEmpty(accountName))
                                {
                                    Log.d("Backup ", "No user account found. Get gmail account from user.");

                                    GetUserAccounts(BackupActivity.this, BACKUP_REQUEST);
                                }
                                else
                                {
                                    UploadFile();
                                }
                            }
                            else
                            {
                                Log.d("Backup ", "Ask user to agree to the Terms and Policy.");

                                TermsAndPolicyDialog(BACKUP_REQUEST);
                            }
                        }
                    }
                    else
                    {
                        snackbar_msg = "You have the latest backup";
                        ShowSnackbar(BackupActivity.this, R.id.BackupRelativeLayout, snackbar_msg);
                    }
                }
                else
                {
                    snackbar_msg = "No data to backup";
                    ShowSnackbar(BackupActivity.this, R.id.BackupRelativeLayout, snackbar_msg);
                }
            }
        });

        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);

                if(getPreferenceBoolean(BackupActivity.this, isRESTORENEEDED))
                {
                    if(ThereIsInternet(v.getContext(), R.id.BackupRelativeLayout, NORMAL_REQUEST))
                    {
                        if(TextUtils.isEmpty(accountName))
                        {
                            GetUserAccounts(BackupActivity.this, RESTORE_REQUEST);
                            //snackbar_msg = "No backup created";
                            //ShowSnackbar(BackupActivity.this, R.id.BackupRelativeLayout, snackbar_msg);
                        }
                        else
                        {
                            progressDialog.setMessage("Please wait...");
                            progressDialog.show();

                            //AuthUser(accountName);
                            CheckUserAccount(accountName, BackupActivity.this, R.id.BackupRelativeLayout);
                        }
                    }
                }
                else
                {
                    snackbar_msg = "You have the latest restored and synced data";
                    ShowSnackbar(BackupActivity.this, R.id.BackupRelativeLayout, snackbar_msg);
                }
            }
        });

        schedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);

                if(getPreferenceBoolean(BackupActivity.this, isTERMSANDPOLICYAGREED))
                {
                    if(TextUtils.isEmpty(getPreferenceString(BackupActivity.this, ACCOUNTNAME)))
                    {
                        GetUserAccounts(BackupActivity.this, AUTO_BACKUP_REQUEST);
                    }
                    else
                    {
                        ScheduleDialog();
                    }
                }
                else
                {
                    TermsAndPolicyDialog(AUTO_BACKUP_REQUEST);
                }
            }
        });
    }

    public void GetUserAccounts(Context context, int my_Request_Code)
    {
        if(TextUtils.isEmpty(getPreferenceString(context, ACCOUNTNAME)))
        {
            Intent intent = newChooseAccountIntent(null, null, new String[]{"com.google"},
                    false, null,
                    null, null, null);

            //startActivityForResult(intent, my_Request_Code);
            ((Activity)context).startActivityForResult(intent, my_Request_Code);
        }
        else
        {
            UploadFile();
        }
    }

    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        if (requestCode == BACKUP_REQUEST && resultCode == RESULT_OK)
        {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            setPreferenceString(this, ACCOUNTNAME, accountName);

            Log.d("Backup ", "Gmail account: "+accountName);

            UploadFile();
        }
        else if (requestCode == RESTORE_REQUEST && resultCode == RESULT_OK)
        {
            String mail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            Log.d("Restore ", "Gmail account: "+mail);

            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            //AuthUser(mail);
            CheckUserAccount(mail, user_account_context, user_account_resId);
        }
        else if (requestCode == AUTO_BACKUP_REQUEST && resultCode == RESULT_OK)
        {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            setPreferenceString(this, ACCOUNTNAME, accountName);

            Log.d("Auto Backup ", "Gmail account: "+accountName);

            ScheduleDialog();
        }
    }

    @Override
    public void onBackPressed() {

        firebaseAuth.signOut();
        //unregisterReceiver(checkNetwork);

        Intent intent = new Intent(BackupActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();

        super.onBackPressed();
    }

    @Override
    protected void onPause() {

        unregisterReceiver(checkNetwork);

        try
        {
            if(uploadTask.isInProgress())
            {
                uploadTask.pause();
                Log.d("Backup ", "Activity in background. Pausing upload task.");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if(fileDownloadTask.isInProgress())
            {
                fileDownloadTask.pause();
                Log.d("Restore ", "BackupActivity in background. Pausing download task.");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        super.onPause();
    }

    @Override
    protected void onResume() {

        IntentFilter intentFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(checkNetwork, intentFilter);

        try
        {
            if(uploadTask.isPaused())
            {
                uploadTask.resume();
                Log.d("Backup ", "Activity in foreground. Resuming upload task.");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        try
        {
            if(fileDownloadTask.isPaused())
            {
                fileDownloadTask.resume();
                Log.d("Restore ", "BackupActivity in foreground. Resuming download task.");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        super.onResume();
    }

    public void TermsAndPolicyDialog(final int code)
    {
        LayoutInflater inflater = LayoutInflater.from(BackupActivity.this);
        View view = inflater.inflate(R.layout.dialog_terms_conditions, null);

        TextView cancel = (TextView) view.findViewById(R.id.tp_cancel);
        TextView agree = (TextView) view.findViewById(R.id.tp_agree);
        TextView links = (TextView) view.findViewById(R.id.links);

        AlertDialog.Builder builder = new AlertDialog.Builder(BackupActivity.this);
        builder.setView(view)
                .setCancelable(false);

        links.setMovementMethod(LinkMovementMethod.getInstance());

        //in order to dismiss the dialog first create() the builder then call show()
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();


        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("Backup ", "Terms and Policy is not agreed.");

                ButtonPressAnimation(v);
                setPreferenceBoolean(v.getContext(), isTERMSANDPOLICYAGREED, false);
                dialog.dismiss();
            }
        });

        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("Backup ", "Terms and Policy agreed.");

                ButtonPressAnimation(v);
                setPreferenceBoolean(v.getContext(), isTERMSANDPOLICYAGREED, true);
                dialog.dismiss();

                if(TextUtils.isEmpty(getPreferenceString(BackupActivity.this, ACCOUNTNAME)))
                {
                    GetUserAccounts(BackupActivity.this, code);
                }
            }
        });
    }


    public void UploadFile()
    {
        //progressDialog.setTitle("Backup");
        progressDialog.setMessage("Creating backup...");
        //progressDialog.setIndeterminate(true);
        //progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        //progressDialog.setProgress(10);
        progressDialog.show();

        Uri file = Uri.fromFile(new File(this.getDatabasePath(PRIMARY_DATABASE).getAbsolutePath()));
        StorageReference fileRef = storageRef.child(accountName+ "/" +file.getLastPathSegment());
        uploadTask = fileRef.putFile(file);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                Log.d("Backup ", exception.getMessage().toString());

                progressDialog.dismiss();

                if(ThereIsInternet(BackupActivity.this, R.id.BackupRelativeLayout, BACKUP_REQUEST))
                {
                    snackbar_msg = "Backup failed. Server error.";
                    ShowSnackbar(BackupActivity.this, R.id.BackupRelativeLayout, snackbar_msg);
                }

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                //double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                //Log.d("Progress ", ""+progress);
                //progressDialog.setProgress((int) progress);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Log.d("Backup ", "Successfully uploaded database on server. Write account name and get download URL.");

                //firebase database does not take dot (.) as path
                String mailID = accountName.replaceAll("[.]", ",");

                databaseReference.child("Accounts").child(mailID)
                        .setValue(taskSnapshot.getDownloadUrl().toString())
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                Log.d("Backup ", e.getMessage().toString());

                                progressDialog.dismiss();

                                if(ThereIsInternet(BackupActivity.this, R.id.BackupRelativeLayout, BACKUP_REQUEST))
                                {
                                    snackbar_msg = "Backup failed. Server error.";
                                    ShowSnackbar(BackupActivity.this, R.id.BackupRelativeLayout, snackbar_msg);
                                }

                            }
                        }).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Log.d("Backup ", "Successfully stored account name on realtime database.");

                        setPreferenceBoolean(BackupActivity.this, isBACKUPNEEDED, false);
                        setPreferenceBoolean(BackupActivity.this, isRESTORENEEDED, false);

                        progressDialog.dismiss();
                        ShowSnackbar(BackupActivity.this, R.id.BackupRelativeLayout, "Backup created");
                    }
                });
            }
        });
    }


    public void AuthUser(final String userMail)
    {
        if(firebaseUser != null)
        {
            Log.d("Restore ", "Firebase auth user found.");
            CheckUserAccount(userMail, BackupActivity.this, R.id.BackupRelativeLayout);
        }
        else
        {
            firebaseAuth.signInWithEmailAndPassword("adminemail@gmail.com", "adminemail")
                    .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    Log.d("Restore ", "Firebase user authentication failed.");
                    e.printStackTrace();
                    progressDialog.dismiss();

                    snackbar_msg = "Failed to restore";
                    ShowSnackbar(BackupActivity.this, R.id.BackupRelativeLayout, snackbar_msg);
                }
            }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {

                    Log.d("Restore ", "Firebase user authentication successful.");
                    CheckUserAccount(userMail, BackupActivity.this, R.id.BackupRelativeLayout);
                }
            });
        }
    }

    public void CheckUserAccount(final String userMail, final Context context, final int resId)
    {
        final String mailID = userMail.replaceAll("[.]", ",");

        databaseReference.child("Accounts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChild(mailID))
                {
                    Log.d("Restore ", "User " +userMail+ " found on realtime database.");

                    setPreferenceString(context, ACCOUNTNAME, userMail);
                    setPreferenceBoolean(context, isTERMSANDPOLICYAGREED, true);

                    progressDialog.setMessage("Restoring...");

                    DownloadFile(userMail, context, resId);
                }
                else
                {
                    Log.d("Restore ", "User " +userMail+ " is not found on realtime database.");

                    progressDialog.dismiss();

                    snackbar_msg = "No backup found linked to this account";
                    ShowSnackbar((Activity)context, resId, snackbar_msg);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Log.d("Restore ", ""+databaseError.getMessage().toString());

                progressDialog.dismiss();

                if(ThereIsInternet(context, resId, RESTORE_REQUEST))
                {
                    snackbar_msg = "Failed to restore. Server error.";
                    ShowSnackbar((Activity)context, resId, snackbar_msg);
                }
            }
        });
    }

    public void DownloadFile(String userMail, final Context context, final int resId)
    {
        final StorageReference fileRef = storageRef.child(userMail+ "/" + PRIMARY_DATABASE);

        try
        {
            final File file = new File(context.getDatabasePath(PRIMARY_DATABASE).getParent()+ "/" +SECONDARY_DATABASE);

            fileDownloadTask = fileRef.getFile(file);

            fileDownloadTask.addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                    Log.d("Restore ", "Successfully downloaded file from server.");

                    dbHelper = new DBHelper(context, PRIMARY_DATABASE);
                    dbHelper.AttachDatabase(context, resId);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {

                    Log.d("Restore ", exception.getMessage().toString());

                    progressDialog.dismiss();

                    if(ThereIsInternet(context, resId, RESTORE_REQUEST))
                    {
                        snackbar_msg = "Failed to restore. Server error.";
                        ShowSnackbar((Activity)context, resId, snackbar_msg);
                    }
                }
            });
        }
        catch (Exception e)
        {
            Log.d("Restore ", "Failed to create file in local storage.");
            Log.d("Restore ", e.getMessage().toString());

            progressDialog.dismiss();

            snackbar_msg = "File error. Try again.";
            ShowSnackbar((Activity)context, resId, snackbar_msg);
        }
    }

    public void ScheduleDialog()
    {
        LayoutInflater inflater = LayoutInflater.from(BackupActivity.this);
        final View view = inflater.inflate(R.layout.layout_choose_schedule, null);

        final Switch schedule_on_off = (Switch) view.findViewById(R.id.schedule_on_off);
        final RadioButton daily = (RadioButton) view.findViewById(R.id.daily);
        final RadioButton weekly = (RadioButton) view.findViewById(R.id.weekly);
        final TextView daily_time_picker = (TextView) view.findViewById(R.id.daily_time_picker);
        final TextView weekly_time_picker = (TextView) view.findViewById(R.id.weekly_time_picker);
        final TextView weekly_day_picker = (TextView) view.findViewById(R.id.weekly_day_picker);
        final Button save = (Button) view.findViewById(R.id.save);

        AlertDialog.Builder builder = new AlertDialog.Builder(BackupActivity.this);
        builder.setView(view)
                .setCancelable(true);

        hour_Schedule = getPreferenceInt(BackupActivity.this, HOURSofSCHEDULE);
        minute_Schedule = getPreferenceInt(BackupActivity.this, MINUTESofSCHEDULE);
        day_Schedule = "Sunday";


        if(getPreferenceBoolean(BackupActivity.this, isBACKUPSCHEDULED))
        {
            schedule_on_off.setChecked(true);
            daily.setEnabled(true);
            weekly.setEnabled(true);

            String result = GetScheduleTime(SHARED_PREFERENCE_TIME_REQUEST);

            if(!TextUtils.isEmpty(result))
            {
                daily_time_picker.setText(result);
                weekly_time_picker.setText(result);
            }

            if(!TextUtils.isEmpty(getPreferenceString(BackupActivity.this, DAYofSCHEDULE)))
            {
                weekly_day_picker.setText("Every " +getPreferenceString(BackupActivity.this, DAYofSCHEDULE));
            }

            if(TextUtils.equals(getPreferenceString(BackupActivity.this, TYPEofSCHEDULE), "Daily"))
            {
                daily.setChecked(true);
                daily_time_picker.setVisibility(View.VISIBLE);
            }
            else if(TextUtils.equals(getPreferenceString(BackupActivity.this, TYPEofSCHEDULE), "Weekly"))
            {
                weekly.setChecked(true);
                weekly_time_picker.setVisibility(View.VISIBLE);
                weekly_day_picker.setVisibility(View.VISIBLE);
            }
        }

        //in order to dismiss the dialog first create() the builder then call show()
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        schedule_on_off.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    daily.setEnabled(true);
                    weekly.setEnabled(true);

                    if(daily.isChecked())
                    {
                        daily_time_picker.setVisibility(View.VISIBLE);
                    }
                    else if(weekly.isChecked())
                    {
                        weekly_time_picker.setVisibility(View.VISIBLE);
                        weekly_day_picker.setVisibility(View.VISIBLE);
                    }
                }
                else
                {
                    daily.setEnabled(false);
                    weekly.setEnabled(false);

                    if(daily.isChecked())
                    {
                        daily_time_picker.setVisibility(View.INVISIBLE);
                    }
                    else if(weekly.isChecked())
                    {
                        weekly_time_picker.setVisibility(View.INVISIBLE);
                        weekly_day_picker.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });

        daily.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    daily_time_picker.setVisibility(View.VISIBLE);
                    weekly.setChecked(false);
                }
                else
                {
                    daily_time_picker.setVisibility(View.INVISIBLE);
                }
            }
        });

        weekly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked)
                {
                    weekly_time_picker.setVisibility(View.VISIBLE);
                    weekly_day_picker.setVisibility(View.VISIBLE);
                    daily.setChecked(false);
                }
                else
                {
                    weekly_time_picker.setVisibility(View.INVISIBLE);
                    weekly_day_picker.setVisibility(View.INVISIBLE);
                }
            }
        });

        daily_time_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);
                PickTime();
            }
        });

        weekly_time_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);
                PickTime();
            }
        });

        weekly_day_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);

                if(!TextUtils.isEmpty(getPreferenceString(BackupActivity.this, DAYofSCHEDULE)))
                {
                    for(int i=0; i<intialdays.length; i++)
                    {
                        if(TextUtils.equals(weekdays[i], getPreferenceString(BackupActivity.this, DAYofSCHEDULE)))
                        {
                            intialdays[i] = true;
                        }
                        else
                        {
                            intialdays[i] = false;
                        }
                    }
                }

                DayPickerDialog.Builder builder = new DayPickerDialog.Builder(BackupActivity.this)
                        .setThemeResId(R.style.TimePickerTheme)
                        .setMultiSelectionAllowed(false)
                        .setInitialSelectedDays(intialdays)
                        .setOnDaysSelectedListener(new DayPickerDialog.OnDaysSelectedListener() {
                            @Override
                            public void onDaysSelected(DayPickerView dayPickerView, boolean[] selectedDays) {

                                for(int i=0; i<selectedDays.length; i++)
                                {
                                    if(selectedDays[i])
                                    {
                                        day_Schedule = weekdays[i];
                                        Log.d("Week Days ", day_Schedule);
                                        weekly_day_picker.setText("Every " +day_Schedule);
                                    }
                                }
                            }
                        });
                builder.build().show();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);

                if(schedule_on_off.isChecked())
                {
                    if(!daily.isChecked() && !weekly.isChecked())
                    {
                        Toast.makeText(BackupActivity.this, "Select a backup type", Toast.LENGTH_SHORT).show();
                    }
                    else if(daily.isChecked() || weekly.isChecked())
                    {
                        setPreferenceBoolean(BackupActivity.this, isBACKUPSCHEDULED, true);

                        if(hour_Schedule == 0)
                        {
                            setPreferenceInt(BackupActivity.this, HOURSofSCHEDULE, 23);
                        }
                        else
                        {
                            setPreferenceInt(BackupActivity.this, HOURSofSCHEDULE, hour_Schedule);
                        }

                        if(minute_Schedule == 0)
                        {
                            setPreferenceInt(BackupActivity.this, MINUTESofSCHEDULE, 00);
                        }
                        else
                        {
                            setPreferenceInt(BackupActivity.this, MINUTESofSCHEDULE, minute_Schedule);
                        }

                        if(daily.isChecked())
                        {
                            setPreferenceString(BackupActivity.this, TYPEofSCHEDULE, "Daily");
                            setPreferenceString(BackupActivity.this, DAYofSCHEDULE, null);
                        }
                        else if(weekly.isChecked())
                        {
                            setPreferenceString(BackupActivity.this, TYPEofSCHEDULE, "Weekly");
                            //setPreferenceInt(BackupActivity.this, HOURSofSCHEDULE, hour_Schedule);
                            //setPreferenceInt(BackupActivity.this, MINUTESofSCHEDULE, minute_Schedule);
                            setPreferenceString(BackupActivity.this, DAYofSCHEDULE, day_Schedule);
                        }

                        ScheduleBackup(v.getContext());

                        dialog.dismiss();
                    }
                }
                else
                {
                    setPreferenceBoolean(BackupActivity.this, isBACKUPSCHEDULED, false);
                    setPreferenceInt(BackupActivity.this, HOURSofSCHEDULE, 0);
                    setPreferenceInt(BackupActivity.this, MINUTESofSCHEDULE, 0);
                    setPreferenceString(BackupActivity.this, DAYofSCHEDULE, null);

                    CancelBackup(v.getContext());
                    dialog.dismiss();
                }
            }
        });
    }

    public void PickTime()
    {
        int hour, minute;

        if(hour_Schedule == 0 && minute_Schedule == 0)
        {
            if(getPreferenceInt(BackupActivity.this, HOURSofSCHEDULE) == 0)
            {
                hour = 23;
            }
            else
            {
                hour = getPreferenceInt(BackupActivity.this, HOURSofSCHEDULE);
            }

            if(getPreferenceInt(BackupActivity.this, MINUTESofSCHEDULE) == 0)
            {
                minute = 00;
            }
            else
            {
                minute = getPreferenceInt(BackupActivity.this, MINUTESofSCHEDULE);
            }
        }
        else
        {
            hour = hour_Schedule;
            minute = minute_Schedule;
        }

        TimePickerDialog timePickerDialog = new TimePickerDialog(BackupActivity.this, R.style.TimePickerTheme, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {

                hour_Schedule = hourOfDay;
                minute_Schedule = minutes;

                Log.d("Time ", hour_Schedule+ ":" +minute_Schedule);

                GetScheduleTime(PICK_TIME_REQUEST);
            }
        }, hour, minute, false);

        timePickerDialog.show();
    }

    public String GetScheduleTime(int code)
    {
        String time = "";
        int hour = 0, minute = 0;

        if(code == SHARED_PREFERENCE_TIME_REQUEST)
        {
            hour = getPreferenceInt(BackupActivity.this, HOURSofSCHEDULE);
            minute = getPreferenceInt(BackupActivity.this, MINUTESofSCHEDULE);
        }
        else if(code == PICK_TIME_REQUEST)
        {
            hour = hour_Schedule;
            minute = minute_Schedule;
        }

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);

        DateFormat dateFormat = new SimpleDateFormat("hh:mm a");
        //Log.d("TimeFormat ", dateFormat.format(c.getTime()).toLowerCase());

        if(hour != 0)
        {
            time = "At " +dateFormat.format(c.getTime()).toLowerCase();
        }

        if(code == PICK_TIME_REQUEST)
        {
            TextView t1 = (TextView) dialog.getWindow().getDecorView().findViewById(R.id.daily_time_picker);
            TextView t2 = (TextView) dialog.getWindow().getDecorView().findViewById(R.id.weekly_time_picker);
            t1.setText(time);
            t2.setText(time);
        }

        return time;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    /*public void GetAccounts()
    {
        accounts = accountManager.getAccountsByType("com.google");

        for (Account account : accounts) {

            Log.d("Gmail ", ""+account.name);

        }
    }

    public void RequestPermission()
    {
        try {
            if (ContextCompat.checkSelfPermission(BackupActivity.this,
                            Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(BackupActivity.this,
                        new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
            } else {

                //
            }
        }
        catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_READ_CONTACTS)
        {
            if(grantResults.length > 0)
            {
                for(int i=0; i<permissions.length; i++)
                {
                    if(permissions[i].equals(Manifest.permission.READ_CONTACTS))
                    {
                        if(grantResults[i] == PackageManager.PERMISSION_GRANTED)
                        {
                            Log.d("Backup ", "Read accounts permission granted.");

                            setPreferencePermission(this, false);
                            HAS_DENIED_TWICE = false;
                            GetAccounts();
                        }
                        else
                        {
                            DENY_COUNT++;
                        }
                    }
                }

                ShowAlertAndRequest();
            }
        }
    }

    public void ShowAlertAndRequest()
    {
        if(HAS_DENIED_TWICE)
        {
            snackbar_msg = "Grant contacts permission from settings";
            ShowSnackbarWithAction(BackupActivity.this, R.id.BackupRelativeLayout, snackbar_msg);

            setPreferencePermission(this, true);
        }
        else
        {
            if(DENY_COUNT == 1)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setMessage("Without accounts permission backup feature won't work. Allow permission?")
                        .setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                RequestPermission();
                            }
                        })

                        .setNegativeButton("Cancel", null)
                        .show();

                Dialog dialog = builder.create();
                dialog.setCanceledOnTouchOutside(false);

            }
            else if(DENY_COUNT > 1)
            {
                snackbar_msg = "Grant contacts permission from settings";
                ShowSnackbarWithAction(BackupActivity.this, R.id.BackupRelativeLayout, snackbar_msg);

                setPreferencePermission(this, true);
            }
        }
    }*/
}
