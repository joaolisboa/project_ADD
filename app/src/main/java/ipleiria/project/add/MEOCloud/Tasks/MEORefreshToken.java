package ipleiria.project.add.MEOCloud.Tasks;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by Lisboa on 20-Mar-17.
 */
//// TODO: 20-Mar-17 test token refresh
class MEORefreshToken extends AsyncTask<String, Void, String> {

    private final Callback callback;
    private Exception exception;

    interface Callback {
        void onComplete(String token);
        void onError(Exception e);
    }

    MEORefreshToken(Callback callback){
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(String token) {
        super.onPostExecute(token);
        if (exception != null) {
            callback.onError(exception);
        } else {
            callback.onComplete(token);
        }
    }

    @Override
    protected String doInBackground(String... params) {
        if(params.length != 3){
            Log.d("TokenTask", "Number of invalid parameters - token, appkey, consumerkey");
            return null;
        }

        String token = params[0];
        String appKey = params[1];
        String consumerKey = params[2];

        try {
            String response = post(params);
            System.out.println("response :" + response);
        } catch (IOException e) {
            Log.e("AccesTokenTask", "Failed response", e);
            exception = e;
        }

        return null;
    }

    private String post(String params[]) throws IOException {
        OkHttpClient client = new OkHttpClient();

        HttpUrl httpURL = new HttpUrl.Builder()
                .scheme("https")
                .host("meocloud.pt")
                .addPathSegment("oauth2")
                .addPathSegment("token")
                .addQueryParameter("grant_type", "refresh_token")
                .addQueryParameter("refresh_token", params[0])
                .addQueryParameter("client_secret", params[1])
                .addQueryParameter("client_id", params[2])
                .build();
        System.out.println(httpURL.toString());
        Request request = new Request.Builder()
                .url(httpURL)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }

}
