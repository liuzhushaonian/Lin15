package com.xp.legend.lin16.hooks;

import com.xp.legend.lin16.utils.BaseHook;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 移动数据按钮hook，取消关闭确认
 */
public class CellularTileHook extends BaseHook implements IXposedHookLoadPackage {

    private static final String CLASS="com.android.systemui.CellularTile";

    private static final String METHOD="maybeShowDisableDialog";



    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!isP()){
            return;
        }

        if (!lpparam.packageName.equals("com.android.systemui")){
            return;
        }

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {

                Object object=XposedHelpers.getObjectField(param.thisObject,"mDataController");

                Method method=object.getClass().getDeclaredMethod("setMobileDataEnabled",boolean.class);

                method.invoke(object,false);

                return null;
            }
        });

    }
}
