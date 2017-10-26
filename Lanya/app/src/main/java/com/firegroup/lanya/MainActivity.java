package com.firegroup.lanya;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

    public void startCircle(View view){
        Intent intent = new Intent(this,CircleControl.class);
        startActivity(intent);
    }

    public void startKey(View view){
        Intent intent = new Intent(this,KeyControl.class);
        startActivity(intent);
    }

    public void startVoice(View view){
        Intent intent = new Intent(this,VoiceControl.class);
        startActivity(intent);
    }

    public void startWiFi(View view){
        myApp.startDiscover();
        myApp.startWIFI();
    }

    public void startBluetooth(View view){
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
