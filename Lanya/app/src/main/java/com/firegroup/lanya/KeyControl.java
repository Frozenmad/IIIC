package com.firegroup.lanya;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class KeyControl extends Activity {

    //String[] peerName;
    boolean start = false;
    boolean begin = false;
    SurfaceView image;
    SurfaceHolder myholder;
    /*Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            if (message.what == 0x200) {
                Canvas canvas = myholder.lockCanvas();
                if (canvas != null) {
                    Bitmap bmp = (Bitmap) message.obj;
                    canvas.drawBitmap(bmp, null, new Rect(0, 0, canvas.getWidth(), canvas.getHeight()), null);
                    myholder.unlockCanvasAndPost(canvas);
                }else{
                    Toast.makeText(getApplicationContext(),"No view is find!",Toast.LENGTH_SHORT).show();
                }
            }

            if (message.what == 0x300){
                Toast.makeText(getApplicationContext(),(String)message.obj,Toast.LENGTH_SHORT).show();
            }
        }
    };*/

    BluetoothConnectThread BluetoothThread;

    //PeerListListener
 /*   WifiP2pManager.PeerListListener mPeerListListener = new WifiP2pManager.PeerListListener() {
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
                    AlertDialog.Builder WIFI = new AlertDialog.Builder(KeyControl.this);
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
    };*/

    //Used to connect the num'th WIFI device
 /*   public void connect(final int num) {
        WifiP2pDevice device = peers.get(num);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(getApplicationContext(), "Conncet sigao", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(getApplicationContext(), "Failed to connect:" + Integer.toString(reason), Toast.LENGTH_LONG).show();
            }
        });
    }*/

    public void sendmessage(){
        String message = String.valueOf(Globals.getvalue());
        if(BluetoothThread!=null){BluetoothThread.write(message);}
        else{Toast.makeText(getApplicationContext(),"Please press the bluetooth button to connect first",Toast.LENGTH_SHORT).show();}
    }

    public void sendMessage(String ms){
        if(BluetoothThread!=null){
            BluetoothThread.write(ms);
        }else{
            Toast.makeText(getApplicationContext(),"Please press the bluetooth button to connect first",Toast.LENGTH_SHORT).show();
        }
    }

    private static final String TAG = "CircleControl";
    private static final boolean D = true;
    //private BluetoothAdapter mBluetoothAdapter = null;
    Button mButtonUp;
    Button mButtonDown;
    Button mButtonLeft;
    Button mButtonRight;

    private static String address = "00:11:03:21:00:43";

    public static class Globals{
        public static int updown = 0;
        public static int leftright = 0;
        public static int getvalue(){
            return updown*3+leftright;
        }
    }


    //WifiP2pManager mManager;
    //Channel mChannel;
    //WiFiDirectBroadcastReceiver mReceiver;
    //IntentFilter mIntentFilter;
    //ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    WiFiConnectThread myAcceptThread;

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        Window window = this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.key_control);
        /*mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this,getMainLooper(),null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager,mChannel,getApplicationContext(),mPeerListListener);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);*/
        MyApplication myapp = (MyApplication)getApplication();
        this.BluetoothThread = myapp.getBluetoothThread();
        myapp.setActivity(this);
        this.myAcceptThread = myapp.getMyAcceptThread();

        Button mwifi = (Button)findViewById(R.id.key_conn);
        mwifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start = false;
                begin = !begin;
                /*mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(getApplicationContext(),"Discover successfully!",Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void onFailure(int i) {}
                });*/
                if(begin) {
                    myAcceptThread.begin();
                }else{
                    myAcceptThread.pause();
                }
            }
        });
//前进
        mButtonUp=(Button)findViewById(R.id.up);
        mButtonUp.setOnTouchListener(new Button.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
// TODO Auto-generated method stub
                int action = event.getAction();
                switch(action)
                {
                    case MotionEvent.ACTION_DOWN:
                        Globals.updown = 1;
                        sendmessage();
                        break;

                    case MotionEvent.ACTION_UP:
                        Globals.updown = 0;
                        sendmessage();
                        break;
                }
                return false;
            }


        });
//后退
        mButtonDown=(Button)findViewById(R.id.down);
        mButtonDown.setOnTouchListener(new Button.OnTouchListener(){


            @Override
            public boolean onTouch(View v, MotionEvent event) {
// TODO Auto-generated method stub
                int action = event.getAction();
                switch(action)
                {
                    case MotionEvent.ACTION_DOWN:
                        Globals.updown=2;
                        sendmessage();
                        break;

                    case MotionEvent.ACTION_UP:
                        Globals.updown = 0;
                        sendmessage();
                        break;
                }
                return false;
            }


        });
//左转
        mButtonLeft=(Button)findViewById(R.id.left);
        mButtonLeft.setOnTouchListener(new Button.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
// TODO Auto-generated method stub
                int action = event.getAction();
                switch(action)
                {
                    case MotionEvent.ACTION_DOWN:
                        Globals.leftright = 1;
                        sendmessage();
                        break;
                    case MotionEvent.ACTION_UP:
                        Globals.leftright = 0;
                        sendmessage();
                        break;
                }
                return false;
            }
        });
//右转
        mButtonRight=(Button)findViewById(R.id.right);
        mButtonRight.setOnTouchListener(new Button.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
// TODO Auto-generated method stub
                int action = event.getAction();
                switch(action)
                {
                    case MotionEvent.ACTION_DOWN:
                        Globals.leftright = 2;
                        sendmessage();
                        break;

                    case MotionEvent.ACTION_UP:
                        Globals.leftright = 0;
                        sendmessage();
                        break;
                }
                return false;
            }


        });

        /*mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available.", Toast.LENGTH_LONG).show();
            finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Please enable your Bluetooth and re-run this program.", Toast.LENGTH_LONG).show();
            finish();
        }*/
        image = findViewById(R.id.key_image);
        myholder = image.getHolder();
        myholder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {}
            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {}
            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}
        });
        myapp.setMyholder(myholder);
/*        Button wifis = (Button)findViewById(R.id.key_wifi);
        wifis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myAcceptThread = new WiFiConnectThread(myHandler);
                myAcceptThread.start();
            }
        });*/

        if(D) Log.e(TAG,"++ On Create ++");
    }

    @Override

    public void onStart() {

        super.onStart();

        if (D) Log.e(TAG, "++ ON START ++");
    }


    @Override

    public void onResume() {

        super.onResume();
        if (D) {
            Log.e(TAG, "+ ON RESUME +");
            Log.e(TAG, "+ ABOUT TO ATTEMPT CLIENT CONNECT +");

        }
/*        registerReceiver(mReceiver,mIntentFilter);
        Button blue = (Button)findViewById(R.id.key_bluetooth);
        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                AlertDialog.Builder builder = new AlertDialog.Builder(KeyControl.this);
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
        });*/
    }

    @Override
    public void onPause() {
        super.onPause();
//        unregisterReceiver(mReceiver);
        if (D)
            Log.e(TAG, "- ON PAUSE -");
/*        if(BluetoothThread != null) {
            BluetoothThread.cancel();
        }*/
        if(begin){
            begin = false;
            myAcceptThread.pause();
        }
    }
}