package ipleiria.project.add.MEOCloud.Data;

import ipleiria.project.add.MEOCloud.ErrorMessageResponse;
import ipleiria.project.add.Utils.HttpStatus;

/**
 * Created by J on 21/03/2017.
 */

public class MEOCloudResponse<Object> {

    private int code;
    private String error;
    private Object response;

    public MEOCloudResponse(){}

    public boolean responseSuccessful(){
        return code == HttpStatus.OK;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        if(response instanceof ErrorMessageResponse)
            error = ((ErrorMessageResponse) response).processRequestCode(code);
        else
            error = HttpStatus.processRequestCode(code);

        this.code = code;
    }

    public String getError() {
        return error;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
}
