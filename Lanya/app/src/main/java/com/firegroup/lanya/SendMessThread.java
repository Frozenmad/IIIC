package com.firegroup.lanya;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.atan;

/**
 * Created by Froze on 2017/12/14.
 */

public class SendMessThread extends Thread {
    private List<Point> mList;
    private int time;
    private double pre;
    private double pre2;
    private MyApplication myApp;
    private boolean running;
    SendMessThread(List<Point> l, int time, double pre, double pre2, MyApplication myApp){
        this.mList = new ArrayList<>();
        for(int i=0 ; i<time-1 ; i++ ) {
            int p_x1=l.get(i).x;
            int p_y1=l.get(i).y;
            int p_x2=l.get(i+1).x;
            int p_y2=l.get(i+1).y;
            int a=p_x2-p_x1 ;
            if(a==0)
                a=1 ;
            int b=p_y1-p_y2 ;
            if(b==0)
                b=1 ;
            Point point = new Point(a,b);
            this.mList.add(point) ;
        }
        this.time = time;
        this.pre = pre;
        this.pre2 = pre2;
        this.myApp = myApp;
    }

    private void TransferMessage(String mess){
        switch (mess){
            case "r":
                myApp.setUpdown(1);
                myApp.setLeftright(2);
                myApp.sendMessage();
                break;
            case "l":
                myApp.setUpdown(1);
                myApp.setLeftright(1);
                myApp.sendMessage();
                break;
            case "f":
                myApp.setUpdown(1);
                myApp.setLeftright(0);
                myApp.sendMessage();
                break;
            default:
                myApp.setActions(0);
                myApp.sendMessage();
        }
    }
    public void cancel(){
        running = false;
    }

    public boolean getST(){
        return running;
    }

    @Override
    public void run(){
        running = true;
        int endX ;
        int endY ;
        int startX ;
        int startY ;
        String order = "";
        double delay;
        double delay2;

        if (time < 1){
            return;
        }

        //第一个点
        endX = mList.get(0).x;
        endY = mList.get(0).y;
        if(atan((float)(endY)/(float)(endX))>0 )
            order ="r" ;
        else if( atan((float)(endY)/(float)(endX))<0)
            order ="l" ;
        else
            order ="f" ;

        delay = Math.abs(3.14/2-atan((float)(endY)/(float)(endX))) ;
        delay2 = Math.abs(Math.sqrt((float)(endY)*(float)(endY)+(float)(endX)*(float)(endX))) ;
        Log.v("delay",Double.toString(delay));
        Log.v("delay2",Double.toString(delay2));

        TransferMessage(order);

        if(order=="r" || order=="l"){
            try{
                Thread.sleep(Math.round(delay*pre2/0.775));
            }catch (InterruptedException e){}}
        else
        {
            try{
                Thread.sleep(Math.round(delay2*10));
            }catch (InterruptedException e){}
        }
        startX = endX;
        startY = endY;


        //后面的点
        for( int i=1 ; i<time-1 && running ; i++ ) {
            endX = mList.get(i).x;
            endY = mList.get(i).y;

            double angle1 =atan((float)(endY)/(float)(endX));
            double angle2 =atan((float)(startY)/(float)(startX));
            // 画线

            delay = Math.abs(angle1- angle2) ;
            delay2 = Math.abs(Math.sqrt((float)(endY)*(float)(endY)+(float)(endX)*(float)(endX))) ;

            Log.v("delay",Double.toString(delay));
            Log.v("delay2",Double.toString(delay2));

            if(  angle1-angle2>pre && angle1*angle2>0 )
                order="l" ;
            else if( angle1-angle2 <(-1)*pre && angle1*angle2 >0 )
                order="r" ;
            else if( endX>0 && endY>0 && startX>0 && startY<0 || endX<0 && endY>0 && startX>0 && startY>0 || endX<0 && endY<0 && startX<0 && startY>0 || endX>0 && endY<0 && startX<0 && startY<0)
                order="l" ;
            else if( endX>0 && endY<0 && startX>0 && startY>0 || endX<0 && endY<0 && startX>0 && startY<0 || endX<0 && endY>0 && startX<0 && startY<0 || endX>0 && endY>0 && startX<0 && startY>0)
                order="r" ;
            else
                order="f" ;

            // 将起点设置为下一个点
            startX = endX;
            startY = endY;
            TransferMessage(order);
            if(order=="r" || order=="l"){
                try{
                    Thread.sleep(Math.round(delay*pre2/0.775));
                }catch (InterruptedException e){}}
            else
            {
                try{
                    Thread.sleep(Math.round(delay2*10));
                }catch (InterruptedException e){}
            }
        }

        order ="s" ;
        TransferMessage(order);
    }
}
