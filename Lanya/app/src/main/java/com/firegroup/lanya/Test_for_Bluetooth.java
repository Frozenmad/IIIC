package com.firegroup.lanya;

import android.os.Bundle;
import android.provider.ContactsContract;
import android.renderscript.Int2;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Froze on 2017/10/28.
 */

public class Test_for_Bluetooth extends AppCompatActivity {
    MyApplication myapp;

    public byte Int2Byte(Integer integer){
        return (byte)(integer & 0xff);
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testbluetooth);
        myapp = (MyApplication)getApplication();
        myapp.setActivity(this);
        final BluetoothConnectThread connectThread = myapp.getBluetoothThread();
        Button commit = (Button)findViewById(R.id.test_commit);
        final EditText mytext = (EditText)findViewById(R.id.test_text);
        commit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message = mytext.getText().toString();
                byte a = Int2Byte(Integer.valueOf(message));
                connectThread.write(a);
            }
        });
        final EditText updown = (EditText)findViewById(R.id.test_updown);
        final EditText leftright = (EditText)findViewById(R.id.test_leftright);
        Button commit2 = (Button)findViewById(R.id.test_commit2);
        commit2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int up = Integer.valueOf(updown.getText().toString());
                int left = Integer.valueOf(leftright.getText().toString());
                byte message = Int2Byte(up+left*7);
                connectThread.write(message);
            }
        });
    }
}
