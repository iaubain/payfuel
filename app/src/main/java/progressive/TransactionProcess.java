package progressive;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.aub.oltranz.payfuel.R;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import appBean.TransactionResponse;
import databaseBean.DBHelper;
import entities.AsyncTransaction;
import entities.Nozzle;
import entities.PaymentMode;
import entities.SellingTransaction;
import features.HandleUrl;
import features.HandleUrlInterface;
import models.MapperClass;

/**
 * Created by Owner on 5/23/2016.
 */
public class TransactionProcess implements HandleUrlInterface {
    String tag="PayFuel:"+getClass().getSimpleName();

    long transactionId;
    SellingTransaction st;
    int userId;
    TransactionFeedsInterface tfi;
    DBHelper db;
    HandleUrl handUrl;
    MapperClass mc;


    //Id generation of transactions
    static AtomicInteger nextId = new AtomicInteger();
    private static final AtomicLong lastTime = new AtomicLong();
    private int id;
    private Context context;

    public TransactionProcess(TransactionFeedsInterface tfi) {
        id = nextId.incrementAndGet();
        this.tfi=tfi;
        Log.d(tag,"Transaction initiated with id: "+id);
    }


    public long transactionDatas(Context context, SellingTransaction st){
        this.context=context;
        this.st=st;
        this.userId=st.getUserId();
        this.transactionId=idGenerator(id);
        db=new DBHelper(context);
        Log.d(tag,"Transaction ID created:"+transactionId);

        st.setDeviceTransactionId(transactionId);
        createTransaction(st);

        return transactionId;
    }

    public long idGenerator(int id){
        Log.d(tag,"Generating transaction Id of transactionObjectId:"+id);
        long now = uniqueNow();
        String ids=""+now+userId+id;
        long finalId=Long.parseLong(ids);
        return finalId;
    }

    public long uniqueNow() {
        long now = System.currentTimeMillis();
        while(true) {
            long lastTime = TransactionProcess.lastTime.get();
            if (lastTime >= now)
                now = lastTime+1;
            if (TransactionProcess.lastTime.compareAndSet(lastTime, now)){
                Log.d(tag,"Unique transaction time: "+now);
                return now;
            }
        }
    }

    @Override
    public void resultObject(Object object) {
        Log.d(tag,"Getting result from the server");
        try{
            if((object.getClass().getSimpleName().equalsIgnoreCase("TransactionResponse"))&&(object!=null)){
                TransactionResponse tr=(TransactionResponse) object;
                if(tr.getStatusCode()==301){
                    Log.d(tag,"A pending transaction: "+tr.getSellingTransaction().getDeviceTransactionId());
                    SellingTransaction st=tr.getSellingTransaction();
                    AsyncTransaction at=new AsyncTransaction();
                    at.setSum(0);
                    at.setDeviceNo(st.getDeviceNo());
                    at.setUserId(st.getUserId());
                    at.setBranchId(st.getBranchId());
                    at.setTransactionId(st.getDeviceTransactionId());

                    long dbId=db.createAsyncTransaction(at);
                    if(dbId<=0){
                        tfi.feedsMessage("Pending transaction Failed to be Recorded:"+st.getDeviceTransactionId());
                    }
                }else if(tr.getStatusCode()==100){
                    SellingTransaction st=tr.getSellingTransaction();
                    PaymentMode pm=db.getSinglePaymentMode(st.getPaymentModeId());
                    if(!pm.getName().equalsIgnoreCase("cash")){
                        Nozzle nozzle=new Nozzle();
                        nozzle=db.getSingleNozzle(st.getNozzleId());
                        incrementIndex(st.getNozzleId(), nozzle.getNozzleIndex(),st.getQuantity());
                    }
                }else if(tr.getStatusCode()==500){
                    SellingTransaction st=tr.getSellingTransaction();
                    long dbId=db.updateTransaction(st);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void feedBack(String message) {
        Log.d(tag,"Server error");
        tfi.feedsMessage(message);
    }

    public void createTransaction(SellingTransaction st){
        Log.d(tag, "Creating a transaction of payment type: " + db.getSinglePaymentMode(st.getPaymentModeId()).getName());

        /*
        * Transaction status: 100(Cash-Success) 301(Pending) 500(canceled)
        * Transaction Receipt generator: 101(Cash-Success-Receipt) 301(Pending-Receipt) 501(Canceled)
        * */

        PaymentMode pm=db.getSinglePaymentMode(st.getPaymentModeId());

        //if payment mode equals cash
        if(pm.getName().equalsIgnoreCase("cash")){
            st.setStatus(100);
            long transactonId=db.createTransaction(st);
            if(transactionId<=0){
                tfi.feedsMessage(context.getResources().getString(R.string.faillurenotification));
                return;
            }

            Nozzle nozzle=new Nozzle();
            nozzle=db.getSingleNozzle(st.getNozzleId());
            incrementIndex(st.getNozzleId(), nozzle.getNozzleIndex(),st.getQuantity());
            st=db.getSingleTransaction(st.getDeviceTransactionId());
        }else{
            st.setStatus(301);
            long transactonId=db.createTransaction(st);
            if(transactionId<=0){
                tfi.feedsMessage(context.getResources().getString(R.string.faillurenotification));
                return;
            }

            st=db.getSingleTransaction(st.getDeviceTransactionId());
        }

        //sending a transaction Online
        mc=new MapperClass();
        handUrl=new HandleUrl(this,context,context.getResources().getString(R.string.transactionurl),context.getResources().getString(R.string.post),mc.mapping(st));
    }

    public void incrementIndex(int nozzleId, Double currentIndex, Double valueToAdd){
        Log.d(tag,"Updating nozzle: "+nozzleId+"'s Indexes");
        Nozzle nozzle=db.getSingleNozzle(nozzleId);
        Double newIndex=currentIndex+valueToAdd;
        nozzle.setNozzleIndex(newIndex);
        long dbId=db.updateNozzle(nozzle);
        Log.v(tag,"Nozzle "+dbId+" Updated");
    }
}
