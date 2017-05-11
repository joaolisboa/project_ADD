package ipleiria.project.add.data.model;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.FirebaseHandler;
import ipleiria.project.add.Model.ApplicationData;

/**
 * Created by Lisboa on 04-Apr-17.
 */

public class Item {

    private List<ItemFile> files;
    private List<ItemFile> deletedFiles;

    private String description;
    private Criteria criteria;
    private String dbKey;
    private int weight;

    public Item(){
        files = new LinkedList<>();
        deletedFiles = new LinkedList<>();
        weight = 1;
    }

    public Item(String description){
        this();
        this.description = description;
    }

    public Item(List<ItemFile> files, String description) {
        this.files = files;
        this.deletedFiles = new LinkedList<>();
        for(ItemFile file: files){
            file.setParent(this);
        }
        this.description = description;
        weight = 1;
    }

    public Item(List<ItemFile> files, String description, Criteria criteria){
        this(files, description);
        this.criteria = criteria;
    }

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }

    public List<ItemFile> getFiles() {
        return files;
    }

    public List<ItemFile> getDeletedFiles(){
        return deletedFiles;
    }

    public void addFile(ItemFile file) {
        files.add(file);
        file.setParent(this);
    }

    public void addFiles(List<ItemFile> files) {
        files.addAll(files);
        for(ItemFile file: files){
            file.setParent(this);
        }
    }

    public void addDeletedFile(ItemFile file) {
        deletedFiles.add(file);
        file.setParent(this);
    }

    public void addDeletedFiles(List<ItemFile> deletedFiles){
        files.addAll(deletedFiles);
        for(ItemFile file: deletedFiles){
            file.setParent(this);
        }
    }

    public void clearFiles() {
        files = new LinkedList<>();
    }

    public void clearDeletedFiles() {
        deletedFiles = new LinkedList<>();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Criteria getCriteria() {
        return criteria;
    }

    public void setCriteria(Criteria criteria){
        this.criteria = criteria;
        criteria.addItem(this);
    }

    public Dimension getDimension(){
        return criteria.getDimension();
    }

    public Area getArea(){
        return criteria.getArea();
    }

    public String getCategoryReference() {
        return criteria.getRealReference();
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    @Override
    public String toString(){
        return getCategoryReference() + ":" + description + ":" + dbKey;
    }

    @Override
    public boolean equals(Object object){
        if(this == object) return true;
        if(object == null || getClass() != object.getClass()) {
            return false;
        }
        if(dbKey == null || ((Item) object).getDbKey() == null){
            return false;
        }
        return dbKey.equals(((Item) object).getDbKey());
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hashCode(dbKey);
    }
}
