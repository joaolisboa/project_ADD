package ipleiria.project.add.Model;

import android.content.SharedPreferences;

import java.io.File;
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
    private List<Item> items;

    private ApplicationData() {
        criterias = new LinkedList<>();
        dimensions = new LinkedList<>();
        areas = new LinkedList<>();
        items = new LinkedList<>();
    }

    public static ApplicationData getInstance() {
        if (instance == null) {
            instance = new ApplicationData();
        }
        return instance;
    }

    public static ApplicationData newInstance() {
        instance = new ApplicationData();
        return instance;
    }

    public void fillTestData() {

        //region CATEGORY/CRITERIA
        Dimension c1 = new Dimension("root1", 1);
        Dimension c2 = new Dimension("root2", 2);

        c1.addAreas(new Area("child" + 1, 1), new Area("child" + 2, 2), new Area("child" + 3, 3));
        c2.addAreas(new Area("child" + 1, 1), new Area("child" + 2, 2), new Area("child" + 3, 3));

        for (Area cat : c1.getAreas()) {
            for (int i = 1; i < 4; i++) {
                Criteria child1_1 = new Criteria("child" + cat.getReference() + "." + i, i);
                cat.addCriterias(child1_1);
            }
        }
        for (Area cat1 : c2.getAreas()) {
            for (int i = 1; i < 4; i++) {
                Criteria child1_2 = new Criteria("child" + cat1.getReference() + "." + i, i);
                cat1.addCriterias(child1_2);
            }
        }
        ApplicationData.getInstance().addDimensions(c1, c2);
        System.out.println(ApplicationData.getInstance().getDimensions().size());
        System.out.println(ApplicationData.getInstance().getAreas().size());
        System.out.println(ApplicationData.getInstance().getCriterias().size());
        //endregion

        //region ITEMS
        for(int i = 0; i < 15; i++){
            items.add(new Item("Dummy item " + i, "description", ApplicationData.getInstance().getCriterias().get(2)));
        }
        //endregion

    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
    }

    public String getUserUID() {
        return userUID;
    }

    public List<Criteria> getCriterias() {
        return criterias;
    }

    public void addCriterias(Criteria... criterias) {
        for (Criteria criteria : criterias) {
            addCriteria(criteria);
        }
    }

    private void addCriteria(Criteria criteria) {
        if (!criterias.contains(criteria)) {
            criterias.add(criteria);
        }
    }

    private void addCriterias(List<Criteria> criterias) {
        for (Criteria criteria : criterias) {
            addCriteria(criteria);
        }
    }

    public void addDimension(Dimension dimension) {
        if (!dimensions.contains(dimension)) {
            dimensions.add(dimension);
            if (!dimension.getAreas().isEmpty()) {
                addAreas(dimension.getAreas());
            }
        }
    }

    public void addDimensions(Dimension... dimensions) {
        for (Dimension dimension : dimensions) {
            addDimension(dimension);
        }
    }

    public void addDimensions(List<Dimension> dimensions) {
        for (Dimension dimension : dimensions) {
            addDimension(dimension);
        }
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    public void addArea(Area area) {
        if (!areas.contains(area)) {
            areas.add(area);
            if (!area.getCriterias().isEmpty()) {
                addCriterias(area.getCriterias());
            }
        }
    }

    public void addAreas(Area... areas) {
        for (Area area : areas) {
            addArea(area);
        }
    }

    public void addAreas(List<Area> areas) {
        for (Area area : areas) {
            addArea(area);
        }
    }

    public List<Area> getAreas() {
        return areas;
    }
}
