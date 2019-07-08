package com.xp.legend.lin15.fragment;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.xp.legend.lin15.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {


    public BaseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        TextView textView = new TextView(getActivity());
        textView.setText(R.string.hello_blank_fragment);
        return textView;
    }

    protected void openAlbum(int code) {
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,code);
    }

    protected void startCropImage(Uri uri, int w, int h, int code) {
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
//        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent, code);
    }

    /**
     * 保存为文件，注意申请权限
     */
    protected File saveAsFile(Uri uri) throws Exception {

        File outFile = new File(getContext().getFilesDir()+"/lin15","pic");

        if (!outFile.getParentFile().exists()){
            outFile.getParentFile().mkdirs();
        }

        InputStream in = null;
        FileOutputStream out = null;
//        BitmapFactory.Options options = null;
        try {

            // save bitmap
            in = getContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(in, null, null);
            out = new FileOutputStream(outFile);
            bitmap.compress(Bitmap.CompressFormat.WEBP, 100, out);
            out.flush();
            bitmap.recycle();
        } finally {
            try { in.close(); } catch (Exception ignored) { }
            try { out.close(); } catch (Exception ignored) { }
        }
        return outFile;


    }

    protected Uri getFileUri(File file){

        if (file==null){

            Log.d("file-->>","file is null");

            return null;
        }

        try {
            return Uri.parse(MediaStore.Images.Media.insertImage(
                    getContext().getContentResolver(), file.getAbsolutePath(), null, null));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return null;

    }


}
