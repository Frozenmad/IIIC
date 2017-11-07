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
 * Created by Froze on 2017/10/15.
 */

public class WiFiConnectThread extends Thread {
    Handler OutHandler;
    DataOutputStream myoutput;
    DataInputStream myinput;
    boolean pause = false;
    WiFiConnectedThread connectedThread;

    void sendMessage(String message)
    {
        Message message1 = new Message();
        message1.what = 0x300;
        message1.obj = message;
        OutHandler.sendMessage(message1);
    }

    public WiFiConnectThread(Handler outHandler){
        OutHandler = outHandler;
    }
    @Override
    public void run() {
        ServerSocket serverSocket = null;
        Socket client = null;
        sendMessage("WiFi start successfully, waiting to be connected");
            try {
                serverSocket = new ServerSocket(8000);
                serverSocket.setReuseAddress(true);
                client = serverSocket.accept();
                sendMessage("WiFi connect successfully!");
                InputStream tmpinput = null;
                OutputStream tmpoutput = null;
                try{
                    tmpinput = client.getInputStream();
                    tmpoutput = client.getOutputStream();
                }catch (IOException e){
                    e.printStackTrace();
                }
                myinput = new DataInputStream(tmpinput);
                myoutput = new DataOutputStream(tmpoutput);
            }catch (IOException e){sendMessage("Failed to connect the WiFi device!");}
    }

    public void begin(){
        connectedThread = new WiFiConnectedThread(myinput,myoutput,OutHandler);
        connectedThread.start();
        send("Begin");
    }

    public void pause(){
        if(connectedThread == null)
            return;
        connectedThread.cancel();
        send("Stop");
    }

    public void send(String ms){
        try{
            byte[] mss = ms.getBytes();
            if(myoutput != null) {
                myoutput.flush();
                myoutput.write(mss);
            }else{
                Message mess = new Message();
                mess.what = 0x300;
                mess.obj = "No myoutput is found!";
                OutHandler.sendMessage(mess);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void setOutHandler(Handler newHandler){
        OutHandler = newHandler;
    }
}