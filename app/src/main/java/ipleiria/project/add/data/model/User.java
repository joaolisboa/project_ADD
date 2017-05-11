package ipleiria.project.add.data.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lisboa on 04-May-17.
 */

public class User {

    @NonNull String uid;

    String name;
    String email;
    List<String> secondaryEmails;
    Uri photo_url;
    boolean isAnonymous;

    public User() {
    }

    public User(String uid) {
        this.uid = uid;
        this.secondaryEmails = new LinkedList<>();
    }

    public User(String uid, String email, Uri photo_url, String name) {
        this(uid);
        this.email = email;
        this.photo_url = photo_url;
        this.name = name;
    }

    @NonNull
    public String getUid() {
        return uid;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    public void setEmail(@Nullable String email) {
        this.email = email;
    }

    @Nullable
    public Uri getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(@Nullable Uri photo_url) {
        this.photo_url = photo_url;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @NonNull
    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(@NonNull boolean anonymous) {
        isAnonymous = anonymous;
    }

    @Override
    public String toString(){
        return name + ":" + email + ":" + isAnonymous;
    }
}
