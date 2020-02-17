package com.imsajib02.passwordmanager;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.fingerprint.FingerprintManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.imsajib02.passwordmanager.Auth.*;
import static com.imsajib02.passwordmanager.FingerPrintHandler.*;
import static com.imsajib02.passwordmanager.Local.*;
import static com.imsajib02.passwordmanager.MyMethods.*;
import static com.imsajib02.passwordmanager.Support.*;

public class MainActivity extends AppCompatActivity {

    public static int GET_PIN;
    public static String GET_QUESTION = "";
    public static String GET_ANSWER = "";

    public static int NO_PIN = 0;
    public static int PIN_RESET = 404;

    public static boolean hasForgotPassword = false;
    public static boolean ChangePin = false;
    public static boolean inPinConfirmPage = false;
    public static boolean inPinChangePage = false;
    public static boolean ChangeQuestion = false;
    public static boolean inQuestionChangePage = false;

    public static int VIEWFLIPPER_SET_PIN_PAGE = 0;
    public static int VIEWFLIPPER_SET_RECOVERY_PAGE = 1;

    public static TextView textView, forgot_pin, set_sec_ques, questionview;
    public static EditText[] pin = new EditText[4];
    public static Button[] number = new Button[10];
    public static Button clear, ok, complete;
    public static ViewFlipper viewFlipper;
    public static Spinner spinner;
    public static EditText answer;
    public static RelativeLayout spinerlayout;
    public static ImageView dropimage, imageview;

    public static String userpin = "";
    public static String snackbar_msg = "";

    public static FingerprintManager fingerprintManager;
    public static MediaPlayer mediaPlayer = new MediaPlayer();
    public FingerPrintHandler fingerPrintHandler;
    public boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewFlipper = (ViewFlipper) findViewById(R.id.viewflipper);
        textView = (TextView) findViewById(R.id.mytext);
        forgot_pin = (TextView) findViewById(R.id.forgot_pin);
        pin[0] = (EditText) findViewById(R.id.pin1);
        pin[1] = (EditText) findViewById(R.id.pin2);
        pin[2] = (EditText) findViewById(R.id.pin3);
        pin[3] = (EditText) findViewById(R.id.pin4);
        number[0] = (Button) findViewById(R.id.button0);
        number[1] = (Button) findViewById(R.id.button1);
        number[2] = (Button) findViewById(R.id.button2);
        number[3] = (Button) findViewById(R.id.button3);
        number[4] = (Button) findViewById(R.id.button4);
        number[5] = (Button) findViewById(R.id.button5);
        number[6] = (Button) findViewById(R.id.button6);
        number[7] = (Button) findViewById(R.id.button7);
        number[8] = (Button) findViewById(R.id.button8);
        number[9] = (Button) findViewById(R.id.button9);
        clear = (Button) findViewById(R.id.button_clear);
        ok = (Button) findViewById(R.id.ok);
        complete = (Button) findViewById(R.id.complete);
        spinner = (Spinner) findViewById(R.id.spinner);
        questionview = (TextView) findViewById(R.id.question);
        set_sec_ques = (TextView) findViewById(R.id.set_sec_ques);
        answer = (EditText) findViewById(R.id.answer);
        spinerlayout = (RelativeLayout) findViewById(R.id.spinnerlayout);
        dropimage = (ImageView) findViewById(R.id.dropimage);
        imageview = (ImageView) findViewById(R.id.imageView);


        //mediaPlayer = MediaPlayer.create(this, R.raw.button_click);

        try
        {
            Log.d("Authentication ", "Getting user credentials...");
            GetUserCredentials(this);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }


