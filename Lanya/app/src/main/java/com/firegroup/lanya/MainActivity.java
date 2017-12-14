package com.firegroup.lanya;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUnderstander;
import com.iflytek.cloud.SpeechUtility;

/**
 * Created by Froze on 2017/10/20.
 */

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.firegroup.lanya";
    public String TAG = "MainActivity";

    MyApplication myApp;

    @Override
    public void onCreate(final Bundle savedInstanceState){
        Window window = this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams localLayoutParams = getWindow().getAttributes();
            localLayoutParams.flags = (WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | localLayoutParams.flags);
        }
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        //Xunfei init
        SpeechUtility.createUtility(this, SpeechConstant.APPID+"=59da4bdb");
        setContentView(R.layout.start_activity);
        myApp = (MyApplication) getApplication();
        myApp.setActivity(this);
        myApp.setMyholder(null);
    }

    public void startRoute(){
        Intent intent = new Intent(this,precise_route.class);
        startActivity(intent);
    }

    public void startCircle(){
        Intent intent = new Intent(this,CircleControl.class);
        startActivity(intent);
    }

    public void startKey(){
        Intent intent = new Intent(this,KeyControl.class);
        startActivity(intent);
    }

    public void startVoice(){
        Intent intent = new Intent(this,VoiceControl.class);
        startActivity(intent);
    }

    public void startGrav(){
        Intent intent = new Intent(this,Gravity_Control.class);
        startActivity(intent);
    }

    public void startGes(){
        Intent intent = new Intent(this, GestureControl.class);
        startActivity(intent);
    }

    public void startTest(){
        Intent intent = new Intent(this,Test_for_Bluetooth.class);
        startActivity(intent);
    }

    public void startOpencv(){
        Intent intent = new Intent(this,Test_for_OpenCV.class);
        startActivity(intent);
    }


    public void startWiFi(){
        myApp.startDiscover();
        myApp.startWIFI();
    }

    public void startBluetooth(){
        myApp.startBluetooth();
    }

    @Override
    public void onRestart(){
        super.onRestart();
        myApp.setActivity(this);
        myApp.setMyholder(null);
    }

    @Override
    public void onResume(){
        super.onResume();
        findViewById(R.id.circle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startCircle();
            }
        });
        findViewById(R.id.key).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startKey();
            }
        });
        findViewById(R.id.voice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startVoice();
            }
        });
        findViewById(R.id.gravity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGrav();
            }
        });
        findViewById(R.id.gesture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGes();
            }
        });
        findViewById(R.id.start_blue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBluetooth();
            }
        });
        findViewById(R.id.start_wifi).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startWiFi();
            }
        });
        findViewById(R.id.start_test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTest();
            }
        });
        findViewById(R.id.start_opencv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOpencv();
            }
        });
        findViewById(R.id.lines).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRoute();
            }
        });
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onStop(){
        super.onStop();
    }
}
