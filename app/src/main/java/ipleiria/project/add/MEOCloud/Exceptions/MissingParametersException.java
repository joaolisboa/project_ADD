package ipleiria.project.add.MEOCloud.Exceptions;

/**
 * Created by Lisboa on 26-Mar-17.
 */

public class MissingParametersException extends Exception {
    public MissingParametersException(){
        super("Missing parameters for the request");
    }

    public MissingParametersException(String param){
        super("Missing parameters for the request: " + param);
    }
}
