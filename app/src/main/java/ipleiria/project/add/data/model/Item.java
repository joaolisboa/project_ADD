package ipleiria.project.add.data.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lisboa on 04-Apr-17.
 */

public class Item {

    private List<ItemFile> files;
    private List<ItemFile> deletedFiles;

    private List<String> tags;

    private String description;
    private Criteria criteria;
    private String dbKey;
    private long weight;

    public Item(){
        tags = new LinkedList<>();
        files = new LinkedList<>();
        deletedFiles = new LinkedList<>();
        weight = 1;
    }

    public Item(String description){
        this();
        this.description = description;
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
        this.files.addAll(files);
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

    public void setCriteria(Criteria criteria, boolean deleted){
        this.criteria = criteria;
        if(deleted){
            criteria.addDeletedItem(this);
        }else {
            criteria.addItem(this);
        }
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

    public long getWeight() {
        return weight;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public void addTag(String tag){
        tags.add(tag);
    }

    public void setTags(List<String> tags){
        this.tags = tags;
    }

    public List<String> getTags(){
        return tags;
    }

    public void removeTag(String tag) {
        tags.remove(tag);
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
