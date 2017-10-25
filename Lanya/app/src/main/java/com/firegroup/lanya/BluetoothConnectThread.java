package com.firegroup.lanya;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Message;
import android.os.Handler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Froze on 2017/10/19.
 */

public class BluetoothConnectThread extends Thread {
    private BluetoothSocket mySocket;
    private BluetoothDevice myDevice;
    private BluetoothAdapter myAdapter;
    private OutputStream myout;
    private Handler outhandler;
    private static final UUID MY_UUID = UUID.fromString("4acb9f75-2858-4340-920c-397cabbb9a8a");
    public BluetoothConnectThread(BluetoothDevice myDevice, BluetoothAdapter myAdapter, Handler handler)
    {
        this.myDevice = myDevice;
        this.myAdapter = myAdapter;
        outhandler = handler;
        myout = null;
    }
    @Override
    public void run(){
        try {
            mySocket = myDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            myAdapter.cancelDiscovery();
            mySocket.connect();
            myout = mySocket.getOutputStream();
            Message ms = new Message();
            ms.what = 0x300;
            ms.obj = "Success in turning on Bluetooth!";
            outhandler.sendMessage(ms);
        }catch (IOException e)
        {
            Message ms = new Message();
            ms.what = 0x300;
            ms.obj = "Create the socket wrong!";
            outhandler.sendMessage(ms);
            e.printStackTrace();
            if(mySocket.isConnected())
            try{
                mySocket.close();
            }catch (IOException e1)
            {
                e1.printStackTrace();
            }
        }
    }

    public boolean write(String message)
    {
        byte[] data = message.getBytes();
        try {
            if (myout != null) {
                myout.write(data);
            }else{
                Message message1 = new Message();
                message1.what = 0x300;
                message1.obj = "No Bluetooth socket is found!";
                outhandler.sendMessage(message1);
                return false;
            }
        }catch (IOException e){
            e.printStackTrace();
            Message ms = new Message();
            ms.what = 0x300;
            ms.obj = "Write failed!";
            outhandler.sendMessage(ms);
            return false;
        }
        return true;
    }

    public void cancel(){
        try{
            if(myout!=null) {
                myout.flush();
                myout.close();
                mySocket.close();
            }else
            {
                Message ms = new Message();
                ms.what = 0x300;
                ms.obj = "No myout need to be closed";
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
