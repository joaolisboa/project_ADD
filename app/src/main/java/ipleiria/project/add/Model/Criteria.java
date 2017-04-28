package ipleiria.project.add.Model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lisboa on 12-Apr-17.
 */

public class Criteria extends Category{

    private Area area;
    private List<Item> items;

    public Criteria(){
        items = new LinkedList<>();
    }

    public Criteria(String name, int reference){
        super(name, reference);
        items = new LinkedList<>();
    }

    public Dimension getDimension() {
        return area.getDimension();
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public void setDimension(Dimension dimension){
        area.setDimension(dimension);
    }

    public String getRealReference() {
        StringBuilder ref = new StringBuilder();
        ref.append(getDimension().getReference()).append(".");
        ref.append(getArea().getReference()).append(".");
        ref.append(reference);
        return ref.toString();
    }

    public boolean contains(String query){
        return name.toLowerCase().contains(query);
    }

    @Override
    public String toString(){
        return getDimension().getReference() + "." + getArea().getReference() + "." + reference + ". " + name;
    }

    public List<Item> getItems(){
        return items;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void deleteItem(Item item){
        items.remove(item);
    }
}
