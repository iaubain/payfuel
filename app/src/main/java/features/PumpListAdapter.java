package features;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aub.oltranz.payfuel.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import databaseBean.DBHelper;
import entities.WorkStatus;

/**
 * Created by Owner on 5/4/2016.
 */

public class PumpListAdapter extends ArrayAdapter<String> {

    String tag="PayFuel: "+getClass().getSimpleName();
    private final Activity context;
    private final List<String> pumpName;
    private final List<String> imgid,pumpIds;
    private final int userId;
    DBHelper db;


    public PumpListAdapter(Activity context,int userId, List<String> pumpName, List<String> pumpIds, List<String> imgid) {
        super(context, R.layout.pump_list_style, pumpName);
        // TODO Auto-generated constructor stub
        Log.d(tag,"Construct Pump List Adapter");
        this.context=context;
        this.userId=userId;
        this.pumpName =pumpName;
        this.pumpIds=pumpIds;
        this.imgid=imgid;
        db=new DBHelper(context);

    }

    public View getView(int position,View view,ViewGroup parent) {
        Log.d(tag,"Pump List Row Default Values Handle");
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.pump_list_style, null, true);

        TextView pumpName = (TextView) rowView.findViewById(R.id.pumpname);
        ImageView pumpImg = (ImageView) rowView.findViewById(R.id.icon);
        TextView pumpLabel = (TextView) rowView.findViewById(R.id.indicator);


        pumpName.setText(this.pumpName.get(position));

        List<WorkStatus>  statuses=new ArrayList<WorkStatus>();
        statuses=db.getAllStatusPerPump(userId,Long.parseLong(String.valueOf(pumpIds.get(position))));
        Iterator iterator=statuses.iterator();

        int nozzleDenialCheck = 0;
        int nozzleAcceptCheck = 0;

        while (iterator.hasNext()){
            WorkStatus ws=new WorkStatus();
            ws=(WorkStatus) iterator.next();
            if(ws.getStatusCode()==1){
//                pumpLabel.setText("Nozzle(s) Accepted");
//                pumpLabel.setTextColor(context.getResources().getColor(R.color.rdcolor));
//                pumpImg.setImageResource(R.drawable.pump_green);
                nozzleAcceptCheck += 1;
            }else if(ws.getStatusCode()==0){
//                pumpImg.setImageResource(R.drawable.pump_red);
//                pumpLabel.setText("Nozzle(s) rejected");
//                pumpLabel.setTextColor(context.getResources().getColor(R.color.error));
//
                nozzleDenialCheck += 1;
                break;
            }
        }

        if(nozzleDenialCheck > 0){
            pumpImg.setImageResource(R.drawable.pump_red);
            pumpLabel.setText("Nozzle(s) rejected");
            pumpLabel.setTextColor(context.getResources().getColor(R.color.error));
        } else if(nozzleAcceptCheck > 0){
            pumpLabel.setText("Nozzle(s) Accepted");
            pumpLabel.setTextColor(context.getResources().getColor(R.color.rdcolor));
            pumpImg.setImageResource(R.drawable.pump_green);
        }else{
            pumpLabel.setText("Available to Choose");
            //pumpLabel.setTextColor(context.getResources().getColor(R.color.rdoff));
            pumpImg.setImageResource(R.drawable.pump_blue);
        }
        return rowView;
    }
}