package com.xp.legend.lin15.hooks;

import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
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

public class O_FullHook extends BaseHook implements IXposedHookLoadPackage {

    private static final String METHOD = "onFinishInflate";
    private static final String CLASS2 = "com.android.systemui.qs.QSContainerImpl";
    private static final String METHOD2="setExpansion";
    private View fullView;
    private FullReceiver receiver;
    private SharedPreferences sharedPreferences;
    private int alphaValue;
    private int height = -1;
    private int defaultDrawable;

    //    private static final int STANDARD = 0x0020;
    private int quality = Conf.LOW_QUALITY;
    private boolean isGaoSi = false;
    private int gaoValue = 25;
    private View header;

    private int rotation = -101;

    private ImageView bgView, hengBgView;

    private boolean isScroll = true;//是否滚动背景

    private MyOrientationEventChangeListener myOrientationEventChangeListener;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!isO()) {
            return;
        }

        if (!lpparam.packageName.equals("com.android.systemui")) {
            return;
        }


        XposedHelpers.findAndHookMethod(CLASS2, lpparam.classLoader, METHOD, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                //注册广播
                register();

                fullView = (View) param.thisObject;//获取实例

                View view = ((ViewGroup) fullView).getChildAt(0);

                int id = AndroidAppHelper
                        .currentApplication()
                        .getResources()
                        .getIdentifier("qs_background", "id", lpparam.packageName);

                if (view != null && view.getId() == id) {
                    view.setVisibility(View.GONE);
                }

//                ((ViewGroup)fullView).getChildAt(0).setVisibility(View.GONE);//去掉背景

//                ViewGroup viewGroup= (ViewGroup) param.thisObject;
//
//                fullView=viewGroup.getChildAt(0);

                header = (View) XposedHelpers.getObjectField(param.thisObject, "mHeader");

                sharedPreferences = AndroidAppHelper.currentApplication().getSharedPreferences(Conf.SHARE, Context.MODE_PRIVATE);

                alphaValue = sharedPreferences.getInt(Conf.FULL_ALPHA_VALUE, 255);

                isScroll = sharedPreferences.getBoolean(Conf.FULL_SCROLL, true);


                defaultDrawable = AndroidAppHelper
                        .currentApplication()
                        .getResources()
                        .getIdentifier("qs_background_primary", "drawable", lpparam.packageName);

                quality = sharedPreferences.getInt(Conf.FULL_QUALITY, Conf.LOW_QUALITY);//初始化

                isGaoSi = sharedPreferences.getBoolean(Conf.FULL_GAO, false);

                gaoValue = sharedPreferences.getInt(Conf.FULL_GAO_VALUE, 25);


                autoSetBg();

                myOrientationEventChangeListener = new MyOrientationEventChangeListener(AndroidAppHelper.currentApplication(),
                        SensorManager.SENSOR_DELAY_NORMAL);

                myOrientationEventChangeListener.enable();

//                XposedBridge.log("full---count--->>"+viewGroup.getChildCount());
//
//                XposedBridge.log("full----------->>"+fullView.toString());
//
//                for (int i=0;i<viewGroup.getChildCount();i++){
//
//                    XposedBridge.log("lin15------->>>"+viewGroup.getChildAt(i).toString());
//
//                }

            }


        });


        XposedHelpers.findAndHookMethod(CLASS2, lpparam.classLoader, METHOD2, float.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                float f=XposedHelpers.getFloatField(param.thisObject,"mQsExpansion");

                autoChangeAlpha(f);



            }
        });


    }


    private void register() {

        if (this.receiver == null) {
            this.receiver = new FullReceiver();

            IntentFilter intentFilter = new IntentFilter();

            intentFilter.addAction(ReceiverAction.SET_N_FULL_VERTICAL_IMAGE);

            intentFilter.addAction(ReceiverAction.SET_N_FULL_HORIZONTAL_IMAGE);

            intentFilter.addAction(ReceiverAction.SEND_O_FLOAT);

            intentFilter.addAction(ReceiverAction.SET_N_FULL_GAO_SI);

            intentFilter.addAction(ReceiverAction.SET_N_FULL_GAO_VALUE);

            intentFilter.addAction(ReceiverAction.N_FULL_ALPHA_VALUE);

            intentFilter.addAction(ReceiverAction.DELETE_FULL_BG);

            intentFilter.addAction(ReceiverAction.GET_FULL_INFO);
            intentFilter.addAction(ReceiverAction.SET_FULL_QUALITY);
            intentFilter.addAction(ReceiverAction.UI_GET_FULL_INFO);
            intentFilter.addAction(ReceiverAction.SEND_FULL_SCROLL);
            intentFilter.addAction(ReceiverAction.SEND_CLEAN_ACTION);
            intentFilter.addAction(ReceiverAction.SEND_CUSTOM_HEIGHT);

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


    class FullReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action == null) {
                return;
            }

            if (fullView == null) {
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

                    sendInfo(intent, context);


                    break;
                case ReceiverAction.SET_FULL_QUALITY:

                    getQuality(intent);

                    break;

                case ReceiverAction.SEND_O_FLOAT:

//                    autoChangeAlpha(intent);

                    break;


                case ReceiverAction.UI_GET_FULL_INFO:

                    sendAllInfo(intent, context);

                    break;

                case ReceiverAction.SEND_FULL_SCROLL:

                    setScroll(intent);

                    break;

                case ReceiverAction.SEND_CLEAN_ACTION:

                    resetAll();

                    break;

                case ReceiverAction.SEND_CUSTOM_HEIGHT://自定义高度


                    setCustomHeight(intent);

                    break;


            }

        }
    }

    private void resetAll() {

        String path = "/data/user_de/0/" + AndroidAppHelper.currentApplication().getPackageName() + "/shared_prefs/" + Conf.SHARE+".xml";

        File file = new File(path);

        if (file.exists()) {
            file.delete();

            Toast.makeText(AndroidAppHelper.currentApplication(), "重置设置(reset success)", Toast.LENGTH_SHORT).show();
        }


        if (getHeaderFile(Conf.HORIZONTAL).exists()){

            getHeaderFile(Conf.HORIZONTAL).delete();

        }

        if (getHeaderFile(Conf.VERTICAL).exists()){
            getHeaderFile(Conf.VERTICAL).delete();
        }

        if (getNFullFile(Conf.HORIZONTAL).exists()){
            getNFullFile(Conf.HORIZONTAL).delete();
        }

        if (getNFullFile(Conf.VERTICAL).exists()){
            getNFullFile(Conf.VERTICAL).delete();
        }





//        Toast.makeText(AndroidAppHelper.currentApplication(), "重置成功(reset success)", Toast.LENGTH_SHORT).show();

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
     * 监听屏幕横竖变化，改变快速设置面板的背景图
     */
    private class MyOrientationEventChangeListener extends OrientationEventListener {


        MyOrientationEventChangeListener(Context context, int rate) {
            super(context, rate);
        }

        @Override
        public void onOrientationChanged(int orientation) {


//            if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
//                return;  //手机平放时，检测不到有效的角度
//            }//只检测是否有四个角度的改变


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

    private void autoSetBg() {

//        if (isGaoSi){
//            setGaoImage();
//        }else {
//            setBg();
//        }


        if (isScroll) {

//            autoBg();

            setScrollBg();

        } else {
            setBg();
        }


    }

    private void saveFullWidthInfo() {

        int horizontal_width = sharedPreferences.getInt(Conf.FULL_HENG_WIDTH, -1);

        int vertical_width = sharedPreferences.getInt(Conf.FULL_SHU_WIDTH, -1);

        if ((horizontal_width <= 0 && !isVertical()) || horizontal_width == vertical_width) {


            horizontal_width = fullView.getWidth();

            sharedPreferences.edit().putInt(Conf.FULL_HENG_WIDTH, horizontal_width).apply();

        }

        if ((vertical_width <= 0 && isVertical()) || horizontal_width == vertical_width) {

            vertical_width = fullView.getWidth();

            sharedPreferences.edit().putInt(Conf.FULL_SHU_WIDTH, vertical_width).apply();

        }


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


    /**
     * 设置高斯模糊图片
     */
    private void setGaoImage() {


        if (fullView == null) {
            return;
        }

        File file = null;

        if (isVertical()) {

            file = getNFullFile(Conf.VERTICAL);

        } else {

            file = getNFullFile(Conf.HORIZONTAL);

        }

        if (!file.exists()) {

            fullView.setBackground(getDefaultDrawable());

            return;
        }


        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        if (bitmap == null) {

            return;

        }

        bitmap = getBitmap(AndroidAppHelper.currentApplication(), bitmap, gaoValue);

        fullView.setBackground(bitmap2Drawable(bitmap));

        fullView.getBackground().setAlpha(alphaValue);

        autoSetHeaderBg();//自动给头部设置背景，避免头部透明化

    }


    /**
     * 判断头部是否存在，如果不存在，则给头部设置上背景
     */
    private void autoSetHeaderBg() {

        if (header == null) {

            return;
        }

        if (header.getBackground() != null) {
            return;
        }

        if (isVertical()) {

            if (!getHeaderFile(Conf.VERTICAL).exists()) {

                header.setBackground(getDefaultDrawable());

            }

        } else {

            if (!getHeaderFile(Conf.VERTICAL).exists()) {

                header.setBackground(getDefaultDrawable());
            }

        }


    }

    /**
     * 获取默认背景
     *
     * @return
     */
    private Drawable getDefaultDrawable() {

        if (defaultDrawable == 0) {
            return null;
        }

        return AndroidAppHelper.currentApplication().getDrawable(defaultDrawable);

    }

    private File getHeaderFile(int type) {

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

    private boolean deleteFile(int type) {

        File file = getNFullFile(type);

        return file.delete();

    }


    /**
     * 设置透明度
     *
     * @param intent
     */
    private void getAlpha(Intent intent) {


        int value = intent.getIntExtra(Conf.FULL_ALPHA_VALUE, -1);

        if (value == -1) {
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

        if (isScroll) {

            if (bgView != null && bgView.getVisibility() == View.VISIBLE) {
                bgView.setImageAlpha(alphaValue);
            }

            if (hengBgView != null && hengBgView.getVisibility() == View.VISIBLE) {
                hengBgView.setImageAlpha(alphaValue);
            }

            return;
        }


        if (fullView == null || fullView.getBackground() == null) {
            return;
        }


        fullView.getBackground().setAlpha(alphaValue);

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

            this.isGaoSi = true;
        } else if (b < 0) {

            this.isGaoSi = false;
        }

        sharedPreferences.edit().putBoolean(Conf.FULL_GAO, isGaoSi).apply();

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

        if (isGaoSi) {
//            setGaoImage();

            autoSetBg();

        }


    }

    private void getQuality(Intent intent) {

//        int type = intent.getIntExtra(Conf.IMAGE_QUALITY, Conf.LOW_QUALITY);

        this.quality = intent.getIntExtra(Conf.IMAGE_QUALITY, Conf.LOW_QUALITY);


        if (!isGaoSi) {
//            setBg();//重新设置画质

            autoSetBg();
        }

        sharedPreferences.edit().putInt(Conf.FULL_QUALITY, quality).apply();

    }


    /**
     * 设置滚动背景
     */
    private void setScrollBg() {


        File file = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

        if (!isGaoSi) {

            switch (this.quality) {

                case Conf.HEIGHT_QUALITY:

                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;

                    break;


                case Conf.LOW_QUALITY:

                    options.inPreferredConfig = Bitmap.Config.RGB_565;

                    break;


            }
        }


        if (isVertical()) {//竖屏

            if (hengBgView != null) {//隐藏横屏背景

                if (hengBgView.getVisibility() == View.VISIBLE) {

                    hengBgView.setVisibility(View.GONE);
                }

            }

            if (bgView == null) {//如果为null，则重新实例化
                bgView = new ImageView(AndroidAppHelper.currentApplication());
            }

            file = getNFullFile(Conf.VERTICAL);

            if (!file.exists()) {//文件不存在

                bgView.setVisibility(View.GONE);//实例化后的惨案
                fullView.setBackground(getDefaultDrawable());

                return;

            }


            if (bgView.getVisibility() == View.GONE) {//如果不可见，则设置为可见
                bgView.setVisibility(View.VISIBLE);
            }




            ((ViewGroup) fullView).removeView(bgView);

            ((ViewGroup) fullView).addView(bgView, 0);//放在第一位

            if (isGaoSi){

                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                bitmap = getBitmap(AndroidAppHelper.currentApplication(), bitmap, gaoValue);
                bgView.setImageBitmap(bitmap);
                bgView.setImageAlpha(alphaValue);

            }else {

                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                bgView.setImageBitmap(bitmap);

                bgView.setImageAlpha(alphaValue);
            }


        } else {//横屏

            if (bgView != null) {

                if (bgView.getVisibility() == View.VISIBLE) {
                    bgView.setVisibility(View.GONE);
                }

            }

            if (hengBgView == null) {

                hengBgView = new ImageView(AndroidAppHelper.currentApplication());
            }

            file = getNFullFile(Conf.HORIZONTAL);

            if (!file.exists()) {//文件不存在

                hengBgView.setVisibility(View.GONE);

                fullView.setBackground(getDefaultDrawable());

                return;

            }

            if (hengBgView.getVisibility() == View.GONE) {

                hengBgView.setVisibility(View.VISIBLE);
            }




            ((ViewGroup) fullView).removeView(hengBgView);
            ((ViewGroup) fullView).addView(hengBgView, 0);//添加到第一位

            if (isGaoSi) {

                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                bitmap = getBitmap(AndroidAppHelper.currentApplication(), bitmap, gaoValue);
                hengBgView.setImageBitmap(bitmap);

                hengBgView.setImageAlpha(alphaValue);

            }else {

                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

                hengBgView.setImageBitmap(bitmap);

                hengBgView.setImageAlpha(alphaValue);

            }

        }

        autoSetHeaderBg();

    }


    /**
     * 自动判断并设置背景
     */
    private void setBg() {


        if (bgView!=null&&bgView.getVisibility()==View.VISIBLE){
            bgView.setVisibility(View.GONE);
        }

        if (hengBgView!=null&&hengBgView.getVisibility()==View.VISIBLE){
            hengBgView.setVisibility(View.GONE);
        }

        if (fullView == null) {
            return;
        }

        File file = null;

        if (isVertical()) {

            file = getNFullFile(Conf.VERTICAL);

        } else {

            file = getNFullFile(Conf.HORIZONTAL);

        }

        if (!file.exists()) {//设置默认背景

            fullView.setBackground(getDefaultDrawable());

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



        if (isGaoSi) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

            fullView.setBackground(bitmap2Drawable(bitmap));

            fullView.getBackground().setAlpha(alphaValue);

        }else {

            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);

            fullView.setBackground(bitmap2Drawable(bitmap));

            fullView.getBackground().setAlpha(alphaValue);
        }



        //判断头部是否存在，如果不存在，则给头部设置上背景

        autoSetHeaderBg();

    }

    private void deleteBg(Intent intent, Context context) {

        if (fullView == null || fullView.getBackground() == null) {
            return;
        }

        int type = intent.getIntExtra(Conf.FULL_DELETE_TYPE, -1);

        if (type == -1) {
            return;
        }

        if (deleteFile(type)) {

            Toast.makeText(context, "清除成功", Toast.LENGTH_SHORT).show();

//            setBg();

            autoSetBg();

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

        Full full = new Full();
        Intent intent1 = new Intent(ReceiverAction.SEND_FULL_INFO);

        switch (type) {

            case Conf.VERTICAL:

                int w = sharedPreferences.getInt(Conf.FULL_SHU_WIDTH, -1);

                int h = sharedPreferences.getInt(Conf.FULL_SHU_HEIGHT, -1);

                full.setWidth(w);
                full.setHeight(h);

                break;


            case Conf.HORIZONTAL:

                int w1 = sharedPreferences.getInt(Conf.FULL_HENG_WIDTH, -1);

                int h1 = sharedPreferences.getInt(Conf.FULL_HENG_HEIGHT, -1);

                full.setWidth(w1);
                full.setHeight(h1);

                break;

        }

        intent1.putExtra(Conf.FULL_RESULT, full);

        intent1.putExtra(Conf.FULL_INFO, type);

        context.sendBroadcast(intent1);


    }


    /**
     * 接收下拉程度，无下拉为0，下拉到底部为1，float
     *
     *
     */
    private void autoChangeAlpha(float f) {

//        float f = intent.getFloatExtra(Conf.N_EXPAND_VALUE, -0.1f);

        scrollBgView(f, fullView.getHeight());


        if (f == 1.0) {//完全下拉状态，保存高度

            if (isVertical()) {

                int height = fullView.getHeight();

                sharedPreferences.edit().putInt(Conf.FULL_SHU_HEIGHT, height).apply();


            } else {

                int height = fullView.getHeight();

                sharedPreferences.edit().putInt(Conf.FULL_HENG_HEIGHT, height).apply();
            }

        }

        if (f < 0 || f > 1) {
            return;
        }


        if (fullView == null || fullView.getBackground() == null) {
            return;
        }

        float alpha = f * alphaValue;

        if (alpha > alphaValue) {
            alpha = alphaValue;
        }

        if (alpha < 0) {
            alpha = 0;
        }

        if (alpha > 255) {
            alpha = 255;
        }

        if (isVertical() && getNFullFile(Conf.VERTICAL).exists()) {

            fullView.getBackground().setAlpha((int) alpha);

            if (f == 1) {//完全下拉
                fullView.getBackground().setAlpha(alphaValue);
            } else if (f == 0) {//完全收缩

                fullView.getBackground().setAlpha(0);
            }

        }

        if (!isVertical() && getNFullFile(Conf.HORIZONTAL).exists()) {

            fullView.getBackground().setAlpha((int) alpha);

            if (f == 1) {//完全下拉
                fullView.getBackground().setAlpha(alphaValue);
            } else if (f == 0) {//完全收缩

                fullView.getBackground().setAlpha(0);
            }

        }


    }


    private void sendAllInfo(Intent intent, Context context) {

        int sdk = intent.getIntExtra(Conf.SDK, -1);

        if (sdk <= 0) {
            return;
        }

        //取8.0或8.1
        if (sdk == Build.VERSION_CODES.O || sdk == Build.VERSION_CODES.O_MR1) {

            int alpha = sharedPreferences.getInt(Conf.FULL_ALPHA_VALUE, 255);

            int quality = sharedPreferences.getInt(Conf.FULL_QUALITY, Conf.LOW_QUALITY);

            boolean gao = sharedPreferences.getBoolean(Conf.FULL_GAO, false);

            int gaoValue = sharedPreferences.getInt(Conf.FULL_GAO_VALUE, 25);

            boolean scroll = sharedPreferences.getBoolean(Conf.FULL_SCROLL, true);

            Result result = new Result();

            result.setAlpha(alpha);
            result.setGao(gao);
            result.setQuality(quality);
            result.setGaoValue(gaoValue);
            result.setScroll(scroll);

            Intent intent1 = new Intent(ReceiverAction.FULL_TO_UI_INFO);

            intent1.putExtra(Conf.FULL_TO_UI_RESULT, result);

            context.sendBroadcast(intent1);

        }


    }


    private int record = -1;//记录者，避免重复设置浪费资源

    /**
     * 滚动背景
     *
     * @param height 传递此时设置面板的高度，根据高度设置滑动位置
     */
    private void scrollBgView(float f, int height) {

        int imageHeight = -1;

        if (isVertical()) {

            imageHeight = sharedPreferences.getInt(Conf.FULL_SHU_HEIGHT, -1);

        } else {

            imageHeight = sharedPreferences.getInt(Conf.FULL_HENG_HEIGHT, -1);
        }


        if (record == height) {

            if (f==0) {

                if (isVertical() && bgView != null && bgView.getVisibility() == View.VISIBLE) {
                    bgView.scrollTo(0, imageHeight);
                } else if (!isVertical() && hengBgView != null && hengBgView.getVisibility() == View.VISIBLE) {
                    hengBgView.scrollTo(0, imageHeight);
                }

            }

            return;
        }


        record = height;//记录

        if (record < 0) {
            return;
        }

        if (imageHeight < 0) {
            return;
        }

        if (f==1.0){

            if (isVertical() && bgView != null && bgView.getVisibility() == View.VISIBLE) {
                bgView.scrollTo(0, 0);
            } else if (!isVertical() && hengBgView != null && hengBgView.getVisibility() == View.VISIBLE) {
                hengBgView.scrollTo(0, 0);
            }

        }else {

            if (isVertical() && bgView != null && bgView.getVisibility() == View.VISIBLE) {
                bgView.scrollTo(0, -(height - imageHeight));
            } else if (!isVertical() && hengBgView != null && hengBgView.getVisibility() == View.VISIBLE) {
                hengBgView.scrollTo(0, -(height - imageHeight));
            }
        }
    }

    private void setScroll(Intent intent) {

        this.isScroll=intent.getBooleanExtra(Conf.FULL_SCROLL, true);
        //保存变量
        sharedPreferences.edit().putBoolean(Conf.FULL_SCROLL, isScroll).apply();

        autoSetBg();//重新设置背景


    }

    private void setCustomHeight(Intent intent){


        int type=intent.getIntExtra("type",-1);

        if (type==-1){
            return;
        }

        String h=intent.getStringExtra("height");

        int height=Integer.parseInt(h);

        switch (type){
            case 10://竖屏

                sharedPreferences.edit().putInt(Conf.FULL_SHU_HEIGHT, height).apply();

                break;

            case 20://横屏

                sharedPreferences.edit().putInt(Conf.FULL_HENG_HEIGHT, height).apply();

                break;
        }

        Toast.makeText(AndroidAppHelper.currentApplication(), "自定义高度成功", Toast.LENGTH_SHORT).show();

    }

}
