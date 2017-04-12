package ipleiria.project.add.Model;

import java.io.File;

/**
 * Created by Lisboa on 04-Apr-17.
 */

public class Item {

    private File file;
    private String name;
    private String description;
    private Criteria criteria;

    public Item(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Item(String name, String description, Criteria criteria){
        this(name, description);
        this.criteria = criteria;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
