package com.xp.legend.lin16.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.xp.legend.lin16.R;
import com.xp.legend.lin16.bean.Full;
import com.xp.legend.lin16.bean.Result;
import com.xp.legend.lin16.interfaces.IFullFragment;
import com.xp.legend.lin16.presenter.FullPresenter;
import com.xp.legend.lin16.utils.Conf;
import com.xp.legend.lin16.utils.ReceiverAction;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FullFragment extends BaseFragment implements IFullFragment {

    private TextView alphaInfo, gaoInfo;
    private SeekBar seekBarAlpha, seekBarGao;
    private ImageView heng, shu;
    private Switch switchGao,switchScroll;
    private FullPresenter presenter;
    private boolean autoSet=false;

    private int shu_width = -1;

    private int shu_height = -1;

    private int heng_width = -1;

    private int heng_height = -1;

    private static final int SELECT_SHU_IMAGE = 21;

    private static final int SELECT_HENG_IMAGE = 31;

    private static final int CUT_SHU_IMAGE = 41;

    private static final int CUT_HENG_IMAGE = 51;

    private RadioGroup radioGroup;

    private FullReceiver fullReceiver;
    private Switch slit;
    private int count=0;


    public FullFragment() {
        // Required empty public constructor
    }

    class FullReceiver extends BroadcastReceiver{


        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent==null){
                return;
            }

            String action=intent.getAction();

            if (action==null){
                return;
            }

            switch (action){

                case ReceiverAction.SEND_FULL_INFO:

                    int type=intent.getIntExtra(Conf.FULL_INFO,-1);

                    if (type==-1){
                        return;
                    }

                    Full full=intent.getParcelableExtra(Conf.FULL_RESULT);

                    switch (type){

                        case Conf.VERTICAL:

                            shu_width=full.getWidth();

                            shu_height=full.getHeight();



                            if (shu_width<=0){

                                Toast.makeText(context, getString(R.string.no_width), Toast.LENGTH_SHORT).show();
                                return;
                            }


                            if (shu_height<=0){

                                Toast.makeText(context, getString(R.string.all_info1), Toast.LENGTH_LONG).show();

                                count++;

                                if (count>=3){
                                    showDialog();
                                }

                                return;

                            }



                            openAlbum(SELECT_SHU_IMAGE);



                            break;

                        case Conf.HORIZONTAL:

                            heng_width=full.getWidth();

                            heng_height=full.getHeight();


                            if (heng_width<=0){

                                Toast.makeText(context, getString(R.string.no_width), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (heng_height<=0){

                                Toast.makeText(context, getString(R.string.all_info2), Toast.LENGTH_LONG).show();

                                count++;

                                if (count>=3){
                                    showDialog();
                                }

                                return;

                            }

                            openAlbum(SELECT_HENG_IMAGE);


                            break;

                    }



                    break;


                case ReceiverAction.FULL_TO_UI_INFO:

                    Result result=intent.getParcelableExtra(Conf.FULL_TO_UI_RESULT);

                    initUi(result);

                    break;

                case ReceiverAction.DELETE_IMG_CALL:

                    cleanUri();

                    break;

            }

        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        register();

        View view=inflater.inflate(R.layout.fragment_full, container, false);

        presenter=new FullPresenter(this);

        getComponent(view);

        event();

        presenter.getAllFullInfo(getActivity());

        return view;
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fullReceiver!=null){
            getActivity().unregisterReceiver(fullReceiver);
        }
    }

    private void register(){

        if (this.fullReceiver==null){
            this.fullReceiver=new FullReceiver();

            IntentFilter intentFilter=new IntentFilter();

            intentFilter.addAction(ReceiverAction.SEND_FULL_INFO);

            intentFilter.addAction(ReceiverAction.FULL_TO_UI_INFO);

            intentFilter.addAction(ReceiverAction.DELETE_IMG_CALL);

            getActivity().registerReceiver(fullReceiver,intentFilter);
        }

    }


    private void getComponent(View view){

        alphaInfo=view.findViewById(R.id.full_alpha_info);

        gaoInfo=view.findViewById(R.id.full_gao_value);

        seekBarAlpha=view.findViewById(R.id.seekBar_full_alpha);

        seekBarGao=view.findViewById(R.id.seekBar_gao_value);

        heng=view.findViewById(R.id.full_heng_header);
        shu=view.findViewById(R.id.full_shu_header);
        switchGao=view.findViewById(R.id.switch_full_gao);
        radioGroup=view.findViewById(R.id.radioGroup2);
        switchScroll=view.findViewById(R.id.scrollSwitch);
        slit=view.findViewById(R.id.slit);

    }


    private void event(){

        seekBarAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                String s=getString(R.string.bg_alpha)+" "+progress;

                alphaInfo.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                presenter.sendAlpha(getActivity(),seekBar.getProgress());
            }
        });


        seekBarGao.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                String s=getString(R.string.gao_si_value)+progress;

                gaoInfo.setText(s);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                presenter.sendGaoValue(getActivity(),seekBar.getProgress());

            }
        });


        shu.setOnClickListener(v -> {

            presenter.getFullInfo(getActivity(),Conf.VERTICAL);

        });

        shu.setOnLongClickListener(v -> {

            presenter.deleteBg(getActivity(),Conf.VERTICAL);

            return true;
        });

        heng.setOnClickListener(v -> {

            presenter.getFullInfo(getActivity(),Conf.HORIZONTAL);

        });

        heng.setOnLongClickListener(v -> {

            presenter.deleteBg(getActivity(),Conf.HORIZONTAL);

            return true;
        });

        switchGao.setOnCheckedChangeListener((buttonView, isChecked) -> {


            presenter.sendGao(getActivity(),isChecked);

            seekBarGao.setEnabled(isChecked);

        });

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {

            if (!isAutoSet) {

                switch (checkedId) {

                    case R.id.full_best:

                        presenter.sendQuality(getActivity(), Conf.HEIGHT_QUALITY);
                        break;

                    case R.id.full_lower:

                        presenter.sendQuality(getActivity(), Conf.LOW_QUALITY);

                        break;

                }

            }

            isAutoSet=false;


//            presenter.sendQuality(getActivity(),type);

        });

        switchScroll.setOnCheckedChangeListener((buttonView, isChecked) -> {


            presenter.sendScroll(getActivity(),isChecked);

        });


        slit.setOnCheckedChangeListener((buttonView, isChecked) -> {

            presenter.sendSlit(getActivity(),isChecked);

        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode){

            case SELECT_SHU_IMAGE:

                if (data==null||data.getData()==null){
                    return;
                }

                Uri u=null;

                try {
                    u=getFileUri(saveAsFile(data.getData()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (u==null){

                    Log.d("u-->>","uri is null");

                    return;
                }

                startCropImage(u,shu_width,shu_height,CUT_SHU_IMAGE);

                uriList.add(u);

//                getContext().getContentResolver().delete(u,null,null);//删除

                break;

            case SELECT_HENG_IMAGE:

                if (data==null||data.getData()==null){
                    return;
                }

                Uri u1=null;

                try {
                    u1=getFileUri(saveAsFile(data.getData()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (u1==null){

                    Log.d("u-->>","uri is null");

                    return;
                }

                uriList.add(u1);

                startCropImage(u1,heng_width,heng_height,CUT_HENG_IMAGE);

                break;


            case CUT_SHU_IMAGE:

                if (data==null||data.getData()==null){

//                    XposedBridge.log("lin16------>>>>data is null or data.getData is null!!!!");

                    Log.d("lin16------>>>>","data is null or data.getData is null!!!!");

                    return;
                }

                String s=data.getData().toString();

                presenter.sendShuImage(getActivity(),s);

                break;

            case CUT_HENG_IMAGE:

                if (data==null||data.getData()==null){
                    return;
                }

                String s1=data.getData().toString();

                presenter.sendHengImage(getActivity(),s1);

                break;


            default:

                super.onActivityResult(requestCode, resultCode, data);

                break;

        }


    }

    boolean isAutoSet=false;



    /**
     * 恢复界面 从full处获取数据并恢复
     * @param result
     */
    private void initUi(Result result) {

        if (result == null) {
            return;
        }

        int a=result.getAlpha();



        a= (int) (a/2.55)+1;

        if (a<0){
            a=0;
        }

        if (a>100){
            a=100;
        }

        String alpha_info = getString(R.string.bg_alpha) +" "+ a;

        alphaInfo.setText(alpha_info);

//        seekBarAlpha.setProgress(result.getAlpha());

        String gao_info = getString(R.string.gao_si_value) +" "+ result.getGaoValue();

        gaoInfo.setText(gao_info);

        int type = result.getQuality();

        autoSet=true;

        isAutoSet=true;



        switchGao.setChecked(result.isGao());
        seekBarGao.setProgress(result.getGaoValue());
        seekBarGao.setEnabled(result.isGao());

        switchScroll.setChecked(result.isScroll());

        slit.setChecked(result.isSlit());

        seekBarAlpha.setProgress(a);

        switch (type) {

            case Conf.LOW_QUALITY:

                radioGroup.check(R.id.full_lower);

                break;


            case Conf.HEIGHT_QUALITY:

                radioGroup.check(R.id.full_best);

                break;

        }

    }

    private void showDialog(){

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());

        builder.setTitle(getString(R.string.tip)).setMessage(getString(R.string.tip_content)).show();


    }


}
