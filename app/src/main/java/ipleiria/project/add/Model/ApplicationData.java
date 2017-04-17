package ipleiria.project.add.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static ipleiria.project.add.FirebaseHandler.FIREBASE_UID_KEY;

/**
 * Created by Lisboa on 11-Apr-17.
 */

public class ApplicationData {

    private static final String TAG = "ApplicationData";
    private static ApplicationData instance = null;

    private SharedPreferences sharedPreferences;
    private Context context;

    private String userUID;
    private Uri profileUri;
    private String displayName;

    private List<Email> emails;
    private List<Dimension> dimensions;
    private List<Area> areas;
    private List<Criteria> criterias;
    private List<Item> items;

    private List<Email> lockedEmails = new LinkedList<>();

    public void fillTestData(Context context) {

        //region CATEGORY/CRITERIA
        /*Dimension c1 = new Dimension("dimension1", 1);
        Dimension c2 = new Dimension("dimension2", 2);

        c1.addAreas(new Area("area" + 1, 1), new Area("area" + 2, 2), new Area("area" + 3, 3));
        c2.addAreas(new Area("area" + 1, 1), new Area("area" + 2, 2), new Area("area" + 3, 3));

        for (Area cat : c1.getAreas()) {
            for (int i = 1; i < 4; i++) {
                Criteria child1_1 = new Criteria("criteria" + cat.getReference() + "_" + i, i);
                cat.addCriterias(child1_1);
            }
        }
        for (Area cat1 : c2.getAreas()) {
            for (int i = 1; i < 4; i++) {
                Criteria child1_2 = new Criteria("criteria" + cat1.getReference() + "_" + i, i);
                cat1.addCriterias(child1_2);
            }
        }
        ApplicationData.getInstance().addDimensions(c1, c2);*/
        //endregion

        //region ITEMS
        File dir = context.getFilesDir();
        for(File f: dir.listFiles()){
            items.add(new Item(f.getName(), "description"));
        }
        for(int i = 0; i < 15; i++){
            items.add(new Item("Dummy item " + i, "description"));
        }
        //endregion

        //region EMAILS
        //emails.add(new Email("dummymail@gmail.com", false));
        //endregion

    }

    public LinkedList<String> getAllCriterias (){
        // get all criterias
        LinkedList<String> criterias;
        criterias = new LinkedList<>();
        for (Dimension d : ApplicationData.getInstance().getDimensions()) {
            for (Area a : d.getAreas()) {
                for (Criteria c : a.getCriterias()) {
                    criterias.add(c.getName().toLowerCase());
                }
            }
        }
        return criterias;
    }

    private ApplicationData() {
        criterias = new LinkedList<>();
        dimensions = new LinkedList<>();
        areas = new LinkedList<>();
        items = new LinkedList<>();
        emails = new LinkedList<>();
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

    public Uri getProfileUri() {
        return profileUri;
    }

    public void setProfileUri(Uri profileUri) {
        this.profileUri = profileUri;
    }

    public String getDisplayName() {
        if(displayName == null)
            displayName = "Anonymous";
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<Email> getEmails() {
        return emails;
    }

    public void setEmails(List<Email> emails) {
        this.emails = emails;
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

    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setUserUID(String userUID) {
        this.userUID = userUID;
        sharedPreferences.edit().putString(FIREBASE_UID_KEY, userUID).apply();
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

    public void addEmail(Email newEmail) {
        if (!emails.contains(newEmail)) {
            if (emails.isEmpty())
                emails.add(newEmail);
            else {
                boolean added = false;
                for (int i = 0; i < emails.size(); i++) {
                    Email email = emails.get(i);
                    if (email.getEmail().equals(newEmail.getEmail())) {
                        if (newEmail.isVerified()) {
                            email.setVerified(true);
                        }
                        if (email.getDbKey() == null && newEmail.getDbKey() != null) {
                            email.setDbKey(newEmail.getDbKey());
                        }
                        System.out.println("email altered: "  + email);
                        added = false;
                        break;
                    } else if(email.getDbKey() != null && newEmail.getDbKey() != null){
                        if(email.getDbKey().equals(newEmail.getDbKey())) {
                            email.setEmail(newEmail.getEmail());
                            if (newEmail.isVerified()) {
                                email.setVerified(true);
                            }
                            System.out.println("email altered: "  + email);
                            added = false;
                            break;
                        }else {
                            added = true;
                        }
                    } else {
                        added = true;
                        System.out.println("emails must be different: " + email.getEmail() + "=" + newEmail.getEmail());
                    }
                }
                if (added) {
                    emails.add(newEmail);
                }
            }
        }
    }

    public boolean emailExists(String emailS) {
        for(Email email: emails){
            if(email.getEmail().equals(emailS)){
                return true;
            }
        }
        return false;
    }

    public void addItem(Item newItem) {
        for(Item item: items){
            if(item.getDbKey() != null &&
                    newItem.getDbKey() != null &&
                    item.getDbKey().equals(newItem.getDbKey())){
                item = newItem;
                return;
            }
        }
        if(!items.contains(newItem)){
            items.add(newItem);
        }
    }


}
