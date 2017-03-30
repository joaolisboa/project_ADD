package ipleiria.project.add.MEOCloud;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by Lisboa on 20-Mar-17.
 */

public class HttpRequestor {

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static Response get(String accessToken, String path, @Nullable HashMap<String, String> params) {
        OkHttpClient client = new OkHttpClient();
        try {
            StringBuilder sb = new StringBuilder()
                    .append("https://")
                    .append(MEOCloudAPI.API_ENDPOINT)
                    .append("/").append(MEOCloudAPI.API_VERSION)
                    .append("/").append(path);

            if (params != null && !params.isEmpty()) {
                sb.append("?").append(encodeParameters(params));
            }

            List<Header> headers = new ArrayList<>();
            headers.add(new Header("Authorization", "Bearer " + accessToken));
            Request.Builder builder = new Request.Builder().get().url(sb.toString());
            for (Header header : headers) {
                builder.addHeader(header.getKey(), header.getValue());
            }
            System.out.println(sb.toString());
            return client.newCall(builder.build()).execute();
        } catch (IOException ex) {
            Log.e("tag", "error on method GET", ex);
        }
        return null;
    }

    public static Response getContent(String accessToken, String path, @Nullable HashMap<String, String> params) {
        OkHttpClient client = new OkHttpClient();
        try {
            StringBuilder sb = new StringBuilder()
                    .append("https://")
                    .append(MEOCloudAPI.API_CONTENT_ENDPOINT)
                    .append("/").append(MEOCloudAPI.API_VERSION)
                    .append("/").append(path);

            if (params != null && !params.isEmpty()) {
                sb.append("?").append(encodeParameters(params));
            }

            List<Header> headers = new ArrayList<>();
            headers.add(new Header("Authorization", "Bearer " + accessToken));
            Request.Builder builder = new Request.Builder().get().url(sb.toString());
            for (Header header : headers) {
                builder.addHeader(header.getKey(), header.getValue());
            }
            System.out.println(sb.toString());
            return client.newCall(builder.build()).execute();
        } catch (IOException ex) {
            Log.e("tag", "error on method GET", ex);
        }
        return null;
    }

    public static Response post(String accessToken, String path, @Nullable HashMap<String, String> params, @NonNull HashMap<String, String> postBody) {
        OkHttpClient client = new OkHttpClient();
        try {
            StringBuilder sb = new StringBuilder()
                    .append("https://")
                    .append(MEOCloudAPI.API_ENDPOINT)
                    .append("/").append(MEOCloudAPI.API_VERSION)
                    .append("/").append(path);

            if (params != null && !params.isEmpty()) {
                sb.append("?").append(encodeParameters(params));
            }

            MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM);
            RequestBody body;
            for (Map.Entry<String, String> e : postBody.entrySet()) {
                bodyBuilder.addFormDataPart(e.getKey(), e.getValue());
            }
            body = bodyBuilder.build();

            List<Header> headers = new ArrayList<>();
            headers.add(new Header("Authorization", "Bearer " + accessToken));

            Request.Builder builder = new Request.Builder().post(body).url(sb.toString());
            for (Header header : headers) {
                builder.addHeader(header.getKey(), header.getValue());
            }
            return client.newCall(builder.build()).execute();
        } catch (IOException ex) {
            Log.e("tag", "error on method POST", ex);
        }
        return null;
    }

    public static Response post(String accessToken, String path, @Nullable HashMap<String, String> params, @NonNull byte[] content) {
        OkHttpClient client = new OkHttpClient();
        try {
            StringBuilder sb = new StringBuilder()
                    .append("https://")
                    .append(MEOCloudAPI.API_ENDPOINT)
                    .append("/").append(MEOCloudAPI.API_VERSION)
                    .append("/").append(path);

            if (params != null && !params.isEmpty()) {
                sb.append("?").append(encodeParameters(params));
            }

            RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), content);

            List<Header> headers = new ArrayList<>();
            headers.add(new Header("Authorization", "Bearer " + accessToken));
            Request.Builder builder = new Request.Builder().post(body).url(sb.toString());
            for (Header header : headers) {
                builder.addHeader(header.getKey(), header.getValue());
            }
            return client.newCall(builder.build()).execute();
        } catch (IOException ex) {
            Log.e("tag", "error on method POST", ex);
        }
        return null;
    }

    private static String encodeParameters(HashMap<String, String> params) {
        StringBuilder stringBuilder = new StringBuilder();
        Iterator it = params.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            stringBuilder.append(entry.getKey())
                    .append("=")
                    .append(entry.getValue());


            if (it.hasNext()) {
                stringBuilder.append("&");
            }
        }
        return stringBuilder.toString();
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
