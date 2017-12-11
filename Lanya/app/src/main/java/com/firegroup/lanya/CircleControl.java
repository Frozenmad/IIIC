package com.firegroup.lanya;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

public class CircleControl extends Activity {

    boolean begin = false;
    SurfaceView image;
    SurfaceHolder MyHolder;

    MyApplication myApp = (MyApplication)getApplication();
    BluetoothConnectThread BluetoothThread;

    private static final String TAG = "CircleControl";
    private static final boolean D = true;
    WiFiConnectThread myAcceptThread;

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
        localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myApp = (MyApplication)getApplication();
        this.BluetoothThread = myApp.getBluetoothThread();
        myApp.setActivity(this);
        this.myAcceptThread = myApp.getMyAcceptThread();
        FrameLayout frame = findViewById(R.id.direct);
        final FingerView fingerView = new FingerView(getApplicationContext());
        fingerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                fingerView.performClick();
                int action = motionEvent.getAction();
                if(action == MotionEvent.ACTION_UP) {
                    fingerView.bitmapX = fingerView.bitmapY = 300;
                    fingerView.invalidate();
                    myApp.setUpdown(0);
                    myApp.setLeftright(0);
                    myApp.sendMessage();
                }
                else{
                    float x = motionEvent.getX();
                    float y = motionEvent.getY();
                    x = x-300;
                    y = y-300;
                    double r = Math.sqrt(x*x+y*y);
                    if(r>175){
                        x = x * 175 / (float) r;
                        y = y * 175 / (float) r;
                    }

                    if(x > 100) myApp.setLeftright(2);
                    else if(x < -100) myApp.setLeftright(1);
                    else myApp.setLeftright(0);

                    if(y > 200) myApp.setUpdown(2);
                    else if(y > 50) myApp.setUpdown(1);
                    else if(y < -200) myApp.setUpdown(4);
                    else if(y < -50) myApp.setUpdown(3);
                    else myApp.setUpdown(0);

                    myApp.sendMessage();

                    fingerView.bitmapX = x;
                    fingerView.bitmapY = y;
                    fingerView.invalidate();
                }
                return true;

            }
        });
        frame.addView(fingerView);
        Button myWifi = findViewById(R.id.cir_conn);
        myWifi.setOnClickListener(new View.OnClickListener() {
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
        MyHolder = image.getHolder();
        MyHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {}
            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {}
            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}
        });
        myApp.setMyholder(MyHolder);

        if(D) Log.e(TAG,"++ On Create ++");
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