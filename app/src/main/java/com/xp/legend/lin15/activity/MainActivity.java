package com.xp.legend.lin15.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.support.v7.app.AlertDialog;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;

import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.dingmouren.colorpicker.ColorPickerDialog;
import com.dingmouren.colorpicker.OnColorPickerListener;
import com.xp.legend.lin15.R;
import com.xp.legend.lin15.bean.Rect;
import com.xp.legend.lin15.hooks.StatusBarHeaderHook;
import com.xp.legend.lin15.interfaces.IMainActivity;
import com.xp.legend.lin15.presenters.MainPresenter;
import com.xp.legend.lin15.utils.ReceiverAction;

import de.robv.android.xposed.XposedBridge;

public class MainActivity extends BaseActivity implements IMainActivity{

    private ImageView imageView1,imageView2;
    private InfoReceiver infoReceiver;
    private Rect rect_header,rect_full;
    private SeekBar alphaSeekBar;
    private RadioGroup radioGroup;
    private int MODE=1;
    private int progress=-1;
    private TextView alphaInfo;
    private Toolbar toolbar;
    private TextView about;
    private Switch data;
    private MainPresenter presenter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getComponent();

        event();
        register();

        initToolbar();

        presenter=new MainPresenter(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        int mode=sharedPreferences.getInt("mode",-1);

        if (mode>=0){
            MODE=mode;
            switch (mode){
                case 0:

                    radioGroup.check(R.id.color_btn);

                    break;
                case 1:

                    radioGroup.check(R.id.imgae_btn);
                    break;
            }
        }

        progress=sharedPreferences.getInt("progress",-1);

        if (progress>=0){
            alphaSeekBar.setProgress(progress);

            int p= (int) (progress/2.55);

            alphaInfo.setText("透明度 "+p+"%");
        }

        boolean b=sharedPreferences.getBoolean("data",false);
        data.setChecked(b);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (infoReceiver!=null){
            unregisterReceiver(infoReceiver);
        }

        if (this.presenter!=null){
            presenter.unregister();
            presenter=null;
        }

    }

    private void getComponent(){

        imageView1=findViewById(R.id.header_setting);
        imageView2=findViewById(R.id.full_image);
        alphaSeekBar=findViewById(R.id.seekBar_alpha);
        radioGroup=findViewById(R.id.radioGroup);
        alphaInfo=findViewById(R.id.textView_alpha);
        toolbar=findViewById(R.id.main_toolbar);
        about=findViewById(R.id.about_text);
        data=findViewById(R.id.switch_cancel_data);
    }

    private void initToolbar(){

        toolbar.setBackgroundColor(getResources().getColor(R.color.colorTeal500));

        setSupportActionBar(toolbar);

    }

