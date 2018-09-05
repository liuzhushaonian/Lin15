package com.xp.legend.lin15.presenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.xp.legend.lin15.interfaces.IFullFragment;
import com.xp.legend.lin15.utils.Conf;
import com.xp.legend.lin15.utils.ReceiverAction;

public class FullPresenter {

    private IFullFragment fragment;

    public FullPresenter(IFullFragment fragment) {
        this.fragment = fragment;
    }


    public void sendAlpha(Activity activity,int p){

        Intent intent=new Intent(ReceiverAction.N_FULL_ALPHA_VALUE);

        intent.putExtra(Conf.FULL_ALPHA_VALUE,p);

        activity.sendBroadcast(intent);

    }

    public void sendGaoValue(Activity activity,int p){

        Intent intent=new Intent(ReceiverAction.SET_N_FULL_GAO_VALUE);

        intent.putExtra(Conf.FULL_GAO_VALUE,p);

        activity.sendBroadcast(intent);

    }

    public void sendGao(Activity activity,boolean b){

        Intent intent=new Intent(ReceiverAction.SET_N_FULL_GAO_SI);

        if (b){

            intent.putExtra(Conf.FULL_GAO,10);

        }else {
            intent.putExtra(Conf.FULL_GAO,-10);

        }

        activity.sendBroadcast(intent);


    }

    public void sendQuality(Activity activity,int type){

        Intent intent=new Intent(ReceiverAction.SET_FULL_QUALITY);

        intent.putExtra(Conf.IMAGE_QUALITY,type);

        activity.sendBroadcast(intent);

        Log.d("sssss----->>>","发送成功！！！");

    }

    public void getFullInfo(Activity activity,int type){

        Intent intent=new Intent(ReceiverAction.GET_FULL_INFO);

        intent.putExtra(Conf.FULL_INFO,type);

        activity.sendBroadcast(intent);

    }


    public void sendShuImage(Activity activity,String s){

        Intent intent=new Intent(ReceiverAction.SET_N_FULL_VERTICAL_IMAGE);

        intent.putExtra(Conf.N_FULL_VERTICAL_FILE,s);

        activity.sendBroadcast(intent);


    }

    public void sendHengImage(Activity activity,String s){

        Intent intent=new Intent(ReceiverAction.SET_N_FULL_HORIZONTAL_IMAGE);

        intent.putExtra(Conf.N_FULL_HORIZONTAL_FILE,s);

        activity.sendBroadcast(intent);

    }

    public void deleteBg(Activity activity,int type){

        Intent intent=new Intent(ReceiverAction.DELETE_FULL_BG);

        intent.putExtra(Conf.FULL_DELETE_TYPE,type);

        activity.sendBroadcast(intent);

    }

    public void getAllFullInfo(Activity activity){

        Intent intent=new Intent(ReceiverAction.UI_GET_FULL_INFO);

        intent.putExtra(Conf.SDK,Build.VERSION.SDK_INT);

        activity.sendBroadcast(intent);

    }

    public void sendScroll(Activity activity,boolean isScroll){

        Intent intent=new Intent(ReceiverAction.SEND_FULL_SCROLL);

        intent.putExtra(Conf.FULL_SCROLL,isScroll);

        activity.sendBroadcast(intent);

    }

    public void sendSlit(Activity activity,boolean slit){

        Intent intent=new Intent(ReceiverAction.SEND_SLIT_INFO);
        intent.putExtra(Conf.SLIT,slit);

        activity.sendBroadcast(intent);

    }

}
