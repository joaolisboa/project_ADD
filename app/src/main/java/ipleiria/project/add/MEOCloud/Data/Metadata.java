package ipleiria.project.add.MEOCloud.Data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import ipleiria.project.add.MEOCloud.ErrorMessageResponse;
import ipleiria.project.add.Utils.HttpStatus;
import ipleiria.project.add.Utils.JsonObject;

/**
 * Created by Lisboa on 26-Mar-17.
 */

public class Metadata extends JsonObject implements ErrorMessageResponse {

    public static final String JSON_TAG_HASH = "hash";
    public static final String JSON_TAG_BYTES = "bytes";
    public static final String JSON_TAG_THUMB_EXISTS = "thumb_exists";
    public static final String JSON_TAG_REV = "rev";
    public static final String JSON_TAG_MODIFIED = "modified";
    public static final String JSON_TAG_CLIENT_MODIFIED = "client_mtime";
    public static final String JSON_TAG_IS_LINK = "is_link";
    public static final String JSON_TAG_PATH = "path";
    public static final String JSON_TAG_IS_DIR = "is_dir";
    public static final String JSON_TAG_ROOT = "root";
    public static final String JSON_TAG_SIZE = "size";
    public static final String JSON_TAG_ICON = "icon";
    public static final String JSON_TAG_MIME_TYPE = "mime_type";
    public static final String JSON_TAG_CONTENTS = "contents";

    @Expose
    @SerializedName(JSON_TAG_REV)
    private String rev;
    @Expose
    @SerializedName(JSON_TAG_THUMB_EXISTS)
    private Boolean thumbExists;
    @Expose
    @SerializedName(JSON_TAG_BYTES)
    private Long bytes;
    @Expose
    @SerializedName(JSON_TAG_MODIFIED)
    private String modified;
    @Expose
    @SerializedName(JSON_TAG_PATH)
    private String path;
    @Expose
    @SerializedName(JSON_TAG_IS_DIR)
    private Boolean isDir;
    @Expose
    @SerializedName(JSON_TAG_SIZE)
    private String size;
    @Expose
    @SerializedName(JSON_TAG_ROOT)
    private String root;
    @Expose
    @SerializedName(JSON_TAG_HASH)
    private String hash;
    @Expose
    @SerializedName(JSON_TAG_IS_LINK)
    private Boolean isLink;
    @Expose
    @SerializedName(JSON_TAG_ICON)
    private String icon;
    @Expose
    @SerializedName(JSON_TAG_MIME_TYPE)
    private String mimeType;
    @Expose
    @SerializedName(JSON_TAG_CONTENTS)
    private List<Metadata> contents;
    @Expose
    @SerializedName(JSON_TAG_CLIENT_MODIFIED)
    private String clientModified;

    @Override
    public String processRequestCode(int code) {
        switch(code){
            case HttpStatus.NOT_MODIFIED:
                // case "hash" parameters is sent in request
                return "Resource hasn't changed";
            case HttpStatus.NOT_ACCEPTABLE:
                return "Too many entries - limit 10.000";
        }
        return HttpStatus.processRequestCode(code);
    }

    public String getClientModified() {
        return clientModified;
    }

    public void setClientModified(String clientModified) {
        this.clientModified = clientModified;
    }

    public String getRev() {
        return rev;
    }

    public void setRev(String rev) {
        this.rev = rev;
    }

    public Boolean getThumbExists() {
        return thumbExists;
    }

    public void setThumbExists(Boolean thumbExists) {
        this.thumbExists = thumbExists;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public String getModified() {
        return modified;
    }

    public void setModified(String modified) {
        this.modified = modified;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Boolean getDir() {
        return isDir;
    }

    public void setDir(Boolean dir) {
        isDir = dir;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Boolean getLink() {
        return isLink;
    }

    public void setLink(Boolean link) {
        isLink = link;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public List<Metadata> getContents() {
        return contents;
    }

    public void setContents(List<Metadata> contents) {
        this.contents = contents;
    }


}
