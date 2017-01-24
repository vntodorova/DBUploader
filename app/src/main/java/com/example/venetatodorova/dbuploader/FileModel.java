package com.example.venetatodorova.dbuploader;

import android.os.Parcel;
import android.os.Parcelable;

class FileModel implements Parcelable {
    private String name;
    private String path;
    private boolean isChecked;

    FileModel(String name, String path, boolean isChecked) {
        this.name = name;
        this.path = path;
        this.isChecked = isChecked;
    }

    private FileModel(Parcel in) {
        this.name = in.readString();
        this.path = in.readString();
        this.isChecked = in.readByte() != 0;
    }

    public String getName() {
        return name;
    }

    boolean getChecked() {
        return isChecked;
    }

    public String getPath() {
        return path;
    }

    void setChecked(boolean checked) {
        this.isChecked = checked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(path);
        parcel.writeByte((byte)(isChecked ? 1 : 0));
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public FileModel createFromParcel(Parcel in) {
            return new FileModel(in);
        }

        public FileModel[] newArray(int size) {
            return new FileModel[size];
        }
    };
}
