package com.xp.legend.lin15.hooks;

import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.xp.legend.lin15.utils.ReceiverAction;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AllAppsGridAdapterHook implements IXposedHookLoadPackage {

    private static final String CLASS = "com.android.launcher3.allapps.AllAppsGridAdapter";

    private AdapterReceiver receiver;
    private Object adapter;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("org.lineageos.trebuchet")) {
            return;
        }

        XposedBridge.hookAllConstructors(lpparam.classLoader.loadClass(CLASS), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                if (receiver == null) {

                    receiver = new AdapterReceiver();
                    IntentFilter intentFilter = new IntentFilter();

                    intentFilter.addAction(ReceiverAction.ADAPTER_REGISTER);

                    AndroidAppHelper.currentApplication().registerReceiver(receiver, intentFilter);

                    adapter = param.thisObject;

                }
            }
        });

    }

    class AdapterReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }

            switch (action) {
                case ReceiverAction.ADAPTER_REGISTER:

                    notifyData();

                    break;

            }
        }
    }

    private void notifyData() {

        if (adapter == null) {
            return;
        }

        try {
            Method method = adapter.getClass().getMethod("notifyDataSetChanged");

            method.invoke(adapter);


        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

    }


}
