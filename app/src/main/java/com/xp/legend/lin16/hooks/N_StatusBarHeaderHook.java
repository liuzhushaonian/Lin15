package com.xp.legend.lin16.hooks;

import android.app.AndroidAppHelper;
import android.content.Intent;
import android.os.Build;

import com.xp.legend.lin16.utils.Conf;
import com.xp.legend.lin16.utils.ReceiverAction;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 *
 * 获取上下拉的高度，用于变化图片透明度
 */
public class N_StatusBarHeaderHook implements IXposedHookLoadPackage {

    private static final String CLASS="com.android.systemui.statusbar.phone.QuickStatusBarHeader";

    private static final String METHOD="setExpansion";


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!isN()){
            return;
        }

        if (!lpparam.packageName.equals("com.android.systemui")){
            return;
        }


        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD, float.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                float f=XposedHelpers.getFloatField(param.thisObject,"mExpansionAmount");

                Intent intent=new Intent(ReceiverAction.N_GET_EXPANSION_FLOAT);

                intent.putExtra(Conf.N_EXPAND_VALUE,f);

                AndroidAppHelper.currentApplication().sendBroadcast(intent);


            }
        });

    }

    private boolean isN() {

        return Build.VERSION.SDK_INT == Build.VERSION_CODES.N || Build.VERSION.SDK_INT == Build.VERSION_CODES.N_MR1;
    }


}
