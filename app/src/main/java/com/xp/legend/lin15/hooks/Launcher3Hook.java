package com.xp.legend.lin15.hooks;

import android.app.Activity;
import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.xp.legend.lin15.utils.Conf;
import com.xp.legend.lin15.utils.ReceiverAction;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class Launcher3Hook implements IXposedHookLoadPackage {



    private static final String CLASS="com.android.launcher3.Launcher";

    private static final String ON_CREATE="onCreate";

    private static final String ON_DESTROY="onDestroy";

    private HideReciver reciver;

    private SharedPreferences sharedPreferences;

    private static final String SHARED = "launch_shared";
    private static final String PASS = "pass_hide_icon";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("org.lineageos.trebuchet")) {
            return;
        }


        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, ON_CREATE, Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                Activity activity= (Activity) param.thisObject;

                sharedPreferences = activity.getSharedPreferences(SHARED, Context.MODE_PRIVATE);

                registerRevicer();

            }
        });


        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, ON_DESTROY, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                if (reciver!=null){
                    AndroidAppHelper.currentApplication().unregisterReceiver(reciver);
                }

            }
        });


    }

    private void registerRevicer(){
        if (this.reciver==null){

            this.reciver=new HideReciver();

            IntentFilter intentFilter=new IntentFilter();
            intentFilter.addAction(ReceiverAction.SHOW_SYSTEM);
            intentFilter.addAction(ReceiverAction.GET_PASS);
            intentFilter.addAction(ReceiverAction.SEND_NEW_PASS);

            AndroidAppHelper.currentApplication().registerReceiver(reciver,intentFilter);
        }

    }


    class HideReciver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent==null){
                return;
            }

            String action=intent.getAction();

            if (action==null){
                return;
            }

            switch (action){
                case ReceiverAction.SHOW_SYSTEM:

                    setShowSystem(intent);
                    break;
                case ReceiverAction.GET_PASS://获取旧密码

                    sendInfo(intent);

                    break;

                case ReceiverAction.SEND_NEW_PASS://修改密码

                    changePass(intent);

                    break;
            }
        }
    }

    private void setShowSystem(Intent intent){

        boolean isShowSystem=intent.getBooleanExtra(Conf.SHOW_SYSTEM,false);

        sharedPreferences.edit().putBoolean(Conf.SHOW_SYSTEM,isShowSystem).apply();//保存
    }

    private void sendInfo(Intent intent){

        String old_pass=intent.getStringExtra(Conf.OLD_PASS);

        String con_pass = getMd5(old_pass);

        if (con_pass == null) {
            sendPassInfo(false);

            return;
        }

        String save_pass = sharedPreferences.getString(PASS, "");

        if (con_pass.equals(save_pass)) {//密码校验正确

            sendPassInfo(true);
        } else {

            sendPassInfo(false);

        }

    }

    private void sendPassInfo(boolean b){

        Intent intent=new Intent(ReceiverAction.SEND_PASS_INFO);

        intent.putExtra(Conf.PASS_INFO,b);

        AndroidAppHelper.currentApplication().sendBroadcast(intent);
    }

    private void changePass(Intent intent){

        String pass=intent.getStringExtra(Conf.NEW_PASS);

        if (TextUtils.isEmpty(pass)){

            Toast.makeText(AndroidAppHelper.currentApplication(), "密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        sharedPreferences.edit().putString(PASS,pass).apply();//保存新密码

    }



    //md5加密改名
    private String getMd5(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            //32位加密
            return buf.toString();
            // 16位的加密
            //return buf.toString().substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

    }

}
