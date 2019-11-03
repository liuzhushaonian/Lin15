package com.xp.legend.lin16.fragment;



import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.theartofdev.edmodo.cropper.CropImage;
import com.xp.legend.lin16.R;
import com.xp.legend.lin16.activity.CropActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class BaseFragment extends Fragment {

    protected List<Uri> uriList = new ArrayList<>();


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
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, code);
    }

    protected void startCropImage(Uri uri, int w, int h, int code) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        //设置数据uri和类型为图片类型
        intent.setDataAndType(uri, "image/*");
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //显示View为可裁剪的
        intent.putExtra("crop", true);
        //裁剪的宽高的比例为1:1
        intent.putExtra("aspectX", w);
        intent.putExtra("aspectY", h);
        //输出图片的宽高均为150
        intent.putExtra("outputX", w);
        intent.putExtra("outputY", h);

        //裁剪之后的数据是通过Intent返回
        intent.putExtra("return-data", true);

//        intent.putExtra("outImage", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        startActivityForResult(intent, code);
    }

    /**
     * 保存为文件，注意申请权限
     */
    protected File saveAsFile(Uri uri) throws Exception {

        File outFile = new File(Objects.requireNonNull(getContext()).getCacheDir()+"/cut", "pic.png");//临时文件

        if (!outFile.getParentFile().exists()) {
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
            try {
                in.close();
            } catch (Exception ignored) {
            }
            try {
                out.close();
            } catch (Exception ignored) {
            }
        }
        return outFile;


    }

    @Deprecated
    protected Uri getFileUri(File file) {

        Uri uri = null;

        if (file == null) {

            Log.d("file-->>", "file is null");

            return null;
        }

        try {
            uri = Uri.parse(MediaStore.Images.Media.insertImage(
                    getContext().getContentResolver(), file.getAbsolutePath(), null, null));

            uriList.add(uri);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return uri;

    }

    protected void cleanUri() {

        File file = new File(Objects.requireNonNull(getContext()).getCacheDir()+"/cut","pic.png");

        if (file.isDirectory()) {

            File[] f = file.listFiles();
            if (f != null) {
                for (File ff : f) {
                    ff.delete();
                }
            }
        }


//        for (int i=0;i<uriList.size();i++){
//
//            getContext().getContentResolver().delete(uriList.get(i),null,null);
//
//        }
//
//        uriList.clear();//清除

    }

    protected void saveFile(Uri uri, String name) {

        File outFile = new File(Objects.requireNonNull(getContext()).getExternalFilesDir(null) + "/lin16", name);//临时文件

        if (!outFile.getParentFile().exists()) {
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
                in.close();
            } catch (Exception ignored) {
            }
            try {
                out.close();
            } catch (Exception ignored) {
            }
        }

        Log.d("out--->>>",outFile.toString());
    }

    protected File getFile(String name) {

        File file = new File(getContext().getExternalFilesDir(null) + "/lin16/" + name);

        return file;

    }

    protected void setPermission(Uri uri) {

        Objects.requireNonNull(getActivity()).grantUriPermission("com.android.systemui", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

    }

    protected void cropPicture(int w,int h,Uri uri,String name,int code,Fragment fragment){

        Intent intent=new Intent(getActivity(), CropActivity.class);

        intent.putExtra("type",name);

        intent.setData(uri);

        intent.putExtra("ww",w);
        intent.putExtra("hh",h);
        intent.putExtra("res",code);

        fragment.startActivityForResult(intent,code);

    }

}
