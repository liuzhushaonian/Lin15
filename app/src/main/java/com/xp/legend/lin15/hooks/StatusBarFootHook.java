package com.xp.legend.lin15.hooks;

import android.app.AndroidAppHelper;
import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import com.xp.legend.lin15.App;
import com.xp.legend.lin15.R;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class StatusBarFootHook implements IXposedHookLoadPackage {

    /**
     * 终于找到了~~~~
     *
     * 这个com.android.systemui.qs.QuickStatusBarHeader是下拉菜单的头部，也就是上半部分
     *
     */
    private static final String METHOD="createDialog";
    private static final String CLASS2="com.android.systemui.qs.QSSecurityFooter";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals("com.android.systemui")){
            XposedBridge.log("lll------>>>测试代码2"+lpparam.packageName);

            return;
        }
        XposedHelpers.findAndHookMethod(CLASS2, lpparam.classLoader, METHOD, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {


                XposedBridge.log("lll------>>>测试代码4");

//                View view= (View) param.thisObject;
//
                View view= (View) XposedHelpers.getObjectField(param.thisObject,"dialogView");



//
//                view.setBackgroundColor(Color.RED);
//
                XposedBridge.log("lll------>>>5"+view.toString());
            }
        });

    }

}
