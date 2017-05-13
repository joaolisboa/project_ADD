package ipleiria.project.add.data.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        return obj.getClass() == getClass() && dbKey.equals(((Category) obj).getDbKey());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dbKey);
    }
}
