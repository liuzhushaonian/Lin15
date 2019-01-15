package com.xp.legend.lin16.hooks;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MyselfHook implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (lpparam.packageName.equals("com.xp.legend.lin16")){

            XposedHelpers.findAndHookMethod("com.xp.legend.lin16.activity.MainActivity",
                    lpparam.classLoader,"isModuleActive",
                    XC_MethodReplacement.returnConstant(true));

        }
    }


}
