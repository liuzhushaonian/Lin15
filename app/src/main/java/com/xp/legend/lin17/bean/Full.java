package com.xp.legend.lin17.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Full implements Parcelable{

    private int width;

    private int height;

    protected Full(Parcel in) {
        width = in.readInt();
        height = in.readInt();
    }

    public static final Creator<Full> CREATOR = new Creator<Full>() {
        @Override
        public Full createFromParcel(Parcel in) {
            return new Full(in);
        }

        @Override
        public Full[] newArray(int size) {
            return new Full[size];
        }
    };

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Full() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(width);
        dest.writeInt(height);
    }
}
