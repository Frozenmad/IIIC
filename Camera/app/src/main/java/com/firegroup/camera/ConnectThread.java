package com.firegroup.camera;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedList;

/**
 * Created by Froze on 2017/10/15.
 */

public class ConnectThread extends Thread {
    private DataOutputStream myout;
    private DataInputStream myin;
    private int previewWidth;
    private int previewHeight;
    private Handler myHandler;
    String host = new String();
    int port;
    Socket mysocket = new Socket();
    private LinkedList<byte[]>linkedList;
    private ConnectedThread sendThread;
    boolean begin;

    public ConnectThread(String host, int port, LinkedList<byte[]>linkedList, int previewWidth, int previewHeight, Handler myHandler){
        this.host = host;
        this.port = port;
        this.linkedList = linkedList;
        this.previewWidth = previewWidth;
        this.previewHeight = previewHeight;
        this.myHandler = myHandler;
    }

    @Override
    public void run(){
        try {
            mysocket.bind(null);
            mysocket.connect((new InetSocketAddress(host, port)), 1000);
            myout = new DataOutputStream(mysocket.getOutputStream());
            myin = new DataInputStream(mysocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("ConnectedThread","myin or myout is wrong!");
        }
        Thread a = new Thread(){
            @Override
            public void run(){
                while(!Thread.currentThread().isInterrupted()){
                    byte[] buffer = new byte[1024];
                    try{
                        while(myin.read(buffer)>0){
                            String mss = new String(buffer);
                            if(mss.contains("Begin"))
                            {
                                Message ms = new Message();
                                ms.what = 0x200;
                                myHandler.sendMessage(ms);
                            }
                            else if(mss.contains("Stop")){
                                Message ms = new Message();
                                ms.what = 0x300;
                                myHandler.sendMessage(ms);
                            }
                            Message msss = new Message();
                            msss.what = 0x100;
                            msss.obj = mss;
                            myHandler.sendMessage(msss);
                        }
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        };
        a.start();
        sendPicture();
    }

    public void sendPicture(){
        begin = true;
        while(!Thread.currentThread().isInterrupted() && begin) {
            if (linkedList != null && (linkedList.size() > 1)) {
                byte[] data = linkedList.poll();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, previewWidth, previewHeight, null);
                yuvImage.compressToJpeg(new Rect(0, 0, previewWidth, previewHeight), 90, byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();
                try {
                    if(myout != null) {
                        myout.writeInt(bytes.length);
                        myout.write(bytes);
                    }else{Log.e("ConnectedThread","myout is empty!");}
                } catch (IOException e) {
                    break;
                }
            }
        }
    }

    public void cancel(){
        try {
            if(sendThread!=null){
                sendThread.cancel();
            }
            if(myin != null){
                myin.close();
            }
            if(myout!= null){
                myout.flush();
                myout.close();
            }
            if(mysocket!=null)
            mysocket.close();
        }catch (IOException e){}
    }
}
