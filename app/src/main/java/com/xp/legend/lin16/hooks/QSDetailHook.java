package com.xp.legend.lin16.hooks;


import android.app.AndroidAppHelper;
import android.content.Intent;
import android.content.res.Configuration;
import com.xp.legend.lin16.utils.ReceiverAction;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class QSDetailHook implements IXposedHookLoadPackage{

    private static final String CLASS="com.android.systemui.qs.QSPanel";

    private static final String METHOD="onConfigurationChanged";


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam){

        if (!lpparam.packageName.equals("com.android.systemui")) {
            return;
        }

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD, Configuration.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {


                super.afterHookedMethod(param);


                Configuration configuration= (Configuration) param.args[0];

                    Intent intent=new Intent(ReceiverAction.SEND_ORI);

                    intent.putExtra("ori",configuration.orientation);

                XposedBridge.log("lin16----->>>旋转屏幕了");

                    AndroidAppHelper.currentApplication().sendBroadcast(intent);

            }
        });

    }
}
