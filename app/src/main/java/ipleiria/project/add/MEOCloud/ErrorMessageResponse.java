package ipleiria.project.add.MEOCloud;

/**
 * Created by Lisboa on 26-Mar-17.
 */

public interface ErrorMessageResponse {

    // any error message specific to this request will be handle before checking for general erros
    String processRequestCode(int code);

}
