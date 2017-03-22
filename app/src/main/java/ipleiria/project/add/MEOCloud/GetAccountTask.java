package ipleiria.project.add.MEOCloud;

import android.os.AsyncTask;

import java.io.IOException;

import ipleiria.project.add.MEOCloud.Data.Account;
import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
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
            Response response = HttpRequestor.get(params[0], "Account/Info", null);
            if (response != null){
                MEOCloudResponse<Account> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                if(response.code() == HttpStatus.OK) {
                    // reading the body() will result in closure so
                    // any other calls to, ie., body().string() will fail
                    String responseBody = response.body().string();
                    Account account = Account.fromJson(responseBody, Account.class);
                    meoCloudResponse.setResponse(account);
                } else{
                    HttpRequestor.processRequest(meoCloudResponse, response.code());
                }
                return meoCloudResponse;
            }
            return null;
        } catch (IOException e) {
            exception = e;
        }
        return null;
    }

    // any error message specific to this request will be handle before checking for general erros
    private <I> void processRequest(MEOCloudResponse<I> meoCloudResponse, int responseCode){
        switch (responseCode){

        }
    }

}
