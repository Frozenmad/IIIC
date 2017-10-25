package com.firegroup.lanya;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by Froze on 2017/10/19.
 */

public class FingerView extends View {
    public float bitmapX;
    public float bitmapY;
    public FingerView(Context context) {
        super(context);
        //set the cooradinate
        bitmapX = 300;
        bitmapY = 300;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        Bitmap bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.button2);
        Matrix matrix = new Matrix();
        float scale = 250f / (float)bitmap.getHeight();
        matrix.postScale(scale,scale);
        Bitmap mybit = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
        canvas.drawBitmap(mybit, bitmapX-125f, bitmapY-125f,paint);
        if(bitmap.isRecycled())
        {
            bitmap.recycle();
        }
    }
}
