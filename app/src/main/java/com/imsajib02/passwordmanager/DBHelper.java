package com.imsajib02.passwordmanager;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import static com.imsajib02.passwordmanager.Local.isBACKUPNEEDED;
import static com.imsajib02.passwordmanager.Local.isRESTORENEEDED;
import static com.imsajib02.passwordmanager.Local.setPreferenceBoolean;
import static com.imsajib02.passwordmanager.MainActivity.snackbar_msg;
import static com.imsajib02.passwordmanager.HomeActivity.*;
import static com.imsajib02.passwordmanager.MyMethods.*;
import static com.imsajib02.passwordmanager.Support.*;

public class DBHelper extends SQLiteOpenHelper {

    public static final String PRIMARY_DATABASE = "PasswordManager.db";
    public static final String SECONDARY_DATABASE = "RestoredDatabase.db";

    public static final String TABLE_NAME = "PasswordStore";
    public static final String[] COLUMN = {"Title", "Email", "Password", "UserName", "Phone", "RecoveryMail"};

    public static String DatabaseAbsolutePath = "";
    BackupActivity backupActivity;

    public DBHelper(Context context, String databaseName)
    {
        super(context, databaseName, null, 1);
        //DatabaseAbsolutePath = context.getDatabasePath(PRIMARY_DATABASE).getAbsolutePath();
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {

        db.disableWriteAheadLogging();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable2 = "CREATE TABLE " + TABLE_NAME +
                " (ID INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN[0] + " TEXT, " + COLUMN[1] + " TEXT, "
                + COLUMN[2] + " TEXT, " + COLUMN[3] + " TEXT, " + COLUMN[4] + " TEXT, " + COLUMN[5] + " TEXT)";
        db.execSQL(createTable2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean HasDuplicate()
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " +COLUMN[0]+ ", " +COLUMN[1]+ " FROM " + TABLE_NAME;
        Cursor result =  db.rawQuery( query, null );

        while(result.moveToNext())
        {
            if(TextUtils.equals(result.getString(0).toLowerCase(), entries[0].getText().toString().toLowerCase())
                    && TextUtils.equals(result.getString(1).toLowerCase(), entries[1].getText().toString().toLowerCase()))
            {
                result.close();
                return true;
            }
        }

        result.close();
        return false;
    }

    public void InsertNewEntry(Context context)
    {
        if(HasDuplicate())
        {
            Log.d("New Entry ", "Duplicate data found.");

            snackbar_msg = "Duplicate data found with same title and email";
            ShowSnackbar(popupView, snackbar_msg);
        }
        else
        {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            for(int i=0; i<COLUMN.length; i++)
            {
                contentValues.put(COLUMN[i], entries[i].getText().toString());
            }

            long result = db.insert(TABLE_NAME, null, contentValues);

            //if data is inserted incorrectly it will return -1
            if(result == -1)
            {
                Log.d("New Entry ", "Insertion unsuccessful.");

                snackbar_msg = "New entry unsuccessful";
                ShowSnackbar(popupView , snackbar_msg);
            }
            else
            {
                Log.d("New Entry ", "Insertion successful.");

                for(int i=0; i<entries.length; i++)
                {
                    entries[i].setText("");
                }

                entries[0].requestFocus();

                setPreferenceBoolean(context, isBACKUPNEEDED, true);
                setPreferenceBoolean(context, isRESTORENEEDED, true);

                getAll(context);

                snackbar_msg = "New entry successful";
                ShowSnackbar(popupView , snackbar_msg);
            }
        }
    }

    public boolean HasData(Context context)
    {
        Log.d("Backup ", "Getting database size.");

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME;

        Cursor result = db.rawQuery( query, null );

        if(result.getCount() > 0)
        {
            result.close();
            return true;
        }

        Log.d("Backup ", "No data in database.");
        result.close();
        return false;
    }

    public void getAll(Context context)
    {
        Log.d("Sqlite ", "Getting all data.");

        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME;

        Cursor result =  db.rawQuery( query, null );

        if(result.getCount() > 0)
        {
            ToggleHomeActivity(View.VISIBLE, View.GONE, false);
            viewData(result, context);
        }
        else
        {
            ToggleHomeActivity(View.GONE, View.VISIBLE, true);
        }

        result.close();
    }

    public int deleteData(int index)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        String query = "ID = '" + index + "'";

        int result =  db.delete(TABLE_NAME, query, null);

        return result;
    }

    public void updateData(Context context, int index)
    {
        if(HasDuplicate())
        {
            Log.d("Update ", "Duplicate data found with same title and email.");

            snackbar_msg = "Duplicate data found";
            ShowSnackbar(popupView, snackbar_msg);
        }
        else
        {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();

            for(int i=0; i<COLUMN.length; i++)
            {
                contentValues.put(COLUMN[i], entries[i].getText().toString());
            }

            String query = "ID = '" + index + "'";

            int result =  db.update(TABLE_NAME, contentValues, query, null);

            if(result > 0)
            {
                Log.d("Update ", "Row "+popup_data[0]+ " updated.");

                for(int i=0; i<entries.length; i++)
                {
                    popup_data[i+1] = entries[i].getText().toString();
                    entries[i].setEnabled(false);
                }

                entries[2].setInputType(InputType.TYPE_CLASS_TEXT);
                create.setText("EDIT");
                //editClicked = false;

                setPreferenceBoolean(context, isBACKUPNEEDED, true);
                setPreferenceBoolean(context, isRESTORENEEDED, false);

                getAll(context);

                snackbar_msg = "Updated";
                ShowSnackbar(popupView , snackbar_msg);
            }
            else
            {
                snackbar_msg = "Update unsuccessful";
                ShowSnackbar(popupView , snackbar_msg);

                Log.d("Update ", "Row "+popup_data[0]+ " update unsuccessful.");
            }
        }
    }

    public void getData(Context context, String title)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN[0] + " = '" + title + "'";

        Cursor result =  db.rawQuery( query, null );

        if(result.getCount() == 0)
        {
            Log.d("Search", "No result found based on user input.");

            snackbar_msg = "No match found";
            ShowSnackbar((Activity)context, R.id.relativeLayout, snackbar_msg);
        }
        else
        {
            Log.d("Search", "Result found based on user input.");

            hasSearched = true;
            SearchboxText = searchbox[0].getText().toString();
            SearchedAdapter = recyclerView.getAdapter();
            viewData(result, context);
        }

        result.close();
    }

