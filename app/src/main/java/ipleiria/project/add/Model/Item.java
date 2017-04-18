package ipleiria.project.add.Model;

/**
 * Created by Lisboa on 04-Apr-17.
 */

public class Item {

    private String filename;
    private String description;
    private Criteria criteria;
    private String dbKey;

    public Item(){

    }

    public Item(String filename, String description) {
        this.filename = filename;
        this.description = description;
    }

    public Item(String name, String description, Criteria criteria){
        this(name, description);
        this.criteria = criteria;
    }

    public String getDbKey() {
        return dbKey;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
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
        return getCategoryReference() + ":" + filename + ":" + dbKey;
    }

}
