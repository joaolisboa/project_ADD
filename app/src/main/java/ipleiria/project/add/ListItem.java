package ipleiria.project.add;

import java.io.File;

/**
 * Created by Lisboa on 04-Apr-17.
 */

public class ListItem {

    private File file;
    private String name;
    private String description;
    private String category;

    public ListItem(File file) {
        this.file = file;
    }

    public ListItem(File file, String name) {
        this(file);
        this.name = name;
    }

    public ListItem(File file, String name, String description) {
        this(file, name);
        this.description = description;
    }

    public ListItem(File file, String name, String description, String category) {
        this(file, name, description);
        this.category = category;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
