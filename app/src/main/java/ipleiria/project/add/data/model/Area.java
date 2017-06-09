package ipleiria.project.add.data.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lisboa on 12-Apr-17.
 */

public class Area extends Category{

    private Dimension dimension;
    private List<Criteria> criterias;

    public Area(){

    }

    public Area(String name, int reference){
        super(name, reference);
        this.criterias = new LinkedList<>();
    }

    public double getPoints(){
        double points = 0;
        // the special case with criterias where all first 4 criterias are handled as a single cell
        // for points evaluation
        // in this case we only return the points of the first criteria since they shouldn't be added
        // ie. if one criteria has 5 points, it'll return 20 instead of 5
        if(dimension.reference == 1 && reference == 1){
            return criterias.get(0).getPoints();
        }
        for(Criteria criteria: criterias){
            points += criteria.getPoints();
        }
        return points;
    }

    @Override
    public String getFormattedString() {
        return dimension.getReference() + "." + reference + " - " + name;
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

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("\n\tarea: " + reference + ". " + name);
        for(Criteria c: criterias){
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }
}
