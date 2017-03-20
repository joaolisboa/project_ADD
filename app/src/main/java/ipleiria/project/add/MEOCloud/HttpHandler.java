package ipleiria.project.add.MEOCloud;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Lisboa on 20-Mar-17.
 */

public class HttpHandler {

    static String get(String accessToken, String path, @Nullable String params) {
        OkHttpClient client = new OkHttpClient();
        try {
            StringBuilder sb = new StringBuilder()
                    .append("https://")
                    .append(MEOCloudAPI.API_ENDPOINT)
                    .append("/").append(MEOCloudAPI.API_VERSION)
                    .append("/").append(path);
            List<Header> headers = new ArrayList<>();
            headers.add(new Header("Authorization", "Bearer " + accessToken));
            Request.Builder builder = new Request.Builder().get().url(sb.toString());
            for (Header header : headers) {
                builder.addHeader(header.getKey(), header.getValue());
            }
            Response response = client.newCall(builder.build()).execute();
            System.out.println(response.body().string());
            return response.body().toString();
        } catch (IOException ex) {
            Log.e("tag", "error on method GET", ex);
        }

        return null;
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
