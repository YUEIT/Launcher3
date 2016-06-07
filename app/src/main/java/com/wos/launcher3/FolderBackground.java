package com.wos.launcher3;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class FolderBackground extends View{
    private Bitmap background = null;
    public FolderBackground(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }
    public FolderBackground(Context context , Bitmap background){
        super(context);
        this.background = background;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        canvas.drawColor(Color.argb(160, 0, 0, 0));
        Paint mPaint = new Paint();
        if(background!=null){
            canvas.drawBitmap(background, 0, 0, mPaint);
        }
    }
}
