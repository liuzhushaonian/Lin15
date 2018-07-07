package com.xp.legend.lin15.hooks;

import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import com.xp.legend.lin15.utils.ReceiverAction;

import java.lang.reflect.Method;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * 关闭流量无需确认
 */
public class DataSaverHook implements IXposedHookLoadPackage {


    private static final String CLASS="com.android.systemui.qs.tiles.CellularTile";
    private static final String METHOD="handleClick";
    private static final String DATA_SETTING="data_setting";
//    private DataReceiver receiver;
//    private boolean set=false;



    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {


        if (!lpparam.packageName.equals("com.android.systemui")){
            return;
        }


//        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "createTileView", Context.class, new XC_MethodHook() {
//            @Override
//            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
//                super.beforeHookedMethod(param);
//
//                if (receiver==null){//注册
//                    receiver=new DataReceiver();
//                    IntentFilter intentFilter=new IntentFilter();
//                    intentFilter.addAction(ReceiverAction.DATA_SETTING);
//                    AndroidAppHelper.currentApplication().registerReceiver(receiver,intentFilter);
//
//                    XposedBridge.log("register------>>>DataReceiver");
//                }
//            }
//        });



        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, "showDisableDialog", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {


                SharedPreferences sharedPreferences=AndroidAppHelper.currentApplication().getSharedPreferences(ReceiverAction.SS,Context.MODE_PRIVATE);

//                set=sharedPreferences.getBoolean("data",false);
//
//                if (!set){
//
//
//                    return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
//                }

                Object object=XposedHelpers.getObjectField(param.thisObject,"mDataController");

                Method method=object.getClass().getDeclaredMethod("setMobileDataEnabled",boolean.class);

                method.invoke(object,false);



                return null;
            }
        });

    }

//    public class DataReceiver extends BroadcastReceiver{
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String action=intent.getAction();
//
//            if (action==null){
//                return;
//            }
//
//            switch (action){
//                case ReceiverAction.DATA_SETTING:
//
//                    set=intent.getBooleanExtra("data",false);
//
//                    SharedPreferences sharedPreferences=AndroidAppHelper.currentApplication().getSharedPreferences(ReceiverAction.SS,Context.MODE_PRIVATE);
//
//                    sharedPreferences.edit().putBoolean("data",set).apply();//保存
//
//                    break;
//            }
//        }
//    }
}
