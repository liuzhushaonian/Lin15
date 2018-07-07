package com.xp.legend.lin15.presenters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.xp.legend.lin15.R;
import com.xp.legend.lin15.interfaces.IMainActivity;
import com.xp.legend.lin15.utils.Conf;
import com.xp.legend.lin15.utils.ReceiverAction;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MainPresenter {

    private IMainActivity activity;

    public MainPresenter(IMainActivity activity) {
        this.activity = activity;
    }

    public void unregister(){

        if (this.activity!=null){
            this.activity=null;
        }
    }

    public void sendDataControl(boolean b, Context context){

        Intent intent=new Intent(ReceiverAction.DATA_SETTING);
        intent.putExtra("data",b);

        context.sendBroadcast(intent);

//        Toast.makeText(context, "发送成功", Toast.LENGTH_SHORT).show();
    }

    public void sendQuality(int quality,Context context){

        Intent intent=new Intent(ReceiverAction.SEND_QUALITY);

        intent.putExtra(Conf.QUALITY,quality);

        context.sendBroadcast(intent);

    }

    public void sendGao(boolean gao,Context context){

        Intent intent=new Intent(ReceiverAction.SEND_GAO_SI);

        intent.putExtra(Conf.GAO_SI,gao);

        context.sendBroadcast(intent);

    }

    public void sendGaoSiValue(int value,Context context){


        Intent intent=new Intent(ReceiverAction.SEND_GAO_VALUE);

        intent.putExtra(Conf.GAO_SI_VALUE,value);

        context.sendBroadcast(intent);
    }

    public void sendHeight(String s, Activity activity){

        if (TextUtils.isEmpty(s)){
            return;
        }

        Intent intent=new Intent(ReceiverAction.SEND_SINGLE_HEIGHT);

        int height=Integer.parseInt(s);

        intent.putExtra(Conf.HEIGHT,height);

        activity.sendBroadcast(intent);

    }

    public void showEditDialog(Activity activity){

        AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        View view= LayoutInflater.from(activity).inflate(R.layout.height_dialog,null,false);

        EditText editText=view.findViewById(R.id.height_edit);

        builder.setTitle("自定义高度(输入0则可撤销)").setView(view).setPositiveButton("确定",(dialogInterface, i) -> {

            String s=editText.getText().toString();

            sendHeight(s,activity);

        }).show();


    }

    public void sendSystem(boolean b,Activity activity){

        Intent intent=new Intent(ReceiverAction.SHOW_SYSTEM);
        intent.putExtra(Conf.SHOW_SYSTEM,b);

        activity.sendBroadcast(intent);

    }

    public void showChangeDialog(Activity activity){

        AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        View view= LayoutInflater.from(activity).inflate(R.layout.height_dialog,null,false);

        EditText editText=view.findViewById(R.id.height_edit);

        editText.setHint("旧密码");

        builder.setTitle("输入原密码").setView(view).setPositiveButton("确定",(dialogInterface, i) -> {

            String s=editText.getText().toString();
            Intent intent=new Intent(ReceiverAction.GET_PASS);
            intent.putExtra(Conf.OLD_PASS,s);

            activity.sendBroadcast(intent);

        }).show();

    }

    public void getInfo(Intent intent,Activity activity){

        boolean b=intent.getBooleanExtra(Conf.PASS_INFO,false);
        if (b){

            showNewDialog(activity);
        }else {
            Toast.makeText(activity, "修改失败", Toast.LENGTH_SHORT).show();
        }

    }

    private void showNewDialog(Activity activity){

        AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        View view= LayoutInflater.from(activity).inflate(R.layout.height_dialog,null,false);

        EditText editText=view.findViewById(R.id.height_edit);

        editText.setHint("新密码");

        builder.setTitle("输入新密码").setView(view).setPositiveButton("确定",(dialogInterface, i) -> {

            String s=editText.getText().toString();
            Intent intent=new Intent(ReceiverAction.SEND_NEW_PASS);
            intent.putExtra(Conf.NEW_PASS,s);

            activity.sendBroadcast(intent);

        }).show();


    }

    public void showHongbao(Activity activity){

        String content=activity.getResources().getString(R.string.hongbao_content);

        AlertDialog.Builder builder=new AlertDialog.Builder(activity);

        View view= LayoutInflater.from(activity).inflate(R.layout.about_content,null,false);

        TextView textView=view.findViewById(R.id.about_content);


        textView.setText(content);

        builder.setView(view).setTitle("伪打赏").setPositiveButton("复制吱口令", (DialogInterface dialog, int which) -> {


            ClipboardManager mClipboardManager = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);

            ClipData clipData = ClipData.newPlainText("1b3l4H43ke", "1b3l4H43ke");
            if (mClipboardManager!=null) {
                mClipboardManager.setPrimaryClip(clipData);

                Toast.makeText(activity, "复制成功，打开支付宝领取红包吧，感谢老铁的支持~", Toast.LENGTH_SHORT).show();

            }

        }).show();

    }

    public void switchBg(Activity activity,boolean is){

        Intent intent=new Intent(ReceiverAction.TOGLE_BG);

        intent.putExtra("change",is);

        activity.sendBroadcast(intent);

    }

}
