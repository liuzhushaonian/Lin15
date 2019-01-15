package com.xp.legend.lin16.hooks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;


public class SlitImageView extends FrameLayout {

    private Bitmap bitmap;
    private int limitHeight;
    private boolean isScale=false;
    private Paint paint;
    private int alpha=255;


    public SlitImageView(Context context) {
        super(context);
        paint=new Paint();
    }

    public SlitImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        paint=new Paint();
    }

    public SlitImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint=new Paint();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmap==null) {
            super.onDraw(canvas);
        }else {

            canvas.clipRect(0,0,bitmap.getWidth(),limitHeight);

            paint.setAntiAlias(true);
            paint.setAlpha(alpha);

            canvas.drawBitmap(bitmap,0,0,paint);


        }
    }


    private Bitmap drawable2Bitmap(int id){

        if (id==-1){
            return null;
        }

        return BitmapFactory.decodeResource(getResources(),id);

    }



    public void change(int limit){

        this.limitHeight=limit;

        postInvalidate();//刷新
    }

    private void scaleBitmap(){

        if (isScale){
            return;
        }

        if (this.bitmap!=null){

            if (bitmap.getHeight()>getMeasuredHeight()||bitmap.getWidth()>getMeasuredWidth()){//图片本身高度大于view


                getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {



                        Matrix matrix=new Matrix();
                        double scaleX=1.0*getMeasuredWidth()/bitmap.getWidth();

                        double scaleY=1.0*getMeasuredHeight()/bitmap.getHeight();

                        matrix.preScale((float) scaleX,(float) scaleY);

                        bitmap=Bitmap.createBitmap(bitmap,0,0,getWidth(),getHeight(),matrix,true);

//                        Bitmap.createScaledBitmap(bitmap,)

                        if (limitHeight<=0){

                            limitHeight=getMeasuredHeight();
                        }

                        isScale=true;


                        getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    }
                });

            }
        }

    }


    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
//        scaleBitmap();
        postInvalidate();


    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }



}
