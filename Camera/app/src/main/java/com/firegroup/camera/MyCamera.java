package com.firegroup.camera;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.opengl.GLES20;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;

public class MyCamera extends AppCompatActivity{
    private String host = "192.168.3.215";

    String[] peerName;
    boolean start = false;
    ArrayList<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    private LinkedList<byte[]> Picturelist = new LinkedList<>();
    private int maxsize = 15;
    Handler myHandler;

    WifiP2pManager.PeerListListener mPeerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            peers.clear();
            peers.addAll(wifiP2pDeviceList.getDeviceList());
            peerName = new String[peers.size()];
            if (peers.size() == 0) {
                peerName = new String[1];
                peerName[0] = "No device";
            } else {
                //Find some devices
                int i = 0;
                for (WifiP2pDevice device : peers) {
                    peerName[i++] = device.deviceName;
                }
                if(!start) {
                    AlertDialog.Builder WIFI = new AlertDialog.Builder(MyCamera.this);
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

    public void connect(final int num) {
        WifiP2pDevice device = peers.get(num);
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        //config.wps.setup = WpsInfo.PBC;
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
    }

    private ConnectThread mThread;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;
    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;
    private Camera mCamera;
    private CameraPreview mPreview;
    private int VideoWidth = 240;
    private int VideoHeight = 320;
    private boolean connectedServer = false;
    private Button Bgbt,Ctbt,commit,connect;
    private EditText iptext;

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public Camera getCameraInstance(Context context){
        Camera c = null;
        try{
            c = Camera.open(0);
            Camera.Parameters parameters = c.getParameters();
            parameters.setPreviewSize(VideoWidth,VideoHeight);
            parameters.setPreviewFpsRange(4, 10);
            parameters.setPictureFormat(ImageFormat.NV21);
            parameters.setPictureSize(VideoWidth,VideoHeight);
            if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
            {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            c.setParameters(parameters);
            int[] textures = new int[1];
            GLES20.glGenTextures(1, textures, 0);
            SurfaceTexture surfaceTexture = new SurfaceTexture(textures[0]);
            try
            {
                c.setPreviewTexture(surfaceTexture);
                c.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {
                        if (connectedServer) {
                            if (bytes != null) {
                                if(Picturelist.size()>maxsize)
                                    {
                                        Picturelist.remove();
                                    }
                                    Picturelist.add(bytes);
                                }
                            }
                        }
                });
                c.startPreview();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }catch(Exception e){
            Toast.makeText(context,"Failed to open Camera",Toast.LENGTH_SHORT).show();
        }
        return c;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        //WiFi part in onCreate
        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager, mChannel,mPeerListListener,getApplicationContext());
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        //Camera part in onCreate
        setContentView(R.layout.activity_my_camera);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //check camera
        if(!checkCameraHardware(getApplicationContext()))
            Toast.makeText(getApplicationContext(),"Please make sure you have cameras",Toast.LENGTH_SHORT).show();

        // Create an instance of Camera
        mCamera = getCameraInstance(getApplicationContext());
        mCamera.setDisplayOrientation(90);
        mPreview = new CameraPreview(getApplicationContext(),mCamera);

        // Create our Preview view and set it as the content of our activity.
        FrameLayout preview = (FrameLayout)findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        //Configure the Handler
        myHandler = new Handler(){
            @Override
            public void handleMessage(Message message){
                if(message.what == 0x100)
                {
                    String ms = (String) message.obj;
                    Toast.makeText(MyCamera.this,ms,Toast.LENGTH_SHORT).show();
                }
                if(message.what == 0x200)
                {
                    connectedServer = true;
                    Toast.makeText(MyCamera.this,"Begin transporting",Toast.LENGTH_SHORT).show();
                }
                if(message.what == 0x300)
                {
                    connectedServer = false;
                    Toast.makeText(MyCamera.this,"End transporting",Toast.LENGTH_SHORT).show();
                }
            }
        };

    }

    @Override
    public void onStart()
    {
        super.onStart();
        connect = (Button)findViewById(R.id.discover);
        commit = (Button)findViewById(R.id.confirm);
        iptext = (EditText)findViewById(R.id.ip);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManager.discoverPeers(mChannel,new WifiP2pManager.ActionListener(){
                    @Override
                    public void onSuccess(){
                        Toast.makeText(getApplicationContext(),"Find successfully",Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(int reason){
                        Toast.makeText(getApplicationContext(),"Can't discover peers: error "+Integer.toString(reason),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                host = iptext.getText().toString();
                commit.setText("OK");
            }
        });
        Bgbt = (Button)findViewById(R.id.button_Begin);
        Ctbt = (Button)findViewById(R.id.button_Connect);
        Bgbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connectedServer = !connectedServer;
                if(connectedServer)
                    Bgbt.setText("OK");
                else
                    Bgbt.setText("Begin_2");
        }});

        Ctbt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mThread = new ConnectThread(host,8000,Picturelist, VideoWidth,VideoHeight,myHandler);
                mThread.start();
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unregisterReceiver(mReceiver);
        if(mThread != null){
            mThread.cancel();
        }
    }

    /** A basic Camera preview class */
    public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera mCamera;

        public CameraPreview(Context context, Camera camera) {
            super(context);
            mCamera = camera;

            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            // The Surface has been created, now tell the camera where to draw the preview.
            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            } catch (IOException e) {
                Log.d("MyCamera", "Error setting camera preview: " + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // empty. Take care of releasing the Camera preview in your activity.
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            // If your preview can change or rotate, take care of those events here.
            // Make sure to stop the preview before resizing or reformatting it.

            if (mHolder.getSurface() == null){
                // preview surface does not exist
                return;
            }

            // stop preview before making changes
            try {
                mCamera.stopPreview();
            } catch (Exception e){
                // ignore: tried to stop a non-existent preview
            }

            // set preview size and make any resize, rotate or
            // reformatting changes here

            // start preview with new settings
            try {
                mCamera.setPreviewDisplay(mHolder);
                mCamera.startPreview();

            } catch (Exception e){
                Log.d("MyCamera", "Error starting camera preview: " + e.getMessage());
            }
        }
    }
}