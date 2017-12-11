package com.firegroup.lanya;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * WiFi connect thread, used to manage message after successfully connect the wifi
 */

public class WiFiConnectedThread extends Thread {
    private boolean Pause;
    private DataInputStream myInput;
    private Handler OutHandler;

    WiFiConnectedThread(DataInputStream input, Handler handler)
    {
        this.myInput = input;
        this.OutHandler = handler;
        Pause = false;
    }

    private void sendMessage(String message)
    {
        Message message1 = new Message();
        message1.what = 0x300;
        message1.obj = message;
        OutHandler.sendMessage(message1);
    }


    @Override
    public void run() {
        byte[] bytes = new byte[1];
        Bitmap bmp;
        Bitmap rotateBmp;
        Matrix matrix = new Matrix();
        matrix.setRotate(90);
        while (!Thread.currentThread().isInterrupted() && !Pause) {
            try {
                int size = myInput.readInt();
                if (size <= 0){
                    sendMessage("No device!");
                    continue;
                }
                if (size > bytes.length)
                    bytes = new byte[size];
                int len = 0;
                while (len < size) {
                    len += myInput.read(bytes, len, size - len);
                }
                bmp = BitmapFactory.decodeByteArray(bytes, 0, size);
                if (bmp != null) {
                    rotateBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
                    Message outMessage = new Message();
                    outMessage.what = 0x200;
                    outMessage.obj = rotateBmp;
                    OutHandler.sendMessage(outMessage);
                }
            } catch (IOException e) {
                sendMessage("Loop break");
                e.printStackTrace();
                break;
            }
        }
    }

    void cancel(){
        Pause = true;
    }
}
