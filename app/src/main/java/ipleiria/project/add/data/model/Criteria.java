package ipleiria.project.add.data.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lisboa on 12-Apr-17.
 */

public class Criteria extends Category{

    private Area area;
    private List<Item> items;
    private Coordinate writeCell;
    private Coordinate readCell;
    private double finalPoints;

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
        return String.valueOf(getDimension().getReference()) + "." +
                getArea().getReference() + "." +
                reference;
    }

    public boolean contains(String query){
        return name.toLowerCase().contains(query);
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

    public Coordinate getWriteCell() {
        return writeCell;
    }

    public void setWriteCell(Coordinate writeCell) {
        this.writeCell = writeCell;
    }

    public Coordinate getReadCell() {
        return readCell;
    }

    public void setReadCell(Coordinate readCell) {
        this.readCell = readCell;
    }

    public static class Coordinate{

        public int x;
        public int y;
        public Coordinate(int x, int y){
            this.x = x;
            this.y = y;
        }

    }
    public double getPoints(){
        double points = 0;
        for(Item item: items){
            points += item.getWeight();
        }
        return points;
    }

    public double getFinalPoints() {
        return finalPoints;
    }

    public void setFinalPoints(double finalPoints) {
        this.finalPoints = finalPoints;
    }

    @Override
    public String toString(){
        return getDimension().getReference() + "." + getArea().getReference() + "." + reference + ". " + name;
    }
}
