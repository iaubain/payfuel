package com.aub.oltranz.payfuel;

import android.app.Dialog;
import android.content.Context;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import databaseBean.DBHelper;
import entities.Logged_in_user;
import entities.SellingTransaction;
import features.RecordAdapter;

public class Report extends ActionBarActivity {

    String tag="PayFuel: "+getClass().getSimpleName();
    int userId;
    int branchId;

    Button records;

    Bundle savedBundle;
    Context context;
    StrictMode.ThreadPolicy policy;
    Dialog dialog;

    DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_repport);


        initAppUI();

        initAppComponents();
    }

    public void initAppUI(){
        Log.d(tag, "Initializing Activity UI");

        records=(Button) findViewById(R.id.rec);

        savedBundle =getIntent().getExtras();
        if(savedBundle!=null){
            String extra= savedBundle.getString(getResources().getString(R.string.userid));
            userId=Integer.parseInt(extra);
        }

        context=this;

        try{
            Logged_in_user user=db.getSingleUser(userId);
            branchId=user.getBranch_id();
        }catch (Exception e){
            Log.e(tag,"Error Occurred. "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void initAppComponents(){
        Log.d(tag, "Initializing Activity Components");

        if (android.os.Build.VERSION.SDK_INT > 9) {
            policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        dialog=new Dialog(this);
        db=new DBHelper(this);
    }
    public void records(View v){
        Log.d(tag, "Reconciliation Room");
        if(dialog.isShowing()){
            dialog.dismiss();
            dialog=new Dialog(this);
        }
        dialog=new Dialog(this);
        dialog.setContentView(R.layout.records);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        if (dividerId != 0) {
            View divider = dialog.findViewById(dividerId);
            divider.setBackgroundColor(getResources().getColor(R.color.appcolor));
        }
        dialog.setTitle(Html.fromHtml("<font color='" + getResources().getColor(R.color.appcolor) + "'>Reconciliation Room</font>"));

        TextView tv=(TextView) dialog.findViewById(R.id.tv);
        Button exit=(Button) dialog.findViewById(R.id.done);
        ListView transView=(ListView) dialog.findViewById(R.id.records);

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        tv.setText("Total Transactions: "+db.getTransactionCount(userId)+" Successful: "+db.getTransactionCountSucceeded(userId)+" Pending: "+db.getTransactionCountPending(userId)+" Cancelled: "+db.getTransactionCountCancelled(userId));

        List<SellingTransaction> sts= db.getAllTransactionsPerUser(userId);
        RecordAdapter ra=new RecordAdapter(this,userId,sts);
        transView.setAdapter(ra);

        dialog.show();
    }

    public void recordPopUp(){

    }
}
