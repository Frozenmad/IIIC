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

/**
 * Created by Froze on 2017/10/20.
 */

public class MainActivity extends AppCompatActivity {

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
        setContentView(R.layout.start_activity);
        myApp = (MyApplication) getApplication();
        myApp.setActivity(this);
        myApp.setMyholder(null);
        Button circle = (Button)findViewById(R.id.circle);
        circle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,CircleControl.class));
            }
        });
        Button key = (Button)findViewById(R.id.key);
        key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,KeyControl.class));
            }
        });
        Button lanya = (Button)findViewById(R.id.start_blue);
        lanya.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myApp.startBluetooth();
            }
        });
        Button Wifi = (Button)findViewById(R.id.start_wifi);
        Wifi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myApp.startDiscover();
                myApp.startWIFI();
            }
        });
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
