package com.aub.oltranz.payfuel;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import appBean.GridData;
import appBean.GridNozzle;
import appBean.GridPump;
import databaseBean.DBHelper;
import entities.Nozzle;
import entities.Pump;
import entities.WorkStatus;
import features.StatusAdapter;

public class Selling extends ActionBarActivity implements AdapterView.OnItemClickListener {

    String tag="PayFuel: "+getClass().getSimpleName();
    int userId;

    TextView tv;
    GridView gv;

    Bundle savedBundle;
    Context context;

    DBHelper db;
    StatusAdapter sAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selling);

        //initialize app Components
        initAppComponents();

        //initialize app UI
        initAppUI();
    }

    public void initAppUI(){
        Log.d(tag, "Initializing Activity UI");

        tv=(TextView) findViewById(R.id.tv);
        gv=(GridView) findViewById(R.id.choosenlist);

        savedBundle =getIntent().getExtras();
        String extra= savedBundle.getString(getResources().getString(R.string.userid));
        userId=Integer.parseInt(extra);

        context=this;

        try{
            getPumpList(workStatusList(userId));
        }catch (Exception e){
            Log.e(tag,"Error Occurred. "+e.getMessage());
            e.printStackTrace();
        }
    }

    public void initAppComponents(){
        Log.d(tag,"Initializing Activity Components");
        db=new DBHelper(this);
    }

    public List<WorkStatus> workStatusList(int userId){
        Log.d(tag,"Getting Selected Pumps and their Nozzles");
        List<WorkStatus> statuses=db.getAllStatus(userId);
        return statuses;
    }

    public void getPumpList(List<WorkStatus> statuses){
        Log.d(tag,"Populating pump and their nozzle from workstatus");
        List<String> tempPumpId=new ArrayList<String>();
        GridPump gridPump;
        GridNozzle gridNozzle;
        List<GridNozzle>gridNozzleList;
        List<GridPump> gridPumpList=new ArrayList<GridPump>();
        GridData gridData=new GridData();
        try{
            Iterator iterator=statuses.iterator();
            while (iterator.hasNext()){
                WorkStatus ws=new WorkStatus();
                ws=(WorkStatus) iterator.next();

                //check if the pump id is there or is not coming more than one time
                if(tempPumpId.isEmpty()||tempPumpId==null || (!tempPumpId.contains(String.valueOf(ws.getPumpId()))) ){
                    tempPumpId.add(String.valueOf(ws.getPumpId()));

                    Pump pump=db.getSinglePump(ws.getPumpId());
                    gridPump=new GridPump();
                    gridPump.setPumpId(pump.getPumpId());
                    gridPump.setPumpName(pump.getPumpName());
                    List<Nozzle> nozzles=db.getAllNozzlePerPump(pump.getPumpId());
                    gridNozzleList=new ArrayList<GridNozzle>();

                    Iterator iterator1=nozzles.iterator();
                    while (iterator1.hasNext()){
                        Nozzle nozzle=new Nozzle();
                        nozzle=(Nozzle) iterator1.next();
                        gridNozzle=new GridNozzle();
                        gridNozzle.setNozzleId(nozzle.getNozzleId());
                        gridNozzle.setNozzleName(nozzle.getNozzleName());
                        gridNozzle.setPrice(nozzle.getUnitPrice());
                        gridNozzle.setProduct(nozzle.getProductName());
                        gridNozzleList.add(gridNozzle);
                    }
                    gridPump.setNozzles(gridNozzleList);
                    gridPumpList.add(gridPump);
                }
            }

            gridData.setmGridData(gridPumpList);
            sAdapter=new StatusAdapter(this,gridData);
            gv.setAdapter(sAdapter);
            gv.setOnItemClickListener(this);
        }catch (Exception e){
            Log.e(tag,"Error Occurred. "+e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Log.v(tag, "You clicked Pump: " + ((TextView) view.findViewById(R.id.pumpid)).getText() + " And Nozzle: " + ((TextView) view.findViewById(R.id.nozzleid)).getText());
    }
}
