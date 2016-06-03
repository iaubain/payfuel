package com.aub.oltranz.payfuel;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import appBean.ChoosenPumpResponse;
import databaseBean.DBHelper;
import entities.Logged_in_user;
import entities.Nozzle;
import entities.Pump;
import entities.WorkStatus;
import features.ExpandableListAdapter;
import features.HandleUrl;
import features.HandleUrlInterface;
import features.NozzleListAdapter;
import features.PumpListAdapter;
import models.ChoosenPumpAndNozzle;
import models.ChoosenPumps;
import models.MapperClass;

public class SelectPumps extends ActionBarActivity implements AdapterView.OnItemClickListener, HandleUrlInterface {

    String tag="PayFuel: "+getClass().getSimpleName();
    Context context;
    ListView mList, nozzleListView;
    Button next;
    TextView tv;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    MapperClass mapperClass;
    DBHelper db;
    PumpListAdapter adapter;
    NozzleListAdapter nozzleAdapter;
    ExpandableListAdapter listAdapter;
    HandleUrl handUrl;

    List pumpList,pumpId,nozzleId, pumpNames;
    int userId;

    Intent intent;
    Bundle savedBundle;
    StrictMode.ThreadPolicy policy;
    Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_pumps);

        savedBundle =getIntent().getExtras();
        //initialize activity UI
        initAppUI();

        //initialize Activity components
        initAppComponents();

        //Check user validity
        userValidity();

        //load pumplist content
        populateList();

    }

    //initialize app UI
    public void initAppUI(){
        Log.d(tag, "Initializing Activity UI");
        mList=(ListView) findViewById(R.id.pumlist);
        next =(Button) findViewById(R.id.next);
        tv=(TextView) findViewById(R.id.tv);
        context=getApplicationContext();
    }

    //initialize app components
    public void initAppComponents(){
        Log.d(tag, "Initializing Activity Components");
        mapperClass=new MapperClass();
        db=new DBHelper(context);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    //Disabling all UI element
    public void disableUI(){
        Log.d(tag, "Disable all UI Elements");
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.pumplayout);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if(child.isEnabled())
                child.setEnabled(false);
        }
    }

    //Enable all UI element
    public void enableUI(){
        Log.d(tag, "Enable all UI Elements");
        LinearLayout layout = (LinearLayout) findViewById(R.id.pumplayout);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(tag,"Pump Selected at: "+position+" and id: "+ pumpId.get(position));
        //ViewGroup.LayoutParams inflater=parent.getLayoutParams();
        String pId=  String.valueOf(pumpId.get(position));
        ImageView imageView = (ImageView) view.findViewById(R.id.icon);
        TextView label = (TextView) view.findViewById(R.id.indicator);

        //pumpName.setText(itemname.get(position));
        //imageView.setImageResource(imgid.get(position));
        //imageView.setImageResource(R.drawable.pump_green);
        //label.setText("Clicked");
        int listViewCount=mList.getCount();
        Log.d(tag,"List view Count: "+listViewCount);

        loadNozzle(userId,Integer.parseInt(pId),view);
//        for(int i=0; i<=listViewCount-1;i++){
//            if(i!=position){
//                View v=mList.getChildAt(i);
//                ImageView img = (ImageView) v.findViewById(R.id.icon);
//                TextView lbl = (TextView) v.findViewById(R.id.indicator);
//                //imageView.setImageResource(imgid.get(position));
//                img.setImageResource(R.drawable.pump_blue);
//                lbl.setText("Available to Choose");
//            }
//        }
    }

    //populate the list with content
    public void populateList(){
        Log.d(tag, "Populating Pump List with the content");
        int pumpCount=db.getPumpCount();
        if(pumpCount>0){
            //when some pump found
            pumpList=new ArrayList<Pump>();
            pumpId=new ArrayList<String>();
            pumpNames=new ArrayList<String>();
            List<String>imgId=new ArrayList<String>();
            pumpList=db.getAllPumps();
            if(!pumpList.isEmpty()){
                List<String> pumpName=new ArrayList<String>();
                Iterator iterator=pumpList.iterator();
                while (iterator.hasNext()){
                    Pump pump=new Pump();
                    pump=(Pump) iterator.next();
                    if(pump.getStatus()==7) {
                        pumpName.add(pump.getPumpName());
                        pumpNames.add(pump.getPumpName());
                        pumpId.add(pump.getPumpId());
                        imgId.add(String.valueOf(R.drawable.pump_blue));
                    }
                }
                //initiating adapter
                adapter=new PumpListAdapter(this,userId, pumpName, pumpId,imgId);
                mList.setAdapter(adapter);
                mList.setOnItemClickListener(this);
            }else{
                intent=new Intent(context,Home.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                finish();
                startActivity(intent);
            }
        }else{
            intent=new Intent(context,Home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            finish();
            startActivity(intent);
        }
    }

    //check user validity for login
    public void userValidity(){
        Log.d(tag, "Checking user validity");
        int userCount=db.getUserCount();
        if(userCount>0){
            Log.d(tag,"Checking user validity succeeded");
            //when some user found


            String extra= savedBundle.getString(getResources().getString(R.string.userid));
                  //  getIntent().getExtras().getString(getResources().getString(R.string.userid));
            userId=Integer.parseInt(extra);
            Logged_in_user user=db.getSingleUser(userId);
            if(user==null){
                Log.d(tag,"Checking user validity failed");
                intent=new Intent(context,Home.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                finish();
                startActivity(intent);
            }
        }else{
            Log.d(tag,"Checking user validity failed");
            intent=new Intent(context,Home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            finish();
            startActivity(intent);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);

    }

    //Load nozzle of a selected pump
    public void loadNozzle(int userId, int pumpId, final View pumpView){
        Log.d(tag, "Load Nozzle and initiate Nozzle list view");
        dialog=new Dialog(this);
        dialog.setContentView(R.layout.nozzle_list);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        if (dividerId != 0) {
            View divider = dialog.findViewById(dividerId);
            divider.setBackgroundColor(getResources().getColor(R.color.appcolor));
        }
        dialog.setTitle(Html.fromHtml("<font color='"+getResources().getColor(R.color.appcolor)+"'>Nozzle Status</font>"));

        nozzleListView =(ListView) dialog.findViewById(R.id.nozzlelist);
        Button finish=(Button) dialog.findViewById(R.id.nozzlefinish);

        populateNozzleList(pumpId, pumpView);

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int nozzleCount = nozzleListView.getCount();
                int nozzleDenialCheck = 0;
                int nozzleAcceptCheck = 0;

                for (int i = 0; i <= nozzleCount - 1; i++) {

                    View v = nozzleListView.getChildAt(i);

                    TextView lbl = (TextView) v.findViewById(R.id.nozzleindicator);
                    TextView pumpLabel = (TextView) pumpView.findViewById(R.id.indicator);
                    //imageView.setImageResource(imgid.get(position));
                    if (lbl.getText().toString().equals("Denied")) {
                        nozzleDenialCheck += 1;
                        break;
                    }
                    if (lbl.getText().toString().equals("Accepted")) {
                        nozzleAcceptCheck += 1;
                    }
                }
                ImageView pumpImg = (ImageView) pumpView.findViewById(R.id.icon);
                TextView pumpLabel = (TextView) pumpView.findViewById(R.id.indicator);
                if (nozzleDenialCheck > 0) {
                    pumpImg.setImageResource(R.drawable.pump_red);
                    pumpLabel.setText("Nozzle(s) rejected");
                    pumpLabel.setTextColor(context.getResources().getColor(R.color.error));
                } else if (nozzleAcceptCheck > 0) {
                    pumpLabel.setText("Nozzle(s) Accepted");
                    pumpLabel.setTextColor(context.getResources().getColor(R.color.rdcolor));
                    pumpImg.setImageResource(R.drawable.pump_green);
                } else {
                    pumpLabel.setText("Available to Choose");
                    pumpLabel.setTextColor(context.getResources().getColor(R.color.rdoff));
                    pumpImg.setImageResource(R.drawable.pump_blue);
                }

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //populate nozzle list
    public void populateNozzleList(int pumpId, View v){
        Log.d(tag, "Populate nozzle list with data of pump:" + pumpId);
        List<Nozzle> nozzleList=new ArrayList<Nozzle>();

        nozzleList=db.getAllNozzlePerPump(pumpId);
        if(!nozzleList.isEmpty()){
            nozzleId=new ArrayList<String>();
            List<String> nozzleNameList=new ArrayList<String>()/*, indexList=new ArrayList<String>(), productList=new ArrayList<String>(),imgIdList=new ArrayList<String>()*/;

            Iterator iterator=nozzleList.iterator();
            while (iterator.hasNext()){
                Nozzle nozzle=new Nozzle();
                nozzle=(Nozzle) iterator.next();

                nozzleNameList.add(nozzle.getNozzleName());
//                indexList.add(String.valueOf(nozzle.getNozzleIndex()));
//                productList.add(nozzle.getProductName());
//                nozzleId.add(nozzle.getNozzleId());

//                if(!(((nozzle.getStatusCode())&7)!= 7)){
//                    imgIdList.add(String.valueOf(R.drawable.nozzle_red));
//                }else
//                    imgIdList.add(String.valueOf(R.drawable.nozzle_blue));
            }

            //I need to add nozzle status list so the one that are not available I deactivate the choosing button

            nozzleAdapter=new NozzleListAdapter(this,userId,pumpId,v,nozzleList,nozzleNameList/*,nozzleNameList,imgIdList,indexList,productList,nozzleId*/);
            nozzleListView.setAdapter(nozzleAdapter);
            nozzleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Log.d(tag,"Clicked Nozzle at: "+position+" with Id:"+ nozzleId.get(position));
                }
            });
        }
    }

    //When Next is Clicked
    public void next(View v){
        Log.d(tag,"Next Clicked");
        if(!db.isThereAnyStatus(userId)){
            //toast an error message
            Log.d(tag, "Next Clicked but no status was there before");

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    next.setText("NEXT");
                    next.setTextColor(getResources().getColor(R.color.positive));
                }
            }, 3000);
            next.setText("No Status Found");
            next.setTextColor(getResources().getColor(R.color.error));
        }else{
            //when there was a status
            Log.d(tag,"A status was found and fetch data to be confirmed");
            dialog=new Dialog(this);
            dialog.setContentView(R.layout.confirmexpandable);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);

            int dividerId = dialog.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
            if (dividerId != 0) {
                View divider = dialog.findViewById(dividerId);
                divider.setBackgroundColor(getResources().getColor(R.color.appcolor));
            }
            dialog.setTitle(Html.fromHtml("<font color='"+getResources().getColor(R.color.appcolor)+"'>Confirm to get Started</font>"));

            expListView = (ExpandableListView) dialog.findViewById(R.id.lvExp);
            Button done=(Button) dialog.findViewById(R.id.done);
            Button back=(Button) dialog.findViewById(R.id.back);

            List<WorkStatus> statuses=new ArrayList<WorkStatus>();
            List<String> pNames=new ArrayList<String>();
            List<String> pIds=new ArrayList<String>();
            List<String> pumpIdTemp=new ArrayList<String>();
            statuses=db.getAllStatus(userId);
            if(!statuses.isEmpty()){
                Iterator iterator=statuses.iterator();
                while (iterator.hasNext()){
                    WorkStatus ws=new WorkStatus();
                    ws=(WorkStatus)iterator.next();
                    Pump pump=new Pump();
                    pump=db.getSinglePump(ws.getPumpId());
                    if(pumpIdTemp.isEmpty()){
                        pumpIdTemp.add(String.valueOf(pump.getPumpId()));
                        pNames.add(pump.getPumpName());
                        pIds.add(String.valueOf(pump.getPumpId()));
                    }else if(!pumpIdTemp.contains(""+pump.getPumpId())){
                        pumpIdTemp.add(String.valueOf(pump.getPumpId()));
                        pNames.add(pump.getPumpName());
                        pIds.add(String.valueOf(pump.getPumpId()));
                    }
                }
            }
            pumpIdTemp=null;

            prepareListData(userId,pNames,pIds);

            listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

            // setting list adapter
            expListView.setAdapter(listAdapter);

            for (int i = 0; i < listAdapter.getGroupCount(); i ++)
                expListView.expandGroup(i);

