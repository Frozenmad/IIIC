package com.firegroup.lanya;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * Created by Froze on 2017/10/26.
 */

public class GestureControl extends AppCompatActivity {
    private GestureDetector mDetector;
    private final static int MIN_MOVE = 200;   //最小距离
    private MyGestureListener mgListener;
    private BluetoothConnectThread BluetoothThread;
    Button Connect;
    boolean begin = false;
    WiFiConnectThread myAcceptThread;
    MyApplication myApp;

    public static class Globals{
        public static int updown = 0;
        public static int leftright = 0;
        public static int getvalue(){
            return updown*3+leftright;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesture_control);
        mgListener = new MyGestureListener();
        mDetector = new GestureDetector(this, mgListener);
        myApp = (MyApplication)getApplication();
        myApp.setActivity(this);
        BluetoothThread = myApp.getBluetoothThread();
        myAcceptThread = myApp.getMyAcceptThread();

        Connect = (Button)findViewById(R.id.gesture_conn);
        Connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                begin = !begin;
                if(begin) {
                    myAcceptThread.begin();
                }else{
                    myAcceptThread.pause();
                }
            }
        });

        SurfaceView image = (SurfaceView)findViewById(R.id.gesture_image);
        SurfaceHolder mHolder = image.getHolder();
        myApp.setMyholder(mHolder);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }

    private class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float v, float v1) {
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();
            if(distanceX > 400) myApp.setLeftright(2);
            else if(distanceX < -400) myApp.setLeftright(1);
            else myApp.setLeftright(0);
            if(distanceY > 400) myApp.setUpdown(3);
            else if (distanceY < -400) myApp.setUpdown(1);
            else myApp.setUpdown(0);
            Toast.makeText(GestureControl.this,Integer.toString(Globals.getvalue()),Toast.LENGTH_SHORT).show();
            myApp.sendMessage();
            return true;
        }
    }
}
