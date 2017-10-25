package com.firegroup.lanya;

import android.app.Activity;
import android.os.Build;
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

public class KeyControl extends Activity {

    boolean start = false;
    boolean begin = false;
    SurfaceView image;
    SurfaceHolder myholder;

    BluetoothConnectThread BluetoothThread;

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