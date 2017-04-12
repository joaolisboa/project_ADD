package ipleiria.project.add.Model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lisboa on 12-Apr-17.
 */

public class Dimension extends Category{

    private List<Area> areas;

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

}
