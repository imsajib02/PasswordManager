package com.imsajib02.passwordmanager;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.imsajib02.passwordmanager.BackupActivity.BACKUP_REQUEST;
import static com.imsajib02.passwordmanager.BackupActivity.RESTORE_REQUEST;
import static com.imsajib02.passwordmanager.HomeActivity.*;
import static com.imsajib02.passwordmanager.Local.HOURSofSCHEDULE;
import static com.imsajib02.passwordmanager.Local.MINUTESofSCHEDULE;
import static com.imsajib02.passwordmanager.Local.getPreferenceInt;
import static com.imsajib02.passwordmanager.MainActivity.*;
import static com.imsajib02.passwordmanager.DBHelper.*;
import static com.imsajib02.passwordmanager.Support.*;

public class MyMethods{

    public static DBHelper mdbhelper;
    public static EditText[] entries = new EditText[6];
    public static Button create, cancel;
    public static LinearLayout.LayoutParams parameter =  (LinearLayout.LayoutParams) questionview.getLayoutParams();


    public static void TextWatcher(final Context context, final EditText[] editTexts)
    {
        mdbhelper = new DBHelper(context, PRIMARY_DATABASE);

        for(int i=0; i<editTexts.length; i++)
        {
            final int finalI = i;

            editTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    try
                    {
                        if(editTexts == pin)
                        {
                            if(count>0)
                            {
                                if(finalI<(editTexts.length-1))
                                {
                                    editTexts[finalI+1].requestFocus();
                                }
                                else if(finalI==(editTexts.length-1))
                                {
                                    //editTexts[finalI].setText(s);
                                    //Log.d("PasswordManager", ""+s);
                                }
                            }
                        }
                        else if(TextUtils.equals(create.getText().toString(), "EDIT") && editTexts == entries)
                        {
                            create.setText("UPDATE");
                        }
                        else if(TextUtils.equals(create.getText().toString(), "UPDATE") && editTexts == entries)
                        {
                            create.setEnabled(true);
                        }
                        else if(editTexts == searchbox)
                        {
                            //on searchbox input clear, show all data in view
                            if(s.length() == 0)
                            {
                                hasSearched = false;
                                SearchboxText = "";
                                mdbhelper.getAll(context);
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();

                        //on searchbox input clear, show all data in view
                        if(s.length() == 0)
                        {
                            hasSearched = false;
                            SearchboxText = "";
                            mdbhelper.getAll(context);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
        }
    }

    public static void ViewFlipperFirstLogin()
    {
        imageview.setImageResource(R.drawable.padlock);
        textView.setText("Set Your Pin Code");
        forgot_pin.setVisibility(View.INVISIBLE);
    }

    public static void ViewFlipperLogin(Context context)
    {
        textView.setText("Your 4 Digit Pin");
        forgot_pin.setVisibility(View.VISIBLE);

        CheckBiometricAvailability(context);
    }

    public static void ViewFlipperResetPin()
    {
        imageview.setImageResource(R.drawable.padlock);
        textView.setText("Reset Your Pin");
        forgot_pin.setVisibility(View.INVISIBLE);
    }

    public static void ViewFlipperSetSecurityQuestion()
    {
        set_sec_ques.setText("Set Security Question");
        spinerlayout.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
        dropimage.setVisibility(View.VISIBLE);
        questionview.setText("");
        answer.setText("");
        complete.setText("COMPLETE SETUP");
    }

    public static void ViewFlipperVerification()
    {
        set_sec_ques.setText("Verify Your Identity");
        spinner.setVisibility(View.INVISIBLE);
        spinerlayout.setVisibility(View.INVISIBLE);
        dropimage.setVisibility(View.INVISIBLE);

        SetQuestionViewMargin(-50);

        questionview.setText(GET_QUESTION);
        answer.setText("");
        complete.setText("VERIFY");
    }

    public static void SetQuestionViewMargin(int a)
    {
        parameter.setMargins(parameter.leftMargin, parameter.topMargin+(a), parameter.rightMargin, parameter.bottomMargin);
        questionview.setLayoutParams(parameter);
    }

    public static void ViewFlipperPinConfirmation()
    {
        imageview.setImageResource(R.drawable.padlock);
        textView.setText("Confirm Your Pin");
        forgot_pin.setVisibility(View.INVISIBLE);
    }

    public static void ViewFlipperChangePin()
    {
        imageview.setImageResource(R.drawable.padlock);
        textView.setText("Your New Pin");
        forgot_pin.setVisibility(View.INVISIBLE);
    }

    public static void ViewFlipperChangeSecurityQuestion()
    {
        set_sec_ques.setText("Set New Security Question");
        spinerlayout.setVisibility(View.VISIBLE);
        spinner.setVisibility(View.VISIBLE);
        dropimage.setVisibility(View.VISIBLE);
        questionview.setText("");
        answer.setText("");
        complete.setText("CHANGE");
    }

    public static void ClearPin()
    {
        for(int i=0; i<pin.length; i++)
        {
            pin[i].setText("");
            pin[i].setBackgroundResource(R.drawable.my_edittext);
        }

        pin[0].requestFocus();
    }

    public static void SetViewFlipperAnimation(Context context, int transition_in, int transition_out)
    {
        Animation in = AnimationUtils.loadAnimation(context, transition_in);
        Animation out = AnimationUtils.loadAnimation(context, transition_out);

        viewFlipper.setInAnimation(in);
        viewFlipper.setOutAnimation(out);
    }

    public static void GetPinFromUserInput(Context context)
    {
        userpin = "";

        for(int i=0; i<pin.length; i++)
        {
            if(TextUtils.isEmpty(pin[i].getText().toString()))
            {
                pin[i].requestFocus();
                snackbar_msg = "Enter whole pin";
                ShowSnackbar((Activity)context, R.id.viewflipper, snackbar_msg);
                userpin = "";
                break;
            }
            else
            {
                userpin = userpin + pin[i].getText();
            }
        }
    }

    public static void NewEntryPopup(final Activity context, final int page) {

        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.layout_add_new, null);

        mdbhelper = new DBHelper(context, PRIMARY_DATABASE);

        entries[0] = (EditText) popupView.findViewById(R.id.title);
        entries[1] = (EditText) popupView.findViewById(R.id.email);
        entries[2] = (EditText) popupView.findViewById(R.id.password);
        entries[3] = (EditText) popupView.findViewById(R.id.username);
        entries[4] = (EditText) popupView.findViewById(R.id.phone);
        entries[5] = (EditText) popupView.findViewById(R.id.recoverymail);
        create = (Button) popupView.findViewById(R.id.create);
        cancel = (Button) popupView.findViewById(R.id.cancel);

        TextWatcher(context, entries);

        if(page == VIEW_PAGE)
        {
            for(int i=0; i<entries.length; i++)
            {
                entries[i].setEnabled(false);
                entries[i].setText(popup_data[i+1]);
            }

            entries[2].setInputType(InputType.TYPE_CLASS_TEXT);
            create.setText("EDIT");
        }
        else if(page == UPDATE_PAGE)
        {
            for(int i=0; i<entries.length; i++)
            {
                entries[i].setText(popup_data[i+1]);
            }

            create.setEnabled(false);
            create.setText("UPDATE");

        }

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // does not let taps outside the popup dismiss it
        final PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupWindow.showAtLocation(relativeLayout, Gravity.FILL, 0, 0);
        //relativeLayout.setAlpha(0.2F);
        context.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);

                if(TextUtils.equals(create.getText().toString(), "ADD NEW"))
                {
                    EmptyChecking(context);

                    if(!isEmpty)
                    {
                        mdbhelper.InsertNewEntry(context);
                    }
                }
                if(TextUtils.equals(create.getText().toString(), "UPDATE"))
                {
                    EmptyChecking(context);

                    if(!isEmpty)
                    {
                        mdbhelper.updateData(context, Integer.parseInt(popup_data[0]));
                    }
                }
                else if(TextUtils.equals(create.getText().toString(), "EDIT"))
                {
                    //create.setText("UPDATE");
                    //editClicked = true;
                    create.setEnabled(false);

                    for(int i=0; i<entries.length; i++)
                    {
                        entries[i].setEnabled(true);
                    }

                    entries[2].setInputType(InputType.TYPE_CLASS_TEXT |
                            InputType.TYPE_TEXT_VARIATION_PASSWORD);

                    //TextWatcher(context, entries);
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);
                popupWindow.dismiss();
            }
        });
    }

    public static void viewData(Cursor result, Context context)
    {
        List<Items> listdata = new ArrayList<>();

        while(result.moveToNext())
        {
            String first_letter = String.valueOf(result.getString(1).charAt(0)).toLowerCase();

            for(int i=0; i<letters.length; i++)
            {
                if(TextUtils.equals(first_letter, letters[i]))
                {
                    listdata.add(new Items(images[i], result.getInt(0), result.getString(1),
                            result.getString(2), result.getString(3), result.getString(4),
                            result.getString(5), result.getString(6)));
                }
            }
        }

        MyAdapter myAdapter = new MyAdapter(context, SortData(listdata));
        recyclerView.setAdapter(myAdapter);

        result.close();
    }

    public static List<Items> SortData(List<Items> list)
    {
        Collections.sort(list, new Comparator<Items>(){
            public int compare(Items s1, Items s2) {
                return s1.getTitle().compareToIgnoreCase(s2.getTitle());
            }
        });

        return list;
    }

    public static void EmptyChecking(Context context)
    {
        for(int i=0; i<entries.length; i++)
        {
            if(TextUtils.isEmpty(entries[i].getText().toString()))
            {
                entries[i].requestFocus();
                snackbar_msg = "Insert " +COLUMN[i].toLowerCase();
                ShowSnackbar(popupView, snackbar_msg);
                isEmpty = true;
                break;
            }
            else
            {
                isEmpty = false;
            }
        }
    }

    public static void ShowSnackbarWithAction(final Activity activity, int resid, String msg)
    {
        Snackbar snack = Snackbar.make(activity.findViewById(resid), msg, Snackbar.LENGTH_LONG);
        View view = snack.getView();

        view.setBackgroundColor(Color.parseColor("#262e4e"));
        view.setPadding(0,0,0,0);

        snack.setAction("Go", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                //Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                //intent.setData(uri);
                activity.startActivity(intent);
            }
        });
                //.setActionTextColor(activity.getResources().getColor(R.color.colorYellow));

        snack.show();
    }

    public static void ShowSnackbar(View rootview, String msg)
    {
        Snackbar snack = Snackbar.make(rootview, msg, Snackbar.LENGTH_SHORT);
        View view = snack.getView();
        view.setBackgroundColor(Color.parseColor("#262e4e"));

        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        //tv.setTextColor(Color.YELLOW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        else
        {
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        snack.show();
    }

    public static void ShowSnackbar(Activity activity, int layout_id, String msg)
    {
        Snackbar snack = Snackbar.make(activity.findViewById(layout_id), msg, Snackbar.LENGTH_SHORT);
        View view = snack.getView();
        view.setBackgroundColor(Color.parseColor("#262e4e"));

        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        //tv.setTextColor(Color.YELLOW);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
        else
        {
            tv.setGravity(Gravity.CENTER_HORIZONTAL);
        }

        snack.show();
    }

    public static void ButtonPressAnimation(View v)
    {
        Animation animation = AnimationUtils.loadAnimation(v.getContext(), R.anim.blink_anim);
        v.startAnimation(animation);
    }

    public static void ShowAboutDialog(Context context)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View view = inflater.inflate(R.layout.dialog_about, null);

        ImageView fb = view.findViewById(R.id.fblink);
        ImageView insta = view.findViewById(R.id.instalink);

        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/imsajib02"));
                v.getContext().startActivity(browserIntent);
            }
        });

        insta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/imsajib2"));
                v.getContext().startActivity(browserIntent);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view)
                .show();

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

    }

    public static void CheckBiometricAvailability(Context context)
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !ChangePin && !ChangeQuestion)
        {
            fingerprintManager = (FingerprintManager) context.getSystemService(FINGERPRINT_SERVICE);

            if(fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints())
            {
                imageview.setImageResource(R.drawable.fingerprint_1);
                textView.setText("Use Fingerprint or Pin");

                FingerPrintHandler fingerPrintHandler = new FingerPrintHandler(context);

                fingerPrintHandler.StartAuthentication(fingerprintManager, null);
            }
        }
    }

    public static boolean ThereIsInternet(Context context, int resId, int code)
    {
        if(isConnected)
        {
            if(isInternetAvailable)
            {
                return true;
            }
            else
            {
                if(code == NORMAL_REQUEST)
                {
                    snackbar_msg = "No internet available";
                    ShowSnackbar((Activity)context, resId, snackbar_msg);
                }
                else if(code == BACKUP_REQUEST)
                {
                    snackbar_msg = "Backup failed. No internet.";
                    ShowSnackbarWithAction((Activity)context, resId, snackbar_msg);
                }
                else if(code == RESTORE_REQUEST)
                {
                    snackbar_msg = "Failed to restore. No internet.";
                    ShowSnackbarWithAction((Activity)context, resId, snackbar_msg);
                }

                return false;
            }
        }
        else
        {
            if(code == NORMAL_REQUEST)
            {
                snackbar_msg = "Turn on wifi/mobile data";
                ShowSnackbarWithAction((Activity)context, resId, snackbar_msg);
            }
            else if(code == BACKUP_REQUEST)
            {
                snackbar_msg = "Backup failed. Turn on wifi/mobile data.";
                ShowSnackbarWithAction((Activity)context, resId, snackbar_msg);
            }
            else if(code == RESTORE_REQUEST)
            {
                snackbar_msg = "Failed to restore. Turn on wifi/mobile data.";
                ShowSnackbarWithAction((Activity)context, resId, snackbar_msg);
            }

            return false;
        }
    }

    public static void InitializeProgressDialog(Context context)
    {
        progressDialog = new ProgressDialog(context);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
    }

    public static void InitializeDatabaseStorage(Context context, int resId)
    {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageRef = FirebaseStorage.getInstance().getReference();
        user_account_context = context;
        user_account_resId = resId;
    }

    public static void ToggleHomeActivity(int v1, int v2, boolean bool)
    {
        recyclerView.setVisibility(v1);

        text_no_data.setVisibility(v2);
        no_data_add.setVisibility(v2);
        text_or.setVisibility(v2);
        no_data_restore.setVisibility(v2);

        isEmptyDatabase = bool;
    }

    public static void ScheduleBackup(Context context)
    {
        //CancelBackup(context);

        Intent intent = new Intent(context, MyAlarmReceiver.class);
        intent.putExtra(FAILED_BACKUP, false);

        boolean alarmUp = (PendingIntent.getBroadcast(context, MyAlarmReceiver.REQUEST_CODE1,
                intent, PendingIntent.FLAG_NO_CREATE) != null);

        if(!alarmUp)
        {

        }

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, MyAlarmReceiver.REQUEST_CODE1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long millis = GetScheduledTime(context);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarm.setRepeating(AlarmManager.RTC_WAKEUP, millis,
                AlarmManager.INTERVAL_DAY, pIntent);

        Log.d("Backup ", "Alarm placed.");
    }

    public static void ScheduleFailedBackup(Context context)
    {
        //Scheduling backup after 5 minutes
        long millis = System.currentTimeMillis() + 300000;

        Intent intent = new Intent(context, MyAlarmReceiver.class);
        intent.putExtra(FAILED_BACKUP, true);

        final PendingIntent pIntent = PendingIntent.getBroadcast(context, MyAlarmReceiver.REQUEST_CODE2,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        alarm.set(AlarmManager.RTC_WAKEUP, millis, pIntent);

        Log.d("Backup ", "Failed alarm placed.");
    }

    public static void CancelBackup(Context context)
    {
        Intent intent = new Intent(context, MyAlarmReceiver.class);

        final PendingIntent pIntent1 = PendingIntent.getBroadcast(context, MyAlarmReceiver.REQUEST_CODE1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        final PendingIntent pIntent2 = PendingIntent.getBroadcast(context, MyAlarmReceiver.REQUEST_CODE2,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent1);
        alarm.cancel(pIntent2);
    }

    public static long GetScheduledTime(Context context)
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

        //SimpleDateFormat sdf1 = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a");

        //Date date = new Date();
        //String backupTime = sdf1.format(date)+ " " +hour+ ":" +minute+ ":00";
        //String backupTime = sdf2.format(calendar.getTime());
        Log.d("TimeFormat ", sdf2.format(calendar.getTime()));

        /*try
        {
            date = sdf2.parse(backupTime);
            millis = date.getTime();

            return millis;
        }
        catch(ParseException e)
        {
            e.printStackTrace();
        }*/

        return millis;
    }
}
