package com.xp.legend.lin15.hooks;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QSFragmentHook implements IXposedHookLoadPackage {

    private static final String METHOD="onViewCreated";
    private static final String CLASS2="com.android.systemui.qs.QSFragment";


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("com.android.systemui")){
            return;
        }

        XposedHelpers.findAndHookMethod(CLASS2, lpparam.classLoader, METHOD, View.class, Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                Fragment fragment= (Fragment) param.thisObject;

                fragment.getView().setBackgroundColor(Color.RED);



            }
        });

    }
}
