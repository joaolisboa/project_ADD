package ipleiria.project.add.MEOCloud.Data;

import ipleiria.project.add.Utils.HttpStatus;

/**
 * Created by J on 21/03/2017.
 */

public class MEOCloudResponse<I> {

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
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
}
