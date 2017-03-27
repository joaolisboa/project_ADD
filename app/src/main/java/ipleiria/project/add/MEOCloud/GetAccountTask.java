package ipleiria.project.add.MEOCloud;

import android.os.AsyncTask;

import java.io.IOException;

import ipleiria.project.add.MEOCloud.Data.Account;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Exceptions.MissingAccessTokenException;
import ipleiria.project.add.Utils.HttpStatus;
import okhttp3.Response;

/**
 * Created by Lisboa on 20-Mar-17.
 */

public class GetAccountTask extends AsyncTask<String, Void, MEOCloudResponse<Account>> {

    private final Callback callback;
    private Exception exception;

    public interface Callback {
        void onComplete(MEOCloudResponse<Account> result);
        void onError(Exception e);
    }

    public GetAccountTask(Callback callback) {
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
            if(params == null){
                throw new MissingAccessTokenException();
            }
            Response response = HttpRequestor.get(params[0], MEOCloudAPI.API_METHOD_ACOUNT_INFO, null);
            if (response != null){
                MEOCloudResponse<Account> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                if(response.code() == HttpStatus.OK) {
                    // reading the body() will result in closure so
                    // any other calls to, ie., body().string() will fail
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
