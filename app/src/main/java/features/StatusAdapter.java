package features;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.aub.oltranz.payfuel.R;

import java.util.List;

import appBean.GridData;

/**
 * Created by Owner on 5/17/2016.
 */
public class StatusAdapter extends BaseAdapter {
    String tag="PayFuel: "+StatusAdapter.class.getSimpleName();

    private Context context;
    private final List<GridData> mData;

    public StatusAdapter(Context context, List<GridData> mData) {
        Log.d(tag,"Initialise griview Content");
        this.context = context;
        this.mData = mData;
    }
    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        Log.d(tag,"Grid View constructing");

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View gridViewElement = new View(context);

        // get layout Style
        gridViewElement = inflater.inflate(R.layout.statuses_style, null);

        // set values
        TextView nozzleId = (TextView) gridViewElement.findViewById(R.id.nozzleid);
        TextView nozzleName = (TextView) gridViewElement.findViewById(R.id.nozzlename);
        TextView pumpId = (TextView) gridViewElement.findViewById(R.id.pumpid);
        TextView pumpName = (TextView) gridViewElement.findViewById(R.id.pumpname);
        TextView product = (TextView) gridViewElement.findViewById(R.id.nozzleproduct);
        TextView index = (TextView) gridViewElement.findViewById(R.id.index);
        TextView price = (TextView) gridViewElement.findViewById(R.id.price);
        TextView productId = (TextView) gridViewElement.findViewById(R.id.productid);
        ImageView icon=(ImageView) gridViewElement.findViewById(R.id.nozzleicon);

//        if (view == null) {
            GridData gridData=new GridData();
            gridData=mData.get(position);
            nozzleId.setText(String.valueOf(gridData.getNozzleId()));
            nozzleName.setText(gridData.getNozzleName());
            product.setText(gridData.getProduct());
            price.setText(String.valueOf(gridData.getPrice()));
            productId.setText(String.valueOf(gridData.getProductId()));
            pumpId.setText(String.valueOf(gridData.getPumpId()));
            pumpName.setText(gridData.getPumpName());
            index.setText(gridData.getIndex());

//            GridPump gridPump;
//            GridNozzle gridNozzle;
//            List<GridNozzle> gridNozzleList;
//            List<GridPump> gridPumpList=new ArrayList<GridPump>();
//            GridData gridData=new GridData();
//
//            gridPumpList=mData.getmGridData();
//            Iterator iterator=gridPumpList.iterator();
//            while(iterator.hasNext()){
//
//                gridPump=new GridPump();
//                gridPump=(GridPump) iterator.next();
//
//                gridNozzleList=new ArrayList<GridNozzle>();
//                gridNozzleList=gridPump.getNozzles();
//                Iterator iterator1=gridNozzleList.iterator();
//                while(iterator1.hasNext()){
//
//                    gridNozzle=new GridNozzle();
//                    gridNozzle=(GridNozzle) iterator1.next();
//
//                    pumpId.setText(String.valueOf(gridPump.getPumpId()));
//                    pumpName.setText(String.valueOf(gridPump.getPumpName()));
//
//                    nozzleId.setText(String.valueOf(gridNozzle.getNozzleId()));
//                    nozzleName.setText(String.valueOf(gridNozzle.getNozzleName()));
//                    product.setText(String.valueOf(gridNozzle.getProduct()));
//                    index.setText("Index");
//                    price.setText(String.valueOf(gridNozzle.getPrice()));
//                }
//            }

//        } else {
//            gridViewElement = (View) view;
//        }

        return gridViewElement;
    }
}
