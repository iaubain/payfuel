package com.aub.oltranz.payfuel;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.StrictMode;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;

import databaseBean.DBHelper;
import entities.Logged_in_user;

public class SellingTabHost extends TabActivity implements TabHost.OnTabChangeListener {
    String tag="PayFuel: "+getClass().getSimpleName();

    TextView name;
    TabHost tHost;
    Context context;

    DBHelper db;

    int userId;

    Intent intent;
    Bundle savedBundle;
    Bundle bundle;
    StrictMode.ThreadPolicy policy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selling_tab_host);

        savedBundle =getIntent().getExtras();

        //initialize Activity components
        initAppComponents();

        //Check user validity
        userValidity();


    }

    public void userValidity(){
        Log.d(tag,"checking user validity");
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


        name=(TextView) findViewById(R.id.lblname);
        tHost = getTabHost();
        TabHost.TabSpec tSpec;
        Intent intent;
        intent = new Intent().setClass(this, Selling.class);
        bundle = new Bundle();
        bundle.putString(getResources().getString(R.string.userid), String.valueOf(uId));
        intent.putExtras(bundle);
        tSpec = tHost.newTabSpec("sell").setIndicator("Sell Portal") .setContent(intent);
        tHost.addTab(tSpec);

        intent = new Intent().setClass(this, Report.class);
        bundle = new Bundle();
        bundle.putString(getResources().getString(R.string.userid), String.valueOf(uId));
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
    }

    public void logout(View v){
        Log.v(tag,"Logging out...");

    }

    @Override
    public void onTabChanged(String tabTag) {
        Log.d(tag,"Current tab tag: "+tHost.getCurrentTabTag());
//        for(int i=0;i<tHost.getTabWidget().getTabCount();i++){
//            if(tabId.equals(tHost.getCurrentTabTag())){
//                tHost.getTabWidget().getChildAt(i) .setBackgroundResource(R.drawable.tab_selector);
//            }
//        }
//        if (tabId.equals("first")) {
//            tHost.getTabWidget().getChildAt(0) .setBackgroundResource(R.drawable.tab_selector);
//            tHost.getTabWidget().getChildAt(1).setBackgroundResource(R.drawable.tabunselcolor);
//        } else if (tabId.equals("second")) {
//            tHost.getTabWidget().getChildAt(1) .setBackgroundResource(R.drawable.tab_selector);
//            tHost.getTabWidget().getChildAt(0).setBackgroundResource(R.drawable.tabunselcolor);
//            //            tHost.getTabWidget().getChildAt(1).setBackgroundColor(getResources().getColor(R.color.transparent));
//            //            tHost.getTabWidget().getChildAt(0).setBackgroundColor(getResources().getColor(R.color.transparent));
//        }
    }
}
