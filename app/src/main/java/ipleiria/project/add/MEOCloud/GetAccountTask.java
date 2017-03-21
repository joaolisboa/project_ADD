package ipleiria.project.add.MEOCloud;

import android.os.AsyncTask;

import java.io.IOException;

import ipleiria.project.add.MEOCloud.Data.Account;
import okhttp3.Response;

/**
 * Created by Lisboa on 20-Mar-17.
 */

class GetAccountTask extends AsyncTask<String, Void, MEOCloudResponse<Account>> {

    private final Callback callback;
    private Exception exception;

    interface Callback {
        void onComplete(MEOCloudResponse<Account> result);
        void onError(Exception e);
    }

    GetAccountTask(Callback callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<Account> result) {
        super.onPostExecute(result);
        if (exception != null) {
            callback.onError(exception);
        } else {
            callback.onComplete(result);
        }
    }

    @Override
    protected MEOCloudResponse<Account> doInBackground(String... params) {
        try {
            Response response = HttpRequestor.get(params[0], "Account/Info", null);
            if (response != null) {
                String responseBody = response.body().string();
                Account account = Account.fromJson(responseBody, Account.class);
                return new MEOCloudResponse<>(response.code(), responseBody, account);
            }
        } catch (IOException e) {
            exception = e;
        }
        return null;
    }
}
