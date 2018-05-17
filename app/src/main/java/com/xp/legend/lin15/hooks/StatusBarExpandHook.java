package com.xp.legend.lin15.hooks;

import android.app.AndroidAppHelper;
import android.content.Intent;


import com.xp.legend.lin15.utils.ReceiverAction;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class StatusBarExpandHook implements IXposedHookLoadPackage {

    private static final String METHOD="setExpanded";
    private static final String CLASS2="com.android.systemui.qs.QuickStatusBarHeader";
    private boolean expanded=true;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {


        if (!lpparam.packageName.equals("com.android.systemui")){
            return;
        }
        XposedHelpers.findAndHookMethod(CLASS2, lpparam.classLoader, METHOD,boolean.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {




                boolean b= (boolean) XposedHelpers.getObjectField(param.thisObject,"mExpanded");

                if (expanded==b){

                    return;
                }

                expanded=b;//保存状态

                Intent intent=new Intent(ReceiverAction.HEADER_SEND_EXPANDED);

                if (b){

                    intent.putExtra("expanded",1);
                }else {
                    intent.putExtra("expanded",0);

                }

                AndroidAppHelper.currentApplication().sendBroadcast(intent);

                XposedBridge.log("lin15--->>>get the expanded");

            }
        });



    }
}
