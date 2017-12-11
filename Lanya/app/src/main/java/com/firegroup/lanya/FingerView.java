package com.firegroup.lanya;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;

/**
 * FingerView for shown of green icon
 */

public class FingerView extends View {
    public float bitmapX;
    public float bitmapY;
    Paint paint;
    Bitmap bitmap, myBit;
    Matrix matrix;
    public FingerView(Context context) {
        super(context);
        //set the x and y of FingerView
        bitmapX = 300;
        bitmapY = 300;
        paint = new Paint();
        bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.button2);
        matrix = new Matrix();
        float scale = 250f / (float)bitmap.getHeight();
        matrix.postScale(scale,scale);
        myBit = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(myBit, bitmapX-125f, bitmapY-125f,paint);
        if(bitmap.isRecycled())
        {
            bitmap.recycle();
        }
    }

    @Override
    public boolean performClick(){
        super.performClick();
        return true;
    }
}
