package com.xp.legend.lin15.hooks;

import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Toast;

import com.xp.legend.lin15.bean.HeaderInfo;
import com.xp.legend.lin15.bean.Result;
import com.xp.legend.lin15.utils.BaseHook;
import com.xp.legend.lin15.utils.Conf;
import com.xp.legend.lin15.utils.ReceiverAction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class O_HeaderHook extends BaseHook implements IXposedHookLoadPackage {

    private static final String CLASS = "com.android.systemui.qs.QuickStatusBarHeader";
    private static final String METHOD = "onFinishInflate";
    private View header;
    private N_HeaderReceiver receiver;
    private SharedPreferences sharedPreferences;
    private boolean isGAO = false;

    private int quality;

    private int alpha_value = 255;

    private int drawable = 0;

    private int gaoValue = 25;

//    private MyOrientationEventChangeListener myOrientationEventChangeListener;

    private int rotation = -101;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!isO()){
            return;
        }

        if (!lpparam.packageName.equals("com.android.systemui")) {
            return;
        }

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                registerReceiver();//注册广播

                sharedPreferences = AndroidAppHelper.currentApplication().getSharedPreferences(Conf.SHARE, Context.MODE_PRIVATE);

                quality = sharedPreferences.getInt(Conf.N_HEADER_QUALITY, Conf.LOW_QUALITY);

                alpha_value = sharedPreferences.getInt(Conf.N_HEADER_ALPHA, 255);

                isGAO = sharedPreferences.getBoolean(Conf.N_HEADER_GAO, false);

                gaoValue = sharedPreferences.getInt(Conf.N_HEADER_GAO_VALUE, 25);

                drawable = AndroidAppHelper
                        .currentApplication()
                        .getResources()
                        .getIdentifier("qs_background_primary", "drawable", lpparam.packageName);

                header= (View) param.thisObject;

                autoSetBg();


