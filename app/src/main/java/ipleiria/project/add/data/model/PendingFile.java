package ipleiria.project.add.data.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Lisboa on 13-Jun-17.
 */

public class PendingFile implements Parcelable{

    public static final String MEO_CLOUD = "MEO_CLOUD";
    public static final String DROPBOX = "DROPBOX";
    public static final String EMAIL = "EMAIL";

    private ItemFile file;
    private String provider;

    public PendingFile(ItemFile file, String provider) {
        this.file = file;
        this.provider = provider;
    }

    public ItemFile getItemFile() {
        return file;
    }

    public void setFile(ItemFile file) {
        this.file = file;
    }

    public String getFilename(){
        return file.getFilename();
    }

    public String getProvider() {
        return provider;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof PendingFile){
            return file.getFilename().equals(((PendingFile) obj).getFilename());
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return provider + ":" + getFilename();
    }

    protected PendingFile(Parcel in) {
        file = new ItemFile(in.readString());
        provider = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(getFilename());
        dest.writeString(provider);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<PendingFile> CREATOR = new Parcelable.Creator<PendingFile>() {
        @Override
        public PendingFile createFromParcel(Parcel in) {
            return new PendingFile(in);
        }

        @Override
        public PendingFile[] newArray(int size) {
            return new PendingFile[size];
        }
    };
}
