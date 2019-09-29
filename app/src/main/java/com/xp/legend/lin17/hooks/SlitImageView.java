package com.xp.legend.lin17.hooks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import android.util.AttributeSet;


public class SlitImageView extends AppCompatImageView {

    private Bitmap bitmap;
    private int limitHeight;
    private boolean isScale = false;
    private Paint paint;
    private int alpha = 255;
    private int offset_height;
    private PorterDuffXfermode xfermode;

    private int radius;

    int width;

    public SlitImageView(Context context) {
        super(context);
        init();
    }

    public SlitImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SlitImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        width = getWidth();
        limitHeight = getHeight();//初始化
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getDrawable() == null) {
            super.onDraw(canvas);
        } else if (radius > 0) {


            int sc = canvas.saveLayer(0, 0, width, limitHeight, null);
            //画源图像，为一个圆角矩形
            canvas.drawRoundRect(new RectF(0, 0, width, limitHeight), radius, radius, paint);
            //设置混合模式
            paint.setXfermode(xfermode);
            //画目标图像
            canvas.drawBitmap(drawableToBitamp(getDrawable()), 0, 0, paint);
            // 还原混合模式
            paint.setXfermode(null);
            canvas.restoreToCount(sc);

            //-------


        } else {

            canvas.clipRect(0, 0, bitmap.getWidth(), limitHeight);

            paint.setAntiAlias(true);
            paint.setAlpha(alpha);

            canvas.drawBitmap(bitmap, 0, 0, paint);

        }
    }


    public void change(int limit) {

        this.limitHeight = limit;

//        Log.d("limit--xposed-->>>",limit+"");

        postInvalidate();//刷新
    }


    //需要圆角
    public void setBitmap(Bitmap bitmap, int radius) {
        this.bitmap = bitmap;
        this.radius = radius;

        setImageBitmap(bitmap);


//        this.bitmap=roundBottomBitmapByShader(bitmap,bitmap.getWidth(),bitmap.getHeight(),radius);


//        scaleBitmap();
        postInvalidate();


    }

    //    //不需要圆角
    public void setBitmap(Bitmap bitmap) {

        this.bitmap = bitmap;

        postInvalidate();

    }

    private void init() {

        paint = new Paint();
        paint.setAntiAlias(true);
        xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);

    }

    private Bitmap drawableToBitamp(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) drawable;
            return bd.getBitmap();
        }
        // 当设置不为图片，为颜色时，获取的drawable宽高会有问题，所有当为颜色时候获取控件的宽高
        int w = this.bitmap.getWidth();
        int h = this.bitmap.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }


    public void setRadius(int radius) {
        this.radius = radius;
        postInvalidate();
    }
}
