package ipleiria.project.add.Model;

import java.io.File;

/**
 * Created by Lisboa on 04-Apr-17.
 */

public class Item {

    private File file;
    private String name;
    private String description;
    private Category category;
    private String categoryReference;

    public Item(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Item(String name, String description, Category category) throws Exception {
        this(name, description);
        setCategory(category);
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) throws Exception {
        if(!category.isLeaf()){
            throw new Exception("Category must be a leaf with depth of 3 (or 2 parents)");
        }
        this.categoryReference = category.getRealReference();
        this.category = category;
    }

    public int getDimension(){
        return category.getRoot().getReference();
    }

    public int getArea(){
        return category.getParent().getReference();
    }

    public String getCategoryReference() {
        return categoryReference;
    }
}
