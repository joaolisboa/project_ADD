package ipleiria.project.add.MEOCloud;

/**
 * Created by Lisboa on 26-Mar-17.
 */

public interface ErrorMessageResponse {

    // override any error message specific to this request
    String processRequestCode(int code);

}
