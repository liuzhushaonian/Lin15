package com.xp.legend.lin16.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class BaseActivity extends AppCompatActivity {

    protected SharedPreferences sharedPreferences;
    private static final String S="lin16_sh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences=getSharedPreferences(S,MODE_PRIVATE);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.TRANSPARENT);

    }

    protected void openAlbum(int code) {
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,code);
    }

    protected void startCropImage(Uri uri, int w, int h,int code) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        //设置数据uri和类型为图片类型
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //显示View为可裁剪的
        intent.putExtra("crop", true);
        //裁剪的宽高的比例为1:1
        intent.putExtra("aspectX", w);
        intent.putExtra("aspectY", h);
        //输出图片的宽高均为150
        intent.putExtra("outputX", w);
        intent.putExtra("outputY", h);

        //裁剪之后的数据是通过Intent返回
        intent.putExtra("return-data", false);

//        intent.putExtra("outImage", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection",true);

        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

//        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent, code);
    }

    protected void saveFile(Bitmap bitmap, String name) {

        File outFile = new File(getExternalFilesDir(null) + "/lin16", name);//临时文件

        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }

        FileOutputStream out = null;
        try {

            // save bitmap
            out = new FileOutputStream(outFile);

            if (bitmap != null) {

                Log.d("w---->>>",bitmap.getWidth()+"");
                Log.d("h---->>>",bitmap.getHeight()+"");

                bitmap.compress(Bitmap.CompressFormat.WEBP, 100, out);
            }
            out.flush();
            if (bitmap != null) {
                bitmap.recycle();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                out.close();
            } catch (Exception ignored) {
            }
        }
    }


}
