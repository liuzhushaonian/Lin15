package com.xp.legend.lin16.hooks;


import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;

import com.xp.legend.lin16.utils.ReceiverAction;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class IconsHandlerHook implements IXposedHookLoadPackage {

    private static final String CLASS = "com.android.launcher3.allapps.AlphabeticalAppsList";
    private static final String METHOD = "refillAdapterItems";
    private List list;
    private List hideList;
    private int c = 0;
    private HideAppReceiver receiver;
    private Object object;
    private SharedPreferences sharedPreferences;


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!isO()){
            return;
        }

        if (!lpparam.packageName.equals("org.lineageos.trebuchet")) {

            return;
        }

        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                list= (List) XposedHelpers.getObjectField(param.thisObject,"mAdapterItems");

                if (hideList==null){
                    hideList=new ArrayList();
                }

                initHide();
            }
        });

        XposedBridge.hookAllConstructors(lpparam.classLoader.loadClass(CLASS), new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                if (receiver == null) {

                    receiver = new HideAppReceiver();
                    IntentFilter intentFilter = new IntentFilter();

                    intentFilter.addAction(ReceiverAction.HIDE_APP);

                    intentFilter.addAction(ReceiverAction.SHOW_APP);

                    AndroidAppHelper.currentApplication().registerReceiver(receiver, intentFilter);

                    object = param.thisObject;//获取实例

                    sharedPreferences=AndroidAppHelper.currentApplication().getSharedPreferences("launch_shared",Context.MODE_PRIVATE);

                }

            }
        });


    }

    private boolean isO() {

        return Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1;
    }

    class HideAppReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }

            switch (action) {

                case ReceiverAction.HIDE_APP://隐藏应用



                    hideApps(intent);

                    break;

                case ReceiverAction.SHOW_APP:

                    resume(intent);

                    break;
            }
        }
    }

    private void initHide(){

        String hide=sharedPreferences.getString("hide_apps","");

        if (hide.isEmpty()){
            return;
        }

        String[] hides=hide.split("-");

        for (String name:hides){

            hideApps(name);
        }


    }

    private void hideApps(Intent intent){

        String name=intent.getStringExtra("hide_name");

        hideApps(name);
    }


    private void hideApps(String packName) {

        if (object == null||packName==null) {
            return;
        }

        if (list == null) {
            return;
        }

        for (int i = 0; i < list.size(); i++) {


            Object item = list.get(i);

            if (item==null){
                return;
            }

            Object app = XposedHelpers.getObjectField(item, "appInfo");

            if (app!=null) {


                ComponentName componentName = (ComponentName) XposedHelpers.getObjectField(app, "componentName");

                String name = componentName.getPackageName();

                if (packName.equals(name)) {




                    list.remove(item);//移除它

                    hideList.add(item);



                    //刷新

                   notifyData();

                }
            }

        }


    }

    private void resume(Intent intent){

        String name=intent.getStringExtra("show_name");

        showApps(name);

    }

    private void showApps(String packName){

        if (list==null||hideList==null||packName==null||packName.isEmpty()){
            return;
        }

        for (int i = 0; i < hideList.size(); i++) {


            Object item = hideList.get(i);

            if (item==null){
                return;
            }

            Object app = XposedHelpers.getObjectField(item, "appInfo");

            if (app!=null) {


                ComponentName componentName = (ComponentName) XposedHelpers.getObjectField(app, "componentName");

                String name = componentName.getPackageName();

                if (packName.equals(name)) {


                    list.add(item);

                    hideList.remove(item);

                    //刷新

                    notifyData();

                }
            }

        }
    }

    private void notifyData(){

        Intent intent = new Intent(ReceiverAction.ADAPTER_REGISTER);

        AndroidAppHelper.currentApplication().sendBroadcast(intent);

    }

}
