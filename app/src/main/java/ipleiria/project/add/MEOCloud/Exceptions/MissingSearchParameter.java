package ipleiria.project.add.MEOCloud.Exceptions;

/**
 * Created by J on 28/03/2017.
 */

public class MissingSearchParameter extends Exception {

    public MissingSearchParameter(){
        super("Search parameters is missing - use either query=%s or mime_type=%s for search");
    }
}
