package com.xp.legend.lin15.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Result implements Parcelable{

    private int alpha;

    private int quality;

    private boolean gao;

    private int gaoValue;

    private boolean isScroll;

    private boolean isSlit;

    private boolean isRadius;

    private int radiusValue;

    public Result() {
    }


    protected Result(Parcel in) {
        alpha = in.readInt();
        quality = in.readInt();
        gao = in.readByte() != 0;
        gaoValue = in.readInt();
        isScroll = in.readByte() != 0;
        isSlit = in.readByte() != 0;
        isRadius = in.readByte() != 0;
        radiusValue = in.readInt();
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

    public boolean isGao() {
        return gao;
    }

    public void setGao(boolean gao) {
        this.gao = gao;
    }

    public int getGaoValue() {
        return gaoValue;
    }

    public void setGaoValue(int gaoValue) {
        this.gaoValue = gaoValue;
    }

    public boolean isScroll() {
        return isScroll;
    }

    public void setScroll(boolean scroll) {
        isScroll = scroll;
    }


    public boolean isSlit() {
        return isSlit;
    }

    public void setSlit(boolean slit) {
        isSlit = slit;
    }

    public boolean isRadius() {
        return isRadius;
    }

    public void setRadius(boolean radius) {
        isRadius = radius;
    }

    public int getRadiusValue() {
        return radiusValue;
    }

    public void setRadiusValue(int radiusValue) {
        this.radiusValue = radiusValue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(alpha);
        dest.writeInt(quality);
        dest.writeByte((byte) (gao ? 1 : 0));
        dest.writeInt(gaoValue);
        dest.writeByte((byte) (isScroll ? 1 : 0));
        dest.writeByte((byte) (isSlit ? 1 : 0));
        dest.writeByte((byte) (isRadius ? 1 : 0));
        dest.writeInt(radiusValue);
    }
}
