package com.xp.legend.lin17.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.xp.legend.lin17.utils.Conf;

public class Result implements Parcelable{

    private int alpha=100;

    private int quality= Conf.LOW_QUALITY;

    private int gao=0;

    private int gaoValue=25;

    private int id;
    private String name="新建背景";
    private String shuFile="";
    private String shuHeaderFile="";
    private String hengFile="";
    private String hengHeaderFile="";
    private int del=0;//表示未删除

    public Result() {
    }


    protected Result(Parcel in) {
        alpha = in.readInt();
        quality = in.readInt();
        gao = in.readInt();
        gaoValue = in.readInt();
        id = in.readInt();
        name = in.readString();
        shuFile = in.readString();
        shuHeaderFile = in.readString();
        hengFile = in.readString();
        hengHeaderFile = in.readString();
        del = in.readInt();
    }

    public static final Creator<Result> CREATOR = new Creator<Result>() {
        @Override
        public Result createFromParcel(Parcel in) {
            return new Result(in);
        }

        @Override
        public Result[] newArray(int size) {
            return new Result[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(alpha);
        parcel.writeInt(quality);
        parcel.writeInt(gao);
        parcel.writeInt(gaoValue);
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(shuFile);
        parcel.writeString(shuHeaderFile);
        parcel.writeString(hengFile);
        parcel.writeString(hengHeaderFile);
        parcel.writeInt(del);
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public int getGao() {
        return gao;
    }

    public void setGao(int gao) {
        this.gao = gao;
    }

    public int getGaoValue() {
        return gaoValue;
    }

    public void setGaoValue(int gaoValue) {
        this.gaoValue = gaoValue;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShuFile() {
        return shuFile;
    }

    public void setShuFile(String shuFile) {
        this.shuFile = shuFile;
    }

    public String getShuHeaderFile() {
        return shuHeaderFile;
    }

    public void setShuHeaderFile(String shuHeaderFile) {
        this.shuHeaderFile = shuHeaderFile;
    }

    public String getHengFile() {
        return hengFile;
    }

    public void setHengFile(String hengFile) {
        this.hengFile = hengFile;
    }

    public String getHengHeaderFile() {
        return hengHeaderFile;
    }

    public void setHengHeaderFile(String hengHeaderFile) {
        this.hengHeaderFile = hengHeaderFile;
    }

    public int getDel() {
        return del;
    }

    public void setDel(int del) {
        this.del = del;
    }
}
