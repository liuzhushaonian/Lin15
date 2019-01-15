package com.xp.legend.lin16.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class HeaderInfo implements Parcelable{

    private int width;

    private int height;

    public HeaderInfo() {
    }

    public HeaderInfo(int width, int height) {
        this.width = width;
        this.height = height;
    }

    protected HeaderInfo(Parcel in) {
        width = in.readInt();
        height = in.readInt();
    }

    public static final Creator<HeaderInfo> CREATOR = new Creator<HeaderInfo>() {
        @Override
        public HeaderInfo createFromParcel(Parcel in) {
            return new HeaderInfo(in);
        }

        @Override
        public HeaderInfo[] newArray(int size) {
            return new HeaderInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(width);
        dest.writeInt(height);
    }

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
}