//            // Listview Group click listener
//            expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
//
//                @Override
//                public boolean onGroupClick(ExpandableListView parent, View v,
//                                            int groupPosition, long id) {
//                    // Toast.makeText(getApplicationContext(),
//                    // "Group Clicked " + listDataHeader.get(groupPosition),
//                    // Toast.LENGTH_SHORT).show();
//                    return false;
//                }
//            });
//
//            // Listview Group expanded listener
//            expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
//
//                @Override
//                public void onGroupExpand(int groupPosition) {
//                    Toast.makeText(getApplicationContext(),
//                            listDataHeader.get(groupPosition) + " Expanded",
//                            Toast.LENGTH_SHORT).show();
//                }
//            });
//
//            // Listview Group collasped listener
//            expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
//
//                @Override
//                public void onGroupCollapse(int groupPosition) {
//                    Toast.makeText(getApplicationContext(),
//                            listDataHeader.get(groupPosition) + " Collapsed",
//                            Toast.LENGTH_SHORT).show();
//
//                }
//            });
//
//            // Listview on child click listener
//            expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//
//                @Override
//                public boolean onChildClick(ExpandableListView parent, View v,
//                                            int groupPosition, int childPosition, long id) {
//                    // TODO Auto-generated method stub
//                    Toast.makeText(
//                            getApplicationContext(),
//                            listDataHeader.get(groupPosition)
//                                    + " : "
//                                    + listDataChild.get(
//                                    listDataHeader.get(groupPosition)).get(
//                                    childPosition), Toast.LENGTH_SHORT)
//                            .show();
//                    return false;
//                }
//            });
//
//
//            back.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Log.e(tag, "Confirmation Dialogue dismissing. restart the process");
//                    dialog.dismiss();
//                }
//            });
//
//            done.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Log.v(tag, "Data confirmed to ready to be forwared to selling Page");
//                }
//            });

            back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.e(tag,"Exiting pump confirmation form");
                    dialog.dismiss();
                }
            });

            done.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v(tag,"Work status confirmed and ready to move");
                    //register pump
                    dialog.dismiss();
                    registerPump();

                }
            });

            dialog.show();
        }
    }

    //prepare confirm data
    private void prepareListData(int userId, List<String> pumpNames, List<String> pumpId) {
        Log.d(tag,"Preparing data for final selected pump report");

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        listDataHeader=pumpNames;
        List<String> nozzleRow;

        List<WorkStatus> statuses=new ArrayList<WorkStatus>();
        for(int i = 0; i < pumpId.size(); i ++){
            statuses=db.getAllStatusPerPump(userId,Long.parseLong(String.valueOf(pumpId.get(i))));
            nozzleRow = new ArrayList<String>();
            Iterator iterator=statuses.iterator();
            while (iterator.hasNext()){
                WorkStatus ws=new WorkStatus();
                ws=(WorkStatus) iterator.next();

                Nozzle nozzle=new Nozzle();
                nozzle=db.getSingleNozzle(ws.getNozzleId());

                nozzleRow.add(nozzle.getNozzleName());
                nozzleRow.add(nozzle.getProductName());
                nozzleRow.add(String.valueOf(nozzle.getNozzleIndex()));
                nozzleRow.add(ws.getMessage());
                nozzleRow.add("");
            }
            listDataChild.put(listDataHeader.get(i), nozzleRow); // Header, Child data
        }

    }

    public void uiFeedBack(String message){
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tv.setText("");
            }
        }, 3000);
        if(dialog.isShowing())
            dialog.dismiss();
        if(message==null || TextUtils.isEmpty(message)){
            tv.setText(getResources().getString(R.string.nulluifeedback));
        }else{
            tv.setText(message);
        }
    }

    @Override
    public void resultObject(Object object) {

        if(object==null){
            uiFeedBack(getResources().getString(R.string.connectionerror));
        }else if(object.getClass().getSimpleName().equalsIgnoreCase("ChoosenPumpResponse")){
            ChoosenPumpResponse cpr=(ChoosenPumpResponse) object;
            if(cpr.getStatusCode()==100){
                //finish the activity and set the user local status to 1
                List<ChoosenPumpAndNozzle> cpns=new ArrayList<ChoosenPumpAndNozzle>();
                cpns=cpr.getAssignPumpModel();
                String errorMessage="";
                Iterator iterator=cpns.iterator();
                while (iterator.hasNext()){
                    ChoosenPumpAndNozzle cpn=new ChoosenPumpAndNozzle();
                    cpn=(ChoosenPumpAndNozzle) iterator.next();
                    if(cpn.getStatus() != 100 ){
                        Pump pump=new Pump();
                        Nozzle nozzle=new Nozzle();
                        nozzle=db.getSingleNozzle(cpn.getNozzleId());
                        pump=db.getSinglePump(cpn.getPumpId());
                        errorMessage+=pump.getPumpName() +" And "+nozzle.getNozzleName()+ " Error: " + cpn.getMessage()+"\n";
                    }
                }
                if(TextUtils.isEmpty(errorMessage) || (errorMessage.length()<=0)){
                    Logged_in_user user=new Logged_in_user();
                    user=db.getSingleUser(userId);
                    user.setLogged(1);
                    if(db.updateUser(user)>0){
                        //user status was updated successfully go to sell portal
                        intent = new Intent(this, SellingTabHost.class);
                        Bundle bundle = new Bundle();
                        bundle.putString(getResources().getString(R.string.userid), String.valueOf(userId));
                        intent.putExtras(bundle);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        finish();
                        startActivity(intent);
                    }else{
                        //updating user failed
                        uiFeedBack(getResources().getString(R.string.faillurenotification));
                    }
                }else{
                    uiFeedBack(errorMessage);
                }

            }else{
                //when some pumps was taken
                uiFeedBack(getResources().getString(R.string.faillurenotification));
            }
        }else{
            uiFeedBack(getResources().getString(R.string.ambiguous));
        }
    }

    @Override
    public void feedBack(String message) {
        uiFeedBack(message);
    }

    public void registerPump(){
        Log.d(tag,"Registering choosen pumps and their nozzles");
        List<WorkStatus> statuses=db.getAllStatus(userId);
        List<ChoosenPumpAndNozzle> choosenPumpsList=new ArrayList<ChoosenPumpAndNozzle>();
        if(statuses.size()>0){
            Iterator iterator=statuses.iterator();
            while (iterator.hasNext()){
                WorkStatus ws=new WorkStatus();
                ChoosenPumpAndNozzle cp=new ChoosenPumpAndNozzle();
                ws=(WorkStatus) iterator.next();

                //Pump data to Post online
                cp.setUserId(ws.getUserId());
                cp.setNozzleId(ws.getNozzleId());
                cp.setPumpId(ws.getPumpId());
                cp.setMessage(ws.getMessage());
                cp.setStatus(ws.getStatusCode());

                choosenPumpsList.add(cp);
            }
            ChoosenPumps choosenPumps=new ChoosenPumps();

            choosenPumps.setAssignPumpModel(choosenPumpsList);

            String jsonString=mapperClass.mapping(choosenPumps);

            handUrl=new HandleUrl(this,this,getResources().getString(R.string.registerpumps),getResources().getString(R.string.post),jsonString);

        }else{
            uiFeedBack(getResources().getString(R.string.invaliddata));
        }
    }
}
