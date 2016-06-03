package features;

import android.app.Activity;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.aub.oltranz.payfuel.R;

import java.util.ArrayList;
import java.util.List;

import appBean.PumpAndNozzle;
import appBean.PumpAndNozzleList;
import databaseBean.DBHelper;
import entities.Nozzle;
import entities.WorkStatus;

/**
 * Created by Owner on 5/4/2016.
 */

public class NozzleListAdapter extends ArrayAdapter<String> {

    String tag="PayFuel: "+getClass().getSimpleName();
    private final Activity context;
    private final List<String> nozzleNameList;
//    private final List<String> imgIdList;
//    private final List<String> indexList;
//    private final List<String> productList;
//    private final List<String> nozzleId;
    private final List<Nozzle> nozzles;
    private final int userId;
    private final int pumpId;
    private final View pumpView;
    DBHelper db;
    PumpAndNozzleList choosenPumpAndNozzle;
    NozzleDenied nd;
    List<PumpAndNozzle> selectedList;



    public NozzleListAdapter(Activity context,int userId, int pumpId, View pumpView, List<Nozzle> nozzles, List<String> nozzleNameList/*, List<String> imgIdList, List<String> indexList,List<String> productList, List<String> nozzleId*/) {
        super(context, R.layout.nozzle_list_style,nozzleNameList);
        // TODO Auto-generated constructor stub
        Log.d(tag,"Construct Nozzle List Adapter of pump: ");
        this.context=context;
        this.userId=userId;
        this.pumpId=pumpId;
        this.nozzles=nozzles;
        this.pumpView=pumpView;
        this.nozzleNameList = nozzleNameList;
//        this.indexList=indexList;
//        this.productList=productList;
//        this.imgIdList = imgIdList;
//        this.nozzleId=nozzleId;
        db=new DBHelper(context);
        nd=new NozzleDenied();
        choosenPumpAndNozzle=new PumpAndNozzleList();
        selectedList=new ArrayList<PumpAndNozzle>();
    }

    public View getView(final int position,View view,ViewGroup parent) {
        Log.d(tag,"Pump List Row Default Values Handle");



        nd.setIsNozzleDenied(false);

        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.nozzle_list_style, null, true);

        TextView nozzleName = (TextView) rowView.findViewById(R.id.nozzlename);
        final ImageView imageView = (ImageView) rowView.findViewById(R.id.nozzleicon);
        TextView index = (TextView) rowView.findViewById(R.id.index);
        TextView product = (TextView) rowView.findViewById(R.id.nozzleproduct);
        final TextView label = (TextView) rowView.findViewById(R.id.nozzleindicator);

        final TextView pumpLabel=(TextView) pumpView.findViewById(R.id.indicator);
        final ImageView pumpImg=(ImageView) pumpView.findViewById(R.id.icon);

        Button refuse=(Button) rowView.findViewById(R.id.refuse);
        Button accept=(Button) rowView.findViewById(R.id.accept);

        final Nozzle nozzle=nozzles.get(position);

        //When a nozzle was already taken
        if(nozzle.getStatusCode()==8){
            nozzleName.setText("Taken by:"+nozzle.getUserName());
            accept.setEnabled(false);
            accept.setClickable(false);

            refuse.setEnabled(false);
            refuse.setClickable(false);

        }else{
            nozzleName.setText(nozzle.getNozzleName());
        }

        index.append("" + nozzle.getNozzleIndex());
        product.setText(nozzle.getProductName());

        //_____________________Check the status of nozzle_____________________\\
        int statusCount=db.getStatusCountByUserAndPump(userId,nozzle.getNozzleId());
        if(statusCount > 0){
            //when some has been selected so far
            WorkStatus ws=db.getSingleStatus(userId,nozzle.getNozzleId());
            if(ws != null){
                //when a nozzle status was found
                if(ws.getStatusCode()==1){
                    //when nozzle was selected
                    label.setTextColor(context.getResources().getColor(R.color.rdcolor));
                    label.setText("Accepted");
                    imageView.setImageResource(R.drawable.nozzle_green);
                }else if(ws.getStatusCode()==0){
                    //when nozzle was denied
                    label.setTextColor(context.getResources().getColor(R.color.error));
                    label.setText("Denied");
                    imageView.setImageResource(R.drawable.nozzle_red);
                }else{
                    //when no status was found
                    label.setText("Available to Choose");
                    imageView.setImageResource(R.drawable.nozzle_blue);
                }
            }else{
                //When no status was not found
                label.setText("Available to Choose");
                imageView.setImageResource(R.drawable.nozzle_blue);
            }

        }else{
            //when nothing was selected before
            label.setText("Available to Choose");
            imageView.setImageResource(R.drawable.nozzle_blue);
        }


        refuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(tag, "Refused: " + nozzle.getNozzleId());
                WorkStatus ws;
                int statusCount=db.getStatusCountByUserAndPump(userId,nozzle.getNozzleId());
                if(statusCount<=0){
                    ws=new WorkStatus();
                    ws.setMessage("Denied");
                    ws.setStatusCode(0);
                    ws.setNozzleId(nozzle.getNozzleId());
                    ws.setPumpId(pumpId);
                    ws.setUserId(userId);
                    int dbId=(int) db.createStatus(ws);
                    if(dbId > 0){
                        label.setTextColor(context.getResources().getColor(R.color.error));
                        label.setText("Denied");
                        imageView.setImageResource(R.drawable.nozzle_red);
                    }else{
                        label.setTextColor(context.getResources().getColor(R.color.error));
                        label.setText("Error Choosing");
                        imageView.setImageResource(R.drawable.nozzle_red);
                    }
                }else if(statusCount > 0){
                    ws=new WorkStatus();
                    ws.setMessage("Denied");
                    ws.setStatusCode(0);
                    ws.setNozzleId(nozzle.getNozzleId());
                    int dbId= db.updateStatus(ws);
                    if(dbId > 0){
                        label.setTextColor(context.getResources().getColor(R.color.error));
                        label.setText("Denied");
                        imageView.setImageResource(R.drawable.nozzle_red);
                    }else{
                        label.setTextColor(context.getResources().getColor(R.color.error));
                        label.setText("Error Choosing");
                        imageView.setImageResource(R.drawable.nozzle_red);
                    }
                }else{
                    label.setTextColor(context.getResources().getColor(R.color.error));
                    label.setText("Status Error");
                    imageView.setImageResource(R.drawable.nozzle_red);
                }

            }
        });

        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(tag, "Accepted: " + nozzle.getNozzleId());
                WorkStatus ws;
                int statusCount=db.getStatusCountByUserAndPump(userId,nozzle.getNozzleId());
                if(statusCount<=0){
                    ws=new WorkStatus();
                    ws.setMessage("Accepted");
                    ws.setStatusCode(1);
                    ws.setNozzleId(nozzle.getNozzleId());
                    ws.setPumpId(pumpId);
                    ws.setUserId(userId);
                    int dbId=(int) db.createStatus(ws);
                    if(dbId > 0){
                        label.setTextColor(context.getResources().getColor(R.color.rdcolor));
                        label.setText("Accepted");
                        imageView.setImageResource(R.drawable.nozzle_green);
                    }else{
                        label.setTextColor(context.getResources().getColor(R.color.error));
                        label.setText("Error Choosing");
                        imageView.setImageResource(R.drawable.nozzle_red);
                    }
                }else if(statusCount > 0){
                    ws=new WorkStatus();
                    ws.setMessage("Accepted");
                    ws.setStatusCode(1);
                    ws.setNozzleId(nozzle.getNozzleId());
                    int dbId= db.updateStatus(ws);
                    if(dbId > 0){
                        label.setTextColor(context.getResources().getColor(R.color.rdcolor));
                        label.setText("Accepted");
                        imageView.setImageResource(R.drawable.nozzle_green);
                    }else{
                        label.setTextColor(context.getResources().getColor(R.color.error));
                        label.setText("Error Choosing");
                        imageView.setImageResource(R.drawable.nozzle_red);
                    }
                }else{
                    label.setTextColor(context.getResources().getColor(R.color.error));
                    label.setText("Status Error");
                    imageView.setImageResource(R.drawable.nozzle_red);
                }

            }
        });

        return rowView;

    }
}