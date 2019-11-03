package com.xp.legend.lin16.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImageView;
import com.xp.legend.lin16.R;
import com.xp.legend.lin16.utils.Conf;

public class CropActivity extends BaseActivity {

    private CropImageView cropImageView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//强制竖屏
        getComponent();
        initToolbar();
        initCrop();
    }

    private void getComponent(){
        cropImageView=findViewById(R.id.cropImageView);
        toolbar=findViewById(R.id.crop_toolbar);
    }


    private void initToolbar(){

        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(v -> {

            finish();

        });

    }

    private void initCrop(){

        Intent intent=getIntent();

        if (intent==null){
            return;
        }

        Uri uri=intent.getData();

        if (uri==null){
            return;
        }

        int w=intent.getIntExtra("ww",0);
        int h=intent.getIntExtra("hh",0);

        cropImageView.setAspectRatio(w, h);
        cropImageView.setFixedAspectRatio(true);
        cropImageView.setCropShape(CropImageView.CropShape.RECTANGLE);
        cropImageView.setScaleType(CropImageView.ScaleType.FIT_CENTER);
        cropImageView.setCropRect(new Rect(0, 0, w, h));
        cropImageView.setMinimumHeight(h);
        cropImageView.setMinimumWidth(w);


        cropImageView.setImageUriAsync(uri);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.crop_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.crop_ac:

                saveAsFile();

                break;

        }

        return true;
    }


    private void saveAsFile(){

        Intent intent=getIntent();

        String p=intent.getStringExtra("type");

        if (p==null){
            return;
        }

        Bitmap cropped = cropImageView.getCroppedImage();

        int w=intent.getIntExtra("ww",0);
        int h=intent.getIntExtra("hh",0);

        cropped=scale(cropped,w,h);

        saveFile(cropped,p);

        Intent intent1=new Intent();

        int r=intent.getIntExtra("res",0);

        setResult(r,intent1);

        finish();

//        Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show();

    }

    private Bitmap scale(Bitmap source,int reW,int reH){

//
        Log.d("lin16--sw->>>",""+source.getWidth());
        Log.d("lin16--sh->>>",""+source.getHeight());

        int w=source.getWidth();
        int h=source.getHeight();

        if (w<=0||h<=0){

            Log.d("lin16--->>>","宽高不对");

            return source;
        }

        float s=(1.0f*reW)/(1.0f*source.getWidth());

        float j=(1.0f*reH)/(1.0f*source.getHeight());

        Log.d("lin16--s->>>",""+s);

        if (s<=0f||j<=0f){
            Log.d("lin16--->>>","尺寸不对");
            return source;
        }

        int dw= (int) (s*w);

        int dh= (int) (j*h);

        Bitmap resizeBmp = Bitmap.createScaledBitmap(source,dw,dh,false);

        Log.d("lin16--w->>>",""+resizeBmp.getWidth());
        Log.d("lin16--h->>>",""+resizeBmp.getHeight());

        Log.d("lin16--dw->>>",""+dw);
        Log.d("lin16--dh->>>",""+dh);

        return resizeBmp;
    }


}
