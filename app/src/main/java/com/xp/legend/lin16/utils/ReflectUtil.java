package com.xp.legend.lin16.utils;

import android.content.Context;

public class ReflectUtil {

    private static int getResourceId(Context context, String name, String type) {
        int id = 0;
        id = context.getResources().getIdentifier(name, type, context.getPackageName());
        return id;
    }

    public static int getViewId(Context context, String name) {
        return getResourceId(context, name, "id");
    }

    public static int getLayoutId(Context context, String name) {
        return getResourceId(context, name, "layout");
    }

    public static int getStringId(Context context, String name) {
        return getResourceId(context, name, "string");
    }

    public static int getDrawableId(Context context, String name) {
        return getResourceId(context, name, "drawable");
    }

    public static int getStyleId(Context context, String name) {
        return getResourceId(context, name, "style");
    }

    public static int getDimenId(Context context, String name) {
        return getResourceId(context, name, "dimen");
    }

    public static int getArrayId(Context context, String name) {
        return getResourceId(context, name, "array");
    }

    public static int getColorId(Context context, String name) {
        return getResourceId(context, name, "color");
    }

    public static int getAnimId(Context context, String name) {
        return getResourceId(context, name, "anim");
    }

    public static boolean isClassFounded(String className)
    {
        try {
            @SuppressWarnings("unused")
            Class<?> aClass = Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Object getObjectByClassName(String className)
    {
        try {
            Class<?> aClass = Class.forName(className);
            return aClass.newInstance();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}