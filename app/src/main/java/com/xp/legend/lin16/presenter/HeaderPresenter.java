package com.xp.legend.lin16.presenter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import com.xp.legend.lin16.interfaces.IHeaderFragment;
import com.xp.legend.lin16.utils.Conf;
import com.xp.legend.lin16.utils.ReceiverAction;

public class HeaderPresenter {

    private IHeaderFragment fragment;

    public HeaderPresenter(IHeaderFragment fragment) {
        this.fragment = fragment;
    }


    /**
     * 发送透明度
     * @param activity
     * @param progress
     */
    public void setAlpha(Activity activity,int progress){

        Intent intent=new Intent(ReceiverAction.SET_N_HEADER_ALPHA_VALUE);

        intent.putExtra(Conf.N_HEADER_ALPHA,progress);

        activity.sendBroadcast(intent);


    }

    public void getInfo(Activity activity,int type){

        Intent intent=new Intent(ReceiverAction.GET_N_HEADER_INFO);

        intent.putExtra(Conf.HEADER_INFO_TYPE,type);

        activity.sendBroadcast(intent);

    }


    /**
     * 获取全部信息并更改UI
     * @param activity
     */
    public void getAllInfo(Activity activity){

        Intent intent=new Intent(ReceiverAction.UI_GET_HEADER_INFO);

        intent.putExtra(Conf.SDK, Build.VERSION.SDK_INT);

        activity.sendBroadcast(intent);

    }

    /**
     * 发送竖屏图
     * @param activity
     * @param s
     */
    public void sendShuImage(Activity activity,String s){


        Intent intent=new Intent(ReceiverAction.SET_N_HEADER_VERTICAL_IMAGE);

        intent.putExtra(Conf.N_HEADER_VERTICAL_IMAGE,s);

        activity.sendBroadcast(intent);

    }

    /**
     * 发送横屏图
     * @param activity
     * @param s
     */
    public void sendHengImage(Activity activity,String s){


        Intent intent=new Intent(ReceiverAction.SET_N_HEADER_HORIZONTAL_IMAGE);

        intent.putExtra(Conf.N_HEADER_HORIZONTAL_IMAGE,s);

        activity.sendBroadcast(intent);

    }

    public void deleteBg(Activity activity,int type){

        Intent intent=new Intent(ReceiverAction.DELETE_N_HEADER_BG);

        intent.putExtra(Conf.N_HEADER_DELETE_TYPE,type);

        activity.sendBroadcast(intent);

    }

    /**
     * 设置是否高斯模糊
     * @param activity
     * @param b
     */
    public void setGao(Activity activity,boolean b){

        Intent intent=new Intent(ReceiverAction.SET_N_HEADER_GAO_SI);

        if (b){

            intent.putExtra(Conf.N_HEADER_GAO,1);

        }else {
            intent.putExtra(Conf.N_HEADER_GAO,-1);
        }

        activity.sendBroadcast(intent);

    }

    /**
     * 设置高斯模糊的值
     * @param activity
     * @param value
     */
    public void sendGaoValue(Activity activity,int value){

        Intent intent=new Intent(ReceiverAction.SET_N_HEADER_GAO_SI_VALUE);

        intent.putExtra(Conf.N_HEADER_GAO_VALUE,value);

        activity.sendBroadcast(intent);

    }

    public void setQuality(Activity activity,int quality){

        Intent intent=new Intent(ReceiverAction.SET_HEADER_QUALITY);

        intent.putExtra(Conf.N_HEADER_QUALITY,quality);

        activity.sendBroadcast(intent);

    }


}
