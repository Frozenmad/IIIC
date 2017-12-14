package com.firegroup.lanya;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Froze on 2017/10/24.
 */



public class MyApplication extends Application {

    FaceDB mFaceDB;
    Uri mImage;
    Scalar LowerBound = new Scalar(20,20,20);
    Scalar UpperBound = new Scalar(100,100,100);
    String[] peerName;
    boolean start = false;
    boolean deal = false;
    boolean control = false;
    int[] Parameter = {0,0,0,0};
    int updown = 0;
    int leftright = 0;
    SurfaceHolder myholder, deal_holder;
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if (message.what == 0x200) {
                if(myholder == null)
                    return;
                Bitmap bmp = (Bitmap) message.obj;
                Canvas canvas = myholder.lockCanvas();
                if (canvas != null) {
                    canvas.drawBitmap(bmp, null, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);
                    myholder.unlockCanvasAndPost(canvas);
                }else{
                    Toast.makeText(show.getApplicationContext(),"No view is find!",Toast.LENGTH_SHORT).show();
                }
                if(deal) {
                    Canvas deals = deal_holder.lockCanvas();
                    if (deals != null) {
                        Bitmap res = FindObject(bmp);
                        if(res != null) {
                            deals.drawBitmap(res, null, new Rect(0, 0, deals.getWidth(), deals.getHeight()), null);
                            if(control)
                                Control_from_picture(res);
                        }
                        else{
                            Toast.makeText(show.getApplicationContext(),"CV failed!",Toast.LENGTH_SHORT).show();
                        }
                        deal_holder.unlockCanvasAndPost(deals);
                    }
                }
            }

