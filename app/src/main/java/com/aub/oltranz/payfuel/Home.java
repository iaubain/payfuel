package com.aub.oltranz.payfuel;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Iterator;
import java.util.List;

import appBean.Login;
import appBean.LoginResponse;
import databaseBean.DBHelper;
import entities.DeviceIdentity;
import entities.Logged_in_user;
import features.HandleUrl;
import features.HandleUrlInterface;
import features.LoadPaymentMode;
import features.LoadPumps;
import features.ThreadControl;
import models.MapperClass;

public class Home extends ActionBarActivity implements HandleUrlInterface {
    String tag = "PayFuel: " + getClass().getSimpleName();

    TextView tv;
    EditText pin;
    Button login;
    Context context;

    ProgressDialog barProgressDialog;
    Handler updateBarHandler;

    MapperClass mapperClass;
    DBHelper db;
    HandleUrl hu;
    LoadPumps lp;
    LoadPaymentMode lpm;

    Intent intent;
    StrictMode.ThreadPolicy policy;
    ThreadControl tc;
    Dialog progress;
    ProgressDialog progressBar;

    int userId, branchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //go full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_home);

        //initialize activity UI
        initAppUI();
        //initialize activity Components
        initAppComponents();
        //device Registration
        deviceRegistration();

    }

    //initialize app UI
    public void initAppUI() {
        Log.d(tag, "Initializing app UI");
        tv = (TextView) findViewById(R.id.tv);
        pin = (EditText) findViewById(R.id.pin);
        login = (Button) findViewById(R.id.login);
        context = this;
    }

    //initialize app components
    public void initAppComponents() {
        Log.d(tag, "Initializing app Components");
        mapperClass = new MapperClass();
        db = new DBHelper(context);
        tc = new ThreadControl();
        updateBarHandler = new Handler();
        if (android.os.Build.VERSION.SDK_INT > 9) {
            policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        barProgressDialog = new ProgressDialog(Home.this);
//        progress = new Dialog (this);
//        progress.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        progress.setContentView(R.layout.progress);
//        progress.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }

    //Handle longin button
    public void loginFunction(View v) {
        //db.truncateDevice();
        // db.truncateUser();
        Log.d(tag, "Login Process");
        //launchBarDialog(v);
       // showDialog("Logging In. Please wait...");
        String data = pin.getText().toString();

        if (TextUtils.isEmpty(data)) {
            uiFeedBack(getResources().getString(R.string.invaliddata));
        } else {
            //process the login
            DeviceIdentity di = new DeviceIdentity();
            di = db.getSingleDevice();
            Login login = new Login();
            login.setDeviceId(di.getDeviceNo());
            login.setUserPin(pin.getText().toString());

            //disabling UI
            disableUI();

            //mapping to object to get JsonData
            String jsonData = mapperClass.mapping(login);
            hu = new HandleUrl(this, context, getResources().getString(R.string.loginurl), getResources().getString(R.string.post), jsonData);

        }
    }

    //Return a message to the user
    public void uiFeedBack(String message) {
        enableUI();
        //dismissDialog();
        if (message != null) {
            Log.d(tag, "FeedBack to the user: " + message);
            tv.setText(message);
        } else {
            Log.d(tag, "FeedBack to the user");
            tv.setText(getResources().getString(R.string.nulluifeedback));
        }
    }

    //check device registration
    public void deviceRegistration() {

        //DeviceIdentity di=new DeviceIdentity();
        //  di=db.getSingleDevice();
        // System.out.println("device name: "+di.getDeviceNo());
        try {
            int devCount = devCount = db.getDeviceCount();
            if (devCount <= 0) {
                //when no device found
                intent = new Intent(this, RegisterDevice.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                finish();
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e(tag, "Exception Occurred " + e.getMessage());
            if (e.getMessage() == null) {
                //when no device found
                intent = new Intent(this, RegisterDevice.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                finish();
                startActivity(intent);
            }
        }
    }

    @Override
    public void resultObject(Object object) {
        if (object == null) {
            uiFeedBack(getResources().getString(R.string.connectionerror));
        } else {
            //uiFeedBack("Objected to: " + object.getClass().getSimpleName());
            if (object.getClass().getSimpleName().equalsIgnoreCase("LoginResponse")) {
                LoginResponse lr = (LoginResponse) object;
                if (lr.getStatusCode() != 100) {
                    //when login response got a problem
                    uiFeedBack(getResources().getString(R.string.loginproblem));
                } else {
                    //when the status code is Okay
                    //check the logged users
                    int userCount = db.getUserCount();
                    if (userCount > 0) {
                        //when user(s) is(are) found
                        int currentUserId = lr.getLogged_in_user().getUser_id();
                        List<Logged_in_user> users = db.getAllUsers();
                        if (isUserLogged(users, currentUserId)) {
                            //user found in local database
                            Logged_in_user logged = lr.getLogged_in_user();
                            logged = db.getSingleUser(logged.getUser_id());
                            userId = logged.getUser_id();
                            branchId = logged.getBranch_id();
                            //uiFeedBack("Success: " + userId);

                            //_______________Return him back to Select pump_nozzles Page_______________\\
                            int pumpCount = db.getPumpCount();
                            if (pumpCount > 0) {
                                int nozzleCount = db.getNozzleCount();
                                if (nozzleCount > 0) {
                                    int selectedPumpCount = db.getStatusCountByUser(userId);
                                    if (selectedPumpCount <= 0) {
                                        //continue with normal login
                                        if (loadPumps(this, userId)) {
                                            //loading payment
                                            // pause();
                                            if (loadPayment(this, userId)) {
                                                // Redirect the user to select pump_nozzles page
                                               // showDialog("Logging In. Done...");
                                                intent = new Intent(context, SelectPumps.class);
                                                Bundle bundle = new Bundle();
                                                bundle.putString(getResources().getString(R.string.userid), String.valueOf(userId));
                                                intent.putExtras(bundle);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                dismissDialog();
                                                finish();
                                                startActivity(intent);
                                            } else {
                                                resetLogin();
                                                uiFeedBack("Loading paymentMode failed: " + userId);
                                            }
                                        } else {
                                            resetLogin();
                                            uiFeedBack("Loading pumps failed: " + userId);
                                        }
                                    } else {
                                        //Select pump_nozzles page redirection
                                                // Redirect the use to sale page
                                               // showDialog("Logging In. Done...");
                                                intent = new Intent(context, SellingTabHost.class);
                                                Bundle bundle = new Bundle();
                                                bundle.putString(getResources().getString(R.string.userid), String.valueOf(userId));
                                                intent.putExtras(bundle);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                                dismissDialog();
                                                finish();
                                                startActivity(intent);

                                    }
                                } else {
                                    resetLogin();
                                    uiFeedBack(getResources().getString(R.string.loginproblem));
                                }
                            } else {
                                resetLogin();
                                uiFeedBack(getResources().getString(R.string.loginproblem));
                            }
//                            //loading pumps if necessary
//                            if(loadPumps(this,userId)){
//                                uiFeedBack("Loading pumps succeeded : "+userId);
//                            }else{
//                                uiFeedBack("Loading pumps failed: ");
//                            }

                        } else {
                            //if user was not found in local database or its status is 0 in local database
                            Logged_in_user logged = lr.getLogged_in_user();
                            logged.setLogged(0);

                            //Delete user if already there in the database
                            db.deleteUser(logged.getUser_id());

                            //create a user which is logging in
                            long internalId = db.createUser(logged);
                            if (internalId <= 0) {
                                //when to register a user on local device failed
                                uiFeedBack(getResources().getString(R.string.loginproblem));
                            } else {
                                //when registering a user on local device succeeded
                                userId = logged.getUser_id();
                                branchId = logged.getBranch_id();
                                //uiFeedBack("Login Success: " + userId);
                                //pause();

                                if (loadPumps(this, userId)) {
                                    //loading payment
                                    // pause();
                                    if (loadPayment(this, userId)) {
                                        // Redirect the use to sale page
                                       // showDialog("Logging In. Done...");
                                        intent = new Intent(context, SelectPumps.class);
                                        Bundle bundle = new Bundle();
                                        bundle.putString(getResources().getString(R.string.userid), String.valueOf(userId));
                                        intent.putExtras(bundle);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                        dismissDialog();
                                        finish();
                                        startActivity(intent);

                                    } else {
                                        resetLogin();
                                        uiFeedBack("Loading paymentMode failed: " + userId);
                                    }
                                } else {
                                    resetLogin();
                                    uiFeedBack("Loading pumps failed: " + userId);
                                }
                            }
                        }
                    } else {
                        //if user was not found in local database or its status is 0 in local database
                        Logged_in_user logged = lr.getLogged_in_user();
                        logged.setLogged(0);

                        //Delete user if already there in the database
                        //db.deleteUser(logged.getUser_id());

                        //create a user which is logging in
                        long internalId = db.createUser(logged);
                        if (internalId <= 0) {
                            //when to register a user on local device failed
                            resetLogin();
                            uiFeedBack(getResources().getString(R.string.loginproblem));
                        } else {
                            //when registering a user on local device succeeded
                            userId = logged.getUser_id();
                            branchId = logged.getBranch_id();
                            //uiFeedBack("Login Success: " + userId);
                            //pause();

                            if (loadPumps(this, userId)) {
                                //loading payment
                                // pause();
                                if (loadPayment(this, userId)) {
                                    // Redirect the use to sale page
                                   // showDialog("Logging In. Done...");
                                    intent = new Intent(context, SelectPumps.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putString(getResources().getString(R.string.userid), String.valueOf(userId));
                                    intent.putExtras(bundle);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                    dismissDialog();
                                    finish();
                                    startActivity(intent);
                                } else {
                                    resetLogin();
                                    uiFeedBack("Loading paymentMode failed: " + userId);
                                }
                            } else {
                                resetLogin();
                                uiFeedBack("Loading pumps failed: " + userId);
                            }
                        }
                    }

                }
            } else {
                resetLogin();
                uiFeedBack(getResources().getString(R.string.ambiguous));
            }
        }
    }

    //resetting the login when a bad login happens
    public void resetLogin(){
        long log=0;
        //delete Work Status
        db.deleteStatusByUser(userId);
        //delete user

        Logged_in_user user=new Logged_in_user();
        user.setLogged(0);
        log=db.updateUser(user);
        Log.v(tag,"User log status 0: "+log);
        db.deleteUser(userId);
        db.deleteStatusByUser(userId);
    }

    @Override
    public void feedBack(String message) {
        uiFeedBack(message);
    }

    //Disabling all UI element
    public void disableUI() {
        Log.d(tag, "Disable all UI Elements");
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.loginlayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child.isEnabled())
                child.setEnabled(false);
        }
    }

    //Enable all UI element
    public void enableUI() {
        Log.d(tag, "Enable all UI Elements");
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.loginlayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (!child.isEnabled())
                child.setEnabled(true);

            //Reset all Edit text
            View view = layout.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText) view).setText("");
            }

        }
    }

    //check if the user was already there
    public boolean isUserLogged(List<Logged_in_user> userList, int currentUserId) {

        Iterator iterator = userList.iterator();
        while (iterator.hasNext()) {
            Logged_in_user user = new Logged_in_user();
            user = (Logged_in_user) iterator.next();
            if (user.getUser_id() == currentUserId && user.getLogged() == 1)
                return true;
        }
        return false;
    }

    public boolean loadPumps(Context context, int userId) {
       // updateDialog("Logging In. Loading pumps...");
        lp = new LoadPumps();
        return lp.fetchPump(context, userId);

    }

    public void logout(int userId) {

    }

    public void pause() {

        try {
            Log.e(tag, "Application Paused attempt");
            Thread.sleep(500);
            //tc.pause();
        } catch (Exception e) {
            Log.e(tag, "Application Paused attempt failed");
            e.printStackTrace();
        }
//        Thread closeActivity = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    Log.e(tag, "Application Paused for 3 seconds");
//                    Thread.sleep(3000);
//                } catch (Exception e) {
//                    e.getLocalizedMessage();
//                }
//            }
//        });
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                Log.e(tag, "Application resumed");
////                onResume();
//                tc.resume();
//            }
//        }, 3000);
//        Log.e(tag, "Application Paused for 3 seconds");
////        onPause();
//        tc.pause();
    }

    public boolean loadPayment(Context context, int userId) {
       // updateDialog("Logging In. Loading payment Modes...");
        lpm = new LoadPaymentMode();
        return lpm.fetchPump(context, userId);

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.e(tag, "Application called onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(tag, "Application called onResume");
    }

    //showing the progress dialog
    public void showDialog(String message) {

        if (!barProgressDialog.isShowing()) {

            barProgressDialog.setTitle("Logging in...");
            barProgressDialog.setMessage(message);
            barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            barProgressDialog.setProgress(0);
            barProgressDialog.setMax(20);
            barProgressDialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        // Here you should write your time consuming task...
                        while (barProgressDialog.getProgress() <= barProgressDialog.getMax()) {

                            Thread.sleep(2000);

                            updateBarHandler.post(new Runnable() {

                                public void run() {

                                    barProgressDialog.incrementProgressBy(2);

                                }

                            });

                            if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {

                                barProgressDialog.dismiss();

                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }).start();
        }
    }

    //updating text on dialog
    public void updateDialog(String message) {

        if (!barProgressDialog.isShowing()) {
            barProgressDialog.setTitle("Logging in...");
            barProgressDialog.setMessage(message);
            barProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            barProgressDialog.setProgress(0);
            barProgressDialog.setMax(20);
            barProgressDialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        // Here you should write your time consuming task...
                        while (barProgressDialog.getProgress() <= barProgressDialog.getMax()) {

                            Thread.sleep(2000);

                            updateBarHandler.post(new Runnable() {

                                public void run() {

                                    barProgressDialog.incrementProgressBy(2);

                                }

                            });

                            if (barProgressDialog.getProgress() == barProgressDialog.getMax()) {

                                barProgressDialog.dismiss();

                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }).start();
        } else {
            barProgressDialog.setMessage(message);
        }


        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        if(! progress.isShowing()){
//            TextView title=(TextView) progress.findViewById(R.id.progresstitle);
//            title.setText(message);
//            progress.show();
//        }else{
//            TextView title=(TextView) progress.findViewById(R.id.progresstitle);
//            title.setText(message);
//        }
    }

    //dismiss dialog text
    public void dismissDialog() {
        barProgressDialog.dismiss();
    }

    public void launchBarDialog(View view) {

    }
}
