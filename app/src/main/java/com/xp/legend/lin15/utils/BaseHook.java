package com.xp.legend.lin15.utils;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

public abstract class BaseHook {


    /**
     * 获取高斯模糊图片
     * @param context 上帝对象context
     * @param source 源Bitmap
     * @param radius 渲染半径（模糊程度）
     * @return 返回模糊后的Bitmap
     */
    protected Bitmap getBitmap(Context context, Bitmap source, int radius) {

        if (radius<=0){
            radius=1;
        }

        if (radius>25){
            radius=25;
        }

        Bitmap bitmap = source;
        RenderScript renderScript = RenderScript.create(context);

        final Allocation input = Allocation.createFromBitmap(renderScript, bitmap);

        final Allocation output = Allocation.createTyped(renderScript, input.getType());

        ScriptIntrinsicBlur scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));

        scriptIntrinsicBlur.setInput(input);

        scriptIntrinsicBlur.setRadius(radius);

        scriptIntrinsicBlur.forEach(output);

        output.copyTo(bitmap);

        renderScript.destroy();

        return bitmap;
    }

    protected Drawable bitmap2Drawable(Bitmap bitmap) {

        return new BitmapDrawable(AndroidAppHelper.currentApplication().getResources(), bitmap);

    }

//    protected boolean isVertical(){
////
////        return AndroidAppHelper
////                .currentApplication()
////                .getResources()
////                .getConfiguration()
////                .orientation== Configuration.ORIENTATION_PORTRAIT;
////    }


    protected boolean isVertical=true;

}
