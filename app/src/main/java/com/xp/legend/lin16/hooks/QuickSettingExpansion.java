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

public class QuickSettingExpansion implements IXposedHookLoadPackage {

    private static final String METHOD="setExpansion";
    private static final String CLASS2="com.android.systemui.qs.QSContainerImpl";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!isO()){
            return;
        }

        if (!lpparam.packageName.equals("com.android.systemui")){
            return;
        }

        XposedHelpers.findAndHookMethod(CLASS2, lpparam.classLoader, METHOD, float.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                float f=XposedHelpers.getFloatField(param.thisObject,"mQsExpansion");


                Intent intent=new Intent(ReceiverAction.SEND_O_FLOAT);

                intent.putExtra(Conf.N_EXPAND_VALUE,f);

                AndroidAppHelper.currentApplication().sendBroadcast(intent);


            }
        });

    }

    private boolean isO() {

        return Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1;
    }
}
