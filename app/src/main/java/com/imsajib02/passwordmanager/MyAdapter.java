package com.imsajib02.passwordmanager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import pl.droidsonroids.gif.GifImageView;

import static com.imsajib02.passwordmanager.HomeActivity.*;
import static com.imsajib02.passwordmanager.Local.isBACKUPNEEDED;
import static com.imsajib02.passwordmanager.Local.isRESTORENEEDED;
import static com.imsajib02.passwordmanager.Local.setPreferenceBoolean;
import static com.imsajib02.passwordmanager.MainActivity.snackbar_msg;
import static com.imsajib02.passwordmanager.MyMethods.*;
import static com.imsajib02.passwordmanager.Support.*;
import static java.security.AccessController.getContext;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    public static List<Items> mDataset;
    public Context context;
    public DBHelper dbHelper;
    public static SwipeRevealLayout swreveallayout;
    public static final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView title, email;
        public CircleImageView Imageview;
        public FrameLayout frontlayout;
        public Button edit, delete;
        public GifImageView rightswipe;

        public MyViewHolder(View v)
        {
            super(v);

            swreveallayout = (SwipeRevealLayout) v.findViewById(R.id.swreveallayout);
            frontlayout = (FrameLayout) v.findViewById(R.id.frontlayout);
            Imageview = (CircleImageView) v.findViewById(R.id.image);
            title = (TextView) v.findViewById(R.id.title);
            email = (TextView) v.findViewById(R.id.email);
            edit = (Button) v.findViewById(R.id.edit);
            delete = (Button) v.findViewById(R.id.delete);
            rightswipe = (GifImageView) v.findViewById(R.id.rightswipe);

            swreveallayout.setSwipeListener(new SwipeRevealLayout.SwipeListener() {
                @Override
                public void onClosed(SwipeRevealLayout view) {

                    if(getAdapterPosition() == 0)
                    {
                        rightswipe.setImageResource(R.drawable.right_swipe);
                    }
                }

                @Override
                public void onOpened(SwipeRevealLayout view) {

                    if(getAdapterPosition() == 0)
                    {
                        rightswipe.setImageResource(R.drawable.left_swipe);
                    }
                }

                @Override
                public void onSlide(SwipeRevealLayout view, float slideOffset) {

                }
            });
        }
    }

    public MyAdapter(Context context, List<Items> myDataset) {
        mDataset = myDataset;
        this.context = context;
        dbHelper = new DBHelper(context, DBHelper.PRIMARY_DATABASE);
        notifyDataSetChanged();
        viewBinderHelper.setOpenOnlyOne(true);
    }

    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_recycler_item, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        viewBinderHelper.bind(swreveallayout, String.valueOf(mDataset.get(position).getIndex()));

        if(position==0)
        {
            holder.rightswipe.setImageResource(R.drawable.right_swipe);
        }

        holder.Imageview.setImageResource(mDataset.get(position).getImageView());
        holder.title.setText(mDataset.get(position).getTitle());
        holder.email.setText(mDataset.get(position).getEmail());

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ButtonPressAnimation(v);

                new AlertDialog.Builder(v.getContext())
                        .setMessage("Are you sure?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                int result = dbHelper.deleteData(mDataset.get(position).getIndex());

                                if(result > 0)
                                {
                                    Log.d("Delete ", "Row "+mDataset.get(position).getIndex()+ " deleted.");

                                    //swreveallayout.close(true);
                                    mDataset.remove(position);

                                    if(mDataset.size() > 0)
                                    {
                                        MyAdapter myAdapter = new MyAdapter(context, mDataset);
                                        recyclerView.setAdapter(myAdapter);
                                        //notifyDataSetChanged();
                                    }
                                    else
                                    {
                                        ToggleHomeActivity(View.GONE, View.VISIBLE, true);
                                    }

                                    setPreferenceBoolean(context, isBACKUPNEEDED, true);
                                    setPreferenceBoolean(context, isRESTORENEEDED, true);

                                    snackbar_msg = "Deleted";
                                    ShowSnackbar((Activity)context, R.id.relativeLayout, snackbar_msg);
                                }
                                else
                                {
                                    //swreveallayout.close(true);
                                    snackbar_msg = "Delete unsuccessful";
                                    ShowSnackbar((Activity)context, R.id.relativeLayout, snackbar_msg);

                                    Log.d("Delete ", "Row "+mDataset.get(position).getIndex()+ " deletion unsuccessful.");
                                }
                            }
                        })

                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //ResetSearchBox();
                                //swreveallayout.close(true);
                                viewBinderHelper.closeLayout(String.valueOf(mDataset.get(position).getIndex()));
                            }
                        })
                        .show();

            }
        });

        holder.frontlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //ResetSearchBox();
                ButtonPressAnimation(v);
                setPopupView(position, holder, VIEW_PAGE);
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //ResetSearchBox();
                ButtonPressAnimation(v);
                setPopupView(position, holder, UPDATE_PAGE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setPopupView(int position, MyViewHolder holder, int page)
    {
        setDataToPopup(position);
        //swreveallayout.close(true);
        CloseAllSwipeLayout();
        NewEntryPopup((Activity) context, page);
    }

    public void setDataToPopup(int position)
    {
        popup_data = new String[]{String.valueOf(mDataset.get(position).getIndex()), mDataset.get(position).getTitle(),
                mDataset.get(position).getEmail(), mDataset.get(position).getPassword(),
                mDataset.get(position).getUsername(), mDataset.get(position).getPhone(),
                mDataset.get(position).getRecoverymail()};
    }

    public static void CloseAllSwipeLayout()
    {
        if(!isEmptyDatabase)
        {
            for(int i=0; i<mDataset.size(); i++)
            {
                viewBinderHelper.closeLayout(String.valueOf(mDataset.get(i).getIndex()));
            }
        }
    }
}
