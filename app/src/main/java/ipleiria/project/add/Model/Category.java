package ipleiria.project.add.Model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.unnamed.b.atv.model.TreeNode;

import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Utils.JsonObject;

/**
 * Created by Lisboa on 11-Apr-17.
 */

public class Category extends JsonObject{

    public static final String JSON_TAG_DISPLAY_NAME = "name";
    public static final String JSON_TAG_REFERENCE = "reference";
    public static final String JSON_TAG_CHILDREN = "children";

    @Expose
    @SerializedName(JSON_TAG_DISPLAY_NAME)
    private String name;
    @Expose
    @SerializedName(JSON_TAG_CHILDREN)
    private List<Category> children;
    @Expose
    @SerializedName(JSON_TAG_REFERENCE)
    private int reference;

    private String realReference;
    private Category parent;

    public Category(String name, int reference){
        this.name = name;
        this.reference = reference;
        this.realReference = String.valueOf(reference);
        this.children = new LinkedList<>();
    }

    public Category(String name, int reference, List<Category> children){
        this.name = name;
        this.reference = reference;
        this.realReference = String.valueOf(reference);
        this.children = children;
    }

    public void addChild(Category category){
        category.parent = this;
        category.realReference = this.realReference + "." + category.reference;
        children.add(category);
    }

    public void addChildren(Category... children){
        for(Category category: children){
            addChild(category);
        }
    }

    public Category getChild(int i){
        return children.get(i);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChildren(List<Category> children) {
        this.children = children;
    }

    public List<Category> getChildren(){
        return children;
    }

    public boolean hasChildren(){
        return children != null && !children.isEmpty();
    }

    public int getReference() {
        return reference;
    }

    public void setReference(int reference) {
        this.reference = reference;
    }

    public Category getParent() {
        return parent;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    // max depth/level is 3 (or 2 parents)
    public boolean isLeaf(){
        int level = 1;
        Category root = this;
        while (root.parent != null) {
            root = root.parent;
            level++;
        }
        return level == 3 && (children == null || children.isEmpty());
    }

    public Category getRoot() {
        Category root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root;
    }

    public String getRealReference(){
        return realReference;
    }
}
