package com.xp.legend.lin15.fragment;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
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

import com.xp.legend.lin15.R;
import com.xp.legend.lin15.bean.HeaderInfo;
import com.xp.legend.lin15.bean.Result;
import com.xp.legend.lin15.interfaces.IHeaderFragment;
import com.xp.legend.lin15.presenter.HeaderPresenter;
import com.xp.legend.lin15.utils.Conf;
import com.xp.legend.lin15.utils.ReceiverAction;


/**
 * A simple {@link Fragment} subclass.
 */
public class HeaderFragment extends BaseFragment implements IHeaderFragment {


    private TextView alphaInfo, gaoInfo;
    private SeekBar seekBarAlpha, seekBarGao;
    private ImageView heng, shu;
    private Switch switchGao;
    private HeaderPresenter presenter;

    private ResultReceiver resultReceiver;

    private int shu_width = -1;

    private int shu_height = -1;

    private int heng_width = -1;

    private int heng_height = -1;

    private static final int SELECT_SHU_IMAGE = 20;

    private static final int SELECT_HENG_IMAGE = 30;

    private static final int CUT_SHU_IMAGE = 40;

    private static final int CUT_HENG_IMAGE = 50;

    private RadioGroup radioGroup;


    public HeaderFragment() {
        // Required empty public constructor
    }


    class ResultReceiver extends BroadcastReceiver {


        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null) {
                return;
            }

            String action = intent.getAction();

            if (action == null) {
                return;
            }

            switch (action) {

                case ReceiverAction.SEND_N_HEADER_INFO://获取头部信息宽和高

                    int type = intent.getIntExtra(Conf.N_HEADER_RESULT, -1);

                    if (type == -1) {
                        return;
                    }

                    HeaderInfo info = intent.getParcelableExtra(Conf.N_HEADER_INFO_RESULT);

                    switch (type) {

                        case Conf.VERTICAL:

                            shu_width = info.getWidth();

                            shu_height = info.getHeight();

                            if (shu_width <= 0 || shu_height <= 0) {

                                Toast.makeText(getContext(), getString(R.string.header_info1), Toast.LENGTH_SHORT).show();

                                return;

                            }

                            openAlbum(SELECT_SHU_IMAGE);

                            break;

                        case Conf.HORIZONTAL:

                            heng_width = info.getWidth();

                            heng_height = info.getHeight();


                            if (heng_width <= 0 || heng_height <= 0) {

                                Toast.makeText(getContext(), getString(R.string.header_info2), Toast.LENGTH_SHORT).show();

                                return;

                            }

                            openAlbum(SELECT_HENG_IMAGE);

                            break;

                    }

                    break;

                case ReceiverAction.HEADER_TO_UI_INFO://获取全部信息

                    Result result=intent.getParcelableExtra(Conf.HEADER_TO_UI_RESULT);

                    initUi(result);

                    break;


            }


        }
    }

    private void register() {

        if (this.resultReceiver == null) {

            this.resultReceiver = new ResultReceiver();

            IntentFilter intentFilter = new IntentFilter();

            intentFilter.addAction(ReceiverAction.SEND_N_HEADER_INFO);//获取
            intentFilter.addAction(ReceiverAction.HEADER_TO_UI_INFO);//获取全部信息

            getContext().registerReceiver(resultReceiver, intentFilter);

        }

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        presenter = new HeaderPresenter(this);

        register();

        View view = inflater.inflate(R.layout.fragment_header, container, false);

        getComponent(view);//获取组件

        event();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        presenter.getAllInfo(getActivity());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (resultReceiver != null) {
            getActivity().unregisterReceiver(resultReceiver);
        }
    }

    private void getComponent(View view) {

        alphaInfo = view.findViewById(R.id.textView2);

        gaoInfo = view.findViewById(R.id.textViewGaoValue);

        seekBarAlpha = view.findViewById(R.id.seekBar_alpha);

        seekBarGao = view.findViewById(R.id.seekBarGaoValue);

        shu = view.findViewById(R.id.shu_header);

        heng = view.findViewById(R.id.heng_header);

        switchGao = view.findViewById(R.id.switchGao);

        radioGroup = view.findViewById(R.id.radioGroup);

    }

    private void event() {

        //滑动透明度
        seekBarAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                String s = getString(R.string.bg_alpha) +" "+ progress;

                alphaInfo.setText(s);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                presenter.setAlpha(getActivity(), seekBar.getProgress());
            }
        });


        //竖屏头部点击事件
        shu.setOnClickListener(v -> {

            presenter.getInfo(getActivity(), Conf.VERTICAL);

        });


        ///横屏头部点击事件
        heng.setOnClickListener(v -> {

            presenter.getInfo(getActivity(), Conf.HORIZONTAL);

        });

        //竖屏长按事件
        shu.setOnLongClickListener(v -> {

            presenter.deleteBg(getActivity(), Conf.VERTICAL);

            return true;
        });

        //横屏长按事件
        heng.setOnLongClickListener(v -> {

            presenter.deleteBg(getActivity(), Conf.HORIZONTAL);

            return true;
        });

        //高斯模糊切换
        switchGao.setOnCheckedChangeListener((buttonView, isChecked) -> {


            presenter.setGao(getActivity(), isChecked);
            seekBarGao.setEnabled(isChecked);
        });

        //滑动高斯模糊
        seekBarGao.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                String s = getString(R.string.gao_si_value) + " " + progress;

                gaoInfo.setText(s);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                presenter.sendGaoValue(getActivity(), seekBar.getProgress());

            }
        });

        //设置图片质量
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {

            switch (checkedId) {

                case R.id.n_header_best:

                    presenter.setQuality(getActivity(), Conf.HEIGHT_QUALITY);

                    break;


                case R.id.n_header_lower:

                    presenter.setQuality(getActivity(), Conf.LOW_QUALITY);
                    break;

            }

            Log.d("header--发送-->>",""+checkedId);


        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {

            case SELECT_SHU_IMAGE:

                if (data == null || data.getData() == null) {
                    return;
                }

                startCropImage(data.getData(), this.shu_width, this.shu_height, CUT_SHU_IMAGE);


                break;


            case SELECT_HENG_IMAGE:

                if (data == null || data.getData() == null) {
                    return;
                }


                startCropImage(data.getData(), this.heng_width, this.heng_height, CUT_HENG_IMAGE);


                break;


            case CUT_SHU_IMAGE:

                if (data == null || data.getData() == null) {
                    return;
                }

                String s = data.getData().toString();

                presenter.sendShuImage(getActivity(), s);

                break;


            case CUT_HENG_IMAGE:

                if (data == null || data.getData() == null) {
                    return;
                }

                String s1 = data.getData().toString();

                presenter.sendHengImage(getActivity(), s1);

                break;

            default:

                super.onActivityResult(requestCode, resultCode, data);

                break;

        }

    }

    /**
     * 恢复界面 从header处获取数据并恢复
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

        seekBarAlpha.setProgress(result.getAlpha());

        String gao_info = getString(R.string.gao_si_value) +" "+ result.getGaoValue();

        gaoInfo.setText(gao_info);

        int type = result.getQuality();

        switch (type) {

            case Conf.LOW_QUALITY:

                radioGroup.check(R.id.n_header_lower);

                break;


            case Conf.HEIGHT_QUALITY:

                radioGroup.check(R.id.n_header_best);

                break;

        }

        switchGao.setChecked(result.isGao());

        seekBarGao.setProgress(result.getGaoValue());
        seekBarGao.setEnabled(result.isGao());

    }

}
