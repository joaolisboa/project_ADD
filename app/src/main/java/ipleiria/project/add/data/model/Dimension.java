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

    public Dimension(String name, int reference){
        super(name, reference);
        this.areas = new LinkedList<>();
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
