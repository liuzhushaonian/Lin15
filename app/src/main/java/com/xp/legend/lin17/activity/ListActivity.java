package com.xp.legend.lin17.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.xp.legend.lin17.R;
import com.xp.legend.lin17.adapter.LinAdapter;
import com.xp.legend.lin17.adapter.OnItemClick;
import com.xp.legend.lin17.bean.HeaderInfo;
import com.xp.legend.lin17.bean.Result;
import com.xp.legend.lin17.utils.Conf;
import com.xp.legend.lin17.utils.Database;
import com.xp.legend.lin17.utils.ItemSpace;
import com.xp.legend.lin17.utils.ReceiverAction;

import java.util.List;

public class ListActivity extends BaseActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private FloatingActionButton add;
    private LinAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private ResultReceiver receiver;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        initReceiver();
        getInfos();
        getComponent();
        initToolbar();
        initList();
        click();

    }


    private void getComponent(){

        toolbar=findViewById(R.id.list_toolbar);
        recyclerView=findViewById(R.id.list);
        add=findViewById(R.id.add_list);

    }

    private void initToolbar(){

        toolbar.setTitle("");

        setSupportActionBar(toolbar);

    }

    private void initList(){

        adapter=new LinAdapter();
        linearLayoutManager=new LinearLayoutManager(this);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.addItemDecoration(new ItemSpace());

        adapter.setItemClick(new OnItemClick() {
            @Override
            public void itemClick(int position, Result result) {

            }

            @Override
            public void itemLongClick(int position, Result result) {

            }
        });

        getData();

    }

    private void click(){

        //添加数据
        add.setOnClickListener(view -> {

            Result result=new Result();

            Database.getDefault(this).addLin(result);

            adapter.addItem(result);

        });

    }

    private void getData(){

        new Thread(){
            @Override
            public void run() {
                super.run();

                List<Result> resultList=Database.getDefault(ListActivity.this).getLins(0);//获取未删除的列表
                Runnable r= () -> adapter.addItems(resultList);

                runOnUiThread(r);
            }
        }.start();
    }


    private void initReceiver(){

        receiver=new ResultReceiver();

        IntentFilter filter=new IntentFilter();
        filter.addAction(ReceiverAction.SEND_N_HEADER_INFO);

        registerReceiver(receiver,filter);

    }

    @Override
    public void finish() {
        super.finish();
        if (receiver!=null){

            unregisterReceiver(receiver);

        }
    }

    private void getInfos(){

        Intent intent=new Intent(ReceiverAction.GET_N_HEADER_INFO);

        intent.putExtra(Conf.HEADER_INFO_TYPE,Conf.VERTICAL);

        sendBroadcast(intent);

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

            int shu_width=sharedPreferences.getInt("shu_width",0);
            int shu_height=sharedPreferences.getInt("shu_height",0);

            if (shu_width>0&&shu_height>0){
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

                                return;

                            }

                            sharedPreferences.edit().putInt("shu_width",shu_width).apply();
                            sharedPreferences.edit().putInt("shu_height",shu_height).apply();

                            break;
                    }
            }
        }
    }


}
