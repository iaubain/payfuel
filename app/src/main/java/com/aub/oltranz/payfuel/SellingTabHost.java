package com.aub.oltranz.payfuel;

import android.app.AlertDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import appBean.LogoutResponse;
import databaseBean.DBHelper;
import entities.DeviceIdentity;
import entities.Logged_in_user;
import features.HandleUrl;
import features.HandleUrlInterface;
import models.LogoutData;
import models.MapperClass;

public class SellingTabHost extends TabActivity implements TabHost.OnTabChangeListener, HandleUrlInterface {
    String tag="PayFuel: "+getClass().getSimpleName();

    TextView name;
    TabHost tHost;
    Context context;

    DBHelper db;
    HandleUrl handleUrl;
    MapperClass mc;

    int userId;
    boolean doubleBackToExitPressedOnce = false;

    Intent intent;
    Bundle savedBundle;
    Bundle bundle;
    StrictMode.ThreadPolicy policy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //go full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActionBar().hide();

        setContentView(R.layout.activity_selling_tab_host);

        savedBundle =getIntent().getExtras();

        //initialize Activity components
        initAppComponents();

        //Check user validity
        userValidity();


    }

    public void userValidity(){
        Log.d(tag, "checking user validity");
        int userCount=db.getUserCount();
        if(userCount>0){
            Log.d(tag,"Checking user validity succeeded");
            //when some user found


            String extra= savedBundle.getString(getResources().getString(R.string.userid));
            //  getIntent().getExtras().getString(getResources().getString(R.string.userid));
            userId=Integer.parseInt(extra);
            Logged_in_user user=db.getSingleUser(userId);
            if(user==null){
                Log.e(tag,"Checking user validity failed");
                intent=new Intent(context,Home.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                finish();
                startActivity(intent);
            }else if(user.getLogged()!=1){
                Log.e(tag,"Checking user validity failed");
                intent=new Intent(context,Home.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                finish();
                startActivity(intent);
            }else if(user.getLogged()==1){
                Log.d(tag,"Checking user validity succeeded");

                //initialize activity UI
                initAppUI();

                name.append(" "+user.getName());
            }
        }else{
            Log.d(tag,"Checking user validity failed");
            intent=new Intent(context,Home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            finish();
            startActivity(intent);
        }
    }

    public void initAppUI() {
        Log.d(tag, "Initializing Activity UI");

        String extra= savedBundle.getString(getResources().getString(R.string.userid));
        int uId=Integer.parseInt(extra);

        bundle = new Bundle();
        bundle.putString(getResources().getString(R.string.userid), String.valueOf(uId));

        name=(TextView) findViewById(R.id.lblname);
        tHost = getTabHost();
        TabHost.TabSpec tSpec;
        Intent intent;

        intent = new Intent().setClass(this, Selling.class);
        intent.putExtras(bundle);
        tSpec = tHost.newTabSpec("sell").setIndicator("Sell Portal") .setContent(intent);
        tHost.addTab(tSpec);

        intent = new Intent().setClass(this, Report.class);
        intent.putExtras(bundle);
        tSpec = tHost.newTabSpec("report").setIndicator("Report Portal") .setContent(intent);
        tHost.addTab(tSpec);


        TextView tv;
        for(int i=0;i<tHost.getTabWidget().getTabCount();i++){
            if(i==0){
                tHost.setCurrentTab(i);
                //tHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.tabselectedcolor);
            }
            tHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.tab_selector);
            tv=(TextView) tHost.getTabWidget().getChildTabViewAt(i).findViewById(android.R.id.title);
            tv.setAllCaps(false);
        }

        tHost.setOnTabChangedListener(this);
    }

    public void initAppComponents(){
        Log.d(tag, "Initializing Activity Components");
        context=this;
        db=new DBHelper(context);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    public void logout(View v){
        Log.v(tag, "Logging out...");

        if (doubleBackToExitPressedOnce) {
            DeviceIdentity di=db.getSingleDevice();
            LogoutData ld=new LogoutData();
            try {
                ld.setDevId(di.getDeviceNo());
                ld.setUserId(userId);
                mc=new MapperClass();

                handleUrl=new HandleUrl(this,this,getResources().getString(R.string.logouturl),getResources().getString(R.string.post),mc.mapping(ld));
            }catch (Exception e){
                uiFeedBack(e.getMessage());
            }

            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click Logout again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    @Override
    public void onTabChanged(String tabTag) {
        Log.d(tag, "Current tab tag: " + tHost.getCurrentTabTag());
    }

    @Override
    public void resultObject(Object object) {
        if (object == null) {
            uiFeedBack(getResources().getString(R.string.connectionerror));
        }else{
            if (object.getClass().getSimpleName().equalsIgnoreCase("LogoutResponse")){
                LogoutResponse lr=(LogoutResponse) object;
                try{
                    if(lr.getStatusCode() != 100)
                        uiFeedBack(lr.getMessage());
                    else{
                        long log=0;
                        //delete Work Status
                        db.deleteStatusByUser(userId);

                        //delete user
                        // db.truncateTransactions();
                        Logged_in_user user=new Logged_in_user();
                        user.setLogged(0);
                        log=db.updateUser(user);
                        Log.v(tag, "User log status 0: " + log);
                        db.deleteUser(userId);
                        db.deleteStatusByUser(userId);

                        intent=new Intent(this,Home.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        finish();
                        startActivity(intent);
                    }
                }catch (Exception e){
                    uiFeedBack(getResources().getString(R.string.faillurenotification));
                }
            }else{
                uiFeedBack(getResources().getString(R.string.ambiguous));
            }
        }
    }

    @Override
    public void feedBack(String message) {
        uiFeedBack(message);
    }

    public void uiFeedBack(String message){
        AlertDialog alertDialog = new AlertDialog.Builder(SellingTabHost.this).create();
        alertDialog.setTitle("Attention");
        if(!TextUtils.isEmpty(message)) {
            alertDialog.setMessage(message);
        }else{
            alertDialog.setMessage(getResources().getString(R.string.faillurenotification));
        }

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
            // do nothing
            Log.e(tag, "action:" + "Menu Key Pressed");
            return true;
        }else if(keyCode == KeyEvent.KEYCODE_BACK){
            //do nothing on back key presssed
            Log.e(tag, "action:" +"Back Key Pressed");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
