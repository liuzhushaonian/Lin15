package com.xp.legend.lin16.hooks;

import android.content.Context;
import android.graphics.Color;

import com.xp.legend.lin16.utils.BaseHook;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QSTileBaseViewHook extends BaseHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("com.android.systemui")){
            return;
        }

//        XposedHelpers.findAndHookConstructor("com.android.systemui.qs.tileimpl.QSTileBaseView", lpparam.classLoader,
//                Context.class, "com.android.systemui.plugins.qs.QSIconView", boolean.class, new XC_MethodHook() {
//                    @Override
//                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                        super.afterHookedMethod(param);
//
//
//
//                        XposedHelpers.setIntField(param.thisObject,"mColorActive", Color.RED);
//
//                        XposedBridge.log("lin16---->>替换颜色");
//
//                    }
//                });


        XposedHelpers.findAndHookMethod("com.android.systemui.qs.tileimpl.QSTileBaseView",
                lpparam.classLoader, "getCircleColor", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                int s= (int) param.args[0];

                XposedBridge.log("sss------->>>>"+s);

                if (s==2) {

                    param.setResult(Color.GREEN);
                }

            }
        });

    }
}
