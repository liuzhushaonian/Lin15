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

import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.xp.legend.lin15.bean.Rect;
import com.xp.legend.lin15.utils.Conf;
import com.xp.legend.lin15.utils.ReceiverAction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class StatusBarHeaderHook implements IXposedHookLoadPackage {

    private static final String CLASS = "com.android.systemui.qs.QuickStatusBarHeader";
    private static final String METHOD = "onFinishInflate";
    private View headerView;
    private HookReceiver hookReceiver;
    private SharedPreferences sharedPreferences;
    private int alpha_value = 0;
    private int drawable = 0;
    private static final int BEST = 0x0010;
//    private static final int STANDARD = 0x0020;
    private static final int LOWEST = 0x0030;
    private boolean isGaoSi = false;
    private int quality = LOWEST;
    private int gaoValue = 25;




    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {


        if (!lpparam.packageName.equals("com.android.systemui")) {
            return;
        }

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {


                registerBroadcast();//注册广播

                sharedPreferences = AndroidAppHelper.currentApplication().getSharedPreferences(ReceiverAction.SS, Context.MODE_PRIVATE);
                headerView = (View) param.thisObject;

                int color = sharedPreferences.getInt(Conf.HEADER_COLOR, -1);

                alpha_value = sharedPreferences.getInt(Conf.ALPHA, 255);

                drawable = AndroidAppHelper.currentApplication().getResources().getIdentifier("qs_background_primary", "drawable", lpparam.packageName);

                quality = sharedPreferences.getInt(Conf.QUALITY, LOWEST);//初始化

                isGaoSi = sharedPreferences.getBoolean(Conf.GAO_SI, false);

                gaoValue=sharedPreferences.getInt(Conf.GAO_SI_VALUE,25);


                if (getFile().exists()) {

                    if (isGaoSi){
                        setGaoSiImage();
                    }else {
                        setBg();
                    }

                } else if (color != -1) {

                    headerView.setBackgroundColor(color);
                    headerView.getBackground().setAlpha(alpha_value);

                }else if (!isFullExists()){//没背景但是全局有背景

                    setBg();

                }


            }
        });

    }

    private void registerBroadcast() {

        if (hookReceiver == null) {//防止重复注册


            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ReceiverAction.HEADER_SEND_ALBUM);
            intentFilter.addAction(ReceiverAction.HEADER_SEND_COLOR);
            intentFilter.addAction(ReceiverAction.HEADER_GET_INFO);
            intentFilter.addAction(ReceiverAction.HEADER_SEND_EXPANDED);
            intentFilter.addAction(ReceiverAction.HEADER_SEND_FLOAT);
            intentFilter.addAction(ReceiverAction.HEADER_SEND_ALPHA);
            intentFilter.addAction(ReceiverAction.HEADER_DELETE_ALBUM);
            intentFilter.addAction(ReceiverAction.SEND_QUALITY);
            intentFilter.addAction(ReceiverAction.SEND_GAO_SI);
            intentFilter.addAction(ReceiverAction.SEND_GAO_VALUE);
            intentFilter.addAction(ReceiverAction.SEND_SET_FULL);
            hookReceiver = new HookReceiver();
            AndroidAppHelper.currentApplication().registerReceiver(hookReceiver, intentFilter);

            XposedBridge.log("register---->>header");
        }

    }


    public class HookReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (action == null) {
                return;
            }

            if (headerView == null) {//避免空指针导致的不断闪退
                return;
            }

            switch (action) {

                case ReceiverAction.HEADER_SEND_ALBUM://接收URI并设置上去，最后保存在本地，方便重启后调用

                    setImageBackground(intent, context);

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


                case ReceiverAction.SEND_QUALITY://接收质量

                    setQuality(intent);

                    break;
                case ReceiverAction.SEND_GAO_SI://设置是否高斯模糊

                    setGaoSi(intent);

                    break;

                case ReceiverAction.SEND_GAO_VALUE://设置高斯模糊半径
                    setGaoSiValue(intent);

                    break;

                case ReceiverAction.SEND_SET_FULL://设置全部后

                    setFullBg(intent);

                    break;
            }
        }
    }

    /**
     * 设置背景图
     *
     * @param intent
     */
    private void setImageBackground(Intent intent, Context context) {

        String s = intent.getStringExtra(Conf.HEADER_FILE);

        if (s == null) {
            return;
        }



        Uri uri = Uri.parse(s);

        if (uri == null) {
            return;
        }
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));

            if (bitmap != null) {
                int r = saveBitmap(bitmap);

                if (r > 0) {//保存成功

                    if (isGaoSi) {

                        setGaoSiImage();
                    } else {
                        setBg();
                    }


                    Toast.makeText(AndroidAppHelper.currentApplication(), "设置成功", Toast.LENGTH_SHORT).show();
                } else {

                    Toast.makeText(context, "设置失败，保存失败", Toast.LENGTH_SHORT).show();
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    /**
     * 返回信息
     *
     * @param context
     */
    private void sendViewInfo(Context context) {

        int w = headerView.getWidth();
        int h = headerView.getHeight();

        Rect rect = new Rect();
        rect.setWidth(w);
        rect.setHeight(h);

        Intent intent1 = new Intent(ReceiverAction.HEADER_SEND_INFO);
        intent1.putExtra(Conf.HEADER_INFO, rect);

        context.sendBroadcast(intent1);


    }

    /**
     * 改变下拉透明度
     *
     * @param intent
     */
    private void changeHeaderAlpha(Intent intent) {

//        String s=sharedPreferences.getString(header,"");
//
//        String fu=sharedPreferences.getString("full_view","");
//
//        int color=sharedPreferences.getInt("header_color",-1);
//
//        /**
//         * 如果全部没有设置，则不需要改透明度
//         */
//        if (s.isEmpty()&&color==-1&&fu.isEmpty()){
//            return;
//        }


        if (headerView.getBackground() == null) {

//            XposedBridge.log("lllll----header---->>null");

            return;
        }

        float f = intent.getFloatExtra(Conf.ALPHA, -0.1f);

        if (f < 0 || f > 1) {
            return;
        }

        float alpha = (1 - f) * alpha_value;

        if (alpha > alpha_value) {
            alpha = alpha_value;
        }

        if (alpha < 0) {
            alpha = 0;
        }

        headerView.getBackground().setAlpha((int) alpha);

        if (f == 1) {
            headerView.getBackground().setAlpha(0);
        } else if (f == 0) {

            headerView.getBackground().setAlpha(alpha_value);
        }
    }

    /**
     * 获取透明度并保存
     *
     * @param intent
     */
    private void getAlphaValue(Intent intent) {

        int value = intent.getIntExtra(Conf.ALPHA, -1);

        if (value < 0) {
            return;
        }

        alpha_value = value;//赋值

        sharedPreferences.edit().putInt(Conf.ALPHA, alpha_value).apply();//保存

        int color=sharedPreferences.getInt(Conf.HEADER_COLOR,-1);

        //文件不存在或是背景不存在
        if (!getFile().exists()|| headerView == null || headerView.getBackground() == null||color==-1) {
            return;
        }

        headerView.getBackground().setAlpha(alpha_value);//更新
    }

    /**
     * 移除背景
     */
    private void deleteBg() {

//        String s = sharedPreferences.getString(header, "");
//
//        int color = sharedPreferences.getInt("header_color", -1);

        deleteFile();

        if (headerView.getBackground()==null){//如果已经为null，则返回，避免空指针异常
            return;
        }


        if (getDefaultDrawable() != null) {
//
//                File file=getFullView();
//
                if (isFullExists()){//全局文件存在

                    headerView.setBackground(getDefaultDrawable());

                }else {//不存在

                    headerView.setBackground(null);
                }

            Toast.makeText(AndroidAppHelper.currentApplication(), "背景已清除", Toast.LENGTH_SHORT).show();
        }


//        sharedPreferences.edit().remove(header).apply();//删除保存的文件
        sharedPreferences.edit().remove(Conf.HEADER_COLOR).apply();


    }

    private Drawable getDefaultDrawable() {

        if (drawable == 0) {
            return null;
        }
        return AndroidAppHelper.currentApplication().getDrawable(drawable);

    }

    private void setColor(Intent intent) {

        int c = intent.getIntExtra(Conf.HEADER_COLOR, -1);
        if (c == -1) {
            return;
        }

        headerView.setBackgroundColor(c);
        headerView.getBackground().setAlpha(alpha_value);

        sharedPreferences.edit().putInt(Conf.HEADER_COLOR, c).apply();//保存颜色

        Toast.makeText(AndroidAppHelper.currentApplication(), "设置成功", Toast.LENGTH_SHORT).show();

    }

    private int saveBitmap(Bitmap bitmap) {

        String path = AndroidAppHelper.currentApplication().getFilesDir().getAbsolutePath();

        int result = -1;

        File file = new File(path + "/" + Conf.HEADER_FILE);

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream);

            result = 1;
        } catch (FileNotFoundException e) {

            result = -1;
            e.printStackTrace();
        }

        return result;
    }

    private void setQuality(Intent intent) {

        this.quality = intent.getIntExtra(Conf.QUALITY, LOWEST);

        sharedPreferences.edit().putInt(Conf.QUALITY, quality).apply();

        setBg();

    }

    private void setBg() {

        if (this.headerView == null) {
            return;
        }


        File file = getFile();

        if (file.exists()) {//文件存在

            BitmapFactory.Options options = new BitmapFactory.Options();

            switch (this.quality) {
                case BEST:

                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                    break;
//                case STANDARD:
//
//                    options.inPreferredConfig = Bitmap.Config.RGBA_F16;
//
//                    break;
                case LOWEST:

                    options.inPreferredConfig = Bitmap.Config.RGB_565;

                    break;

                default:

                    options.inPreferredConfig = Bitmap.Config.RGBA_F16;

                    break;
            }

            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            XposedBridge.log("size--header--->>" + bitmap.getByteCount());

            headerView.setBackground(new BitmapDrawable(AndroidAppHelper.currentApplication().getResources(), bitmap));

            headerView.getBackground().setAlpha(alpha_value);

        } else if (isFullExists()) {//如果全部存在的话，那就设置背景

            headerView.setBackground(getDefaultDrawable());
            headerView.getBackground().setAlpha(255);//恢复背景透明度

            XposedBridge.log("header------->>>setBG");

        }

    }

    /**
     * 设置是否高斯模糊
     *
     * @param intent
     */
    private void setGaoSi(Intent intent) {

        this.isGaoSi = intent.getBooleanExtra(Conf.GAO_SI, false);

        sharedPreferences.edit().putBoolean(Conf.GAO_SI, isGaoSi).apply();

        if (isGaoSi) {

            setGaoSiImage();//设置高斯模糊图片
        } else {
            removeGaosi();
        }

    }

    /**
     * 接收高斯模糊半径
     *
     * @param intent
     */
    private void setGaoSiValue(Intent intent) {

        this.gaoValue = intent.getIntExtra(Conf.GAO_SI_VALUE, 25);

        sharedPreferences.edit().putInt(Conf.GAO_SI_VALUE, gaoValue).apply();

        setGaoSiImage();

    }

    /**
     * 设置高斯模糊图
     */
    private void setGaoSiImage() {

        File file = getFile();
        if (!file.exists() || headerView == null) {//如果文件不存在，则不继续
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        int value = sharedPreferences.getInt(Conf.GAO_SI_VALUE, 25);

        bitmap = getBitmap(AndroidAppHelper.currentApplication(), bitmap, value);

        headerView.setBackground(bitmap2Drawable(bitmap));
    }

    /**
     * 移除高斯模糊效果
     */
    private void removeGaosi() {

        setBg();
    }


    public static Bitmap getBitmap(Context context, Bitmap source, int radius) {

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

    private Drawable bitmap2Drawable(Bitmap bitmap) {

        return new BitmapDrawable(AndroidAppHelper.currentApplication().getResources(), bitmap);

    }

    private File getFile() {

        String path = AndroidAppHelper.currentApplication().getFilesDir().getAbsolutePath();

        return new File(path + "/" + Conf.HEADER_FILE);

    }

    /**
     * 删除文件
     *
     * @return
     */
    private boolean deleteFile() {

        String path = AndroidAppHelper.currentApplication().getFilesDir().getAbsolutePath();

        File file = new File(path + "/" + Conf.HEADER_FILE);

        return file.delete();
    }

    private File getFullFile() {

        String path = AndroidAppHelper.currentApplication().getFilesDir().getAbsolutePath();

        return new File(path + "/"+Conf.FULL_FILE);

    }

    /**
     * 判断全部是否设置过背景
     * @return
     */
    private boolean isFullExists(){

        int color=sharedPreferences.getInt(Conf.FULL_COLOR,-1);

        File file=getFullFile();

        return color!=-1||file.exists();//返回存在为true，不存在为false

    }

    /**
     * 通过接收全部传过来的消息判断是否该给自身添加背景
     * @param intent
     */
    private void setFullBg(Intent intent){
        boolean set=intent.getBooleanExtra(Conf.SET_FULL,false);

        if (set){//全部设置了背景

            if (!isBgExists()) {//但是头部没设置背景

                headerView.setBackground(getDefaultDrawable());
            }

        }else {//全部没设置背景

            if (!isBgExists()){//头部也没设置背景
                headerView.setBackground(null);//取消背景
            }

        }


    }

    /**
     * 本身是否存在背景
     * @return
     */
    private boolean isBgExists(){

        int color=sharedPreferences.getInt(Conf.HEADER_COLOR,-1);

        return color!=-1||getFile().exists();//存在则true，不存在则false

    }

}
