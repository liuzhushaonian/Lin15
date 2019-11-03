package com.xp.legend.lin16.hooks;

import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.xp.legend.lin16.bean.HeaderInfo;
import com.xp.legend.lin16.bean.Result;
import com.xp.legend.lin16.utils.BaseHook;
import com.xp.legend.lin16.utils.Conf;
import com.xp.legend.lin16.utils.ReceiverAction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class P_HeaderHook extends BaseHook implements IXposedHookLoadPackage {

    private static final String CLASS = "com.android.systemui.qs.QSContainerImpl";
    private static final String METHOD = "onFinishInflate";
    private static final String METHOD2 = "updateExpansion";
    private static final String METHOD3 = "onConfigurationChanged";
    private static final String METHOD4 = "onLayout";
    private N_HeaderReceiver receiver;
    private SharedPreferences sharedPreferences;
    private boolean isGAO = false;

    private int quality;

    private int alpha_value = 255;

    private int drawable = 0;

    private int gaoValue = 25;

//    private MyOrientationEventChangeListener myOrientationEventChangeListener;

    private int rotation = -101;

    private SlitImageView shuHeader, hengHeader;
    private ViewGroup fullView;
    private int radius;
    private boolean isFirst = true;
    private View mBackgroundGradient, mStatusBarBackground;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!isP()) {
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

                fullView = (ViewGroup) param.thisObject;

                mBackgroundGradient = (View) XposedHelpers.getObjectField(param.thisObject, "mBackgroundGradient");
                mStatusBarBackground = (View) XposedHelpers.getObjectField(param.thisObject, "mStatusBarBackground");

                TypedValue typedValue = new TypedValue();
                AndroidAppHelper.currentApplication().getTheme().resolveAttribute(android.R.attr.dialogCornerRadius, typedValue, true);


                /**
                 * 获取圆角度数
                 */
                radius = TypedValue.complexToDimensionPixelSize(typedValue.data,
                        AndroidAppHelper.currentApplication().getResources().getDisplayMetrics());

                new Thread(){

                    @Override
                    public void run() {
                        super.run();

                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Runnable runnable= P_HeaderHook.this::autoSetBg;

                        fullView.post(runnable);


                    }
                }.start();


//                autoSetBg();


            }
        });


        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD3, Configuration.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);

                Configuration configuration = (Configuration) param.args[0];

                autoSetPosition(configuration.orientation);

            }
        });

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD2, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                float f = XposedHelpers.getFloatField(param.thisObject, "mQsExpansion");

                autoChangeAlpha(f);

            }
        });

//        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD4, boolean.class, int.class, int.class, int.class, int.class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//
//                if (isFirst) {
//                    isFirst = false;
//                    autoSetBg();
//
//                }
//
//            }
//        });


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

//                    autoChangeAlpha(intent);

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

                    deleteBg(intent, context);

                    break;


                case ReceiverAction.SET_HEADER_QUALITY:

                    setImageQuality(intent);

                    break;

                case ReceiverAction.UI_GET_HEADER_INFO:

                    sendAllInfo(intent, context);

                    break;
                case ReceiverAction.SEND_ORI://接收屏幕旋转信息