    public void AttachDatabase(Context context, int resId)
    {
        DBHelper primary = new DBHelper(context, PRIMARY_DATABASE);
        DBHelper secondary = new DBHelper(context, SECONDARY_DATABASE);

        SQLiteDatabase db1 = primary.getReadableDatabase();
        SQLiteDatabase db2 = secondary.getReadableDatabase();

        Log.d("Primary ", ""+db1.getAttachedDbs());
        Log.d("Secondary ", ""+db2.getAttachedDbs());

        String query1 = "SELECT * FROM " + TABLE_NAME;

        //get data from both database
        Cursor result1 =  db1.rawQuery( query1, null );
        Cursor result2 =  db2.rawQuery( query1, null );

        //check both database for duplicate data
        while(result2.moveToNext())
        {
            result1.moveToFirst();

            while(result1.moveToNext())
            {
                if(TextUtils.equals(result1.getString(1).toLowerCase(), result2.getString(1).toLowerCase())
                    && TextUtils.equals(result1.getString(2).toLowerCase(), result2.getString(2).toLowerCase()))
                {
                    //found duplicate data
                    //delete from restored database
                    db2 = secondary.getWritableDatabase();
                    String query = "ID = '" + result2.getInt(0) + "'";
                    db2.delete(TABLE_NAME, query, null);
                }
            }
        }

        //get all data from restored database after duplicate checking
        db2 = secondary.getReadableDatabase();
        Cursor result3 = db2.rawQuery( query1, null );

        ContentValues contentValues = new ContentValues();

        while(result3.moveToNext())
        {
            for(int i=0; i<COLUMN.length; i++)
            {
                contentValues.put(COLUMN[i], result3.getString(i+1));
            }
        }

        //free up cursor memory/resources
        result1.close();
        result2.close();
        result3.close();

        //insert data from restored database to primary database
        db1 = primary.getWritableDatabase();
        long result4 = db1.insert(TABLE_NAME, null, contentValues);

        backupActivity = new BackupActivity();

        if(result4 == -1)
        {
            progressDialog.dismiss();
            Log.d("Restore ", "Failed to merge data in database.");

            snackbar_msg = "Failed to merge data. Try again.";
            ShowSnackbar((Activity)context, resId, snackbar_msg);
        }
        else
        {
            progressDialog.dismiss();
            Log.d("Restore ", "Successfully merged data in database.");

            //setPreferenceBoolean(context, isBACKUPNEEDED, true);
            setPreferenceBoolean(context, isRESTORENEEDED, false);

            getAll(context);

            snackbar_msg = "Successfully restored";
            ShowSnackbar((Activity)context, resId, snackbar_msg);
        }
    }
}