//                myOrientationEventChangeListener = new MyOrientationEventChangeListener(AndroidAppHelper.currentApplication(),
//                        SensorManager.SENSOR_DELAY_NORMAL);
//
//                myOrientationEventChangeListener.enable();


            }
        });


    }

    /**
     * 注册广播接收器
     */
    private void registerReceiver() {

        if (this.receiver == null) {

            this.receiver = new N_HeaderReceiver();
            IntentFilter intentFilter = new IntentFilter();

            intentFilter.addAction(ReceiverAction.SET_N_HEADER_VERTICAL_IMAGE);//设置竖屏图

            intentFilter.addAction(ReceiverAction.SEND_O_FLOAT);//传递下拉高度

            intentFilter.addAction(ReceiverAction.SET_N_HEADER_ALPHA_VALUE);//设置透明度

            intentFilter.addAction(ReceiverAction.SET_N_HEADER_GAO_SI);//设置是否高斯模糊

            intentFilter.addAction(ReceiverAction.SET_N_HEADER_HORIZONTAL_IMAGE);//设置横屏图

            intentFilter.addAction(ReceiverAction.GET_N_HEADER_INFO);//获取宽高信息

            intentFilter.addAction(ReceiverAction.SET_N_HEADER_GAO_SI_VALUE);//设置高斯模糊值

            intentFilter.addAction(ReceiverAction.DELETE_N_HEADER_BG);//删除背景

            intentFilter.addAction(ReceiverAction.SET_HEADER_QUALITY);

            intentFilter.addAction(ReceiverAction.UI_GET_HEADER_INFO);

            intentFilter.addAction(ReceiverAction.SEND_ORI);

            AndroidAppHelper.currentApplication().registerReceiver(receiver, intentFilter);
        }

    }

    /**
     * 判断是否是O系列
     *
     * @return
     */
    private boolean isO() {

        return Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1;
    }


    private class N_HeaderReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            String action = intent.getAction();

            if (action == null) {
                return;
            }

            switch (action) {

                case ReceiverAction.SET_N_HEADER_VERTICAL_IMAGE://设置竖屏背景图

                    setNHeaderVerticalImage(intent, context);

                    break;

                case ReceiverAction.SEND_O_FLOAT://传递下拉程度

                    autoChangeAlpha(intent);

                    break;

                case ReceiverAction.SET_N_HEADER_ALPHA_VALUE://设置透明度

                    getAlphaValue(intent);

                    break;

                case ReceiverAction.SET_N_HEADER_GAO_SI://设置是否高斯模糊

                    getGaoSi(intent);

                    break;

                case ReceiverAction.SET_N_HEADER_HORIZONTAL_IMAGE://设置横屏背景图

                    setNHeaderHorizontalImage(intent, context);

                    break;

                case ReceiverAction.GET_N_HEADER_INFO://获取信息

                    sendInfo(intent);

                    break;

                case ReceiverAction.SET_N_HEADER_GAO_SI_VALUE://设置高斯模糊半径

                    getGaoSiValue(intent);

                    break;


                case ReceiverAction.DELETE_N_HEADER_BG://删除背景

                    deleteBg(intent,context);

                    break;


                case ReceiverAction.SET_HEADER_QUALITY:

                    setImageQuality(intent);

                    break;

                case ReceiverAction.UI_GET_HEADER_INFO:

                    sendAllInfo(intent,context);

                    break;
                case ReceiverAction.SEND_ORI://接收屏幕旋转信息

                    autoSetPosition(intent);

                    break;

            }

        }
    }




    /**
     * 获取并保存header的宽度信息
     */
    private void saveHeaderWidthInfo() {

//        sharedPreferences.edit().remove(Conf.N_HEADER_VERTICAL_WIDTH).apply();
//        sharedPreferences.edit().remove(Conf.N_HEADER_HORIZONTAL_WIDTH).apply();

        int n_header_vertical_width = sharedPreferences.getInt(Conf.N_HEADER_VERTICAL_WIDTH, -1);

        int n_header_horizontal_width = sharedPreferences.getInt(Conf.N_HEADER_HORIZONTAL_WIDTH, -1);

        //还没有保存信息且是垂直模式
        if (n_header_vertical_width <= 0 && isVertical) {

            n_header_vertical_width = header.getWidth();

            sharedPreferences.edit().putInt(Conf.N_HEADER_VERTICAL_WIDTH, n_header_vertical_width).apply();//保存

        }


        if ((n_header_horizontal_width <= 0 && !isVertical)) {

            n_header_horizontal_width = header.getWidth();

            sharedPreferences.edit().putInt(Conf.N_HEADER_HORIZONTAL_WIDTH, n_header_horizontal_width).apply();//保存

        }

    }

    /**
     * 保存高度信息
     */
    private void saveHeaderHeightInfo() {

//        sharedPreferences.edit().remove(Conf.N_HEADER_VERTICAL_HEIGHT).apply();
//
//        sharedPreferences.edit().remove(Conf.N_HEADER_HORIZONTAL_HEIGHT).apply();

        int n_header_vertical_height = sharedPreferences.getInt(Conf.N_HEADER_VERTICAL_HEIGHT, -1);


        if (n_header_vertical_height <= 0 && isVertical) {
            n_header_vertical_height = header.getHeight();

            sharedPreferences.edit().putInt(Conf.N_HEADER_VERTICAL_HEIGHT, n_header_vertical_height).apply();
        }

        int n_header_horizontal_height = sharedPreferences.getInt(Conf.N_HEADER_HORIZONTAL_HEIGHT, -1);


        if (n_header_horizontal_height <= 0 && !isVertical) {

            n_header_horizontal_height = header.getHeight();

            sharedPreferences.edit().putInt(Conf.N_HEADER_HORIZONTAL_HEIGHT, n_header_horizontal_height).apply();
        }


    }

    /**
     * 自动设置壁纸
     * 判断是否高斯模糊
     */
    private void autoSetBg() {


//        if (header==null){
//            return;
//        }

        if (isGAO) {
            setGaoSiImage();
        } else {
            setBg();
        }

    }

    /**
     * 设置高斯模糊
     */
    private void setGaoSiImage() {

        File file = null;

        if (header==null){
            return;
        }

        if (isVertical) {

            file = getNHeaderFile(Conf.VERTICAL);

        } else {
            file = getNHeaderFile(Conf.HORIZONTAL);
        }

        if (!file.exists()) {

            if (isFullExists(Conf.VERTICAL)) {//查看全局是否存在,存在则设置背景，不存在则

                if (sharedPreferences.getBoolean(Conf.SLIT,false)){

                    header.setBackground(null);
                    header.setBackgroundColor(Color.TRANSPARENT);

                }else {
                    header.setBackground(getDefaultDrawable());
                }

            } else {

                header.setBackground(null);
            }

//            if (isVertical) {
//
//                if (isFullExists(Conf.VERTICAL)) {//查看全局是否存在,存在则设置背景，不存在则
//
//                    header.setBackground(getDefaultDrawable());
//
//                } else {
//                    header.setBackground(null);
//                }
//
//            } else {
//
//                if (isFullExists(Conf.HORIZONTAL)) {
//                    header.setBackground(getDefaultDrawable());
//
//                } else {
//                    header.setBackground(null);
//                }
//
//            }

            return;
        }


        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        int value = sharedPreferences.getInt(Conf.N_HEADER_GAO_VALUE, 25);

        bitmap = getBitmap(AndroidAppHelper.currentApplication(), bitmap, value);

        header.setBackground(bitmap2Drawable(bitmap));


    }

    /**
     * 设置普通图片
     * 自动判断当前横竖屏
     */
    private void setBg() {

        if (header == null) {
            return;
        }

        File file = null;

        //自动判断是横屏或是竖屏
        if (isVertical) {

            file = getNHeaderFile(Conf.VERTICAL);


        } else {
            file = getNHeaderFile(Conf.HORIZONTAL);


        }

        if (!file.exists()) {//文件不存在

//

            if (isFullExists(Conf.VERTICAL)) {//查看全局是否存在,存在则设置背景，不存在则

                if (sharedPreferences.getBoolean(Conf.SLIT,false)){

                    header.setBackground(null);
                    header.setBackgroundColor(Color.TRANSPARENT);

                }else {
                    header.setBackground(getDefaultDrawable());
                }

            } else {

                header.setBackground(null);
            }



            return;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();

        switch (this.quality) {

            case Conf.HEIGHT_QUALITY:

                options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                break;

            case Conf.LOW_QUALITY:

                options.inPreferredConfig = Bitmap.Config.RGB_565;

                break;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

        header.setBackground(bitmap2Drawable(bitmap));

        header.getBackground().setAlpha(alpha_value);

    }


    /**
     * 判断全局是否设置过背景
     *
     * @return
     */
    private boolean isFullExists(int type) {

        return getFullFile(type).exists();
    }


    /**
     * 获取全局图片
     * @param type
     * @return
     */
    private File getFullFile(int type){

        String path = AndroidAppHelper.currentApplication().getFilesDir().getAbsolutePath();

        File file = null;

        switch (type) {
            case Conf.VERTICAL://竖屏图

                file = new File(path + "/" + Conf.N_FULL_VERTICAL_FILE);

                break;

            case Conf.HORIZONTAL://横屏图

                file = new File(path + "/" + Conf.N_FULL_HORIZONTAL_FILE);

                break;
        }

        return file;

    }

    /**
     * 获取默认背景
     *
     * @return
     */
    private Drawable getDefaultDrawable() {

        if (drawable == 0) {
            return null;
        }

        return AndroidAppHelper.currentApplication().getDrawable(drawable);

    }


    /**
     * 删除背景
     *
     * @param intent
     */
    private void deleteBg(Intent intent,Context context) {

        if (header == null || header.getBackground() == null) {

            return;
        }

        int type = intent.getIntExtra(Conf.N_HEADER_DELETE_TYPE, -1);

        if (type==-1){
            return;
        }

        if (deleteFile(type)) {

            Toast.makeText(context, "清除成功", Toast.LENGTH_SHORT).show();

            if (isVertical&&isFullExists(Conf.VERTICAL)){



                header.setBackgroundColor(Color.TRANSPARENT);




            }else if (!isVertical&&isFullExists(Conf.HORIZONTAL)){



                header.setBackgroundColor(Color.TRANSPARENT);



            }else {

                setBg();
            }
        }

    }



    private boolean deleteFile(int type) {

        File file = getNHeaderFile(type);
        return file.delete();
    }

    /**
     * 获取图片文件
     *
     * @param type 类型
     * @return 返回文件
     */
    private File getNHeaderFile(int type) {

        String path = AndroidAppHelper.currentApplication().getFilesDir().getAbsolutePath();


        File file = null;

        switch (type) {
            case Conf.VERTICAL://竖屏图

                file = new File(path + "/" + Conf.N_HEADER_VERTICAL_FILE);

                break;

            case Conf.HORIZONTAL://横屏图

                file = new File(path + "/" + Conf.N_HEADER_HORIZONTAL_FILE);

                break;
        }

        return file;
    }

    /**
     * 设置竖屏图片
     *
     * @param intent  intent对象
     * @param context 上帝对象context
     */
    private void setNHeaderVerticalImage(Intent intent, Context context) {

        String s = intent.getStringExtra(Conf.N_HEADER_VERTICAL_IMAGE);

        if (s == null || s.isEmpty()) {
            return;
        }

        Uri uri = Uri.parse(s);

        if (uri == null) {
            return;
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));

            if (bitmap != null) {

                int result = saveBitmap(bitmap, Conf.VERTICAL);

                if (result > 0) {

                    if (isVertical) {


                        if (isGAO) {

                            setGaoSiImage();
                        } else {
                            setBg();
                        }

                        Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT).show();

                    }

                } else {

                    Toast.makeText(context, "设置失败，保存失败", Toast.LENGTH_SHORT).show();

                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置横屏背景
     *
     * @param intent
     * @param context
     */
    private void setNHeaderHorizontalImage(Intent intent, Context context) {

        String s = intent.getStringExtra(Conf.N_HEADER_HORIZONTAL_IMAGE);

        if (s == null || s.isEmpty()) {

            return;
        }

        Uri uri = Uri.parse(s);

        if (uri == null) {
            return;
        }

        try {
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));

            if (bitmap != null) {

                int result = saveBitmap(bitmap, Conf.HORIZONTAL);

                if (result > 0) {

                    if (!isVertical) {//如果当前是横屏状态


                        if (isGAO) {

                            setGaoSiImage();
                        } else {
                            setBg();
                        }
                    }

                    Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(context, "设置失败，保存失败", Toast.LENGTH_SHORT).show();

                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * 保存背景图
     *
     * @param bitmap
     * @param type
     * @return
     */
    private int saveBitmap(Bitmap bitmap, int type) {

        String path = AndroidAppHelper.currentApplication().getFilesDir().getAbsolutePath();

        int result = -1;


        File file = null;


        switch (type) {
            case Conf.VERTICAL:

                file = new File(path + "/" + Conf.N_HEADER_VERTICAL_FILE);

                break;

            case Conf.HORIZONTAL:

                file = new File(path + "/" + Conf.N_HEADER_HORIZONTAL_FILE);

                break;
        }

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

    /**
     * 根据下拉程度改变透明度
     *
     * @param intent
     */
    private void autoChangeAlpha(Intent intent) {

        float f = intent.getFloatExtra(Conf.N_EXPAND_VALUE, -0.1f);

        if (header == null || header.getBackground() == null) {

            return;
        }



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

        header.getBackground().setAlpha((int) alpha);

        if (f == 1) {
            header.getBackground().setAlpha(0);
        } else if (f == 0) {

            header.getBackground().setAlpha(alpha_value);
        }


    }

    /**
     * 接收透明度
     *
     * @param intent
     */
    private void getAlphaValue(Intent intent) {

        int value = intent.getIntExtra(Conf.N_HEADER_ALPHA, -1);

        if (value==-1){
            return;
        }

        value = (int) (2.55 * value);

        if (value < 0) {
            value = 0;
        }

        if (value > 255) {
            value = 255;
        }

        alpha_value = value;

        sharedPreferences.edit().putInt(Conf.N_HEADER_ALPHA, alpha_value).apply();

        if (header.getBackground() == null) {
            return;
        }

        if (isVertical) {

            if (getNHeaderFile(Conf.VERTICAL).exists()) {

                header.getBackground().setAlpha(value);

            }

        } else {

            if (getNHeaderFile(Conf.HORIZONTAL).exists()) {

                header.getBackground().setAlpha(value);

            }

        }
    }

    private void getGaoSi(Intent intent) {

        int b = intent.getIntExtra(Conf.N_HEADER_GAO, -100);

        if (b == -100) {
            return;
        } else if (b > 0) {

            this.isGAO = true;
        } else if (b < 0) {

            this.isGAO = false;
        }


        if (isGAO) {
            setGaoSiImage();

        } else {
            setBg();
        }

        sharedPreferences.edit().putBoolean(Conf.N_HEADER_GAO,isGAO).apply();

    }

    /**
     * 接收高斯模糊半径
     *
     * @param intent
     */
    private void getGaoSiValue(Intent intent) {

        this.gaoValue = intent.getIntExtra(Conf.N_HEADER_GAO_VALUE, 25);

        sharedPreferences.edit().putInt(Conf.N_HEADER_GAO_VALUE, gaoValue).apply();

//        setGaoSiImage();

        if (isGAO){
            setGaoSiImage();
        }

//        autoSetBg();

    }


    /**
     * 发送header的数据给UI那边以便剪切图片
     *
     * @param intent
     */
    private void sendInfo(Intent intent) {

        int type = intent.getIntExtra(Conf.HEADER_INFO_TYPE, -1);

        if (type != -1) {

            HeaderInfo info = new HeaderInfo();
            Intent intent1 = new Intent(ReceiverAction.SEND_N_HEADER_INFO);

            switch (type) {

                case Conf.VERTICAL:

                    int width = header.getWidth();

                    int height = header.getHeight();

                    info.setHeight(height);

                    info.setWidth(width);

                    logs("发送头部竖屏 height--->>>"+height);
                    logs("发送头部竖屏 width--->>>"+width);

//                    intent1.putExtra(Conf.N_HEADER_RESULT,VERTICAL);

                    break;

                case Conf.HORIZONTAL:

                    int width1 = sharedPreferences.getInt(Conf.N_HEADER_HORIZONTAL_WIDTH, -1);

                    int height1 = sharedPreferences.getInt(Conf.N_HEADER_HORIZONTAL_HEIGHT, -1);

                    info.setHeight(height1);

                    info.setWidth(width1);

                    logs("发送头部横屏 height--->>>"+height1);
                    logs("发送头部横屏 width--->>>"+width1);

//                    intent1.putExtra(Conf.N_HEADER_RESULT,HORIZONTAL);

                    break;

            }

            intent1.putExtra(Conf.N_HEADER_RESULT, type);

            intent1.putExtra(Conf.N_HEADER_INFO_RESULT, info);//将info放到intent里面

            AndroidAppHelper.currentApplication().sendBroadcast(intent1);


        }

    }

    /**
     * 设置图片质量
     * @param intent
     */
    private void setImageQuality(Intent intent) {

        this.quality = intent.getIntExtra(Conf.N_HEADER_QUALITY, Conf.LOW_QUALITY);

        autoSetBg();

        sharedPreferences.edit().putInt(Conf.N_HEADER_QUALITY,this.quality).apply();

    }

    private void sendAllInfo(Intent intent,Context context){

        int sdk=intent.getIntExtra(Conf.SDK,-1);

        if (sdk<=0){
            return;
        }

        //取8.0或8.1
        if (sdk==Build.VERSION_CODES.O||sdk==Build.VERSION_CODES.O_MR1){

            int alpha=sharedPreferences.getInt(Conf.N_HEADER_ALPHA,255);

            int quality=sharedPreferences.getInt(Conf.N_HEADER_QUALITY,Conf.LOW_QUALITY);

            boolean gao=sharedPreferences.getBoolean(Conf.N_HEADER_GAO,false);

            int gaoValue=sharedPreferences.getInt(Conf.N_HEADER_GAO_VALUE,25);

            Result result=new Result();

            result.setAlpha(alpha);
            result.setGao(gao);
            result.setQuality(quality);
            result.setGaoValue(gaoValue);

            Intent intent1=new Intent(ReceiverAction.HEADER_TO_UI_INFO);

            intent1.putExtra(Conf.HEADER_TO_UI_RESULT,result);

            context.sendBroadcast(intent1);

        }


    }


    /**
     * 自动根据当前屏幕设置背景，代替之前的监听
     *
     * @param intent
     */
    private void autoSetPosition(Intent intent) {

        int p = intent.getIntExtra("ori", -1);

        if (p == -1) {
            return;
        }

        switch (p) {

            case Configuration.ORIENTATION_LANDSCAPE://横屏

                if (!isVertical){//避免重复
                    return;
                }
                
                isVertical=false;

                autoSetBg();

                break;

            case Configuration.ORIENTATION_PORTRAIT://竖屏
            default:

                if (isVertical){//避免重复
                    return;
                }


                isVertical=true;

                autoSetBg();

                break;

        }

        saveHeaderHeightInfo();

        saveHeaderWidthInfo();

    }


}
