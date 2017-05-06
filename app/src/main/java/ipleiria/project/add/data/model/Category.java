package ipleiria.project.add.data.model;

/**
 * Created by Lisboa on 11-Apr-17.
 */

public class Category{

    protected String name;
    protected String dbKey;
    protected int reference;

    public Category(){

    }

    public Category(String name, int reference){
        this.name = name;
        this.reference = reference;
    }

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getReference() {
        return reference;
    }

    public void setReference(int reference) {
        this.reference = reference;
    }
}
