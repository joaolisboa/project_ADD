package ipleiria.project.add.data.model;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lisboa on 12-Apr-17.
 */

public class Criteria extends Category{

    private Area area;
    private List<Item> items;
    // move deleted items to this list so they don't count towards the points
    private List<Item> deletedItems;
    private Coordinate writeCell;
    private Coordinate readCell;
    private double finalPoints;

    private String observations;
    private String requiredDocument;
    private String weightsInformation;

    public Criteria(){
        items = new LinkedList<>();
    }

    public Criteria(String name, int reference){
        super(name, reference);
        items = new LinkedList<>();
        deletedItems = new LinkedList<>();
    }

    public Dimension getDimension() {
        return area.getDimension();
    }

    public double getPoints(){
        return finalPoints;
    }

    @Override
    public int getNumberOfItems() {
        return items.size();
    }

    @Override
    public int getNumberOfDeletedItems() {
        return deletedItems.size();
    }

    public double getWeights(){
        double weights = 0;
        for(Item item: items){
            weights += item.getWeight();
        }
        return weights;
    }

    public void deleteItem(Item item){
        items.remove(item);
        deletedItems.add(item);
    }

    public void restoreItem(Item item){
        items.add(item);
        deletedItems.remove(item);
    }

    public void permanentlyDeleteItem(Item item){
        deletedItems.remove(item);
    }

    @Override
    public String getFormattedString() {
        return getRealReference() + " - " + name;
    }

    public void setFinalPoints(double finalPoints) {
        this.finalPoints = finalPoints;
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
        return getDimension().reference + "." +
                getArea().reference + "." +
                reference;
    }

    public boolean contains(String query){
        return name.toLowerCase().contains(query);
    }

    public List<Item> getItems(){
        return items;
    }

    public List<Item> getDeletedItems() {
        return deletedItems;
    }

    public void addItem(Item item) {
        if(!items.contains(item)) {
            items.add(item);
        }else{
            int pos = items.indexOf(item);
            items.remove(pos);
            items.add(pos, item);
        }
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getRequiredDocument() {
        return requiredDocument;
    }

    public void setRequiredDocument(String requiredDocument) {
        this.requiredDocument = requiredDocument;
    }

    public String getWeightsInformation() {
        return weightsInformation;
    }

    public void setWeightsInformation(String weightsInformation) {
        this.weightsInformation = weightsInformation;
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

    @Override
    public String toString(){
        return getDimension().getReference() + "." + getArea().getReference() + "." + reference + ". " + name;
    }
}
