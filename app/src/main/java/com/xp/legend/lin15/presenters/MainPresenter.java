package com.xp.legend.lin15.presenters;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.xp.legend.lin15.interfaces.IMainActivity;
import com.xp.legend.lin15.utils.ReceiverAction;

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

        Toast.makeText(context, "发送成功", Toast.LENGTH_SHORT).show();
    }
}
