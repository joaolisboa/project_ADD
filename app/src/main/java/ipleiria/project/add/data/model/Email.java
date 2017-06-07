package ipleiria.project.add.data.model;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by J on 07/06/2017.
 */

public class Email {

    private String dbKey;
    private String body;
    private List<File> attachments;

    public Email(String dbKey, String body){
        this(body);
        this.dbKey = dbKey;
    }

    public Email(String body) {
        this.body = body;
        this.attachments = new LinkedList<>();
    }

    public void addAttachment(File file){
        attachments.add(file);
    }

    public List<File> getAttachments() {
        return attachments;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    /*@Override
    public boolean equals(Object object){
        //if(dbKey != null && ((Email)))
        return
    }*/
}
