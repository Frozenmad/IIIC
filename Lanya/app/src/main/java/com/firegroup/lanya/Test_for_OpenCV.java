package com.firegroup.lanya;

import android.app.Application;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;

import com.firegroup.lanya.MyApplication;
import com.firegroup.lanya.R;
import com.firegroup.lanya.WiFiConnectThread;

import java.util.ArrayList;

/**
 * Created by Froze on 2017/11/4.
 */

public class Test_for_OpenCV extends AppCompatActivity {
    MyApplication MyApp;
    WiFiConnectThread MyAcceptThread;
    SurfaceView img_src, img_res;
    boolean start = false;
    SurfaceHolder src_Holder, res_Holder;
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.opencv_test);
        MyApp = (MyApplication)getApplication();
        MyApp.setActivity(this);
        MyAcceptThread = MyApp.getMyAcceptThread();

    }

    @Override
    public void onStart(){
        super.onStart();
        Button commit = (Button) findViewById(R.id.cv_commit);
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.setDeal(true);
            }
        });
        Button stop = (Button)findViewById(R.id.cv_stop);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyApp.setDeal(false);
            }
        });

        Button begin = (Button)findViewById(R.id.cv_begin);
        begin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start = !start;
                if(start) {
                    MyAcceptThread.begin();
                }
                else{
                    MyAcceptThread.pause();
                }
            }
        });
        img_src = (SurfaceView)findViewById(R.id.cv_src);
        src_Holder = img_src.getHolder();
        img_res = (SurfaceView)findViewById(R.id.cv_res);
        res_Holder = img_res.getHolder();
        MyApp.setMyholder(src_Holder);
        MyApp.setDeal_holder(res_Holder);
        SeekBar[] mylist = new SeekBar[6];
        int[] idlist = {R.id.cv_H_Low,R.id.cv_S_Low,R.id.cv_V_Low,R.id.cv_H_High,R.id.cv_S_High,R.id.cv_V_High};
        for(int iter = 0; iter < 6 ; iter++){
            final int index = iter;
            mylist[iter] = (SeekBar)findViewById(idlist[iter]);
            mylist[iter].setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    if(index < 3){
                        MyApp.setLow(i,index);
                    }
                    else{
                        MyApp.setHigh(i,index-3);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        MyApp.setDeal(false);
        if(start){
            start = false;
            MyAcceptThread.pause();
        }
    }
}
