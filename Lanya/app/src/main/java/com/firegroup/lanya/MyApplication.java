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
import android.graphics.Canvas;
import android.graphics.Rect;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by Froze on 2017/10/24.
 */

public class MyApplication extends Application {

    String[] peerName;
    boolean start = false;
    SurfaceHolder myholder;
    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if (message.what == 0x200) {
                if(myholder == null)
                    return;
                Canvas canvas = myholder.lockCanvas();
                if (canvas != null) {
                    Bitmap bmp = (Bitmap) message.obj;
                    canvas.drawBitmap(bmp, null, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);
                    myholder.unlockCanvasAndPost(canvas);
                }else{
                    Toast.makeText(show.getApplicationContext(),"No view is find!",Toast.LENGTH_SHORT).show();
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

    public void sendMessage(String ms){
        if(BluetoothThread!=null){
            BluetoothThread.write(ms);
        }else{
            Toast.makeText(show.getApplicationContext(),"Please press the bluetooth button to connect first",Toast.LENGTH_SHORT).show();
        }
    }

    private static final boolean D = true;
    private BluetoothAdapter mBluetoothAdapter = null;

    private static String address = "00:11:03:21:00:43";


    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    WiFiDirectBroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    WiFiConnectThread myAcceptThread;

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

    @Override
    public void onCreate() {
        super.onCreate();
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager,mChannel,getApplicationContext(),mPeerListListener);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(D) Log.e(TAG,"++ On Create ++");
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
