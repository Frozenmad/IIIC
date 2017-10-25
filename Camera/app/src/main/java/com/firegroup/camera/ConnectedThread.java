package com.firegroup.camera;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Handler;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by Froze on 2017/10/24.
 */

public class ConnectedThread extends Thread {
    private LinkedList<byte[]> myList;
    private DataOutputStream myout;
    private DataInputStream myin;
    private int previewWidth;
    private int previewHeight;
    private Handler myHandler;
    private boolean begin;

    public ConnectedThread(LinkedList<byte[]>myList, DataOutputStream myout, DataInputStream myin, int previewHeight, int previewWidth, Handler myHandler){
        this.myList = myList;
        this.myin = myin;
        this.myout = myout;
        this.myHandler = myHandler;
        this.previewHeight = previewHeight;
        this.previewWidth = previewWidth;
        this.begin = true;
    }

    @Override
    public void run(){
        begin = true;
        while(!Thread.currentThread().isInterrupted() && begin) {
            if (myList != null && (myList.size() > 1)) {
                byte[] data = myList.poll();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, previewWidth, previewHeight, null);
                yuvImage.compressToJpeg(new Rect(0, 0, previewWidth, previewHeight), 90, byteArrayOutputStream);
                byte[] bytes = byteArrayOutputStream.toByteArray();
                try {
                    if(myout != null) {
                        myout.writeInt(bytes.length);
                        myout.write(bytes);
                    }else{
                        Log.e("ConnectedThread","myout is empty!");}
                } catch (IOException e) {
                    break;
                }
            }
        }
        try{
            myout.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void pause(){
        begin = false;
    }

    public void cancel(){
        begin = false;
        try {
            if(myin != null) {
                myin.close();
            }
            if(myout != null)
            {
                myout.flush();
                myout.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
