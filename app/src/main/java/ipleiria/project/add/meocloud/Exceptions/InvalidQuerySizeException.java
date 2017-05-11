package ipleiria.project.add.meocloud.exceptions;

/**
 * Created by J on 28/03/2017.
 */

public class InvalidQuerySizeException extends Exception {

    public InvalidQuerySizeException(){
        super("Query string is invalid - size must be 3-20 chars");
    }
}
