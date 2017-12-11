package com.firegroup.kc_iiic_partb;

import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.bluetooth.BluetoothServerSocket;

import java.util.UUID;
import java.io.InputStream;
import android.content.Intent;
import java.io.OutputStream;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;

import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    public static int b = 0;
    public String message = "First_signal";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

            }
        });
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            int REQUEST_ENABLE_BT = 1;
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }



    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume(){
        super.onResume();
        new AcceptThread().start();
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;

        private AcceptThread() {
            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                UUID MY_UUID = UUID.fromString("4acb9f75-2858-4340-920c-397cabbb9a8a");
                String NAME = "tooth";
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {

            BluetoothSocket socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                EditText edit = (EditText)findViewById(R.id.text);
                try {

                    socket = mmServerSocket.accept();

                } catch (IOException e) {
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    new ConnectedThread(socket).start();
                    try {
                        mmServerSocket.close();
                    }catch (IOException e) {
                        break;
                    }
                }
            }
        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }

    //This is what you can do with the bluetooth
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                    message = new String(buffer);
                    final EditText editText = (EditText) findViewById(R.id.text);
                    editText.post(new Runnable() {
                        @Override
                        public void run() {
                            editText.setText(message);
                        }
                    });
                } catch (IOException e) {
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }

    private class UIshowThread extends Thread{
        public void run()
        {
            EditText editText = (EditText)findViewById(R.id.text);
            editText.setText(message);
            TextView textView = (TextView)findViewById(R.id.output);
            textView.setText("10101");
        }
    }
}
