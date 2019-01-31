package com.xp.legend.lin16.utils;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.widget.Toast;

import de.robv.android.xposed.XposedBridge;


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


    SharedPreferences sharedPreferences;



    protected void openLogs(Intent intent){

        boolean log=intent.getBooleanExtra(Conf.LOG,false);

        //保存
        sharedPreferences.edit().putBoolean(Conf.LOG,log).apply();

        if (log) {

            Toast.makeText(AndroidAppHelper.currentApplication(), "日志已打开", Toast.LENGTH_SHORT).show();
        }else {

            Toast.makeText(AndroidAppHelper.currentApplication(), "日志已关闭", Toast.LENGTH_SHORT).show();
        }

    }

    protected boolean isVertical=true;

    /**
     * 记录日志
     * @param infos 日志内容
     */
    protected void logs(String infos){

        if (sharedPreferences==null) {
            sharedPreferences = AndroidAppHelper.currentApplication().getSharedPreferences(Conf.SHARE, Context.MODE_PRIVATE);
        }

        if (sharedPreferences.getBoolean(Conf.LOG,false)) {//判断是否开启了log

            XposedBridge.log("lin15---->>>"+infos);
        }

    }

    public BaseHook() {




    }

    protected boolean isP(){

        return Build.VERSION.SDK_INT == Build.VERSION_CODES.P;

    }
}