    private void event(){

        imageView1.setOnClickListener(v -> {

            switch (MODE){
                case 1:
                    Intent intent=new Intent(ReceiverAction.HEADER_GET_INFO);
                    sendBroadcast(intent);
                    break;
                case 0:
                    setHeaderColor();
                    break;
            }
        });

        imageView2.setOnClickListener(v -> {

            switch (MODE){
                case 1:
                    settingFullImage();
                    break;
                case 0:
                    setFullcolor();
                    break;
            }
        });


        alphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                int p= (int) (progress/2.55);

                alphaInfo.setText("透明度 "+p+"%");

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                Intent intent=new Intent(ReceiverAction.HEADER_SEND_ALPHA);
                intent.putExtra("alpha",seekBar.getProgress());
                sendBroadcast(intent);

                sharedPreferences.edit().putInt("progress",seekBar.getProgress()).apply();

            }
        });


        imageView1.setOnLongClickListener(v -> {

            Intent intent=new Intent(ReceiverAction.HEADER_DELETE_ALBUM);

            sendBroadcast(intent);

            return true;

        });

        imageView2.setOnLongClickListener(v -> {

            Intent intent=new Intent(ReceiverAction.FULL_DELETE_ALBUM);

            sendBroadcast(intent);

            return true;
        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {

            switch (checkedId){
                case R.id.imgae_btn:
                    MODE=1;
                    break;
                case R.id.color_btn:
                    MODE=0;
                    break;
            }

            sharedPreferences.edit().putInt("mode",MODE).apply();//保存

        });

        about.setOnClickListener(v -> {

            showDialog();

        });

        data.setOnCheckedChangeListener((compoundButton, b) -> {
            presenter.sendDataControl(b,this);
            sharedPreferences.edit().putBoolean("data",b).apply();
        });

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){
            case 100:

                if (rect_full==null||data==null||data.getData()==null){
                    return;
                }

                startCropImage(data.getData(),rect_full.getWidth(),rect_full.getHeight(),400);


                break;


            case 200:

                if (rect_header==null||data==null||data.getData()==null){
                    return;
                }

                startCropImage(data.getData(),rect_header.getWidth(),rect_header.getHeight(),300);

                break;
            case 300:

                if (data==null||data.getData()==null){

                    return;
                }

                Intent intent=new Intent(ReceiverAction.HEADER_SEND_ALBUM);

                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                String s=data.getData().toString();

                if (s.startsWith("file:///")) {

                    s = s.replace("file:///", "");

                    s=s+"-file";
                }else if (s.startsWith("content:")){


                    s=s.replace("content:","");

                    s=s+"-content";
                }


                intent.putExtra("file",s);

                sendBroadcast(intent);

                break;


            case 400:

                if (data==null||data.getData()==null){
                    return;
                }

                Intent intent1=new Intent(ReceiverAction.FULL_SEND_ALBUM);

                intent1.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                String s1=data.getData().toString();

//                s=s1.replace("file:///","");

                if (s1.startsWith("file:///")) {

                    s1 = s1.replace("file:///", "");

                    s1=s1+"-file";
                }else if (s1.startsWith("content:")){


                    s1=s1.replace("content:","");

                    s1=s1+"-content";
                }


                intent1.putExtra("file",s1);

                sendBroadcast(intent1);

                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void register(){

        if (infoReceiver!=null){
            return;
        }

        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(ReceiverAction.HEADER_SEND_INFO);
        intentFilter.addAction(ReceiverAction.FULL_SEND_INFO);

        infoReceiver=new InfoReceiver();

        registerReceiver(infoReceiver,intentFilter);



    }

    /**
     * 设置流量关闭
     * @param b
     */
    @Override
    public void sendDataControl(boolean b) {

    }

    public class InfoReceiver extends BroadcastReceiver{


        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();
            if (action==null){
                return;
            }

            switch (action){
                case ReceiverAction.HEADER_SEND_INFO://接受图形信息并打开相册进行选择相片

                    rect_header= (Rect) intent.getSerializableExtra("info");

                    openAlbum(200);

                    break;
                case ReceiverAction.FULL_SEND_INFO:

                    rect_full= (Rect) intent.getSerializableExtra("full_info");

                    if (rect_full.getHeight()<0){

                        Toast.makeText(MainActivity.this, "请下拉一次快速设置菜单到底以便测量高度", Toast.LENGTH_SHORT).show();
                        
                        return;
                    }

                    openAlbum(100);

                    break;
            }
        }
    }

    private void settingFullImage(){

        Intent intent=new Intent(ReceiverAction.FULL_GET_INFO);
        sendBroadcast(intent);

    }

    private void setHeaderColor(){

        new ColorPickerDialog(
                MainActivity.this,
                getResources().getColor(R.color.colorPrimary),
                true,
                new OnColorPickerListener() {
                    @Override
                    public void onColorCancel(ColorPickerDialog dialog) {

                    }

                    @Override
                    public void onColorChange(ColorPickerDialog dialog, int color) {

                    }

                    @Override
                    public void onColorConfirm(ColorPickerDialog dialog, int color) {

                        Intent intent=new Intent(ReceiverAction.HEADER_SEND_COLOR);
                        intent.putExtra("color",color);
                        sendBroadcast(intent);

                        Toast.makeText(MainActivity.this, ""+color, Toast.LENGTH_SHORT).show();
                    }
                }

        ).show();

    }

    private void setFullcolor(){

        new ColorPickerDialog(
                MainActivity.this,
                getResources().getColor(R.color.colorPrimary),
                true,
                new OnColorPickerListener() {
                    @Override
                    public void onColorCancel(ColorPickerDialog dialog) {

                    }

                    @Override
                    public void onColorChange(ColorPickerDialog dialog, int color) {

                    }

                    @Override
                    public void onColorConfirm(ColorPickerDialog dialog, int color) {

                        Intent intent=new Intent(ReceiverAction.FULL_SEND_COLOR);
                        intent.putExtra("color",color);
                        sendBroadcast(intent);

                        Toast.makeText(MainActivity.this, ""+color, Toast.LENGTH_SHORT).show();

                    }
                }

        ).show();
    }


    private void showDialog(){

        final AlertDialog.Builder builder=new AlertDialog.Builder(this);

        LayoutInflater inflater=LayoutInflater.from(this);

        View view=inflater.inflate(R.layout.main_item,null,false);

        TextView textView=view.findViewById(R.id.about);

        textView.setText(getResources().getText(R.string.string_about));

        builder.setView(view).setTitle("给用户的说明书");
        builder.show();

    }
}
