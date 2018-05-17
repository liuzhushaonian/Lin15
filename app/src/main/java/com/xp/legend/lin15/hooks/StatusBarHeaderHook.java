package com.xp.legend.lin15.hooks;

import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import android.view.View;
import android.widget.Toast;

import com.xp.legend.lin15.bean.Rect;
import com.xp.legend.lin15.utils.ReceiverAction;

import java.io.FileNotFoundException;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class StatusBarHeaderHook implements IXposedHookLoadPackage {

    private static final String CLASS = "com.android.systemui.qs.QuickStatusBarHeader";
    private static final String METHOD = "onFinishInflate";
    private static View headerView;
    private StatusBarHeaderHook statusBarHeaderHook;
    private HookReceiver hookReceiver;
    private SharedPreferences sharedPreferences;
    private static final String header="header_view";
    private int alpha_value=0;
    private String ALPHA="alpha";
    private int drawable=0;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {


        if (!lpparam.packageName.equals("com.android.systemui")) {
            return;
        }

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {

                XposedBridge.log("lin15----->>in the header");

                sharedPreferences=AndroidAppHelper.currentApplication().getSharedPreferences(ReceiverAction.SS,Context.MODE_PRIVATE);
                headerView = (View) param.thisObject;

                String s=sharedPreferences.getString(header,"");

                int color=sharedPreferences.getInt("header_color",-1);

                alpha_value=sharedPreferences.getInt(ALPHA,255);

                drawable=AndroidAppHelper.currentApplication().getResources().getIdentifier("qs_background_primary","drawable",lpparam.packageName);


                if (!s.isEmpty()){

                    s="file:///"+s;

                    Uri uri= Uri.parse(s);

                    Bitmap bitmap= BitmapFactory.decodeStream(AndroidAppHelper.currentApplication().getContentResolver().openInputStream(uri));
                    headerView.setBackground(new BitmapDrawable(AndroidAppHelper.currentApplication().getResources(),bitmap));
                    headerView.getBackground().setAlpha(alpha_value);

                }else if (color!=-1){

                    headerView.setBackgroundColor(color);
                    headerView.getBackground().setAlpha(alpha_value);

                }

                registerBroadcast();

                XposedBridge.log("lin15----->>in the header end");

            }
        });


    }

    private void registerBroadcast() {

        if (hookReceiver!=null){//防止重复注册
            return;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ReceiverAction.HEADER_SEND_ALBUM);
        intentFilter.addAction(ReceiverAction.HEADER_SEND_COLOR);
        intentFilter.addAction(ReceiverAction.HEADER_GET_INFO);
        intentFilter.addAction(ReceiverAction.HEADER_SEND_EXPANDED);
        intentFilter.addAction(ReceiverAction.HEADER_SEND_FLOAT);
        intentFilter.addAction(ReceiverAction.HEADER_SEND_ALPHA);
        intentFilter.addAction(ReceiverAction.HEADER_DELETE_ALBUM);
        hookReceiver = new HookReceiver();
        AndroidAppHelper.currentApplication().registerReceiver(hookReceiver, intentFilter);

        XposedBridge.log("lin15----->>register the hookReceiver");


    }


    public class HookReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action == null) {
                return;
            }

            switch (action){

                case ReceiverAction.HEADER_SEND_ALBUM://接收URI并设置上去，最后保存在本地，方便重启后调用

                    setImageBackground(intent,context);

                    break;
                case ReceiverAction.HEADER_SEND_COLOR://接收颜色并设置上去，最后保存在本地，方便重启后调用

                    setColor(intent);

                    break;

                case ReceiverAction.HEADER_GET_INFO:

                    sendViewInfo(context);

                    break;
                case ReceiverAction.HEADER_SEND_EXPANDED:

                    break;
                case ReceiverAction.HEADER_SEND_FLOAT://根据下拉程度设置头部透明度

                    changeHeaderAlpha(intent);

                    break;
                case ReceiverAction.HEADER_SEND_ALPHA://传递透明度

                    getAlphaValue(intent);

                    break;

                case ReceiverAction.HEADER_DELETE_ALBUM:

                    deleteBg();
                    break;
            }
        }
    }

    /**
     * 设置背景图
     * @param intent
     */
    private void setImageBackground(Intent intent,Context context){

        String s=intent.getStringExtra("file");



        Uri uri= Uri.parse("file:///"+s);

        if (uri==null){
            return;
        }
        try {
            Bitmap bitmap= BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
            headerView.setBackground(new BitmapDrawable(context.getResources(),bitmap));
            headerView.getBackground().setAlpha(alpha_value);//设置上透明度
            sharedPreferences.edit().putString(header,s).apply();//保存
            Toast.makeText(AndroidAppHelper.currentApplication(), "设置成功", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    /**
     * 返回信息
     * @param context
     */
    private void sendViewInfo(Context context){

        int w=headerView.getWidth();
        int h=headerView.getHeight();

        Rect rect=new Rect();
        rect.setWidth(w);
        rect.setHeight(h);

        Intent intent1=new Intent(ReceiverAction.HEADER_SEND_INFO);
        intent1.putExtra("info",rect);

        context.sendBroadcast(intent1);


    }

    /**
     * 改变下拉透明度
     * @param intent
     */
    private void changeHeaderAlpha(Intent intent){

//        String s=sharedPreferences.getString(header,"");

        if (headerView==null||headerView.getBackground()==null){
            return;
        }

        float f=intent.getFloatExtra("float",-0.1f);

        if (f<0||f>1){
            return;
        }

        float alpha=(1-f)*alpha_value;

        if (alpha>alpha_value){
            alpha=alpha_value;
        }

        if (alpha<0){
            alpha=0;
        }

        headerView.getBackground().setAlpha((int) alpha);

        if (f==1){
            headerView.getBackground().setAlpha(0);
        }else if (f==0){

            headerView.getBackground().setAlpha(alpha_value);
        }
    }

    /**
     * 获取透明度并保存
     * @param intent
     */
    private void getAlphaValue(Intent intent){

        int value=intent.getIntExtra("alpha",-1);

        if (value<0){
            return;
        }

        alpha_value=value;//赋值

        sharedPreferences.edit().putInt(ALPHA,alpha_value).apply();//保存

        String s=sharedPreferences.getString(header,"");

        if (s.isEmpty()||headerView==null||headerView.getBackground()==null){
            return;
        }

        headerView.getBackground().setAlpha(alpha_value);//更新
    }

    /**
     * 移除背景
     */
    private void deleteBg(){

        String s=sharedPreferences.getString(header,"");

        int color=sharedPreferences.getInt("header_color",-1);

        if (color>0||!s.isEmpty()){

            if (getDefaultDrawable()!=null) {
                headerView.setBackground(null);
                headerView.setBackground(getDefaultDrawable());

                Toast.makeText(AndroidAppHelper.currentApplication(),"背景已清除",Toast.LENGTH_SHORT).show();
            }

        }

        sharedPreferences.edit().remove(header).apply();//删除保存的文件
        sharedPreferences.edit().remove("header_color").apply();



    }

    private Drawable getDefaultDrawable(){

        if (drawable==0){
            return null;
        }
        return AndroidAppHelper.currentApplication().getDrawable(drawable);

    }

    private void setColor(Intent intent){

        int c=intent.getIntExtra("color",-1);
        if (c==-1){
            return;
        }

        headerView.setBackgroundColor(c);
        headerView.getBackground().setAlpha(alpha_value);

        sharedPreferences.edit().putInt("header_color",c).apply();//保存颜色

        Toast.makeText(AndroidAppHelper.currentApplication(), "设置成功", Toast.LENGTH_SHORT).show();

    }
}
