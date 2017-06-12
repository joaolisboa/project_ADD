package ipleiria.project.add.data.model;

import android.annotation.SuppressLint;
import android.net.Uri;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lisboa on 04-May-17.
 */

public class User {

    private String uid;
    private String name;
    private String email;
    private Uri photoUrl;
    private boolean isAnonymous;
    private String department;
    private List<EvaluationPeriod> evaluationPeriods;

    public User() {
    }

    public User(String uid) {
        this.uid = uid;
        this.evaluationPeriods = new LinkedList<>();
    }

    public User(String uid, String email, Uri photoUrl, String name) {
        this(uid);
        this.email = email;
        this.photoUrl = photoUrl;
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setDepartment(String department){
        this.department = department;
    }

    public String getDepartment() {
        return department;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Uri getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(Uri photoUrl) {
        this.photoUrl = photoUrl;
    }

    public List<EvaluationPeriod> getEvaluationPeriods() {
        return evaluationPeriods;
    }

    public void addEvaluationPeriod(EvaluationPeriod period){
        evaluationPeriods.add(period);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAnonymous() {
        return isAnonymous;
    }

    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    @Override
    public String toString(){
        return name + ":" + email + ":" + isAnonymous;
    }

}
