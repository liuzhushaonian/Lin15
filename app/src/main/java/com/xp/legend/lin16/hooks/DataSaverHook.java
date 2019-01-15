package com.xp.legend.lin16.hooks;


import android.os.Build;
import java.lang.reflect.Method;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 关闭流量无需确认
 */
public class DataSaverHook implements IXposedHookLoadPackage {


    private static final String CLASS="com.android.systemui.qs.tiles.CellularTile";


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!isO()){
            return;
        }


        if (!lpparam.packageName.equals("com.android.systemui")){
            return;
        }


        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "showDisableDialog", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {


//                SharedPreferences sharedPreferences=AndroidAppHelper.currentApplication().getSharedPreferences(ReceiverAction.SS,Context.MODE_PRIVATE);


                Object object=XposedHelpers.getObjectField(param.thisObject,"mDataController");

                Method method=object.getClass().getDeclaredMethod("setMobileDataEnabled",boolean.class);

                method.invoke(object,false);

                return null;
            }
        });

    }


    /**
     * 判断是否是O系列
     *
     * @return
     */
    private boolean isO() {

        return Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1;
    }


}
