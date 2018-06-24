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

/**
 * 全局设置背景
 * 流程：一开始hook方法，获取整个view实例，注册广播，并判断SharedPreferences里是否有图片路径，如果有则设置上，并一起获取透明度并设置，如果没有，则跳过，等待设置
 */

public class StatusBarFullHook implements IXposedHookLoadPackage {

    private static final String METHOD = "onFinishInflate";
    private static final String CLASS2 = "com.android.systemui.qs.QSContainerImpl";
    private View fullView;
    private FullReceiver fullReceiver;
    private SharedPreferences sharedPreferences;
    private int alphaValue;
    private int height = -1;
    private int defaultDrawable;
    private static final int BEST = 0x0010;
//    private static final int STANDARD = 0x0020;
    private static final int LOWEST = 0x0030;
    private int quality = LOWEST;
    private boolean isGaoSi=false;
    private int gaoValue=25;




    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.systemui")) {
            return;
        }
        XposedHelpers.findAndHookMethod(CLASS2, lpparam.classLoader, METHOD, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                //注册广播
                registerBroadcast();

                fullView = (View) param.thisObject;//获取实例

                sharedPreferences = AndroidAppHelper.currentApplication().getSharedPreferences(ReceiverAction.SS, Context.MODE_PRIVATE);

                alphaValue = sharedPreferences.getInt(Conf.ALPHA, 255);

                int color = sharedPreferences.getInt(Conf.FULL_COLOR, -1);

                defaultDrawable = AndroidAppHelper.currentApplication().getResources().getIdentifier("qs_background_primary", "drawable", lpparam.packageName);

                quality=sharedPreferences.getInt(Conf.QUALITY,LOWEST);//初始化

                isGaoSi=sharedPreferences.getBoolean(Conf.GAO_SI,false);

                gaoValue=sharedPreferences.getInt(Conf.GAO_SI_VALUE,25);

                if (color != -1) {

                    fullView.setBackgroundColor(color);
                    fullView.getBackground().setAlpha(alphaValue);

                } else {

                    if(isGaoSi) {
                        setGaoSiImage();
                    }else {
                        setBg();
                    }
                }

            }
        });
    }

    private void registerBroadcast() {

        if (fullReceiver == null) {
            fullReceiver = new FullReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ReceiverAction.FULL_SEND_ALBUM);
            intentFilter.addAction(ReceiverAction.FULL_GET_INFO);
            intentFilter.addAction(ReceiverAction.HEADER_SEND_ALPHA);
            intentFilter.addAction(ReceiverAction.HEADER_SEND_FLOAT);
            intentFilter.addAction(ReceiverAction.FULL_DELETE_ALBUM);
            intentFilter.addAction(ReceiverAction.FULL_SEND_COLOR);
            intentFilter.addAction(ReceiverAction.SEND_QUALITY);
            intentFilter.addAction(ReceiverAction.SEND_GAO_SI);
            intentFilter.addAction(ReceiverAction.SEND_GAO_VALUE);
            intentFilter.addAction(ReceiverAction.SEND_SINGLE_HEIGHT);
            AndroidAppHelper.currentApplication().registerReceiver(fullReceiver, intentFilter);
        }
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

                case ReceiverAction.FULL_SEND_ALBUM:

                    setFullImage(intent, context);

                    break;

                case ReceiverAction.FULL_GET_INFO:
                    sendViewInfo(context);
                    break;

                case ReceiverAction.HEADER_SEND_ALPHA://接收透明度

                    setAlpha(intent);

                    break;

                case ReceiverAction.HEADER_SEND_FLOAT://接收下拉距离

                    changeAlpha(intent);

                    break;

                case ReceiverAction.FULL_DELETE_ALBUM:

                    deleteBg();

                    break;

                case ReceiverAction.FULL_SEND_COLOR://设置颜色

                    setColor(intent);

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

                case ReceiverAction.SEND_SINGLE_HEIGHT:

                    getHeight(intent);
                    break;


            }

        }
    }

    private void setFullImage(Intent intent, Context context) {


        String s = intent.getStringExtra(Conf.FULL_FILE);


        if (s == null||s.isEmpty()) {
            return;
        }

        Uri uri = Uri.parse(s);

        if (uri == null) {
            return;
        }
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));

            if (bitmap != null) {

                String path = context.getFilesDir().getAbsolutePath();

                File file = new File(path + "/" + Conf.FULL_FILE);

                FileOutputStream outputStream = new FileOutputStream(file);

                bitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream);

                if (isGaoSi){//高斯模糊方式
                    setGaoSiImage();
                }else {//普通方式
                    setBg();
                }

                Toast.makeText(AndroidAppHelper.currentApplication(), "设置成功", Toast.LENGTH_SHORT).show();

            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * 发送view的长和宽
     *
     * @param context
     */
    private void sendViewInfo(Context context) {

        Intent intent = new Intent(ReceiverAction.FULL_SEND_INFO);

        int w = fullView.getWidth();

        int height=sharedPreferences.getInt(Conf.HEIGHT,-1);

        Rect rect = new Rect();
        rect.setHeight(height);
        rect.setWidth(w);

        intent.putExtra(Conf.FULL_INFO, rect);


        context.sendBroadcast(intent);

    }

    /**
     * 接收透明度
     *
     * @param intent
     */
    private void setAlpha(Intent intent) {


        int value = intent.getIntExtra(Conf.ALPHA, -1);

        if (value < 0) {
            return;
        }

        alphaValue = value;//赋值

        sharedPreferences.edit().putInt(Conf.ALPHA, alphaValue).apply();//保存


        fullView.getBackground().setAlpha(alphaValue);//更新

    }

    /**
     * 根据下拉程度改透明度
     *
     * @param intent
     */
    private void changeAlpha(Intent intent) {

        float f = intent.getFloatExtra(Conf.DROP_FLOAT, -0.1f);

        if (f == 1) {//完全下拉状态，保存高度
            if (height <= 0) {

                height = fullView.getHeight();

                sharedPreferences.edit().putInt(Conf.HEIGHT, height).apply();//保存
            }
        }


        if (fullView.getBackground() == null) {


            return;
        }


        int color = sharedPreferences.getInt(Conf.FULL_COLOR, -1);


        if (getFile().exists() || color != -1) {

            if (f < 0 || f > 1) {
                return;
            }

            float alpha = f * alphaValue;

            if (alpha > alphaValue) {
                alpha = alphaValue;
            }

            if (alpha < 0) {
                alpha = 0;
            }

            fullView.getBackground().setAlpha((int) alpha);


            if (f == 1) {//完全下拉
                fullView.getBackground().setAlpha(alphaValue);
            } else if (f == 0) {//完全收缩

                fullView.getBackground().setAlpha(0);
            }
        }
    }

    /**
     * 删除背景
     */
    private void deleteBg() {

        int color=sharedPreferences.getInt(Conf.FULL_COLOR,-1);

        if (fullView.getBackground()==null){
            return;
        }

        if (getFile().exists()||color!=-1) {
            fullView.setBackground(getDefaultDrawable());

            sendSetFull(false);

            Toast.makeText(AndroidAppHelper.currentApplication(), "背景已清除", Toast.LENGTH_SHORT).show();
        }

        deleteFile();//删除文件

        sharedPreferences.edit().remove(Conf.FULL_COLOR).apply();//删除保存的颜色


    }

    private Drawable getDefaultDrawable() {

        if (defaultDrawable == 0) {
            return null;
        }
        return AndroidAppHelper.currentApplication().getDrawable(defaultDrawable);

    }

    private void setColor(Intent intent) {

        int color = intent.getIntExtra(Conf.FULL_COLOR, -1);
        if (color == -1) {
            return;
        }

        deleteFile();//删除文件

        fullView.setBackgroundColor(color);
        fullView.getBackground().setAlpha(alphaValue);
        sharedPreferences.edit().putInt(Conf.FULL_COLOR, color).apply();//保存

        sendSetFull(true);

        Toast.makeText(AndroidAppHelper.currentApplication(), "设置成功", Toast.LENGTH_SHORT).show();
    }

    /**
     * 设置背景
     * 寻找背景图，如果不存在，则设置默认背景
     */
    private void setBg() {

        if (this.fullView == null) {
            return;
        }

        String path = AndroidAppHelper.currentApplication().getFilesDir().getAbsolutePath();

        File file = new File(path + "/" + Conf.FULL_FILE);

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

            XposedBridge.log("size---full-->>"+bitmap.getByteCount());

            fullView.setBackground(new BitmapDrawable(AndroidAppHelper.currentApplication().getResources(), bitmap));

            fullView.getBackground().setAlpha(alphaValue);

            sendSetFull(true);

        } else {

            fullView.setBackground(getDefaultDrawable());
            fullView.getBackground().setAlpha(255);//恢复背景透明度

            sendSetFull(false);
        }

    }

    private int saveBitmap(Bitmap bitmap) {

        String path = AndroidAppHelper.currentApplication().getFilesDir().getAbsolutePath();

        int result = -1;

        File file = new File(path + "/" + Conf.FULL_FILE);

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
     * 接收质量
     * @param intent
     */
    private void setQuality(Intent intent) {

        this.quality = intent.getIntExtra(Conf.QUALITY, LOWEST);

        setBg();//再次设置背景图

        sharedPreferences.edit().putInt(Conf.QUALITY, quality).apply();

    }

    /**
     * 删除文件
     * @return
     */
    private boolean deleteFile() {

        String path = AndroidAppHelper.currentApplication().getFilesDir().getAbsolutePath();

        File file = new File(path + "/" + Conf.FULL_FILE);

        return file.delete();
    }

    /**
     * 接收是否高斯模糊
     * @param intent
     */
    private void setGaoSi(Intent intent){

        this.isGaoSi=intent.getBooleanExtra(Conf.GAO_SI,false);

        sharedPreferences.edit().putBoolean(Conf.GAO_SI,isGaoSi).apply();

        if (isGaoSi){

            setGaoSiImage();//设置高斯模糊

        }else {

            removeGaosi();//移除高斯模糊
        }

    }

    /**
     * 接收高斯模糊半径
     * @param intent
     */
    private void setGaoSiValue(Intent intent){

        this.gaoValue=intent.getIntExtra(Conf.GAO_SI_VALUE,25);

        sharedPreferences.edit().putInt(Conf.GAO_SI_VALUE,gaoValue).apply();

        setGaoSiImage();//设置高斯模糊

    }

    /**
     * 设置高斯模糊图
     */
    private void setGaoSiImage(){

        File file=getFile();
        if (!file.exists()||fullView==null){//如果文件不存在，则不继续
            return;
        }

        Bitmap bitmap=BitmapFactory.decodeFile(file.getAbsolutePath());

        int value=sharedPreferences.getInt(Conf.GAO_SI_VALUE,25);

        bitmap=getBitmap(AndroidAppHelper.currentApplication(),bitmap,value);

        fullView.setBackground(bitmap2Drawable(bitmap));

        sendSetFull(true);
    }

    /**
     * 移除高斯模糊效果
     */
    private void removeGaosi(){

        setBg();
    }

    private File getFile(){

        String path=AndroidAppHelper.currentApplication().getFilesDir().getAbsolutePath();

        return new File(path+"/"+Conf.FULL_FILE);

    }

    public static Bitmap getBitmap(Context context, Bitmap source, int radius){

        if (radius<=0){
            radius=1;
        }

        if (radius>25){
            radius=25;
        }

        Bitmap bitmap=source;
        RenderScript renderScript=RenderScript.create(context);

        final Allocation input=Allocation.createFromBitmap(renderScript,bitmap);

        final Allocation output=Allocation.createTyped(renderScript,input.getType());

        ScriptIntrinsicBlur scriptIntrinsicBlur=ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));

        scriptIntrinsicBlur.setInput(input);

        scriptIntrinsicBlur.setRadius(radius);

        scriptIntrinsicBlur.forEach(output);

        output.copyTo(bitmap);

        renderScript.destroy();

        return bitmap;
    }

    private Drawable bitmap2Drawable(Bitmap bitmap){

        return new BitmapDrawable(AndroidAppHelper.currentApplication().getResources(),bitmap);

    }

    /**
     * 向头部发出提示
     * @param b
     */
    private void sendSetFull(boolean b){

        Intent intent=new Intent(ReceiverAction.SEND_SET_FULL);

        intent.putExtra(Conf.SET_FULL,b);

        AndroidAppHelper.currentApplication().sendBroadcast(intent);
    }

    private void getHeight(Intent intent){

        this.height=intent.getIntExtra(Conf.HEIGHT,-1);

        sharedPreferences.edit().putInt(Conf.HEIGHT,height).apply();//保存

        Toast.makeText(AndroidAppHelper.currentApplication(), "已设置自定义高度", Toast.LENGTH_SHORT).show();

    }

}
