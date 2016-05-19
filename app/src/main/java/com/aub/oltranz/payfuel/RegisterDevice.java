package com.aub.oltranz.payfuel;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import appBean.DeviceRegistrationResponse;
import databaseBean.DBHelper;
import entities.DeviceIdentity;
import features.HandleUrl;
import features.HandleUrlInterface;
import models.DeviceBean;
import models.MapperClass;

public class RegisterDevice extends ActionBarActivity implements HandleUrlInterface{

    String tag="PayFuel: "+getClass().getSimpleName();
    TextView tv;
    EditText userName, password,devName;
    Button reg;
    String devRegUrl, deviceSerial,deviceName;
    Context context;

    DBHelper db;
    MapperClass mapper;
    HandleUrl hu;

    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_device);
        Log.d(tag,"DeviceRegistration Activity Created");
        //initialize UI
        initActUI();
        //initialize Activity components
        initActComponent();
    }

    //Initialise Activity UI
    public void initActUI(){
        Log.d(tag,"Initialize Activity UI");
        tv=(TextView) findViewById(R.id.tv);
        userName=(EditText) findViewById(R.id.username);
        password=(EditText) findViewById(R.id.pw);
        devName=(EditText) findViewById(R.id.devname);
        context=getApplicationContext();
    }

    //initialize Activity components
    public void initActComponent(){
        Log.d(tag,"Initialize Activity Components");
        deviceSerial= Build.SERIAL;
        db=new DBHelper(context);
        mapper=new MapperClass();
    }

    public void register(View v){
        Log.d(tag, "Register Device Process");
        if((!userName.getText().toString().equalsIgnoreCase(""))&&(!password.getText().toString().equalsIgnoreCase(""))&&(!devName.getText().toString().equalsIgnoreCase(""))){
            deviceName=devName.getText().toString();

            DeviceBean devBean=new DeviceBean();
            devBean.setDeviceId(devName.getText().toString());
            devBean.setEmail(userName.getText().toString());
            devBean.setPassword(password.getText().toString());

            //disabling the UI
            disableUI();

            String jsonData=mapper.mapping(devBean);
            hu=new HandleUrl(this,context,getResources().getString(R.string.registerdeviceurl),getResources().getString(R.string.post),jsonData);
       }
    }
    //Return a message to the user
    public void uiFeedBack(String message){
        //ReEnabling UI
        enableUI();

        if(message!=null) {
            Log.d(tag, "FeedBack to the user: " + message);
            tv.setText(message);
        }else{
            Log.d(tag, "FeedBack to the user");
            tv.setText(getResources().getString(R.string.nulluifeedback));
        }
    }

    @Override
    public void resultObject(Object object) {
        if(object==null){
            uiFeedBack(getResources().getString(R.string.connectionerror));
        }else {
            Log.d(tag, "Objected to: " + object.getClass().getSimpleName());
            if (object.getClass().getSimpleName().equalsIgnoreCase("DeviceRegistrationResponse")) {
                DeviceRegistrationResponse drr=(DeviceRegistrationResponse) object;
                if(drr.getStatusCode()!=100){
                    //when device registration had a problem
                    uiFeedBack(getResources().getString(R.string.deviceregfail));
                }else{
                    //when the status code is Okay
                    db.truncateDevice();

                    DeviceIdentity di=new DeviceIdentity();
                    di.setSerialNumber(deviceSerial);
                    di.setDeviceNo(deviceName);
                    long dbId=db.createDevice(di);
                    if(dbId>=1){
                        intent=new Intent(this,Home.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        finish();
                        startActivity(intent);
                    }else{
                        uiFeedBack(getResources().getString(R.string.invaliddata));
                    }
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

    //Disabling all UI element
    public void disableUI(){
        Log.d(tag,"Disable all UI Elements");
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.reglayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if(child.isEnabled())
            child.setEnabled(false);
        }
    }

    //Enable all UI element
    public void enableUI(){
        Log.d(tag,"Enable all UI Elements");
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.reglayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if(!child.isEnabled())
                child.setEnabled(true);

            //Reset all Edit text
            View view = layout.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText)view).setText("");
            }
        }
    }
}
