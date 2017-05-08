package ipleiria.project.add.data.model;

import com.google.firebase.database.DataSnapshot;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.FirebaseHandler;
import ipleiria.project.add.Model.ApplicationData;

/**
 * Created by Lisboa on 04-Apr-17.
 */

public class Item {

    private List<ItemFile> filenames;
    private String description;
    private Criteria criteria;
    private String dbKey;
    private int weight;

    public Item(){
        filenames = new LinkedList<>();
        weight = 1;
    }

    public Item(String description){
        this();
        this.description = description;
    }

    public Item(List<ItemFile> filenames, String description) {
        this.filenames = filenames;
        for(ItemFile files: filenames){
            files.setParent(this);
        }
        this.description = description;
        weight = 1;
    }

    public Item(List<ItemFile> filenames, String description, Criteria criteria){
        this(filenames, description);
        this.criteria = criteria;
    }

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }

    public List<ItemFile> getFiles() {
        return filenames;
    }

    public List<ItemFile> getDeletedFiles(){
        List<ItemFile> deletedItems = new LinkedList<>();
        for(ItemFile file: filenames){
            if(file.isDeleted()){
                deletedItems.add(file);
            }
        }
        return deletedItems;
    }

    public boolean isItemDeleted(){
        // item is considered deleted if all files are deleted
        // and is in AppData.deletedItems
        for(ItemFile file: filenames){
            if(!file.isDeleted()){
                return false;
            }
        }
        return ApplicationData.getInstance().getDeletedItems().contains(this);
    }

    public boolean hasDeletedFiles(){
        return !getDeletedFiles().isEmpty();
    }

    public List<ItemFile> getFiles(boolean flag){
        List<ItemFile> files = new LinkedList<>();
        for(ItemFile file: filenames){
            if(file.isDeleted() == flag){
                files.add(file);
            }
        }
        return files;
    }

    public void addFile(ItemFile filename) {
        filenames.add(filename);
        filename.setParent(this);
    }

    public void addFile(String filename){
        filenames.add(new ItemFile(filename, this));
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

    public void addFiles(List<ItemFile> itemFiles) {
        filenames.addAll(itemFiles);
        for(ItemFile files: itemFiles){
            files.setParent(this);
        }
    }

    public void deleteFile(ItemFile file) {
        file.setDeleted(true);
        if(isItemDeleted()){

        }
        FirebaseHandler.getInstance().writeItem(this);
    }

    public void restoreFile(ItemFile file){
        file.setDeleted(false);
        FirebaseHandler.getInstance().writeItem(this);

    }

    public void permanentlyDeleteFile(ItemFile file){
        filenames.remove(file);
        FirebaseHandler.getInstance().writeItem(this);
    }

    public void clearDeleteFiles() {
        for(int i = 0; i < getDeletedFiles().size(); i++) {
            filenames.remove(getDeletedFiles().get(i));
        }
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
        if(object == null || getClass() != object.getClass())
            return false;

        return dbKey.equals(((Item) object).getDbKey());
    }
}
