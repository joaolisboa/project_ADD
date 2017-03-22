package ipleiria.project.add.MEOCloud;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.Utils.HttpStatus;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Lisboa on 20-Mar-17.
 */

class HttpRequestor {

    static Response get(String accessToken, String path, @Nullable HashMap<String, String> params) {
        OkHttpClient client = new OkHttpClient();
        try {
            StringBuilder sb = new StringBuilder()
                    .append("https://")
                    .append(MEOCloudAPI.API_ENDPOINT)
                    .append("/").append(MEOCloudAPI.API_VERSION)
                    .append("/").append(path);

            if(params != null && !params.isEmpty()){
                String parameters = encodeParameters(params);
                sb.append("?").append(parameters);
            }

            List<Header> headers = new ArrayList<>();
            headers.add(new Header("Authorization", "Bearer " + accessToken));
            Request.Builder builder = new Request.Builder().get().url(sb.toString());
            for (Header header : headers) {
                builder.addHeader(header.getKey(), header.getValue());
            }
            return client.newCall(builder.build()).execute();
        } catch (IOException ex) {
            Log.e("tag", "error on method GET", ex);
        }
        return null;
    }

    private static String encodeParameters(HashMap<String, String> params){
        StringBuilder stringBuilder = new StringBuilder();
        Iterator it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            stringBuilder.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());

            if(it.hasNext()){
                stringBuilder.append("&");
            }
        }
        return stringBuilder.toString();
    }

    static <I> void processRequest(MEOCloudResponse<I> response, int responseCode){
        switch(responseCode){
            case HttpStatus.BAD_REQUEST:
                break;
            case HttpStatus.UNAUTHORIZED:
                response.setError("Unauthorized request");
                break;
            case HttpStatus.NOT_MODIFIED:
                response.setError("Unmodified Content");
                break;
            case HttpStatus.FORBIDDEN:
                break;
            case HttpStatus.NOT_FOUND:
                break;
            case HttpStatus.METHOD_NOT_ALLOWED:
                break;
            case HttpStatus.NOT_ACCEPTABLE:
                response.setError("Too many entries");
                break;
            case HttpStatus.INTERNAL_SERVER_ERROR:
                break;
            case HttpStatus.OVER_QUOTA:
                break;
            default:
                break;
        }
    }

    private static final class Header {
        private final String key;
        private final String value;

        Header(String key, String value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Returns header name.
         *
         * @return header name
         */
        String getKey() {
            return key;
        }

        /**
         * Returns header value.
         *
         * @return header value
         */
        String getValue() {
            return value;
        }
    }
}
