package ipleiria.project.add.Model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lisboa on 04-Apr-17.
 */

public class Item {

    private List<ItemFile> filenames;
    private String description;
    private Criteria criteria;
    private boolean deleted = false;
    private String dbKey;

    public Item(){
        filenames = new LinkedList<>();
    }

    public Item(String description){
        this.description = description;
    }

    public Item(List<ItemFile> filenames, String description) {
        this.filenames = filenames;
        this.description = description;
    }

    public Item(List<ItemFile> filenames, String description, Criteria criteria){
        this(filenames, description);
        this.criteria = criteria;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        for(ItemFile file: filenames){
            file.setDeleted(deleted);
        }
        this.deleted = deleted;
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

    public void addFile(ItemFile filename) {
        filenames.add(filename);
    }

    public void addFile(String filename){
        filenames.add(new ItemFile(filename));
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

    public void setReference(String reference) {
        String[] s = reference.split("\\.");
        int dimension = Integer.valueOf(s[0]) - 1;
        int area = Integer.valueOf(s[1]) - 1;
        int criteria = Integer.valueOf(s[2]) - 1;
        this.criteria = ApplicationData.getInstance().getCriteria(dimension, area, criteria);
    }

    @Override
    public String toString(){
        return getCategoryReference() + ":" + description + ":" + dbKey + ":" + deleted;
    }

}
