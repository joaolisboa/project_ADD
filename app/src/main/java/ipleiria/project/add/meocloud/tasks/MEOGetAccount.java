package ipleiria.project.add.meocloud.tasks;

import android.os.AsyncTask;

import java.io.IOException;

import ipleiria.project.add.meocloud.data.Account;
import ipleiria.project.add.meocloud.data.MEOCloudResponse;
import ipleiria.project.add.meocloud.exceptions.HttpErrorException;
import ipleiria.project.add.meocloud.exceptions.MissingAccessTokenException;
import ipleiria.project.add.meocloud.HttpRequestor;
import ipleiria.project.add.meocloud.MEOCallback;
import ipleiria.project.add.meocloud.MEOCloudAPI;
import ipleiria.project.add.meocloud.MEOCloudClient;
import ipleiria.project.add.utils.HttpStatus;
import okhttp3.Response;

/**
 * Created by Lisboa on 20-Mar-17.
 */

public class MEOGetAccount extends AsyncTask<String, Void, MEOCloudResponse<Account>> {

    private final MEOCallback<Account> callback;
    private Exception exception;

    public MEOGetAccount(MEOCallback<Account> callback) {
        this.callback = callback;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<Account> result) {
        super.onPostExecute(result);
        if (exception != null) {
            callback.onError(exception);
        } else {
            if(result.responseSuccessful()){
                callback.onComplete(result.getResponse());
            }else{
                callback.onRequestError(new HttpErrorException(result));
            }
        }
    }

    @Override
    protected MEOCloudResponse<Account> doInBackground(String... params) {
        try {
            Response response = HttpRequestor.get(MEOCloudClient.getAccessToken(),
                                    MEOCloudAPI.API_METHOD_ACOUNT_INFO, null);
            if (response != null){
                MEOCloudResponse<Account> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                if(response.code() == HttpStatus.OK) {
                    String responseBody = response.body().string();
                    Account account = Account.fromJson(responseBody, Account.class);
                    meoCloudResponse.setResponse(account);
                }
                return meoCloudResponse;
            }
            return null;
        } catch (IOException | MissingAccessTokenException e) {
            exception = e;
        }
        return null;
    }

}
