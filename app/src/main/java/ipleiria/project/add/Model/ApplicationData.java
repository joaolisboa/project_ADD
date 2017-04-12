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

    private List<Dimension> dimensions;
    private List<Area> areas;
    private List<Criteria> criterias;

    private ApplicationData(){
        criterias = new LinkedList<>();
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

    public List<Criteria> getCriterias() {
        return criterias;
    }

    public void addCriterias(Criteria... criterias) {
        for(Criteria criteria: criterias){
            addCriteria(criteria);
        }
    }

    private void addCriteria(Criteria criteria) {
        if(!criterias.contains(criteria)){
            criterias.add(criteria);
        }
    }

    private void addCriterias(List<Criteria> criterias) {
        for(Criteria criteria: criterias){
            addCriteria(criteria);
        }
    }

    public void addDimension(Dimension dimension){
        if(!dimensions.contains(dimension)){
            dimensions.add(dimension);
            if(!dimension.getAreas().isEmpty()){
                addAreas(dimension.getAreas());
            }
        }
    }

    public void addDimensions(Dimension... dimensions){
        for(Dimension dimension: dimensions){
            addDimension(dimension);
        }
    }

    public void addDimensions(List<Dimension> dimensions){
        for(Dimension dimension: dimensions){
            addDimension(dimension);
        }
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public void addArea(Area area){
        if(!areas.contains(area)){
            areas.add(area);
            if(!area.getCriterias().isEmpty()){
                addCriterias(area.getCriterias());
            }
        }
    }

    public void addAreas(Area... areas){
        for(Area area: areas){
            addArea(area);
        }
    }

    public void addAreas(List<Area> areas){
        for(Area area: areas){
            addArea(area);
        }
    }

    public List<Area> getAreas() {
        return areas;
    }
}
