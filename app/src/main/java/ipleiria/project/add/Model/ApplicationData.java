package ipleiria.project.add.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.dropbox.core.DbxException;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.MEOCloud.Data.Account;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.MEOGetAccount;

/**
 * Created by Lisboa on 11-Apr-17.
 */

public class ApplicationData {

    private static final String TAG = "ApplicationData";
    private static ApplicationData instance = null;

    private SharedPreferences sharedPreferences;

    private String userUID;
    private Uri profileUri;
    private String displayName;

    private Map<String, Boolean> emails;

    private List<Dimension> dimensions;

    private List<Area> areas;
    private List<Criteria> criterias;
    private List<Item> items;
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
        /*File dir = context.getFilesDir();
        for(File f: dir.listFiles()){
            items.add(new Item(f.getName(), "description", ApplicationData.getInstance().getCriterias().get(0)));
        }
        for(int i = 0; i < 15; i++){
            items.add(new Item("Dummy item " + i, "description", ApplicationData.getInstance().getCriterias().get(2)));
        }*/
        //endregion

        //region EMAILS
        emails.put("dummymail@gmail.com", false);
        if(DropboxClientFactory.isClientInitialized()){
            try {
                emails.put(DropboxClientFactory.getClient().users().getCurrentAccount().getEmail(), true);
            } catch (DbxException e) {
                e.printStackTrace();
            }
        }
        if(MEOCloudClient.isClientInitialized()){
            new MEOGetAccount(new MEOCallback<Account>() {
                @Override
                public void onComplete(MEOCloudResponse<Account> result) {
                    emails.put(result.getResponse().getEmail(), true);
                }

                @Override
                public void onRequestError(HttpErrorException httpE) {
                    Log.e(TAG, httpE.getMessage(), httpE);
                }

                @Override
                public void onError(Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }).execute();
        }
        //endregion

    }

    private ApplicationData() {
        criterias = new LinkedList<>();
        dimensions = new LinkedList<>();
        areas = new LinkedList<>();
        items = new LinkedList<>();
        emails = new HashMap<>();
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
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Map<String, Boolean> getEmails() {
        return emails;
    }

    public void setEmails(Map<String, Boolean> emails) {
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
