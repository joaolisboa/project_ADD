package ipleiria.project.add.MEOCloud;

import android.os.AsyncTask;

/**
 * Created by Lisboa on 20-Mar-17.
 */

public class GetAccountTask extends AsyncTask<String, Void, String> {

    private final Callback callback;
    private Exception exception;
    private String accessToken;

    public interface Callback {
        void onComplete(String response);
        void onError(Exception e);
    }

    public GetAccountTask(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(String response) {
        super.onPostExecute(response);
        if (exception != null) {
            callback.onError(exception);
        } else {
            callback.onComplete(response);
        }
    }

    @Override
    protected String doInBackground(String... params) {
        return HttpHandler.get(params[0], "Account/Info", null);
    }
}
