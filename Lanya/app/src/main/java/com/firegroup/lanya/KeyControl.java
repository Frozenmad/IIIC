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
import android.widget.Toast;
import android.os.SystemClock;
import java.util.LinkedList;
import java.util.Queue;


class commandStruct{
    public static long startTime;
    long comm_time;
    int comm;
    public commandStruct(long t, int c){
        comm_time = t;
        comm = c;
    }
}


public class KeyControl extends Activity implements View.OnClickListener{

    boolean memPathFlag = false;
    boolean start = false;
    boolean begin = false;
    SurfaceView image;
    SurfaceHolder myHolder;
    MyApplication myApp;

    BluetoothConnectThread BluetoothThread;
    Queue<commandStruct> pathQue = new LinkedList<commandStruct>();


    public void startMemPath(){
        pathQue.clear();
        commandStruct.startTime = SystemClock.uptimeMillis();
        memPathFlag = true;
        Toast.makeText(getApplicationContext(),"start memorising path",Toast.LENGTH_SHORT).show();
    }

    public void stopMemPath(){
        memPathFlag = false;
        Toast.makeText(getApplicationContext(),"Path memorization stopped",Toast.LENGTH_SHORT).show();
    }

    public void replayPath(){
        memPathFlag = false;
        Queue<commandStruct> tmpQue = new LinkedList<commandStruct>();
        Toast.makeText(getApplicationContext(),"Replaying the path",Toast.LENGTH_SHORT).show();
        long replayStartTime = SystemClock.uptimeMillis();
        while(!pathQue.isEmpty()) {
            long curtime = SystemClock.uptimeMillis();
            commandStruct recentCom = pathQue.peek();
            // once the reaches the time limit
            if (curtime - replayStartTime >= recentCom.comm_time - commandStruct.startTime) {
                sendMessage(recentCom.comm);
                tmpQue.offer(pathQue.poll());
            }
        }
        Toast.makeText(getApplicationContext(),"Path replay finished",Toast.LENGTH_SHORT).show();
        pathQue = tmpQue;
    }

    public void sendMessage(int ms){
        if(myApp!=null){
            myApp.setActions(ms);
            myApp.sendMessage();
            if(memPathFlag)
                pathQue.offer(new commandStruct(SystemClock.uptimeMillis(),ms));
        }else{
            Toast.makeText(getApplicationContext(),"Please press the bluetooth button to connect first",Toast.LENGTH_SHORT).show();
        }
    }

    private static final String TAG = "CircleControl";
    private static final boolean D = true;
    Button mButtonUp;
    Button mButtonDown;
    Button mButtonLeft;
    Button mButtonRight;

    WiFiConnectThread myAcceptThread;

    @Override
    public void onCreate(final Bundle savedInstanceState) {

        WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
        localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.key_control);
        myApp = (MyApplication)getApplication();
        this.BluetoothThread = myApp.getBluetoothThread();
        myApp.setActivity(this);
        this.myAcceptThread = myApp.getMyAcceptThread();

        Button mWifi = findViewById(R.id.key_conn);
        mWifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start = false;
                begin = !begin;
                if(begin) {
                    myAcceptThread.begin();
                }else{
                    myAcceptThread.pause();
                }
            }
        });
//前进
        mButtonUp = findViewById(R.id.up);
        mButtonUp.setOnTouchListener(new Button.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
// TODO Auto-generated method stub
                int action = event.getAction();
                switch(action)
                {
                    case MotionEvent.ACTION_DOWN:
                        myApp.setUpdown(1);
                        myApp.sendMessage();
                        if(memPathFlag)
                            pathQue.offer(new commandStruct(SystemClock.uptimeMillis(),myApp.getActions()));
                        break;

                    case MotionEvent.ACTION_UP:
                        myApp.setUpdown(0);
                        myApp.sendMessage();
                        if(memPathFlag)
                            pathQue.offer(new commandStruct(SystemClock.uptimeMillis(),myApp.getActions()));
                        break;
                }
                return false;
            }
        });
//后退
        mButtonDown = findViewById(R.id.down);
        mButtonDown.setOnTouchListener(new Button.OnTouchListener(){


            @Override
            public boolean onTouch(View v, MotionEvent event) {
// TODO Auto-generated method stub
                int action = event.getAction();
                switch(action)
                {
                    case MotionEvent.ACTION_DOWN:
                        myApp.setUpdown(3);
                        myApp.sendMessage();
                        if(memPathFlag)
                            pathQue.offer(new commandStruct(SystemClock.uptimeMillis(),myApp.getActions()));
                        break;

                    case MotionEvent.ACTION_UP:
                        myApp.setUpdown(0);
                        myApp.sendMessage();
                        if(memPathFlag)
                            pathQue.offer(new commandStruct(SystemClock.uptimeMillis(),myApp.getActions()));
                        break;
                }
                return false;
            }


        });
//左转
        mButtonLeft = findViewById(R.id.left);
        mButtonLeft.setOnTouchListener(new Button.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
// TODO Auto-generated method stub
                int action = event.getAction();
                switch(action)
                {
                    case MotionEvent.ACTION_DOWN:
                        myApp.setLeftright(1);
                        myApp.sendMessage();
                        if(memPathFlag)
                            pathQue.offer(new commandStruct(SystemClock.uptimeMillis(),myApp.getActions()));
                        break;
                    case MotionEvent.ACTION_UP:
                        myApp.setLeftright(0);
                        myApp.sendMessage();
                        if(memPathFlag)
                            pathQue.offer(new commandStruct(SystemClock.uptimeMillis(),myApp.getActions()));
                        break;
                }
                return false;
            }
        });
//右转
        mButtonRight = findViewById(R.id.right);
        mButtonRight.setOnTouchListener(new Button.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
// TODO Auto-generated method stub
                int action = event.getAction();
                switch(action)
                {
                    case MotionEvent.ACTION_DOWN:
                        myApp.setLeftright(2);
                        myApp.sendMessage();
                        if(memPathFlag)
                            pathQue.offer(new commandStruct(SystemClock.uptimeMillis(),myApp.getActions()));
                        break;

                    case MotionEvent.ACTION_UP:
                        myApp.setLeftright(0);
                        myApp.sendMessage();
                        if(memPathFlag)
                            pathQue.offer(new commandStruct(SystemClock.uptimeMillis(),myApp.getActions()));
                        break;
                }
                return false;
            }
        });
        image = findViewById(R.id.key_image);
        myHolder = image.getHolder();
        myHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {}
            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {}
            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {}
        });
        myApp.setMyholder(myHolder);
        memPathFlag = false;
        setButtonListener();
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
        if(memPathFlag){
            stopMemPath();
        }
    }

    private void setButtonListener(){
        findViewById(R.id.record).setOnClickListener(this);
        findViewById(R.id.recoverPath).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId())
        {
            case R.id.record:
                memPathFlag = !memPathFlag;
                Button record = findViewById(R.id.record);
                if(memPathFlag){
                    record.setText("end");
                    startMemPath();
                }
                else{
                    record.setText("record");
                    stopMemPath();
                }
                break;

            case R.id.recoverPath:
                replayPath();
                break;
        }
    }


}