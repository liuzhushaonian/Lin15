package com.xp.legend.lin15.hooks;

import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.Toast;


import com.xp.legend.lin15.bean.Full;
import com.xp.legend.lin15.bean.Result;
import com.xp.legend.lin15.utils.BaseHook;
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

public class N_FullHook extends BaseHook implements IXposedHookLoadPackage {

    private static final String CLASS = "com.android.systemui.qs.QSContainer";

    private static final String METHOD = "onFinishInflate";


    private View full;

    private int alphaValue = 255;

    private boolean isGao = false;

    private int gaoValue = 25;

    private int drawable = 0;

    private SharedPreferences sharedPreferences;

    private int rotation = -101;

    private FullReceiver receiver;

    private int quality = Conf.LOW_QUALITY;

    private MyOrientationEventChangeListener myOrientationEventChangeListener;

    private View header;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!isN()){
            return;
        }

        if (!lpparam.packageName.equals("com.android.systemui")) {
            return;
        }

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                register();//注册广播

                sharedPreferences = AndroidAppHelper.currentApplication().getSharedPreferences(Conf.SHARE, Context.MODE_PRIVATE);

                quality = sharedPreferences.getInt(Conf.FULL_QUALITY, Conf.LOW_QUALITY);

                alphaValue = sharedPreferences.getInt(Conf.FULL_ALPHA_VALUE, 255);

                isGao = sharedPreferences.getBoolean(Conf.FULL_GAO, false);

                gaoValue = sharedPreferences.getInt(Conf.FULL_GAO_VALUE, 25);


                drawable = AndroidAppHelper
                        .currentApplication()
                        .getResources()
                        .getIdentifier("qs_background_primary", "drawable", lpparam.packageName);


                full = (View) param.thisObject;

                header = (View) XposedHelpers.getObjectField(param.thisObject, "mHeader");


                autoSetBg();

                myOrientationEventChangeListener = new MyOrientationEventChangeListener(AndroidAppHelper.currentApplication(), SensorManager.SENSOR_DELAY_NORMAL);

                myOrientationEventChangeListener.enable();
            }
        });

    }


    private void autoSetBg() {

        if (isGao) {
            setGaoImage();
        } else {
            setBg();
        }

    }

    /**
     * 判断是否是N系列
     *
     * @return
     */
    private boolean isN() {

        return Build.VERSION.SDK_INT == Build.VERSION_CODES.N || Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1;
    }


    private void register() {

        if (this.receiver == null) {
            this.receiver = new FullReceiver();

            IntentFilter intentFilter = new IntentFilter();

            intentFilter.addAction(ReceiverAction.SET_N_FULL_VERTICAL_IMAGE);

            intentFilter.addAction(ReceiverAction.SET_N_FULL_HORIZONTAL_IMAGE);

            intentFilter.addAction(ReceiverAction.N_GET_EXPANSION_FLOAT);

            intentFilter.addAction(ReceiverAction.SET_N_FULL_GAO_SI);

            intentFilter.addAction(ReceiverAction.SET_N_FULL_GAO_VALUE);

            intentFilter.addAction(ReceiverAction.N_FULL_ALPHA_VALUE);

            intentFilter.addAction(ReceiverAction.DELETE_FULL_BG);

            intentFilter.addAction(ReceiverAction.GET_FULL_INFO);

            intentFilter.addAction(ReceiverAction.SET_FULL_QUALITY);

            intentFilter.addAction(ReceiverAction.UI_GET_FULL_INFO);


            AndroidAppHelper.currentApplication().registerReceiver(receiver, intentFilter);


        }

    }


    class FullReceiver extends BroadcastReceiver {


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

                case ReceiverAction.SET_N_FULL_VERTICAL_IMAGE:

                    setFullVerticalImage(intent, context);

                    break;

                case ReceiverAction.SET_N_FULL_HORIZONTAL_IMAGE:

                    setFullHorizontalImage(context, intent);


                    break;
                case ReceiverAction.N_FULL_ALPHA_VALUE:
                    getAlpha(intent);

                    break;
                case ReceiverAction.SET_N_FULL_GAO_SI:

                    getGaoSi(intent);

                    break;
                case ReceiverAction.SET_N_FULL_GAO_VALUE:

                    getGaoValue(intent);

                    break;
                case ReceiverAction.DELETE_FULL_BG:

                    deleteBg(intent, context);

                    break;
                case ReceiverAction.GET_FULL_INFO:

                    sendInfo(intent,context);


                    break;
                case ReceiverAction.SET_FULL_QUALITY:

                    XposedBridge.log("lin15------>>>get the action");

                    Log.d("lin15------>>>","get the action");

                    getQuality(intent);

                    break;

                case ReceiverAction.N_GET_EXPANSION_FLOAT:

                    autoChangeAlpha(intent);

                    break;


                case ReceiverAction.UI_GET_FULL_INFO:

                    sendAllInfo(intent,context);

                    break;

            }
        }
    }


    /**
     * 设置竖屏图片
     *
     * @param intent
     */
    private void setFullVerticalImage(Intent intent, Context context) {

        String s = intent.getStringExtra(Conf.N_FULL_VERTICAL_FILE);

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

                    if (isVertical()) {

                        autoSetBg();

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


    private void setFullHorizontalImage(Context context, Intent intent) {

        String s = intent.getStringExtra(Conf.N_FULL_HORIZONTAL_FILE);

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

                    if (!isVertical()) {

                        autoSetBg();

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
     * 自动判断并设置背景
     */
    private void setBg() {

        if (full == null) {
            return;
        }

        File file = null;

        if (isVertical()) {

            file = getNFullFile(Conf.VERTICAL);

        } else {

            file = getNFullFile(Conf.HORIZONTAL);

        }

        if (!file.exists()) {

            full.setBackground(getDefaultDrawable());

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

        full.setBackground(bitmap2Drawable(bitmap));

        full.getBackground().setAlpha(alphaValue);

        //判断头部是否存在，如果不存在，则给头部设置上背景

        autoSetHeaderBg();

    }

    /**
     * 判断头部是否存在，如果不存在，则给头部设置上背景
     */
    private void autoSetHeaderBg(){

        if (header==null){



            return;
        }

        if (header.getBackground()!=null){



            return;
        }

        if (isVertical()){

            if (!getHeaderFile(Conf.VERTICAL).exists()){



                header.setBackground(getDefaultDrawable());

            }

        }else {

            if (!getHeaderFile(Conf.VERTICAL).exists()){



                header.setBackground(getDefaultDrawable());
            }

        }


    }


    /**
     * 设置高斯模糊图片
     */
    private void setGaoImage() {


        if (full == null) {
            return;
        }

        File file = null;

        if (isVertical()) {

            file = getNFullFile(Conf.VERTICAL);

        } else {

            file = getNFullFile(Conf.HORIZONTAL);

        }

        if (!file.exists()) {

            full.setBackground(getDefaultDrawable());

            return;
        }


        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        if (bitmap == null) {

            return;

        }

        bitmap = getBitmap(AndroidAppHelper.currentApplication(), bitmap, gaoValue);

        full.setBackground(bitmap2Drawable(bitmap));

        full.getBackground().setAlpha(alphaValue);

        autoSetHeaderBg();//自动给头部设置背景，避免头部透明化

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

                file = new File(path + "/" + Conf.N_FULL_VERTICAL_FILE);

                break;

            case Conf.HORIZONTAL:

                file = new File(path + "/" + Conf.N_FULL_HORIZONTAL_FILE);

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
     * 获取图片文件
     *
     * @param type 类型
     * @return 返回文件
     */
    private File getNFullFile(int type) {

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


    private boolean deleteFile(int type) {

        File file = getNFullFile(type);

        return file.delete();

    }


    /**
     * 监听屏幕横竖变化，改变快速设置面板的背景图
     */
    private class MyOrientationEventChangeListener extends OrientationEventListener {


        MyOrientationEventChangeListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {


            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                return;  //手机平放时，检测不到有效的角度
            }//只检测是否有四个角度的改变


            if (orientation > 350 || orientation < 10) { //0度

                saveFullWidthInfo();//保存宽度信息

//                saveFullHeightInfo();//保存高度信息


                if (rotation == 10) {
                    return;
                }

                if (isVertical()) {

                    rotation = 10;

                    autoSetBg();
                }


            } else if (orientation > 80 && orientation < 100) { //90度

                saveFullWidthInfo();//保存宽度信息

//                saveFullHeightInfo();//保存高度信息

                if (rotation == 20) {
                    return;
                }

                if (!isVertical()) {

                    rotation = 20;//不一致时表示屏幕旋转，重新赋值

                    autoSetBg();
                }


            } else if (orientation > 170 && orientation < 190) { //180度

                saveFullWidthInfo();//保存宽度信息

//                saveFullHeightInfo();//保存高度信息

                if (rotation == 30) {
                    return;
                }

                if (isVertical()) {

                    rotation = 30;//不一致时表示屏幕旋转，重新赋值

                    autoSetBg();
                }


            } else if (orientation > 265 && orientation < 275) { //270度

                saveFullWidthInfo();//保存宽度信息

//                saveFullHeightInfo();//保存高度信息

                if (rotation == 40) {
                    return;
                }

                if (!isVertical()) {

                    rotation = 40;//不一致时表示屏幕旋转，重新赋值

                    autoSetBg();
                }


            }
        }
    }

//    private void saveFullHeightInfo() {
//
//        int full_horizontal_height = sharedPreferences.getInt(Conf.FULL_SHU_HEIGHT, -1);
//
//        int full_vertical_height = sharedPreferences.getInt(Conf.FULL_HENG_HEIGHT, -1);
//
//
//        if (full_horizontal_height <= 0 && !isVertical()) {
//
//            full_horizontal_height = full.getHeight();
//
//            sharedPreferences.edit().putInt(Conf.FULL_HENG_HEIGHT, full_horizontal_height).apply();
//
//        }
//
//        if ((full_vertical_height <= 0 && isVertical()) || full_horizontal_height == full_vertical_height) {
//
//            full_vertical_height = full.getHeight();
//
//            sharedPreferences.edit().putInt(Conf.FULL_SHU_HEIGHT, full_vertical_height).apply();
//
//        }
//
//
//    }

    private void saveFullWidthInfo() {

        int horizontal_width = sharedPreferences.getInt(Conf.FULL_HENG_WIDTH, -1);

        int vertical_width = sharedPreferences.getInt(Conf.FULL_SHU_WIDTH, -1);

        if ((horizontal_width <= 0 && !isVertical())||horizontal_width==vertical_width) {


            horizontal_width = full.getWidth();

            sharedPreferences.edit().putInt(Conf.FULL_HENG_WIDTH, horizontal_width).apply();

        }

        if ((vertical_width <= 0 && isVertical())||horizontal_width==vertical_width) {

            vertical_width = full.getWidth();

            sharedPreferences.edit().putInt(Conf.FULL_SHU_WIDTH, vertical_width).apply();

        }


    }

    /**
     * 设置透明度
     *
     * @param intent
     */
    private void getAlpha(Intent intent) {


        int value = intent.getIntExtra(Conf.FULL_ALPHA_VALUE, -1);

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

        alphaValue = value;


        if (full == null || full.getBackground() == null) {
            return;
        }

        full.getBackground().setAlpha(alphaValue);


        sharedPreferences.edit().putInt(Conf.FULL_ALPHA_VALUE, alphaValue).apply();


    }

    /**
     * 设置是否高斯模糊
     *
     * @param intent
     */
    private void getGaoSi(Intent intent) {

        int b = intent.getIntExtra(Conf.FULL_GAO, -100);

        if (b == -100) {
            return;
        } else if (b > 0) {

            this.isGao = true;
        } else if (b < 0) {

            this.isGao = false;
        }

        sharedPreferences.edit().putBoolean(Conf.FULL_GAO, isGao).apply();

        autoSetBg();


    }

    /**
     * 接收高斯模糊半径
     *
     * @param intent
     */
    private void getGaoValue(Intent intent) {

        this.gaoValue = intent.getIntExtra(Conf.FULL_GAO_VALUE, 25);

        sharedPreferences.edit().putInt(Conf.FULL_GAO_VALUE, gaoValue).apply();

//        autoSetBg();

        if (isGao) {
            setGaoImage();
        }


    }

    private void getQuality(Intent intent) {

        int type = intent.getIntExtra(Conf.IMAGE_QUALITY, Conf.LOW_QUALITY);

        switch (type) {

            case Conf.HEIGHT_QUALITY:

                this.quality = Conf.HEIGHT_QUALITY;

                break;


            case Conf.LOW_QUALITY:

                this.quality = Conf.LOW_QUALITY;

                break;


        }

        if (!isGao) {
            setBg();//重新设置画质
        }

        XposedBridge.log("lin15----->>>>"+quality);


        sharedPreferences.edit().putInt(Conf.FULL_QUALITY, quality).apply();

    }

    private void deleteBg(Intent intent, Context context) {

        if (full == null || full.getBackground() == null) {
            return;
        }

        int type = intent.getIntExtra(Conf.FULL_DELETE_TYPE, -1);

        if (type == -1) {
            return;
        }

        if (deleteFile(type)) {

            Toast.makeText(context, "清除成功", Toast.LENGTH_SHORT).show();

            setBg();

        }

    }


    /**
     * 发送信息到UI
     *
     * @param intent
     * @param context
     */
    private void sendInfo(Intent intent, Context context) {


        int type = intent.getIntExtra(Conf.FULL_INFO, -1);

        if (type == -1) {
            return;
        }

        Full full=new Full();
        Intent intent1=new Intent(ReceiverAction.SEND_FULL_INFO);

        switch (type) {

            case Conf.VERTICAL:

                int w=sharedPreferences.getInt(Conf.FULL_SHU_WIDTH,-1);

                int h=sharedPreferences.getInt(Conf.FULL_SHU_HEIGHT,-1);

                full.setWidth(w);
                full.setHeight(h);

                break;


            case Conf.HORIZONTAL:

                int w1=sharedPreferences.getInt(Conf.FULL_HENG_WIDTH,-1);

                int h1=sharedPreferences.getInt(Conf.FULL_HENG_HEIGHT,-1);

                full.setWidth(w1);
                full.setHeight(h1);

                break;

        }

        intent1.putExtra(Conf.FULL_RESULT,full);

        intent1.putExtra(Conf.FULL_INFO,type);

        context.sendBroadcast(intent1);



    }

    private void autoChangeAlpha(Intent intent){

        float f = intent.getFloatExtra(Conf.N_EXPAND_VALUE, -0.1f);

        if (f == 1) {//完全下拉状态，保存高度

            if (isVertical()){

                int height=full.getHeight();

                sharedPreferences.edit().putInt(Conf.FULL_SHU_HEIGHT,height).apply();

            }else {

                int height=full.getHeight();

                sharedPreferences.edit().putInt(Conf.FULL_HENG_HEIGHT,height).apply();
            }

        }

        if (f < 0 || f > 1) {
            return;
        }


        if (full==null||full.getBackground()==null){
            return;
        }

        float alpha = f * alphaValue;

        if (alpha > alphaValue) {
            alpha = alphaValue;
        }

        if (alpha < 0) {
            alpha = 0;
        }

        if (alpha>255){
            alpha=255;
        }

        if (isVertical()&&getNFullFile(Conf.VERTICAL).exists()){

            full.getBackground().setAlpha((int) alpha);

            if (f == 1) {//完全下拉
                full.getBackground().setAlpha(alphaValue);
            } else if (f == 0) {//完全收缩

                full.getBackground().setAlpha(0);
            }

        }

        if (!isVertical()&&getNFullFile(Conf.HORIZONTAL).exists()){

            full.getBackground().setAlpha((int) alpha);

            if (f == 1) {//完全下拉
                full.getBackground().setAlpha(alphaValue);
            } else if (f == 0) {//完全收缩

                full.getBackground().setAlpha(0);
            }

        }


    }


    private File getHeaderFile(int type){

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


    private void sendAllInfo(Intent intent,Context context){

        int sdk=intent.getIntExtra(Conf.SDK,-1);

        if (sdk<=0){
            return;
        }

        //取7.0或7.1
        if (sdk==Build.VERSION_CODES.N||sdk==Build.VERSION_CODES.N_MR1){

            int alpha=sharedPreferences.getInt(Conf.FULL_ALPHA_VALUE,255);

            int quality=sharedPreferences.getInt(Conf.FULL_QUALITY,Conf.LOW_QUALITY);

            boolean gao=sharedPreferences.getBoolean(Conf.FULL_GAO,false);

            int gaoValue=sharedPreferences.getInt(Conf.FULL_GAO_VALUE,25);

            Result result=new Result();

            result.setAlpha(alpha);
            result.setGao(gao);
            result.setQuality(quality);
            result.setGaoValue(gaoValue);

            Intent intent1=new Intent(ReceiverAction.FULL_TO_UI_INFO);

            intent1.putExtra(Conf.FULL_TO_UI_RESULT,result);

            context.sendBroadcast(intent1);

        }


    }


}