//                    autoSetPosition(intent);

                    break;

            }

        }
    }


    /**
     * 自动设置壁纸
     * 判断是否高斯模糊
     */
    private void autoSetBg() {


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


        if (isVertical) {

            file = getNHeaderFile(Conf.VERTICAL);

        } else {
            file = getNHeaderFile(Conf.HORIZONTAL);
        }

        if (!file.exists()) {


            cleanHeaderBg();

            return;
        }


        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

        int value = sharedPreferences.getInt(Conf.N_HEADER_GAO_VALUE, 25);

        bitmap = getBitmap(AndroidAppHelper.currentApplication(), bitmap, value);

        showHeader();

        if (isVertical) {

            hideHengHeader();//隐藏横屏头部

            if (shuHeader == null) {
                initShuHeader();
            }

            if (shuHeader == null || fullView == null) {
                return;
            }

            //还原
            if (hengHeader != null) {
                fullView.removeView(hengHeader);
            }
            fullView.removeView(shuHeader);

            ViewGroup viewGroup= (ViewGroup) shuHeader.getParent();
            if (viewGroup!=null){
                viewGroup.removeView(shuHeader);
            }


            fullView.addView(shuHeader,1);


            shuHeader.setImageBitmap(bitmap);

            setAlpha(shuHeader);


        } else {

            hideShuHeader();//隐藏竖屏头部

            if (hengHeader == null) {
                initHengHeader();
            }

            if (hengHeader == null || fullView == null) {
                return;
            }

            if (shuHeader != null) {
                fullView.removeView(shuHeader);
            }

            fullView.removeView(hengHeader);

            ViewGroup viewGroup= (ViewGroup) hengHeader.getParent();
            if (viewGroup!=null){
                viewGroup.removeView(hengHeader);
            }

            fullView.addView(hengHeader,1);

            hengHeader.setImageBitmap(bitmap);

            setAlpha(hengHeader);


        }

        autoChangePosition();//自动调整位置


    }

    /**
     * 设置普通图片
     * 自动判断当前横竖屏
     */
    private void setBg() {


        File file = null;

        //自动判断是横屏或是竖屏
        if (isVertical) {

            file = getNHeaderFile(Conf.VERTICAL);


        } else {
            file = getNHeaderFile(Conf.HORIZONTAL);


        }

        if (!file.exists()) {//文件不存在


            cleanHeaderBg();


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

        showHeader();

        if (isVertical) {

            hideHengHeader();

            if (shuHeader == null) {
                initShuHeader();
            }

            if (shuHeader == null || fullView == null) {
                return;
            }

            //还原
            if (hengHeader != null) {
                fullView.removeView(hengHeader);
            }
            fullView.removeView(shuHeader);

            ViewGroup viewGroup= (ViewGroup) shuHeader.getParent();
            if (viewGroup!=null){
                viewGroup.removeView(shuHeader);
            }

            fullView.addView(shuHeader,1);//位置放着全部背景


            shuHeader.setBitmap(bitmap,radius);

            setAlpha(shuHeader);

        } else {

            hideShuHeader();

            if (hengHeader == null) {
                initHengHeader();
            }

            if (hengHeader == null || fullView == null) {
                return;
            }

            if (shuHeader != null) {
                fullView.removeView(shuHeader);
            }

            fullView.removeView(hengHeader);

            ViewGroup viewGroup= (ViewGroup) hengHeader.getParent();
            if (viewGroup!=null){
                viewGroup.removeView(hengHeader);
            }

            fullView.addView(hengHeader,1);

            hengHeader.setBitmap(bitmap,radius);

            setAlpha(hengHeader);

        }

        autoChangePosition();//摆正位置

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
     *
     * @param type
     * @return
     */
    private File getFullFile(int type) {

        String path = AndroidAppHelper.currentApplication().getFilesDir().getAbsolutePath();

        File file = null;

        switch (type) {
            case Conf.VERTICAL://竖屏图

                file = new File(path + "/" + Conf.FULL_VERTICAL_FILE);

                break;

            case Conf.HORIZONTAL://横屏图

                file = new File(path + "/" + Conf.FULL_HORIZONTAL_FILE);

                break;
        }

        return file;

    }

    /**
     * 获取默认背景
     * 用自定义view作为背景，所以舍弃
     *
     * @return
     */
    @Deprecated
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
    private void deleteBg(Intent intent, Context context) {


        int type = intent.getIntExtra(Conf.N_HEADER_DELETE_TYPE, -1);

        if (type == -1) {
            return;
        }

        if (deleteFile(type)) {

            Toast.makeText(context, "清除成功", Toast.LENGTH_SHORT).show();


            setBg();

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

                file = new File(path + "/" + Conf.HEADER_VERTICAL_FILE);

                break;

            case Conf.HORIZONTAL://横屏图

                file = new File(path + "/" + Conf.HEADER_HORIZONTAL_FILE);

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
                        deleteUriFile(uri,context);
                        Intent intent1=new Intent(ReceiverAction.DELETE_IMG_CALL);

                        AndroidAppHelper.currentApplication().sendBroadcast(intent1);

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

                        autoSetBg();



                    }

                    Toast.makeText(context, "设置成功", Toast.LENGTH_SHORT).show();

                    deleteUriFile(uri,context);
                    Intent intent1=new Intent(ReceiverAction.DELETE_IMG_CALL);

                    AndroidAppHelper.currentApplication().sendBroadcast(intent1);



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

                try {
                    AndroidAppHelper.currentApplication().openFileOutput(Conf.HEADER_VERTICAL_FILE,Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                file = new File(path + "/" + Conf.HEADER_VERTICAL_FILE);

                break;

            case Conf.HORIZONTAL:

                try {
                    AndroidAppHelper.currentApplication().openFileOutput(Conf.HEADER_HORIZONTAL_FILE,Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                file = new File(path + "/" + Conf.HEADER_HORIZONTAL_FILE);

                break;
        }

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file,false);
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
     * @param f
     */
    private void autoChangeAlpha(float f) {

        if (f < 0 || f > 1) {
            return;
        }

        float alpha = ((1 - f) * (alpha_value / 255.0f));

        if (alpha > 1.0f) {
            alpha = 1.0f;
        }

        if (alpha < 0) {
            alpha = 0f;
        }

        if (getNHeaderFile(Conf.VERTICAL).exists()) {

            if (shuHeader != null && shuHeader.getVisibility() == View.VISIBLE && isVertical) {

                shuHeader.setAlpha(alpha);

                if (f == 1) {
                    shuHeader.setAlpha(0f);
                }

            }
        }

        if (getNHeaderFile(Conf.HORIZONTAL).exists()) {

            if (hengHeader != null && hengHeader.getVisibility() == View.VISIBLE && !isVertical) {

                hengHeader.setAlpha(alpha);

                if (f == 1) {
                    hengHeader.setAlpha(0f);
                }

            }
        }


    }

    /**
     * 接收透明度
     *
     * @param intent ？
     */
    private void getAlphaValue(Intent intent) {

        int value = intent.getIntExtra(Conf.N_HEADER_ALPHA, -1);

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

        alpha_value = value;

        sharedPreferences.edit().putInt(Conf.N_HEADER_ALPHA, alpha_value).apply();


        autoSetBg();


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


        autoSetBg();

        sharedPreferences.edit().putBoolean(Conf.N_HEADER_GAO, isGAO).apply();

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

        if (isGAO) {
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

                    int width = getWidth(true);

                    int height = getHeight(true);

                    info.setHeight(height);

                    info.setWidth(width);

                    logs("发送头部竖屏 height--->>>" + height);
                    logs("发送头部竖屏 width--->>>" + width);

//                    intent1.putExtra(Conf.N_HEADER_RESULT,VERTICAL);

                    break;

                case Conf.HORIZONTAL:

                    int width1 = getWidth(false);

                    int height1 = getHeight(false);

                    info.setHeight(height1);

                    info.setWidth(width1);

                    logs("发送头部横屏 height--->>>" + height1);
                    logs("发送头部横屏 width--->>>" + width1);

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
     *
     * @param intent
     */
    private void setImageQuality(Intent intent) {

        this.quality = intent.getIntExtra(Conf.N_HEADER_QUALITY, Conf.LOW_QUALITY);

        autoSetBg();

        sharedPreferences.edit().putInt(Conf.N_HEADER_QUALITY, this.quality).apply();

    }

    private void sendAllInfo(Intent intent, Context context) {

        int sdk = intent.getIntExtra(Conf.SDK, -1);

        if (sdk <= 0) {
            return;
        }

        //取9.0
        if (isP()) {

            int alpha = sharedPreferences.getInt(Conf.N_HEADER_ALPHA, 255);

            int quality = sharedPreferences.getInt(Conf.N_HEADER_QUALITY, Conf.LOW_QUALITY);

            boolean gao = sharedPreferences.getBoolean(Conf.N_HEADER_GAO, false);

            int gaoValue = sharedPreferences.getInt(Conf.N_HEADER_GAO_VALUE, 25);

            Result result = new Result();

            result.setAlpha(alpha);
            result.setGao(gao);
            result.setQuality(quality);
            result.setGaoValue(gaoValue);

            Intent intent1 = new Intent(ReceiverAction.HEADER_TO_UI_INFO);

            intent1.putExtra(Conf.HEADER_TO_UI_RESULT, result);

            context.sendBroadcast(intent1);

        }


    }


    /**
     * 自动根据当前屏幕设置背景，代替之前的监听
     *
     * @param p
     */
    private void autoSetPosition(int p) {


        if (p == -1) {
            return;
        }

        switch (p) {

            case Configuration.ORIENTATION_LANDSCAPE://横屏

                if (!isVertical) {//避免重复
                    return;
                }

                isVertical = false;

                autoSetBg();

                break;

            case Configuration.ORIENTATION_PORTRAIT://竖屏
            default:

                if (isVertical) {//避免重复
                    return;
                }


                isVertical = true;

                autoSetBg();

                break;

        }


    }

    //初始化竖屏头部，宽度取保存好的数值
    private void initShuHeader() {

        if (shuHeader != null) {
            return;
        }

        int width = getWidth(true);
        int height = getHeight(true);

        if (width <= 0 || height <= 0) {
            logs("竖屏头部信息");
            logs("width-->>>" + width);
            logs("height-->>>" + height);
            return;
        }

        shuHeader = new SlitImageView(AndroidAppHelper.currentApplication());
        shuHeader.setWidth(width);
        ViewGroup.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);

        shuHeader.setLayoutParams(layoutParams);

        shuHeader.setRadius(this.radius);//设置上圆角度数

    }

    /**
     * 初始化横屏头部
     */
    private void initHengHeader() {

        if (hengHeader != null) {
            return;
        }

        int width = getWidth(false);
        int height = getHeight(false);

        if (width <= 0 || height <= 0) {
            logs("横屏头部信息");
            logs("width--->>>" + width);
            logs("height-->>>" + height);
            return;
        }

        hengHeader = new SlitImageView(AndroidAppHelper.currentApplication());
        hengHeader.setWidth(width);
        ViewGroup.LayoutParams layoutParams = new FrameLayout.LayoutParams(width, height);
        hengHeader.setLayoutParams(layoutParams);
        hengHeader.setRadius(this.radius);//设置上圆角度数

    }

    /**
     * 获取宽度
     *
     * @return 返回保存好的宽度值
     */
    private int getWidth(boolean isV) {

        if (isV) {

            return sharedPreferences.getInt(Conf.FULL_SHU_WIDTH, -1);

        } else {


            return sharedPreferences.getInt(Conf.FULL_HENG_WIDTH, -1);
        }

    }


    /**
     * 获取高度
     *
     * @return 返回保存好的高度值
     */
    private int getHeight(boolean isV) {

        if (isV) {

            return sharedPreferences.getInt(Conf.N_HEADER_VERTICAL_HEIGHT, -1);

        } else {

            return sharedPreferences.getInt(Conf.N_HEADER_HORIZONTAL_HEIGHT, -1);
        }
    }


    /**
     * 清除整个头部的背景，还原
     */
    private void cleanHeaderBg() {

        if (shuHeader != null) {

            shuHeader.setVisibility(View.GONE);

            if (fullView!=null) {
                fullView.removeView(shuHeader);
            }

            shuHeader = null;
        }

        if (hengHeader != null) {
            hengHeader.setVisibility(View.GONE);
            if (fullView!=null) {
                fullView.removeView(hengHeader);
            }
            hengHeader = null;
        }

    }


    private void autoChangePosition() {

        if (shuHeader != null && shuHeader.getVisibility() == View.VISIBLE && isVertical) {

            int full_width = sharedPreferences.getInt(Conf.F_V_W, 0);

            int bg_width = getWidth(true);

            int margin = (full_width - bg_width) / 2;//计算距离两边的距离

            int ii = AndroidAppHelper
                    .currentApplication()
                    .getResources()
                    .getIdentifier("android:dimen/quick_qs_offset_height", "dimen", AndroidAppHelper.currentPackageName());


            int offset_height = (int) AndroidAppHelper.currentApplication().getResources().getDimension(ii);


            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) shuHeader.getLayoutParams();
            layoutParams.setMargins(margin, offset_height, margin, 0);

            shuHeader.setLayoutParams(layoutParams);

            shuHeader.setElevation(dp2px(4));

            hideBackView();

        }

        if (hengHeader != null && hengHeader.getVisibility() == View.VISIBLE && !isVertical) {

            int full_width = sharedPreferences.getInt(Conf.F_H_W, 0);

            int bg_width = getWidth(false);

            int margin = (full_width - bg_width) / 2;//计算距离两边的距离


            int ii = AndroidAppHelper
                    .currentApplication()
                    .getResources()
                    .getIdentifier("android:dimen/quick_qs_offset_height", "dimen", AndroidAppHelper.currentPackageName());

            int offset_height = (int) AndroidAppHelper.currentApplication().getResources().getDimension(ii);


            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) hengHeader.getLayoutParams();
            layoutParams.setMargins(margin, offset_height, margin, 0);

            hengHeader.setLayoutParams(layoutParams);

            hengHeader.setElevation(dp2px(4));

            hideBackView();

        }


    }

    private void showHeader() {

        if (shuHeader != null && shuHeader.getVisibility() == View.GONE && isVertical) {
            shuHeader.setVisibility(View.VISIBLE);
        }


        if (hengHeader != null && hengHeader.getVisibility() == View.GONE && !isVertical) {

            hengHeader.setVisibility(View.VISIBLE);

        }

    }

    private void hideShuHeader() {

        if (shuHeader != null && shuHeader.getVisibility() == View.VISIBLE) {
            shuHeader.setVisibility(View.GONE);
        }

    }

    private void hideHengHeader() {

        if (hengHeader != null && hengHeader.getVisibility() == View.VISIBLE) {
            hengHeader.setVisibility(View.GONE);
        }

    }

    private void setAlpha(View view) {

        if (view != null) {

            float f = alpha_value / 255.0f;

            if (f > 1f) {
                f = 1.0f;
            }

            if (f < 0f) {
                f = 0f;
            }

            view.setAlpha(f);

        }

    }

    private static int dp2px(float dipValue) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dipValue,
                AndroidAppHelper.currentApplication().getResources().getDisplayMetrics());
    }

    /**
     * 隐藏一些无关紧要的view
     */
    private void hideBackView() {

        if (mStatusBarBackground != null) {
            mStatusBarBackground.setVisibility(View.GONE);
        }

        if (mBackgroundGradient != null) {
            mBackgroundGradient.setVisibility(View.GONE);
        }

    }

}
