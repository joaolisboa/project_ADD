package ipleiria.project.add.MEOCloud;

/**
 * Created by J on 21/03/2017.
 */

public class MEOCloudResponse<I> {

    private int code;
    private String message;
    private Object response;

    public MEOCloudResponse(){}

    public MEOCloudResponse(int code, String message, I response){
        this.code = code;
        this.message = message;
        this.response = response;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
}
