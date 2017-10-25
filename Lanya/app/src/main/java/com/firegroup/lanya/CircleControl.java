package com.firegroup.lanya;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import java.text.DecimalFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

public class CircleControl extends Activity {

    boolean begin = false;
    SurfaceView image;
    SurfaceHolder myholder;
    MyApplication myapp = (MyApplication)getApplication();
    BluetoothConnectThread BluetoothThread;

    public void sendMessage(String ms){
        if(BluetoothThread!=null){
            BluetoothThread.write(ms);
        }else{
            Toast.makeText(getApplicationContext(),"Please press the bluetooth button to connect first",Toast.LENGTH_SHORT).show();
        }
    }

    private static final String TAG = "CircleControl";
    private static final boolean D = true;
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
        setContentView(R.layout.activity_main);
        myapp = (MyApplication)getApplication();
        this.BluetoothThread = myapp.getBluetoothThread();
        myapp.setActivity(this);
        this.myAcceptThread = myapp.getMyAcceptThread();
        FrameLayout frame = (FrameLayout)findViewById(R.id.direct);
        final FingerView fingerView = new FingerView(getApplicationContext());
        fingerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                int action = motionEvent.getAction();
                if(action == MotionEvent.ACTION_UP) {
                    fingerView.bitmapX = fingerView.bitmapY = 300;
                    sendMessage("x:000.00|y:000.00|");
                }
                else{
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
                    double r = Math.sqrt((x-300)*(x-300)+(y-300)*(y-300));
                    if(r>175){
                        x = (x-300) * 175 / (float) r + 300;
                        y = (y-300) * 175 / (float) r + 300;
                    }
                    DecimalFormat decimalFormat = new DecimalFormat("000.00");
                    String sx = decimalFormat.format(x-300);
                    String sy = decimalFormat.format(y-300);
                    sendMessage("x:"+sx+"|y:"+sy+"|");
                    fingerView.bitmapX = x;
                    fingerView.bitmapY = y;
                    fingerView.invalidate();
                }
                return true;

            }
        });
        frame.addView(fingerView);
        Button mwifi = (Button)findViewById(R.id.cir_conn);
        mwifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                begin = !begin;
                if(begin){
                    myAcceptThread.begin();
                }
                else{
                    myAcceptThread.pause();
                }

            }
        });

        image = findViewById(R.id.cir_image);
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
        Button blue = (Button)findViewById(R.id.bluetooth);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(CircleControl.this);
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
        if (D)
            Log.e(TAG, "- ON PAUSE -");
        if(begin){
            begin = false;
            myAcceptThread.pause();
        }
    }
}