package com.xp.legend.lin15.hooks;


import java.lang.reflect.Method;
import java.util.List;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class IconsHandlerHook implements IXposedHookLoadPackage {

    private static final String CLASS="com.android.launcher3.allapps.AlphabeticalAppsList";
    private static final String METHOD="getAdapterItems";


    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals("org.lineageos.trebuchet")){

            return;
        }


//        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD,new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//
////                Class clazz=lpparam.classLoader.loadClass("com.android.launcher3.allapps.AlphabeticalAppsList$AdapterItem");
//
//
//                List list= (List) param.getResult();
//
//                Object object=list.get(10);
//
//
////                XposedBridge.log("lll--size->>"+list.size()+"--->>"+object.toString());
//
//
////                XposedBridge.log("lll--size->>"+list.size()+"\nlist---->>"+list.toString());
//
//            }
//        });


    }
}
