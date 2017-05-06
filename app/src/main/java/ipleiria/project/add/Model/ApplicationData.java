package ipleiria.project.add.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.FirebaseHandler;
import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.Item;

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
    private List<Item> deletedItems;
    private List<File> localPendingFiles;

    private ApplicationData() {
        criterias = new LinkedList<>();
        dimensions = new LinkedList<>();
        areas = new LinkedList<>();
        items = new LinkedList<>();
        emails = new LinkedList<>();
        deletedItems = new LinkedList<>();
        localPendingFiles = new LinkedList<>();
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

    public List<File> getLocalPendingFiles() {
        return localPendingFiles;
    }

    public void setLocalPendingFiles(List<File> localPendingFiles) {
        this.localPendingFiles = localPendingFiles;
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

    public List<Item> getItems(boolean flag) {
        return !flag ? items : getDeletedItems();
    }

    public List<Item> sortItems(){


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

    public Criteria getCriteria(int dimension, int area, int criteria){
        return dimensions.get(dimension).getArea(area).getCriteria(criteria);
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
        for(int i = 0; i < criterias.size(); i++){
            if(criterias.get(i).getDbKey() != null &&
                    criteria.getDbKey() != null &&
                    criterias.get(i).getDbKey().equals(criteria.getDbKey())){
                criterias.remove(i);
                criterias.add(i, criteria);
                return;
            }
        }
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
        for(int i = 0; i < dimensions.size(); i++){
            if(dimensions.get(i).getDbKey() != null && dimension.getDbKey() != null
                    && dimensions.get(i).getDbKey().equals(dimension.getDbKey())){
                dimensions.remove(i);
                dimensions.add(i, dimension);
                return;
            }
        }
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
        for(int i = 0; i < areas.size(); i++){
            if(areas.get(i).getDbKey() != null && area.getDbKey() != null
                    && areas.get(i).getDbKey().equals(area.getDbKey())){
                areas.remove(i);
                areas.add(i, area);
                return;
            }
        }
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
        for(int i = 0; i < items.size(); i++){
            if(items.get(i).getDbKey() != null &&
                    newItem.getDbKey() != null &&
                    items.get(i).getDbKey().equals(newItem.getDbKey())){
                items.remove(i);
                items.add(i, newItem);
                return;
            }
        }
        if(!items.contains(newItem)){
            items.add(newItem);
        }
    }

    public void addDeletedItem(Item newItem) {
        for(int i = 0; i < deletedItems.size(); i++){
            if(deletedItems.get(i).getDbKey() != null &&
                    newItem.getDbKey() != null &&
                    deletedItems.get(i).getDbKey().equals(newItem.getDbKey())){
                deletedItems.remove(i);
                deletedItems.add(i, newItem);
                return;
            }
        }
        if(!deletedItems.contains(newItem)){
            deletedItems.add(newItem);
        }
    }

    public void permanentlyDeleteItem(Item item){
        if(items.contains(item)){
            items.remove(item);
        }
        deletedItems.remove(item);
        FirebaseHandler.getInstance().permanentlyDeleteItem(item);
    }

    public void deleteItem(String itemKey){
        for(int i = 0; i < items.size(); i++){
            Item item = items.get(i);
            if(item.getDbKey() != null &&
                    item.getDbKey().equals(itemKey)){
                items.remove(i);
                return;
            }
        }
    }

    public void deleteDeletedItem(Item item){
        deletedItems.remove(item);
    }

    public void deleteDeletedItem(String itemKey) {
        for(int i = 0; i < deletedItems.size(); i++){
            Item item = deletedItems.get(i);
            if(item.getDbKey() != null &&
                    item.getDbKey().equals(itemKey)){
                deletedItems.remove(i);
                return;
            }
        }
    }

    public void deleteItem(Item item) {
        items.remove(item);
        deletedItems.add(item);
        FirebaseHandler.getInstance().deleteItem(item);
    }

    public void restoreItem(Item item){
        deletedItems.remove(item);
        items.add(item);
        FirebaseHandler.getInstance().restoreItem(item);
    }

    public List<Item> getDeletedItems(){
        List<Item> itemsWithFilesDeleted = new LinkedList<>();
        itemsWithFilesDeleted.addAll(deletedItems);
        for(Item item: items){
            if(item.hasDeletedFiles() && !itemsWithFilesDeleted.contains(item)){
                itemsWithFilesDeleted.add(item);
            }
        }
        return itemsWithFilesDeleted;
    }

    public Item getItemNonDescript(String itemDbKey){
        for(Item item: items){
            if(item.getDbKey() != null &&
                    item.getDbKey().equals(itemDbKey)){
                return item;
            }
        }
        return getDeletedItem(itemDbKey);
    }

    public Item getItem(String itemDbKey) {
        for(Item item: items){
            if(item.getDbKey() != null &&
                    item.getDbKey().equals(itemDbKey)){
                return item;
            }
        }
        return null;
    }

    public Item getDeletedItem(String itemDbKey) {
        for(Item item: deletedItems){
            if(item.getDbKey() != null &&
                    item.getDbKey().equals(itemDbKey)){
                return item;
            }
        }
        return null;
    }
}
