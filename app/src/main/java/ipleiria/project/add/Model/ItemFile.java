package ipleiria.project.add.Model;

/**
 * Created by J on 19/04/2017.
 */

public class ItemFile {

    private String filename;
    private boolean deleted = false;
    private String dbKey;

    public ItemFile(){

    }

    public ItemFile(String filename) {
        this.filename = filename;
    }

    public ItemFile(String filename, String dbKey){
        this.filename = filename;
        this.dbKey = dbKey;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
