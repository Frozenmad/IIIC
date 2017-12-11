package com.firegroup.lanya;

import android.os.Handler;
import android.os.Message;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Used to manage the connection to wifi and write something to another device
 */

public class WiFiConnectThread extends Thread {
    private Handler OutHandler;
    private DataOutputStream myOutput;
    private DataInputStream myInput;
    private WiFiConnectedThread connectedThread;

    private void sendMessage(String message)
    {
        Message message1 = new Message();
        message1.what = 0x300;
        message1.obj = message;
        OutHandler.sendMessage(message1);
    }

    WiFiConnectThread(Handler outHandler){
        OutHandler = outHandler;
    }
    @Override
    public void run() {
        ServerSocket serverSocket;
        Socket client;
        sendMessage("WiFi start successfully, waiting to be connected");
            try {
                serverSocket = new ServerSocket(8000);
                serverSocket.setReuseAddress(true);
                client = serverSocket.accept();
                sendMessage("WiFi connect successfully!");
                InputStream tmpInput = null;
                OutputStream tmpOutput = null;
                try{
                    tmpInput = client.getInputStream();
                    tmpOutput = client.getOutputStream();
                }catch (IOException e){
                    e.printStackTrace();
                }
                if(tmpInput != null && tmpOutput != null) {
                    myInput = new DataInputStream(tmpInput);
                    myOutput = new DataOutputStream(tmpOutput);
                }else{
                    sendMessage("Failed to get input and output stream!");
                }
            }catch (IOException e){sendMessage("Failed to connect the WiFi device!");}
    }

    void begin(){
        connectedThread = new WiFiConnectedThread(myInput,OutHandler);
        connectedThread.start();
        send("Begin");
    }

    void pause(){
        if(connectedThread == null)
            return;
        connectedThread.cancel();
        send("Stop");
    }

    private void send(String ms){
        try{
            byte[] mss = ms.getBytes();
            if(myOutput != null) {
                myOutput.flush();
                myOutput.write(mss);
            }else{
                Message mess = new Message();
                mess.what = 0x300;
                mess.obj = "No myOutput is found!";
                OutHandler.sendMessage(mess);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}