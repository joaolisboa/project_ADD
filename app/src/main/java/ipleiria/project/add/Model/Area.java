package ipleiria.project.add.Model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lisboa on 12-Apr-17.
 */

public class Area extends Category{

    private Dimension dimension;
    private List<Criteria> criterias;

    public Area(String name, int reference){
        super(name, reference);
        this.criterias = new LinkedList<>();
    }

    public void addCriteria(Criteria criteria){
        criteria.setArea(this);
        criterias.add(criteria);
    }

    public void addCriterias(Criteria... criterias){
        for(Criteria criteria: criterias){
            addCriteria(criteria);
        }
    }

    public List<Criteria> getCriterias() {
        return criterias;
    }

    public Criteria getCriteria(int index){
        return criterias.get(index);
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }
}