            if (message.what == 0x300){
                Toast.makeText(show.getApplicationContext(),(String)message.obj,Toast.LENGTH_SHORT).show();
            }
        }
    };

    BluetoothConnectThread BluetoothThread;
    Activity show;
    String TAG = "MyApplication";

    //PeerListListener
    WifiP2pManager.PeerListListener mPeerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            peers.clear();
            peers.addAll(wifiP2pDeviceList.getDeviceList());
            peerName = new String[peers.size()];
            if (peers.size() == 0) {
                peerName = new String[1];
                peerName[0] = "No device";
                Log.e(TAG,"No device");
            } else {
                //Find some devices
                Log.e(TAG,"Find device");
                int i = 0;
                for (WifiP2pDevice device : peers) {
                    peerName[i++] = device.deviceName;
                }
                if(!start) {
                    AlertDialog.Builder WIFI = new AlertDialog.Builder(show);
                    WIFI.setTitle("Please select one device");
                    WIFI.setItems(peerName, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            connect(i);
                        }
                    });
                    WIFI.show();
                    start = true;
                }
            }
        }
    };

    //Used to connect the num'th WIFI device
    public void connect(final int num) {
        WifiP2pDevice device = peers.get(num);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(show.getApplicationContext(), "Conncet sigao", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(show.getApplicationContext(), "Failed to connect:" + Integer.toString(reason), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void setControl(boolean controls){
        control = controls;
    }

    private static final boolean D = true;
    private BluetoothAdapter mBluetoothAdapter = null;

    private static String address = "00:11:03:21:00:43";

    private Bitmap scaleBitmap(Bitmap origin, float ratio) {
        if (origin == null) {
            return null;
        }
        int width = origin.getWidth();
        int height = origin.getHeight();
        Matrix matrix = new Matrix();
        matrix.preScale(ratio, ratio);
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (newBM.equals(origin)) {
            return newBM;
        }
        origin.recycle();
        return newBM;
    }

    public void setParameter(int position, int value){
        Parameter[position] = value;
    }

    public Bitmap FindObject(Bitmap bitmap){
        Bitmap mymap = scaleBitmap(bitmap,(float)0.2);
        Mat image_RGB = new Mat();
        Utils.bitmapToMat(mymap,image_RGB);
        Mat image_HSV = new Mat();
        //Get the HSV mat
        Imgproc.cvtColor(image_RGB,image_HSV,Imgproc.COLOR_RGB2HSV);
        Mat image_Filter = new Mat();
        Core.inRange(image_HSV,LowerBound,UpperBound,image_Filter);
        ArrayList<int[]> Compute_list = GetArray(image_Filter);
        double mid = getMean(Compute_list,0,true,Compute_list.size());
        Bitmap result = Bitmap.createBitmap(mymap);
        Utils.matToBitmap(image_Filter,result);
        result.setPixel((int)mid,0, Color.GREEN);
        result.setPixel(result.getWidth()/2,2,Color.BLUE);
        result.setPixel(result.getWidth()/2 - Parameter[2], 2, Color.MAGENTA);
        result.setPixel(result.getWidth()/2 + Parameter[2], 2, Color.MAGENTA);
        result.setPixel(result.getWidth()/2 + Parameter[1]/2,4,Color.RED);
        result.setPixel(result.getWidth()/2 + Parameter[1]/2,4,Color.RED);
        result.setPixel(result.getWidth()/2 + Parameter[0]/2,6,Color.YELLOW);
        result.setPixel(result.getWidth()/2 + Parameter[0]/2,6,Color.YELLOW);
        //Log.e("Length",String.valueOf(image_Filter.cols()));
        //Log.e("Width",String.valueOf(image_Filter.rows()));
        return result;
    }

    public void Control_from_picture(Bitmap bitmap){
        Mat image_Filter = new Mat();
        Utils.bitmapToMat(bitmap,image_Filter);
        ArrayList<int[]> Compute_list = GetArray(image_Filter);
        double bound_left = getMean(Compute_list,0,true,30);
        double bound_right = getMean(Compute_list,0,false,30);
        double width = bound_right - bound_left;
        double mid = getMean(Compute_list,0,true,Compute_list.size());
        double eval = image_Filter.cols()/2 - mid;
        Log.e("Mid",String.valueOf(mid));
        Log.e("Eval",String.valueOf(eval));
        Log.e("width",String.valueOf(width));
        if(eval > Parameter[2]) {
            setLeftright(1);
            setUpdown(0);
            sendMessage();
        }
        else if(eval < -Parameter[2]){
            setLeftright(2);
            setUpdown(0);
            sendMessage();
        }
        else{
            setLeftright(0);
            if(width > Parameter[0]) if (Parameter[3]==0) setUpdown(3); else setUpdown(4);
            else if(width < Parameter[1]) if (Parameter[3]==0) setUpdown(1); else setUpdown(2);
            else setUpdown(0);
            sendMessage();
        }
    }

    public ArrayList<int[]> SortList(ArrayList<int[]> TargetList, boolean low_to_high, int col_or_row){
        if(TargetList.size() == 0 || TargetList.size() == 1) return TargetList;
        if(TargetList.size() == 2){
            ArrayList<int[]> result = new ArrayList<>();
            result.add(TargetList.get(1));
            result.add(TargetList.get(0));
            if(TargetList.get(0)[col_or_row] < TargetList.get(1)[col_or_row]) return low_to_high ? TargetList : result;
            else return low_to_high ? result : TargetList;
        }
        int mid = TargetList.size() / 2;
        ArrayList<int[]> result = new ArrayList<>();
        ArrayList<int[]> half_a = new ArrayList<>();
        ArrayList<int[]> half_b = new ArrayList<>();
        for(int index = 0; index < mid; index++) half_a.add(TargetList.get(index));
        for(int index = mid; index < TargetList.size(); index++) half_b.add(TargetList.get(index));
        result.addAll(SortList(half_a,low_to_high,col_or_row));
        result.addAll(SortList(half_b,low_to_high,col_or_row));
        return result;
    }

    public ArrayList<int[]> GetArray(Mat mat){
        // Transfer mat into array
        // if array = 0, transfer by column
        // else, transfer by row
        // Return an array whose element is [column, row]
        ArrayList<int[]> result = new ArrayList<>();
        for (int column = 0; column < mat.cols(); column += 1) {
            for (int row = 0; row < mat.rows(); row += 1) {
                if (mat.get(row, column)[0] > 0.01) {
                    int[] item = {column, row};
                    result.add(item);
                }
            }
        }
        return result;
    }

    public double getMean(ArrayList<int[]> arrayList,int col, boolean begin, int n){
        // Get the mean of certain element
        n = (n <= arrayList.size()) ? n : arrayList.size();
        if (n == 0) return 0;
        double total = 0.;
        if(begin){
            for (int index = 0; index < n; index++){
                total += arrayList.get(index)[col];
            }
        }else{
            for (int index = arrayList.size()-1; index >= arrayList.size()-n; index--){
                total += arrayList.get(index)[col];
            }
        }
        total = total / n;
        return total;
    }

    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WiFiDirectBroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    WiFiConnectThread myAcceptThread;
    long Time_val;

    public void startDiscover(){
        start = false;
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(show.getApplicationContext(),"Discover successfully!",Toast.LENGTH_LONG).show();
            }
            @Override
            public void onFailure(int i) {}
        });
    }

    public void setMyholder(SurfaceHolder myholder){
        this.myholder = myholder;
    }

    public void setActivity(Activity myact){
        show = myact;
    }

    public void startWIFI(){

        myAcceptThread = new WiFiConnectThread(myHandler);
        myAcceptThread.start();
    }

    public void setUpdown(int updown){
        this.updown = updown;
    }

    public void setLeftright(int leftright){
        this.leftright = leftright;
    }

    public void setActions(int actions){
        setUpdown(actions % 5);
        setLeftright((int)(actions / 5));
    }

    public int getActions(){
        return updown + 5 * leftright;
    }

    public void sendMessage(){
        long current = SystemClock.uptimeMillis();
        if ((current - Time_val) < 50 && (Time_val - current) < 50) return;
        Time_val = current;
        byte message;
        if (leftright != 0){
            if(updown == 0) message = (byte)((leftright+4) & 0xff);
            else if(updown == 1 || updown == 2) message = (byte)((leftright+6) & 0xff);
            else message = (byte)((leftright+8) & 0xff);
        }
        else{
            message = (byte)((updown) & 0xff);
        }
        if(BluetoothThread!=null){BluetoothThread.write(message);}
        else{Toast.makeText(show,"Please press the bluetooth button to connect first",Toast.LENGTH_SHORT).show();}
    }

    @Override
    public void onCreate() {
        super.onCreate();
        staticLoadCVLibraries();
        if(mManager == null) mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if(mChannel == null) mChannel = mManager.initialize(this,getMainLooper(),null);
        if(mReceiver == null) mReceiver = new WiFiDirectBroadcastReceiver(mManager,mChannel,getApplicationContext(),mPeerListListener);
        if(mIntentFilter == null) {
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        }
        if(mBluetoothAdapter == null) mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deal = false;
        if(D) Log.e(TAG,"++ On Create ++");
        mFaceDB = new FaceDB(this.getExternalCacheDir().getPath());
        mImage = null;
        Time_val = SystemClock.uptimeMillis();
    }

    private void staticLoadCVLibraries(){
        boolean load = OpenCVLoader.initDebug();
//        System.loadLibrary("opencv_java");
        if(load) {
            Log.i("CV", "Open CV Libraries loaded...");
        }
    }

    public void startBluetooth(){

        if (mBluetoothAdapter == null) {
            Toast.makeText(show.getApplicationContext(), "Bluetooth is not available.", Toast.LENGTH_LONG).show();
            show.finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(show.getApplicationContext(), "Please enable your Bluetooth and re-run this program.", Toast.LENGTH_LONG).show();
            show.finish();
        }
        registerReceiver(mReceiver,mIntentFilter);
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        final String[] devices = new String[pairedDevices.size()];
        final String[] addresses = new String[pairedDevices.size()];
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            int i = 0;
            for (BluetoothDevice device : pairedDevices) {
                devices[i] = device.getName();
                addresses[i] = device.getAddress();
                i = i+1;
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(show);
        builder.setTitle("Please select one device");
        builder.setItems(devices,new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                address = addresses[which];
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

                BluetoothThread = new BluetoothConnectThread(device,mBluetoothAdapter,myHandler);
                BluetoothThread.start();
            }

        });
        builder.show();
    }

    public void setCaptureImage(Uri uri) {
        mImage = uri;
    }

    public Uri getCaptureImage() {
        return mImage;
    }

    public static Bitmap decodeImage(String path) {
        Bitmap res;
        try {
            ExifInterface exif = new ExifInterface(path);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inSampleSize = 1;
            op.inJustDecodeBounds = false;
            //op.inMutable = true;
            res = BitmapFactory.decodeFile(path, op);
            //rotate and scale.
            Matrix matrix = new Matrix();

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }

            Bitmap temp = Bitmap.createBitmap(res, 0, 0, res.getWidth(), res.getHeight(), matrix, true);
            Log.d("com.firegroup", "check target Image:" + temp.getWidth() + "X" + temp.getHeight());

            if (!temp.equals(res)) {
                res.recycle();
            }
            return temp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setLowUp(double l1, double l2, double l3, double u1, double u2, double u3) {
        LowerBound = new Scalar(l1,l2,l3);
        UpperBound = new Scalar(u1,u2,u3);
    }

    public void setLow(int l,int i){
        if(i<3 && i>=0)
            LowerBound.val[i] = l;
    }

    public void setHigh(int h, int i){
        if(i<3 && i>=0)
            UpperBound.val[i] = h;
    }

    public void setDeal(boolean on_off){
        this.deal = on_off;
    }

    public void setDeal_holder(SurfaceHolder myholder){
        this.deal_holder = myholder;
    }

    public void Out(){
        unregisterReceiver(mReceiver);
        if(BluetoothThread != null) {
            BluetoothThread.cancel();
        }
    }

    public BluetoothConnectThread getBluetoothThread(){
        return BluetoothThread;
    }

    public WiFiConnectThread getMyAcceptThread(){
        return myAcceptThread;
    }
}
