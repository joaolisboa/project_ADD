package ipleiria.project.add.utils;

/**
 * Created by J on 22/03/2017.
 */

public class HttpStatus{

    public static final int OK = 200;
    public static final int NOT_MODIFIED = 304;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int METHOD_NOT_ALLOWED = 405;
    public static final int NOT_ACCEPTABLE = 406;
    public static final int INTERNAL_SERVER_ERROR = 500;
    public static final int OVER_QUOTA = 507;


    public static String processRequestCode(int code) {
        switch(code){
            case OK:                    return "OK";
            case BAD_REQUEST:           return "Bad request - invalid request/parameters";
            case UNAUTHORIZED:          return "Unauthorized request - authentication required/failed or not provided";
            case FORBIDDEN:             return "Request was valid, but the server is refusing action. The user might not have the necessary permissions";
            case NOT_FOUND:             return "Resource not found";
            case METHOD_NOT_ALLOWED:    return "A request method is not supported for the requested resource;" +
                                                "ie. GET request on a form that requires data to be presented via POST, or a PUT request on a read-only resource";
            case NOT_ACCEPTABLE:        return "The requested resource is capable of generating only content not acceptable according to the Accept headers sent in the request";
            case INTERNAL_SERVER_ERROR: return "A generic error message, an unexpected condition was encountered and no more specific message is suitable";
            case OVER_QUOTA:            return "Insufficient storage to fulfill request";
        }
        return "Invalid error code";
    }
}