        //keypad disabled for pin codes
        for(int i=0; i<pin.length; i++)
        {
            pin[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            for(int j=0; j<i; j++)
            {
                if(TextUtils.isEmpty(pin[j].getText().toString()))
                {
                    pin[j].requestFocus();
                    break;
                }
            }
        }


        if(GET_PIN == NO_PIN && GET_QUESTION == null && GET_ANSWER == null && !ChangePin && !ChangeQuestion)
        {
            Log.d("Setup ", "App opened for the first time.");

            setPreferenceBoolean(MainActivity.this, isRESTORENEEDED, true);
            ViewFlipperFirstLogin();
            TextWatcher(this, pin);
        }
        else if(GET_PIN != NO_PIN && GET_QUESTION == null && GET_ANSWER == null && !ChangePin && !ChangeQuestion)
        {
            Log.d("Setup ", "Pin codes are set but security question is not set.");

            ViewFlipperSetSecurityQuestion();
            viewFlipper.showNext();
        }
        else if(ChangePin || ChangeQuestion)
        {
            ViewFlipperPinConfirmation();
            TextWatcher(this, pin);
            Log.d("Change of Credentials ", "Pin confirmation from the user.");
        }
        else if(GET_PIN != NO_PIN && GET_QUESTION != null && GET_ANSWER != null && !ChangePin && !ChangeQuestion)
        {
            Log.d("Password Manager ", "Active user login.");

            ViewFlipperLogin(this);
            TextWatcher(this, pin);
        }


        //custom keyboard click
        for(int i=0; i<number.length; i++)
        {
            final int finalI = i;

            number[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    ButtonPressAnimation(v);

                    //mediaPlayer.start();

                    for(int j=0; j<pin.length; j++)
                    {
                        if(pin[j].hasFocus())
                        {
                            if(TextUtils.isEmpty(pin[j].getText().toString()))
                            {
                                pin[j].setText(number[finalI].getText().toString());
                                pin[j].setBackgroundResource(R.drawable.my_edittext4);
                                break;
                            }
                        }
                    }
                }
            });
        }


        /*try
        {
            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {

                    mediaPlayer.release();
                }
            });
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }*/


        forgot_pin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);
                StopAuthentication();

                Log.d("Forgot Pin ", "Verifying user through security question.");
                hasForgotPassword = true;
                ViewFlipperVerification();
                SetViewFlipperAnimation(v.getContext(), R.anim.in_from_right, R.anim.out_from_left);
                viewFlipper.showNext();
            }
        });


        //clear pin one by one
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);

                for(int j=0; j<pin.length; j++)
                {
                    if(pin[j].hasFocus())
                    {
                        if(TextUtils.isEmpty(pin[j].getText().toString()) && j != 0)
                        {
                            pin[j-1].requestFocus();
                            pin[j-1].setText("");
                            pin[j-1].setBackgroundResource(R.drawable.my_edittext);
                        }
                        else
                        {
                            pin[j].setText("");
                            pin[j].setBackgroundResource(R.drawable.my_edittext);
                        }
                    }
                }
            }
        });



        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);

                GetPinFromUserInput(v.getContext());

                if(!TextUtils.isEmpty(userpin))
                {
                    if(GET_PIN == NO_PIN && !inPinConfirmPage && !inPinChangePage)
                    {
                        setPreferenceInt(v.getContext(), PINCODE, Integer.parseInt(userpin));
                        Log.d("Setup ", "Pin set to local storage.");

                        ViewFlipperSetSecurityQuestion();
                        SetViewFlipperAnimation(v.getContext(), R.anim.in_from_right, R.anim.out_from_left);
                        viewFlipper.showNext();
                    }
                    else if(GET_PIN != NO_PIN && GET_PIN != PIN_RESET && !inPinConfirmPage && !inPinChangePage)
                    {
                        if(Integer.parseInt(userpin) == GET_PIN)
                        {
                            Log.d("Login ", "User logged in.");

                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Log.d("Login ", "Pin does not match.");

                            snackbar_msg = "Incorrect pin";
                            ShowSnackbar(MainActivity.this, R.id.viewflipper, snackbar_msg);
                        }
                    }
                    else if(GET_PIN == PIN_RESET && !inPinConfirmPage && !inPinChangePage)
                    {
                        setPreferenceInt(v.getContext(), PINCODE, Integer.parseInt(userpin));
                        Log.d("Forgot Pin ", "Pin reset to local storage.");

                        SetQuestionViewMargin(50);

                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else if(inPinConfirmPage && !inPinChangePage)
                    {
                        if(Integer.parseInt(userpin) == GET_PIN)
                        {
                            Log.d("Change of Credentials ", "Pin confirmed by the user.");

                            SetViewFlipperAnimation(v.getContext(), R.anim.in_from_right, R.anim.out_from_left);

                            if(ChangePin)
                            {
                                inPinConfirmPage = false;
                                inPinChangePage = true;
                                ClearPin();

                                ViewFlipperChangePin();
                                viewFlipper.setDisplayedChild(VIEWFLIPPER_SET_PIN_PAGE);
                                TextWatcher(v.getContext(), pin);
                            }
                            else if(ChangeQuestion)
                            {
                                inPinConfirmPage = false;
                                inQuestionChangePage = true;
                                ClearPin();

                                ViewFlipperChangeSecurityQuestion();
                                viewFlipper.setDisplayedChild(VIEWFLIPPER_SET_RECOVERY_PAGE);
                            }
                        }
                        else
                        {
                            Log.d("Change of Credentials ", "Pin does not match.");

                            snackbar_msg = "Incorrect pin";
                            ShowSnackbar(MainActivity.this, R.id.viewflipper, snackbar_msg);
                        }
                    }
                    else if(inPinChangePage && !inPinConfirmPage)
                    {
                        if(Integer.parseInt(userpin) == GET_PIN)
                        {
                            Log.d("Change of Pin ", "Trying to set same old pin.");

                            snackbar_msg = "Use different pin";
                            ShowSnackbar(MainActivity.this, R.id.viewflipper, snackbar_msg);
                        }
                        else
                        {
                            setPreferenceInt(v.getContext(), PINCODE, Integer.parseInt(userpin));
                            Log.d("Change of Pin ", "New Pin stored to local storage.");

                            inPinChangePage = false;

                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                }
            }
        });



        final ArrayAdapter<String> ar = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, QuestionSet){

            @Override
            public boolean isEnabled(int position){
                if(position == 0)
                {
                    //Disable the first item of Spinner
                    return false;
                }
                else
                {
                    return true;
                }
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;

                if(position == 0)
                {
                    //Set the hint text color
                    tv.setTextColor(Color.WHITE);
                }
                else
                {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {

                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;

                if(position == 0)
                {
                    //Set the disabled item text color
                    tv.setTextColor(Color.GRAY);
                }
                else
                {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }

        };

        ar.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(ar);



        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if(position > 0)
                {
                    if(inQuestionChangePage)
                    {
                        if(TextUtils.equals(spinner.getItemAtPosition(position).toString(), GET_QUESTION))
                        {
                            Log.d("Change of Question ", "Trying to set the same old question.");

                            spinner.setSelection(0);
                            questionview.setText("");
                            snackbar_msg = "Choose different question";
                            ShowSnackbar(MainActivity.this, R.id.viewflipper, snackbar_msg);
                        }
                        else
                        {
                            spinner.setSelection(0);
                            questionview.setText(spinner.getItemAtPosition(position).toString());
                        }
                    }
                    else
                    {
                        spinner.setSelection(0);
                        questionview.setText(spinner.getItemAtPosition(position).toString());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        complete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);

                String ans = answer.getText().toString();

                if(!hasForgotPassword && !inQuestionChangePage)
                {
                    if(TextUtils.isEmpty(questionview.getText().toString()))
                    {
                        snackbar_msg = "Select a question";
                        ShowSnackbar(MainActivity.this, R.id.viewflipper, snackbar_msg);
                    }
                    else
                    {
                        if(TextUtils.isEmpty(ans))
                        {
                            answer.requestFocus();
                            snackbar_msg = "Put your answer";
                            ShowSnackbar(MainActivity.this, R.id.viewflipper, snackbar_msg);
                        }
                        else
                        {
                            setPreferenceString(v.getContext(), QUESTION, questionview.getText().toString());
                            setPreferenceString(v.getContext(), ANSWER, ans);
                            Log.d("Setup ", "Security question and answer set to local storage.");

                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                        }
                    }
                }
                else if(hasForgotPassword && !inQuestionChangePage)
                {
                    if(TextUtils.isEmpty(ans))
                    {
                        answer.requestFocus();
                        snackbar_msg = "Put your answer";
                        ShowSnackbar(MainActivity.this, R.id.viewflipper, snackbar_msg);
                    }
                    else
                    {
                        if(TextUtils.equals(ans, GET_ANSWER))
                        {
                            Log.d("Forgot Password ", "Answer matched. Sending user to pin reset page.");

                            GET_PIN = PIN_RESET;

                            ViewFlipperResetPin();
                            TextWatcher(v.getContext(), pin);

                            SetViewFlipperAnimation(v.getContext(), R.anim.in_from_right, R.anim.out_from_left);
                            viewFlipper.showPrevious();
                        }
                        else
                        {
                            Log.d("Forgot Password ", "Answer does not match.");

                            snackbar_msg = "Answer do not match";
                            ShowSnackbar(MainActivity.this, R.id.viewflipper, snackbar_msg);
                        }
                    }
                }
                else if(inQuestionChangePage)
                {
                    if(TextUtils.isEmpty(questionview.getText().toString()))
                    {
                        snackbar_msg = "Select a question";
                        ShowSnackbar(MainActivity.this, R.id.viewflipper, snackbar_msg);
                    }
                    else
                    {
                        if(TextUtils.isEmpty(ans))
                        {
                            answer.requestFocus();
                            snackbar_msg = "Put your answer";
                            ShowSnackbar(MainActivity.this, R.id.viewflipper, snackbar_msg);
                        }
                        else
                        {
                            setPreferenceString(v.getContext(), QUESTION, questionview.getText().toString());
                            setPreferenceString(v.getContext(), ANSWER, ans);
                            Log.d("Change of Question ", "New security question and answer set to local storage.");

                            inQuestionChangePage = false;

                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                            startActivity(intent);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onPause() {

        StopAuthentication();
        //mediaPlayer.release();
        super.onPause();
    }

    @Override
    public void onResume(){

        if(!ChangePin && !ChangeQuestion && !hasForgotPassword
                && GET_PIN != NO_PIN && GET_QUESTION != null && GET_ANSWER != null && GET_PIN != PIN_RESET)
        {
            //Toast.makeText(this, "yesss", Toast.LENGTH_SHORT).show();
            CheckBiometricAvailability(MainActivity.this);
        }

        super.onResume();
    }

    @Override
    public void onBackPressed() {

        if(viewFlipper.getDisplayedChild() == VIEWFLIPPER_SET_PIN_PAGE)
        {
            if(hasForgotPassword)
            {
                hasForgotPassword = false;

                Log.d("Forgot Pin ", "Pin reset page back pressed. Sending user to login page.");

                ViewFlipperLogin(this);
                ClearPin();
                TextWatcher(this, pin);

                SetQuestionViewMargin(50);

                SetViewFlipperAnimation(this, R.anim.in_from_left, R.anim.out_from_right);
                viewFlipper.setDisplayedChild(VIEWFLIPPER_SET_PIN_PAGE);
            }
            else if(inPinConfirmPage)
            {
                inPinConfirmPage = false;
                ChangePin = false;
                ChangeQuestion = false;

                Log.d("Change of Credentials ", "Pin verification back pressed. Sending user to home page.");

                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
            else if(inPinChangePage)
            {
                inPinChangePage = false;
                ChangePin = false;
                ChangeQuestion = false;

                Log.d("Change of Pin ", "Change page back pressed. Sending user to home page.");

                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                SystemExit();
            }
        }

        else if(viewFlipper.getDisplayedChild() == VIEWFLIPPER_SET_RECOVERY_PAGE)
        {
            GetUserCredentials(this);

            if(hasForgotPassword)
            {
                hasForgotPassword = false;

                //ViewFlipperSetSecurityQuestion();
                Log.d("Forgot Pin ", "Verification page back pressed. Sending user to login page.");

                SetQuestionViewMargin(50);

                ViewFlipperLogin(this);
                ClearPin();
                TextWatcher(this, pin);

                SetViewFlipperAnimation(this, R.anim.in_from_left, R.anim.out_from_right);
                viewFlipper.showPrevious();
            }
            else if(inQuestionChangePage)
            {
                inQuestionChangePage = false;
                ChangeQuestion = false;
                ChangePin = false;

                Log.d("Change of Question ", "Change page back pressed. Sending user to home page.");

                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                SystemExit();
            }
        }

        else
        {
            super.onBackPressed();
        }
    }

    public void SystemExit()
    {
        //press again to exit
        if(doubleBackToExitPressedOnce)
        {
            Log.d("Password Manager ", "Closing the app.");

            this.finishAffinity();
            System.exit(0);
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}
