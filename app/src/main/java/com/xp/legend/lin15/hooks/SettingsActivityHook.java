package com.xp.legend.lin15.hooks;

import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import java.util.Arrays;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SettingsActivityHook implements IXposedHookLoadPackage,IXposedHookInitPackageResources {

    private static final String CLASS="com.android.launcher3.SettingsActivity$LauncherSettingsFragment";
    private static final String METHOD="onCreate";
    private SettingReceiver settingReceiver;



    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("org.lineageos.trebuchet")){

//            XposedBridge.log("lll--sss-->>"+ lpparam.packageName);
            return;
        }



//        XposedHelpers.findAndHookConstructor(CLASS, lpparam.classLoader, Bundle.class, new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//
//                XposedBridge.log("lll--sss-->>"+ Arrays.toString(param.args));
//
////                if (settingReceiver==null){
////
////                    settingReceiver=new SettingReceiver();
////                    IntentFilter intentFilter=new IntentFilter();
////
////                    AndroidAppHelper.currentApplication().registerReceiver(settingReceiver,intentFilter);
////                }
//
//            }
//        });

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD, Bundle.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {


                XposedBridge.log("lll--args-->>"+ Arrays.toString(param.args));

                return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
            }
        });



    }

    /**
     * 修改资源
     * @param resparam
     * @throws Throwable
     */
    @Override
    public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {


    }

    class SettingReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}
