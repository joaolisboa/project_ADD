package ipleiria.project.add.Model;

/**
 * Created by Lisboa on 15-Apr-17.
 */

public class Email {

    private String email;
    private boolean verified;
    private String dbKey;

    public Email(){

    }

    public Email(String email, boolean verified) {
        this.email = email;
        this.verified = verified;
    }

    public Email(String email, boolean verified, String dbKey) {
        this.email = email;
        this.verified = verified;
        this.dbKey = dbKey;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }

    @Override
    public String toString(){
        return email + ":" + verified + ":" + dbKey;
    }
}