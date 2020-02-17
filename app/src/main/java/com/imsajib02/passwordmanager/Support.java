package com.imsajib02.passwordmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.StorageReference;

import static com.imsajib02.passwordmanager.MainActivity.*;

public class Support {

    public static boolean isEmpty = false;
    public static boolean hasSearched = false;
    public static boolean isConnected = false;
    public static boolean isInternetAvailable = false;
    public static boolean isEmptyDatabase = false;

    public static int INSERT_PAGE = 1;
    public static int VIEW_PAGE = 2;
    public static int UPDATE_PAGE = 3;
    public static int user_account_resId;
    public static int NORMAL_REQUEST = 132;

    public static String[] popup_data;
    public static String SearchboxText = "";
    public static String FAILED_BACKUP = "FailedBackup";

    public static Context user_account_context;
    public static RecyclerView.Adapter SearchedAdapter;
    public static ProgressDialog progressDialog;
    public static DatabaseReference databaseReference;
    public static StorageReference storageRef;
    public static FileDownloadTask fileDownloadTask;

    public static String[] QuestionSet = {"Choose a question ...",
            "What was your childhood nickname?",
            "In what city did you meet your spouse/significant other?",
            "What is the name of your favorite childhood friend?",
            "What street did you live on in third grade?",
            "What is the middle name of your youngest child?",
            "Where were you when you had your first kiss?",
            "In which city were you born?",
            "What is the name of your favorite childhood teacher?",
            "What is the country of your ultimate dream vacation?",
            "What was the first concert you attended?",
            "What was the name of your first pet?",
            "What was the first thing you learned to cook?",
            "What was the first film you saw in theater?",
            "What was the name of your manager at your first job?",
            "What is your favourite novel/book?",
            "Who was your first roommate?",
            "What is your favorite superhero character?"};

    public static String[] letters = {"a", "b", "c", "d", "e", "f", "g", "h", "i",
                                        "j", "k", "l", "m", "n", "o", "p", "q", "r",
                                            "s", "t", "u", "v", "w", "x", "y", "z"};

    public static int[] images = {R.drawable.a, R.drawable.b, R.drawable.c,
            R.drawable.d, R.drawable.e, R.drawable.f, R.drawable.g,
            R.drawable.h, R.drawable.i, R.drawable.j, R.drawable.k,
            R.drawable.l, R.drawable.m, R.drawable.n, R.drawable.o,
            R.drawable.p, R.drawable.q, R.drawable.r, R.drawable.s,
            R.drawable.t, R.drawable.u, R.drawable.v, R.drawable.w,
            R.drawable.x, R.drawable.y, R.drawable.z};

    public static String[] weekdays = {"Sunday", "Monday", "Tuesday",
                                    "Wednesday", "Thursday", "Friday", "Saturday"};
}
