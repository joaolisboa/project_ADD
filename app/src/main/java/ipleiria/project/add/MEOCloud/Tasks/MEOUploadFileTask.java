package ipleiria.project.add.MEOCloud.Tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.Metadata;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingFilePathException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingParametersException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingRemoteFilePathException;
import ipleiria.project.add.MEOCloud.HttpRequestor;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudAPI;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.Utils.HttpStatus;
import okhttp3.Response;

/**
 * Created by Lisboa on 26-Mar-17.
 */

public class MEOUploadFileTask extends AsyncTask<String, Void, MEOCloudResponse<Metadata>> {

    private final MEOCallback<Metadata> callback;
    private Exception exception;
    private Context context;

    public MEOUploadFileTask(Context context, MEOCallback<Metadata> callback) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<Metadata> result) {
        super.onPostExecute(result);
        if (exception != null) {
            callback.onError(exception);
        } else {
            if(result.responseSuccessful()){
                callback.onComplete(result);
            }else{
                callback.onRequestError(new HttpErrorException(result.getError()));
            }
        }
    }

    @Override
    protected MEOCloudResponse<Metadata> doInBackground(String... params) {
        try {
            if(params == null){
                throw new MissingParametersException();
            }else if(params[0] == null || params[0].isEmpty()){
                throw new MissingFilePathException();
            }else if(params[1] == null || params[1].isEmpty()){
                throw new MissingRemoteFilePathException();
            }

            if (params[1].startsWith("/")) {
                params[1] = params[1].substring(1);
            }

            String token = MEOCloudClient.getAccessToken();
            String localFilePath = params[0];
            String remoteFilePath = params[1];

            HashMap<String, String> map = new HashMap<>();
            if(params.length > 2 && params[2] != null) {
                map.put("overwrite", params[2]);
            }
            if (params.length > 3 && params[3] != null) {
                map.put("parent_rev", params[3]);
            }

            String path = MEOCloudAPI.API_METHOD_FILES + "/" + MEOCloudAPI.API_MODE + "/" + remoteFilePath;
            System.out.println(path);

            InputStream is = context.getContentResolver().openInputStream(Uri.parse(localFilePath));
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            if (is != null) {
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
            }

            Response response = HttpRequestor.post(token, path, map, buffer.toByteArray());
            if (response != null) {
                MEOCloudResponse<Metadata> meoCloudResponse = new MEOCloudResponse<>();
                meoCloudResponse.setCode(response.code());
                if (response.code() == HttpStatus.OK) {
                    String responseBody = response.body().string();
                    Metadata metadata = Metadata.fromJson(responseBody, Metadata.class);
                    meoCloudResponse.setResponse(metadata);
                }
                return meoCloudResponse;
            }
            return null;
        } catch (IOException
                | MissingParametersException
                | MissingFilePathException
                | MissingAccessTokenException
                | MissingRemoteFilePathException e) {
            exception = e;
        }
        return null;
    }
}