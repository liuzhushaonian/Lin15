package com.xp.legend.lin15.hooks;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AndroidAppHelper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.renderscript.ScriptGroup;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xp.legend.lin15.utils.Conf;
import com.xp.legend.lin15.utils.ReceiverAction;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class SettingsActivityHook implements IXposedHookLoadPackage {

    private static final String CLASS = "com.android.launcher3.SettingsActivity$LauncherSettingsFragment";
    private static final String METHOD = "onCreate";

    private SharedPreferences sharedPreferences;
    private static final String SHARED = "launch_shared";
    private static final String PASS = "pass_hide_icon";
    private LinearLayoutManager linearLayoutManager;

    private List<PackInfo> hideList = new ArrayList<>();//已隐藏的列表

    private PreferenceFragment fragment;

    private boolean isShowSystem=false;




    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!isO()){
            return;
        }


        if (!lpparam.packageName.equals("org.lineageos.trebuchet")) {


            return;
        }


        XposedHelpers.findAndHookMethod(CLASS, lpparam.classLoader, METHOD, Bundle.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);


                fragment = (PreferenceFragment) param.thisObject;

                PreferenceScreen preferenceScreen = fragment.getPreferenceScreen();

                Preference preference = new Preference(fragment.getContext());

                preference.setKey("hide_icon");
                preference.setTitle("隐藏应用");
                preference.setSummary("隐藏那些……你懂的~");

                preferenceScreen.addPreference(preference);

                preference.setOnPreferenceClickListener(preference1 -> {

                    checkPassIfExits(fragment.getActivity());

                    return true;
                });

                sharedPreferences = fragment.getActivity().getSharedPreferences(SHARED, Context.MODE_PRIVATE);

            }
        });

    }

    private boolean isO() {

        return Build.VERSION.SDK_INT == Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1;
    }



    /**
     * 需要密码或是设置密码
     */
    private void checkPassIfExits(Activity activity) {

        String pass = sharedPreferences.getString(PASS, "");

        if (pass.isEmpty()) {//如果密码是空的，设置密码


            showSettingPassDialog(activity);

        } else {//密码不是空的，弹出输入框输入密码

            showConfirmPassDialog(activity);

        }

    }

    /**
     * 设置密码界面
     */
    private void showSettingPassDialog(Activity activity) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        EditText editText = new EditText(activity);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        builder.setPositiveButton("下一步", (dialogInterface, i) -> {

            String pass = editText.getText().toString();

            if (pass.isEmpty()) {
                return;
            }

            confirmPassDialog(activity, pass);
        });

        builder.setView(editText).setTitle("请设置密码");


        builder.show();

    }

    /**
     * 确认密码
     *
     * @param activity
     * @param pass
     */
    private void confirmPassDialog(Activity activity, String pass) {


        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        EditText editText = new EditText(activity);
        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        builder.setPositiveButton("确认", (dialogInterface, i) -> {

            String confirm_pass = editText.getText().toString();


            if (pass.equals(confirm_pass)) {

                savePass(confirm_pass, activity);

            } else {

                Toast.makeText(activity, "密码不一致", Toast.LENGTH_SHORT).show();
            }

        });

        builder.setView(editText).setTitle("请确认密码");

        builder.show();

    }

    //md5加密改名
    private String getMd5(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            //32位加密
            return buf.toString();
            // 16位的加密
            //return buf.toString().substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * 保存密码
     *
     * @param pass
     */
    private void savePass(String pass, Activity activity) {

        String save_pass = getMd5(pass);

        sharedPreferences.edit().putString(PASS, save_pass).apply();//保存

        showHideApps(activity);

    }


    /**
     * 已有密码，弹出输入密码框
     *
     * @param activity
     */
    private void showConfirmPassDialog(Activity activity) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        EditText editText = new EditText(activity);

        editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        builder.setPositiveButton("确定", (dialogInterface, i) -> {
            String pass = editText.getText().toString();

            confirmPass(activity, pass);

        });

        builder.setView(editText).setTitle("请输入密码");

        builder.show();

    }

    /**
     * 输入密码并确认
     *
     * @param pass
     */
    private void confirmPass(Activity activity, String pass) {

        String con_pass = getMd5(pass);

        if (con_pass == null) {
            Toast.makeText(AndroidAppHelper.currentApplication(), "密码错误", Toast.LENGTH_SHORT).show();
            return;
        }

        String save_pass = sharedPreferences.getString(PASS, "");

        if (con_pass.equals(save_pass)) {

            showHideApps(activity);
        } else {

            Toast.makeText(AndroidAppHelper.currentApplication(), "密码错误", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 展示已隐藏APP
     *
     * @param activity
     */
    private void showHideApps(Activity activity) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);


        RecyclerView recyclerView = null;
//        if (linearLayoutManager==null){
        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView = new RecyclerView(activity);

        HideIconAdapter adapter = new HideIconAdapter(HideIconAdapter.HIDE_TYPE);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        int width = activity.getResources().getDisplayMetrics().widthPixels;
        int height = activity.getResources().getDisplayMetrics().heightPixels;

        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(width, height);

        recyclerView.setLayoutParams(layoutParams);

        builder.setNegativeButton("添加应用", (dialogInterface, i) -> {
            showAllAppsDialog(activity);
        });

        builder.setNeutralButton("退出", (dialogInterface, i) -> {
            builder.create().cancel();
        });

        builder.setTitle("已隐藏的APP(点击启动，长按取消隐藏)").setView(recyclerView).create().show();

        new Thread() {
            @Override
            public void run() {
                super.run();

                String hide = sharedPreferences.getString("hide_apps", "");

                String[] hides = hide.split("-");

                if (hides.length != 0) {//列表不为空的时候进行加载

                    List<PackageInfo> packageInfoList = activity.getPackageManager().getInstalledPackages(0);

                    List<PackInfo> packInfoList = new ArrayList<>();


                    for (String s : hides) {

                        for (int o = 0; o < packageInfoList.size(); o++) {

                            PackageInfo packageInfo = packageInfoList.get(o);

                            String name = packageInfo.packageName;

                            if (s.equals(name)) {//相同则添加

                                PackInfo packInfo = new PackInfo();
                                packInfo.setPackageInfo(packageInfo);

                                packInfoList.add(packInfo);

                            }

                        }

                    }

                    activity.runOnUiThread(() -> {//主线程设置

                        adapter.setPackageInfoList(packInfoList);

                    });
                }

            }
        }.start();

//        }
    }

    /**
     * 展示所有应用
     *
     * @param activity
     */
    private void showAllAppsDialog(Activity activity) {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        RecyclerView recyclerView = null;
//        if (linearLayoutManager==null){
        linearLayoutManager = new LinearLayoutManager(activity);
        recyclerView = new RecyclerView(activity);

        HideIconAdapter adapter = new HideIconAdapter(HideIconAdapter.ALL_TYPE);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);

        builder.setNeutralButton("退出", (dialogInterface, i) -> {
            builder.create().cancel();
        });

        builder.setNegativeButton("确认添加", (dialogInterface, i) -> {

            //添加应用到隐藏

            addToHide();

        });

        builder.setTitle("请选择需要隐藏的应用").setView(recyclerView).create().show();

        new Thread() {
            @Override
            public void run() {//获取所有应用
                super.run();

                PackageManager packageManager = activity.getPackageManager();

                List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);

                List<PackInfo> packInfoList = new ArrayList<>();

                String hide = sharedPreferences.getString("hide_apps", "");

                String[] hides = hide.split("-");

//                isShowSystem=sharedPreferences.getBoolean(Conf.SHOW_SYSTEM,false);


                for (String name : hides) {

                    for (int o = 0; o < packageInfos.size(); o++) {


//                        if (isShowSystem){
//
//
//                            PackageInfo packageInfo = packageInfos.get(o);
//
//                            if (name.equals(packageInfo.packageName)) {
//                                packageInfos.remove(packageInfo);
//                            }
//
//                        }else {

                            if (((packageInfos.get(o).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)) {//只要非系统APP

                                PackageInfo packageInfo = packageInfos.get(o);

                                if (name.equals(packageInfo.packageName)) {//选择非隐藏的应用
                                    packageInfos.remove(packageInfo);
                                }
                            }

//                        }
                    }
                }

                for (int o = 0; o < packageInfos.size(); o++){

                    if (((packageInfos.get(o).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)){

                        PackageInfo packageInfo=packageInfos.get(o);
                        PackInfo packInfo=new PackInfo();
                        packInfo.setPackageInfo(packageInfo);
                        packInfoList.add(packInfo);
                    }
                }


                activity.runOnUiThread(() -> {
                    adapter.setPackageInfoList(packInfoList);
                });

            }
        }.start();

    }

    /**
     * 将隐藏列表添加到隐藏结果里
     */
    private void addToHide() {

        if (hideList == null) {
            return;
        }

        for (PackInfo info : hideList) {//一个一个隐藏

            hideApp(info);
        }

        hideList.clear();

        hideList = null;

        Toast.makeText(AndroidAppHelper.currentApplication(), "添加成功", Toast.LENGTH_SHORT).show();

    }

    /**
     * 将选择的APP保存到本地，以包名的形式
     *
     * @param packInfo
     */
    private void hideApp(PackInfo packInfo) {

        if (packInfo == null) {
            return;
        }

        String hide_apps = sharedPreferences.getString("hide_apps", "");

        if (hide_apps.isEmpty()) {
            hide_apps = packInfo.packageInfo.packageName;
        } else {

            hide_apps = hide_apps + "-" + packInfo.packageInfo.packageName;
        }

        sharedPreferences.edit().putString("hide_apps", hide_apps).apply();

        //实时隐藏

        Intent intent=new Intent(ReceiverAction.HIDE_APP);

        intent.putExtra("hide_name",packInfo.packageInfo.packageName);

        AndroidAppHelper.currentApplication().sendBroadcast(intent);


    }

    private void deleteHideApps(PackInfo packInfo){

        if (packInfo==null){
            return;
        }

        String hide_apps = sharedPreferences.getString("hide_apps", "");

        String[] hides=hide_apps.split("-");

        String packName="";

        for (int i=0;i<hides.length;i++){

            if (hides[i].equals(packInfo.packageInfo.packageName)){

                packName=hides[i];//传递变量

                hides[i]="";//删除

            }
        }

        String hide="";

        for (String name:hides){

            if (!name.isEmpty()){

                if (hide.equals("")){

                    hide=name;
                }else {

                    hide=hide+"-"+name;
                }

            }

        }

        sharedPreferences.edit().putString("hide_apps",hide).apply();

        Intent intent=new Intent(ReceiverAction.SHOW_APP);
        intent.putExtra("show_name",packName);

        AndroidAppHelper.currentApplication().sendBroadcast(intent);


        Toast.makeText(AndroidAppHelper.currentApplication(), "删除成功~", Toast.LENGTH_SHORT).show();


    }

    private void launchApp(PackInfo packInfo){

        if (packInfo==null){
            return;
        }

        PackageInfo packageInfo=packInfo.packageInfo;

        PackageManager packageManager=fragment.getActivity().getPackageManager();

        Intent intent=packageManager.getLaunchIntentForPackage(packageInfo.packageName);

        fragment.getActivity().startActivity(intent);


    }


    class HideIconAdapter extends RecyclerView.Adapter<HideIconAdapter.ViewHolder> {

        int image_id = 0;
        int text_id = 0;

        public static final int HIDE_TYPE = 0x0010;
        private int TYPE = HIDE_TYPE;
        public static final int ALL_TYPE = 0x0020;


        public HideIconAdapter(int TYPE) {
            this.TYPE = TYPE;
        }

        private List<PackInfo> packageInfoList;

        private PackageManager packageManager;

        public void setPackageInfoList(List<PackInfo> packageInfoList) {
            this.packageInfoList = packageInfoList;
            notifyDataSetChanged();
        }


        @Override
        public HideIconAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            packageManager = parent.getContext().getPackageManager();

            View view = createView(parent.getContext());

            ViewHolder viewHolder = new ViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(HideIconAdapter.ViewHolder holder, int position) {

            if (packageInfoList == null) {
                return;
            }

            PackInfo info = packageInfoList.get(position);

            PackageInfo packageInfo = info.packageInfo;

            switch (TYPE) {

                case HIDE_TYPE:
                    //打开应用

                    holder.view.setOnLongClickListener(view -> {

                        deleteHideApps(info);

                        packageInfoList.remove(info);

//                        notifyItemChanged(position);
                        notifyDataSetChanged();

                        return true;
                    });

                    holder.view.setOnClickListener(view -> {

                        launchApp(info);



                    });


                    break;
                case ALL_TYPE:
                    //选择应用，改变颜色

                    holder.view.setOnClickListener(v -> {

                        info.isSelect = -info.isSelect;//取反

                        notifyItemChanged(position);
                        handlerApp(info);

                    });

                    break;

            }


            holder.imageView.setImageDrawable(packageInfo.applicationInfo.loadIcon(packageManager));//图标

            holder.textView.setText(packageInfo.applicationInfo.loadLabel(packageManager));//名字

            if (info.isSelect > 0) {

                holder.view.setBackgroundColor(Color.parseColor("#2bc777"));
            } else {
                holder.view.setBackgroundColor(Color.WHITE);
            }


        }

        @Override
        public int getItemCount() {

            if (packageInfoList != null) {
                return packageInfoList.size();
            }
            return 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            ImageView imageView;
            View view;
            TextView textView;

            public ViewHolder(View itemView) {
                super(itemView);
                view = itemView;
                this.imageView = itemView.findViewById(image_id);
                this.textView = itemView.findViewById(text_id);
            }
        }

        private View createView(Context context) {

            int dp16 = getValue(16, context.getResources());

            int dp72 = getValue(72, context.getResources());

            int dp48 = getValue(48, context.getResources());

            int dp12 = getValue(12, context.getResources());

            LinearLayout linearLayout = new LinearLayout(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp72);

            linearLayout.setHorizontalGravity(LinearLayout.HORIZONTAL);//横向布局

            linearLayout.setLayoutParams(params);
            linearLayout.setGravity(Gravity.CENTER);

            ImageView imageView = new ImageView(context);

            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp48, dp48);

            imageParams.setMargins(dp16, dp12, dp16, dp12);

            imageView.setLayoutParams(imageParams);

            int image_id = View.generateViewId();

            imageView.setId(image_id);

            this.image_id = image_id;//外传ID

            TextView textView = new TextView(context);

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            textParams.rightMargin = dp16;

            textView.setLayoutParams(textParams);

            textView.setMaxLines(1);

            textView.setEllipsize(TextUtils.TruncateAt.END);

            text_id = View.generateViewId();

            textView.setId(text_id);

            linearLayout.addView(imageView);
            linearLayout.addView(textView);

            return linearLayout;

        }

        private int getValue(int a, Resources resources) {

            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, a, resources.getDisplayMetrics());
        }

        private void handlerApp(PackInfo packInfo) {

            if (hideList == null) {
                hideList = new ArrayList<>();
            }

            if (hideList.contains(packInfo)) {
                hideList.remove(packInfo);
            } else {
                hideList.add(packInfo);
            }

        }

    }

    static class PackInfo {


        private PackageInfo packageInfo;
        private int isSelect = -1;

        public int getIsSelect() {
            return isSelect;
        }

        public void setIsSelect(int isSelect) {
            this.isSelect = isSelect;
        }

        public PackageInfo getPackageInfo() {
            return packageInfo;
        }

        public void setPackageInfo(PackageInfo packageInfo) {
            this.packageInfo = packageInfo;
        }


    }


}
