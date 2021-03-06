package com.vlk.multimager.utils;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by vansikrishna on 08/06/2016.
 */
public class Image implements Parcelable {

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel source) {
            return new Image(source);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
    public long _id;
    public Uri uri;
    public String imagePath;
    public boolean isPortraitImage;

    public Image(long _id, Uri uri, String imagePath, boolean isPortraitImage) {
        this._id = _id;
        this.uri = uri;
        this.imagePath = imagePath;
        this.isPortraitImage = isPortraitImage;
    }

    protected Image(Parcel in) {
        this._id = in.readLong();
        this.uri = in.readParcelable(Uri.class.getClassLoader());
        this.imagePath = in.readString();
        this.isPortraitImage = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this._id);
        dest.writeParcelable(this.uri, flags);
        dest.writeString(this.imagePath);
        dest.writeByte(this.isPortraitImage ? (byte) 1 : (byte) 0);
    }
}
