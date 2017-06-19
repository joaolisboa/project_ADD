package ipleiria.project.add.data.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lisboa on 12-Apr-17.
 */

public class Dimension extends Category{

    private List<Area> areas;

    public Dimension(){

    }

    public Dimension(String name, int reference){
        super(name, reference);
        this.areas = new LinkedList<>();
    }

    @Override
    public double getPoints(){
        double points = 0;
        for(Area area: areas){
            points += area.getPoints();
        }
        return points;
    }

    @Override
    public String getFormattedString() {
        return reference + " - " + name;
    }

    @Override
    public int getNumberOfItems() {
        int num = 0;
        for(Area area: areas){
            num += area.getNumberOfItems();
        }
        return num;
    }

    @Override
    public int getNumberOfDeletedItems() {
        int num = 0;
        for(Area area: areas){
            num += area.getNumberOfDeletedItems();
        }
        return num;
    }

    public void addArea(Area area){
        area.setDimension(this);
        areas.add(area);
    }

    public void addAreas(Area... areas){
        for(Area area: areas){
            addArea(area);
        }
    }

    public List<Area> getAreas() {
        return areas;
    }

    public Area getArea(int index){
        return areas.get(index);
    }

    public Area getArea(String key){
        for(Area area: areas){
            if(area.getDbKey().equals(key)){
                return area;
            }
        }
        return null;
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("dimension: " + reference + ". " + name);
        for(Area a: areas){
            stringBuilder.append(a);
        }
        return stringBuilder.toString();
    }

}
