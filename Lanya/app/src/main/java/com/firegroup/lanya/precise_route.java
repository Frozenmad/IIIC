package com.firegroup.lanya;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Thread.currentThread;
import static java.lang.Thread.sleep;

public class precise_route extends AppCompatActivity {
    double pre =0.02 ;
    int pre2=300 ;
    //TextView tv=new TextView(this);
    private ImageView show_trail;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;
    private Point point=null ;
    List<Point> list = null ;
    List<Point> list2 = null ;
    SendMessThread messThread = null;
    int times =0;
    MyApplication MyApp;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_precise_route);

        list = new ArrayList<Point>();
        list2 = new ArrayList<Point>();

        show_trail = (ImageView) this.findViewById(R.id.image);
        // 创建一张空白图片
        baseBitmap = Bitmap.createBitmap(1000,1300, Bitmap.Config.ARGB_8888);
        // 创建一张画布
        canvas = new Canvas(baseBitmap);
        // 画布背景为灰色
        canvas.drawColor(Color.BLACK);
        // 创建画笔
        paint = new Paint();
        // 画笔颜色为红色
        paint.setColor(Color.WHITE);
        // 宽度5个像素
        paint.setStrokeWidth(10);
        // 先将灰色背景画上
        canvas.drawBitmap(baseBitmap, new Matrix(), paint);
        show_trail.setImageBitmap(baseBitmap);

        show_trail.setOnTouchListener(new View.OnTouchListener() {
            int startX;
            int startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                point = new Point((int) event.getX(), (int) event.getY());
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        clear();
                        if(!(messThread == null || messThread.getState().equals(Thread.State.TERMINATED))){
                            messThread.cancel();
                            while (!messThread.getState().equals(Thread.State.TERMINATED)) try {
                                Toast.makeText(getApplicationContext(),"Please wait",Toast.LENGTH_SHORT).show();
                                sleep(100);
                            } catch (Exception e) {
                            }
                        }
                        // 获取手按下时的坐标
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        list.add(point);
                        times=times+1 ;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 获取手移动后的坐标
                        int stopX = (int) event.getX();
                        int stopY = (int) event.getY();
                        // 在开始和结束坐标间画一条线
                        float a= (float) (startX);
                        float b= (float) (startY);
                        float c= (float) (stopX);
                        float d= (float) (stopY);
                        canvas.drawLine(a,b,c,d,paint);
                        // 实时更新开始坐标
                        startX = (int) event.getX();
                        startY = (int) event.getY();
                        list.add(point);
                        times=times+1 ;
                        show_trail.setImageBitmap(baseBitmap);
                        break;
                    case MotionEvent.ACTION_UP:
                        Toast.makeText(getApplicationContext(),"Total:\t"+String.valueOf(times),Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        MyApp = (MyApplication)getApplication();
        MyApp.setActivity(this);
    }

    public void clear (View view)
    {
        if(!(messThread == null || messThread.getState().equals(Thread.State.TERMINATED))){
            messThread.cancel();
            while (!messThread.getState().equals(Thread.State.TERMINATED)) try {
                sleep(100);
            } catch (Exception e) {
            }
        }
        baseBitmap = Bitmap.createBitmap(1000,1300, Bitmap.Config.ARGB_8888);
        // 创建一张画布
        canvas = new Canvas(baseBitmap);
        // 画布背景为灰色
        canvas.drawColor(Color.BLACK);
        // 创建画笔
        paint = new Paint();
        // 画笔颜色为红色
        paint.setColor(Color.WHITE);
        // 宽度5个像素
        paint.setStrokeWidth(10);
        // 先将灰色背景画上
        canvas.drawBitmap(baseBitmap, new Matrix(), paint);
        show_trail.setImageBitmap(baseBitmap);
        list = new ArrayList<Point>();
        list2 = new ArrayList<Point>();
        times=0 ;
    }

    public void clear(){
        baseBitmap = Bitmap.createBitmap(1000,1300, Bitmap.Config.ARGB_8888);
        // 创建一张画布
        canvas = new Canvas(baseBitmap);
        // 画布背景为灰色
        canvas.drawColor(Color.BLACK);
        // 创建画笔
        paint = new Paint();
        // 画笔颜色为红色
        paint.setColor(Color.WHITE);
        // 宽度5个像素
        paint.setStrokeWidth(10);
        // 先将灰色背景画上
        canvas.drawBitmap(baseBitmap, new Matrix(), paint);
        show_trail.setImageBitmap(baseBitmap);
        list = new ArrayList<Point>();
        list2 = new ArrayList<Point>();
        times=0 ;
    }

    public void drawImage (View view)
    {
        if(!(messThread == null || messThread.getState().equals(Thread.State.TERMINATED))){
            messThread.cancel();
            while (!messThread.getState().equals(Thread.State.TERMINATED)) try {
                sleep(100);
            } catch (Exception e) {
            }
        }
        messThread = new SendMessThread(list,times,pre,pre2,MyApp);
        messThread.start();
    }

}
