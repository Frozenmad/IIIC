package com.firegroup.lanya;

import android.content.Context;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Created by Froze on 2017/10/26.
 */

public class Gravity_Control extends AppCompatActivity {
    private SensorManager msensorManager;
    private BackLayer mContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ///////////////////////////////////////////////////
        // comment this to use dynamic content view
        //setContentView(R.layout.activity_grav_con);
        ////////////////////////////////////////////////////
        Log.e("GravCon", "build layout");

        msensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mContainer = new BackLayer(this);
        mContainer.setBackgroundResource(R.drawable.wood);
        setContentView(mContainer);
    }


    class smallBall extends View {
        private float mPosX = (float) Math.random();
        private float mPosY = (float) Math.random();
        private float mVelX;
        private float mVelY;

        public smallBall(Context context) {
            super(context);
        }

        public void computePhysics(float sx, float sy, float dT) {

            final float ax = -sx / 5;
            final float ay = -sy / 5;

            mPosX += mVelX * dT + ax * dT * dT / 2;
            mPosY += mVelY * dT + ay * dT * dT / 2;

            mVelX += ax * dT;
            mVelY += ay * dT;
        }
    }


    class BackLayer extends FrameLayout implements SensorEventListener {
        private static final float sBallDiameter = 0.01f;   //1 cm
        private static final float sBallDiameter2 = sBallDiameter*sBallDiameter;

        private Sensor msensor;

        private float mSensorX;
        private float mSensorY;
        private float mCenterX;
        private float mCenterY;
        private float mXDpi;
        private float mYDpi;
        private float mMetersToPixelsX;
        private float mMetersToPixelsY;
        private int mDstWidth;
        private int mDstHeight;

        private smallBall mBall;

        public BackLayer (Context context){
            super(context);
            msensor = msensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            msensorManager.registerListener(this,msensor,SensorManager.SENSOR_DELAY_GAME);

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            mXDpi = metrics.xdpi;
            mYDpi = metrics.ydpi;
            mMetersToPixelsX = mXDpi / 0.0254f;
            mMetersToPixelsY = mYDpi / 0.0254f;
            mDstWidth = (int) (sBallDiameter*mMetersToPixelsX+0.5f);
            mDstHeight = (int) (sBallDiameter*mMetersToPixelsY+0.5f);

            mBall = new smallBall(getContext());
            mBall.setBackgroundResource(R.drawable.ball);
            mBall.setLayerType(LAYER_TYPE_HARDWARE,null);
            addView(mBall, new ViewGroup.LayoutParams(mDstWidth, mDstHeight));
        }

        @Override
        public void onSensorChanged(SensorEvent event){
            if(event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
                return;

            mSensorX = event.values[0];
            mSensorY = event.values[1];
        }

        @Override
        protected void onDraw(Canvas canvas){
            // the maximum ax is 10 meter/s2
            final float ax = -mSensorX/300;
            final float ay = mSensorY/250;
            mContainer.mBall.setTranslationX(mCenterX+ax*mMetersToPixelsX);
            mContainer.mBall.setTranslationY(mCenterY+ay*mMetersToPixelsY);
            invalidate();
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            // compute the origin of the screen relative to the origin of
            // the bitmap
            mCenterX = (w - mDstWidth) * 0.5f;
            mCenterY= (h - mDstHeight) * 0.5f;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

    }
}
