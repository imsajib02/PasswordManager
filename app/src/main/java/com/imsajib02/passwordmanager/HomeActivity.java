package com.imsajib02.passwordmanager;

import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import static com.imsajib02.passwordmanager.Auth.*;
import static com.imsajib02.passwordmanager.BackupActivity.*;
import static com.imsajib02.passwordmanager.FingerPrintHandler.*;
import static com.imsajib02.passwordmanager.MainActivity.*;
import static com.imsajib02.passwordmanager.MyMethods.*;
import static com.imsajib02.passwordmanager.MyAdapter.*;
import static com.imsajib02.passwordmanager.Support.*;
import static com.imsajib02.passwordmanager.DBHelper.*;
import static com.imsajib02.passwordmanager.Local.*;

public class HomeActivity extends AppCompatActivity {

    Toolbar toolbar;
    private RecyclerView.LayoutManager layoutManager;
    public static RelativeLayout relativeLayout;
    public static View popupView;
    public static View termsConditionsView;
    public static TextView text_no_data, no_data_add, text_or, no_data_restore;
    RelativeLayout relative;

    DBHelper dbHelper;

    public static RecyclerView recyclerView;
    public static EditText[] searchbox = new EditText[1];
    public static Button search;

    CheckNetwork checkNetwork;
    BackupActivity backupActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //relative = findViewById(R.id.tandc);

        dbHelper = new DBHelper(this, PRIMARY_DATABASE);
        checkNetwork = new CheckNetwork();
        backupActivity = new BackupActivity();
        InitializeProgressDialog(HomeActivity.this);
        InitializeDatabaseStorage(HomeActivity.this, R.id.relativeLayout);

        this.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        toolbar.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);

        relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        searchbox[0] = (EditText) findViewById(R.id.searchbox);
        search = (Button) findViewById(R.id.search);
        text_no_data = (TextView) findViewById(R.id.text_no_data);
        no_data_add = (TextView) findViewById(R.id.no_data_add_new);
        text_or = (TextView) findViewById(R.id.text_or);
        no_data_restore = (TextView) findViewById(R.id.no_data_restore);

        layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL,false);
        recyclerView.setLayoutManager(layoutManager);

        if(hasSearched)
        {
            recyclerView.setAdapter(SearchedAdapter);

            if(!TextUtils.isEmpty(SearchboxText))
            {
                searchbox[0].setText(SearchboxText);
                SearchboxText = "";
                TextWatcher(this, searchbox);
            }
        }
        else
        {
            dbHelper.getAll(this);

            if(!TextUtils.isEmpty(SearchboxText))
            {
                searchbox[0].setText(SearchboxText);
                SearchboxText = "";
                TextWatcher(this, searchbox);
            }
        }

        if(ChangePin)
        {
            ChangePin = false;

            snackbar_msg = "Pin changed";
            ShowSnackbar(HomeActivity.this, R.id.relativeLayout, snackbar_msg);
        }
        else if(ChangeQuestion)
        {
            ChangeQuestion = false;

            snackbar_msg = "Security question changed";
            ShowSnackbar(HomeActivity.this, R.id.relativeLayout, snackbar_msg);
        }

        no_data_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NewEntryPopup(HomeActivity.this, INSERT_PAGE);
            }
        });

        no_data_restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ThereIsInternet(v.getContext(), R.id.relativeLayout, NORMAL_REQUEST))
                {
                    if(TextUtils.isEmpty(getPreferenceString(v.getContext(), ACCOUNTNAME)))
                    {
                        backupActivity.GetUserAccounts(HomeActivity.this, RESTORE_REQUEST);
                    }
                    else
                    {
                        progressDialog.setMessage("Please wait...");
                        progressDialog.show();

                        //AuthUser(accountName);
                        backupActivity.CheckUserAccount(getPreferenceString(v.getContext(), ACCOUNTNAME), HomeActivity.this, R.id.relativeLayout);
                    }
                }
            }
        });

        searchbox[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CloseAllSwipeLayout();
                searchbox[0].setCursorVisible(true);
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);
                CloseAllSwipeLayout();

                InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(relativeLayout.getWindowToken(), 0);

                if(isEmptyDatabase)
                {
                    snackbar_msg = "No data available";
                    ShowSnackbar(HomeActivity.this, R.id.relativeLayout, snackbar_msg);
                }
                else
                {
                    if(TextUtils.isEmpty(searchbox[0].getText().toString()))
                    {
                        searchbox[0].requestFocus();
                        snackbar_msg = "Enter title";
                        ShowSnackbar(HomeActivity.this, R.id.relativeLayout, snackbar_msg);
                    }
                    else
                    {
                        Log.d("Search ", "Performing search from user input.");
                        dbHelper.getData(v.getContext(), searchbox[0].getText().toString());
                        TextWatcher(v.getContext(), searchbox);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == RESTORE_REQUEST && resultCode == RESULT_OK)
        {
            String mail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            Log.d("Restore ", "Gmail account: "+mail);

            progressDialog.setMessage("Please wait...");
            progressDialog.show();

            //AuthUser(mail);
            backupActivity.CheckUserAccount(mail, user_account_context, user_account_resId);
        }
    }

    @Override
    protected void onPause() {

        unregisterReceiver(checkNetwork);

        try
        {
            if(fileDownloadTask.isInProgress())
            {
                fileDownloadTask.pause();
                Log.d("Restore ", "HomeActivity in background. Pausing download task.");
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
            if(fileDownloadTask.isPaused())
            {
                fileDownloadTask.resume();
                Log.d("Restore ", "HomeActivity in foreground. Resuming download task.");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        super.onResume();
    }

    @Override
    public void onBackPressed() {

        if((!ChangePin && !ChangeQuestion))
        {
            hasSearched = false;
            SearchboxText = "";
        }

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.toolbarmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.addnew:

                //ResetSearchBox();
                CloseAllSwipeLayout();
                NewEntryPopup(HomeActivity.this, INSERT_PAGE);
                return true;

            case R.id.changepin:

                //ResetSearchBox();
                SearchboxText = searchbox[0].getText().toString();
                CloseAllSwipeLayout();
                inPinConfirmPage = true;
                ChangePin = true;
                onBackPressed();
                return true;

            case R.id.changesecques:

                //ResetSearchBox();
                SearchboxText = searchbox[0].getText().toString();
                CloseAllSwipeLayout();
                inPinConfirmPage = true;
                ChangeQuestion = true;
                onBackPressed();
                return true;

            case R.id.backup:

                SearchboxText = searchbox[0].getText().toString();
                CloseAllSwipeLayout();
                Intent intent = new Intent(HomeActivity.this, BackupActivity.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.privacy_policy:

                CloseAllSwipeLayout();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/pass-manager-privacy-policy-02"));
                startActivity(browserIntent);
                return true;

            case R.id.terms_conditions:

                CloseAllSwipeLayout();
                Intent browserIntent2 = new Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/pass-manager-terms-02"));
                startActivity(browserIntent2);
                return true;

            case R.id.about:

                CloseAllSwipeLayout();
                ShowAboutDialog(this);
                //ResetSearchBox();
                return true;

            case R.id.logout:

                onBackPressed();
                return true;

            default:
                //ResetSearchBox();
                CloseAllSwipeLayout();
                return super.onOptionsItemSelected(item);

        }
    }



    public static void ResetSearchBox()
    {
        searchbox[0].setText("");
        searchbox[0].setCursorVisible(false);
    }
}
