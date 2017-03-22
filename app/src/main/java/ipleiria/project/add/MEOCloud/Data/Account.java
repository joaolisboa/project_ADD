package ipleiria.project.add.MEOCloud.Data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ipleiria.project.add.Utils.JsonObject;

/**
 * Created by J on 21/03/2017.
 */

public class Account extends JsonObject{

    public static final String JSON_TAG_DISPLAY_NAME = "display_name";
    public static final String JSON_TAG_UID = "uid";
    public static final String JSON_TAG_LAST_EVENT = "last_event";
    public static final String JSON_TAG_ACTIVE = "active";
    public static final String JSON_TAG_EMAIl = "email";
    public static final String JSON_TAG_REFERRAL_CODE = "referral_code";
    public static final String JSON_TAG_CREATED_ON = "created_on";
    public static final String JSON_TAG_SEGMENT = "segment";
    public static final String JSON_TAG_REFERRAL_LINK = "referral_link";
    public static final String JSON_TAG_QUOTA_INFO = "quota_info";

    @Expose
    @SerializedName(JSON_TAG_DISPLAY_NAME)
    private String displayName;
    @Expose
    @SerializedName(JSON_TAG_UID)
    private String uid;
    @Expose
    @SerializedName(JSON_TAG_LAST_EVENT)
    private String lastEvent;
    @Expose
    @SerializedName(JSON_TAG_ACTIVE)
    private Boolean active;
    @Expose
    @SerializedName(JSON_TAG_EMAIl)
    private String email;
    @Expose
    @SerializedName(JSON_TAG_REFERRAL_CODE)
    private String referralCode;
    @Expose
    @SerializedName(JSON_TAG_CREATED_ON)
    private String createdOn;
    @Expose
    @SerializedName(JSON_TAG_SEGMENT)
    private String segment;
    @Expose
    @SerializedName(JSON_TAG_REFERRAL_LINK)
    private String referralLink;
    @Expose
    @SerializedName(JSON_TAG_QUOTA_INFO)
    private Quota quota;

    public String getDisplayName() {
        return displayName;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public String getUid() {
        return uid;
    }
    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getLastEvent() {
        return lastEvent;
    }
    public void setLastEvent(String lastEvent) {
        this.lastEvent = lastEvent;
    }
    public Boolean getActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getReferralCode() {
        return referralCode;
    }
    public void setReferralCode(String referralCode) {
        this.referralCode = referralCode;
    }
    public String getCreatedOn() {
        return createdOn;
    }
    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }
    public String getSegment() {
        return segment;
    }
    public void setSegment(String segment) {
        this.segment = segment;
    }
    public String getReferralLink() {
        return referralLink;
    }
    public void setReferralLink(String referralLink) {
        this.referralLink = referralLink;
    }
    public Quota getQuota() {
        return quota;
    }
    public void setQuota(Quota quota) {
        this.quota = quota;
    }

}
