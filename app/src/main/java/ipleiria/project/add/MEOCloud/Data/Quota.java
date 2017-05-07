package ipleiria.project.add.MEOCloud.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import ipleiria.project.add.Utils.JsonObject;

/**
 * Created by J on 22/03/2017.
 */

public class Quota extends JsonObject{

    public static final String JSON_TAG_SHARED = "shared";
    public static final String JSON_TAG_QUOTA = "quota";
    public static final String JSON_TAG_NORMAL = "normal";

    @Expose
    @SerializedName(JSON_TAG_SHARED)
    private Long shared;
    @Expose
    @SerializedName(JSON_TAG_QUOTA)
    private Long quota;
    @Expose
    @SerializedName(JSON_TAG_NORMAL)
    private Long normal;

    public Long getShared() {
        return shared;
    }
    public void setShared(Long shared) {
        this.shared = shared;
    }
    public Long getQuota() {
        return quota;
    }
    public void setQuota(Long quota) {
        this.quota = quota;
    }
    public Long getNormal() {
        return normal;
    }
    public void setNormal(Long normal) {
        this.normal = normal;
    }

}
