package ipleiria.project.add.Model;

import android.content.SharedPreferences;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lisboa on 11-Apr-17.
 */

public class ApplicationData {

    private static ApplicationData instance = null;

    private String userUID;
    private SharedPreferences sharedPreferences;

    private List<Category> dimensions;
    private List<Category> areas;
    private List<Category> categories;

    private ApplicationData(){
        categories = new LinkedList<>();
        dimensions = new LinkedList<>();
        areas = new LinkedList<>();
    }

    public static ApplicationData getInstance() {
        if(instance == null) {
            instance = new ApplicationData();
        }
        return instance;
    }

    public static ApplicationData newInstance() {
        instance = new ApplicationData();
        return instance;
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences){
        this.sharedPreferences = sharedPreferences;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getUserUID(){
        return userUID;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public void addCategories(Category... categories) {
        for(Category category: categories){
            addCategory(category);
        }
    }

    private void addCategory(Category category) {
        if(!categories.contains(category)){
            categories.add(category);
        }
    }

    private void addCategories(List<Category> categories) {
        for(Category category: categories){
            addCategory(category);
        }
    }

    public void addDimension(Category dimension){
        if(!dimensions.contains(dimension)){
            dimensions.add(dimension);
            if(dimension.hasChildren()){
                addAreas(dimension.getChildren());
            }
        }
    }

    public void addDimensions(Category... dimensions){
        for(Category dimension: dimensions){
            addDimension(dimension);
        }
    }

    public void addDimensions(List<Category> dimensions){
        for(Category dimension: dimensions){
            addDimension(dimension);
        }
    }

    public List<Category> getDimensions() {
        return dimensions;
    }

    public void addArea(Category area){
        if(!areas.contains(area)){
            areas.add(area);
            if(area.hasChildren()){
                addCategories(area.getChildren());
            }
        }
    }

    public void addAreas(Category... areas){
        for(Category area: areas){
            addArea(area);
        }
    }

    public void addAreas(List<Category> areas){
        for(Category area: areas){
            addArea(area);
        }
    }

    public List<Category> getAreas() {
        return areas;
    }
}
