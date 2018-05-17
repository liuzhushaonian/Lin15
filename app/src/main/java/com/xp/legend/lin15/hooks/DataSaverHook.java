package com.xp.legend.lin15.hooks;

import java.lang.reflect.Method;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class DataSaverHook implements IXposedHookLoadPackage {


    private static final String CLASS="com.android.systemui.qs.tiles.CellularTile";
    private static final String METHOD="handleClick";



    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {


        if (!lpparam.packageName.equals("com.android.systemui")){
            return;
        }

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

            }
        });
        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "showDisableDialog", new XC_MethodReplacement() {
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
