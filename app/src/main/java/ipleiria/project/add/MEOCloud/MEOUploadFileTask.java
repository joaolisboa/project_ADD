package ipleiria.project.add.MEOCloud;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import ipleiria.project.add.MEOCloud.Data.MEOCloudResponse;
import ipleiria.project.add.MEOCloud.Data.Metadata;
import ipleiria.project.add.MEOCloud.Exceptions.MissingAccessTokenException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingFilePathException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingParametersException;
import ipleiria.project.add.MEOCloud.Exceptions.MissingRemoteFilePathException;
import ipleiria.project.add.Utils.HttpStatus;
import okhttp3.Response;

/**
 * Created by Lisboa on 26-Mar-17.
 */

public class MEOUploadFileTask extends AsyncTask<String, Void, MEOCloudResponse<Metadata>> {

    private final MEOUploadFileTask.Callback callback;
    private Exception exception;
    private Context context;

    public interface Callback {
        void onComplete(MEOCloudResponse<Metadata> result);

        void onError(Exception e);
    }

    public MEOUploadFileTask(Context context, MEOUploadFileTask.Callback callback) {
        this.callback = callback;
        this.context = context;
    }

    @Override
    protected void onPostExecute(MEOCloudResponse<Metadata> result) {
        super.onPostExecute(result);
        if (exception != null) {
            callback.onError(exception);
        } else {
            callback.onComplete(result);
        }
    }

    @Override
    protected MEOCloudResponse<Metadata> doInBackground(String... params) {
        try {
            if(params == null){
                throw new MissingParametersException();
            }else if(params[0] == null || params[0].isEmpty()){
                throw new MissingAccessTokenException();
            }else if(params[1] == null || params[1].isEmpty()){
                throw new MissingFilePathException();
            }else if(params[2] == null || params[2].isEmpty()){
                throw new MissingRemoteFilePathException();
            }

            if (params[2].startsWith("/")) {
                params[2] = params[2].substring(1);
            }

            String path = MEOCloudAPI.API_METHOD_FILES + "/" + MEOCloudAPI.API_MODE + "/" + params[2];
            System.out.println(params[1]);
            System.out.println(path);

            InputStream is = context.getContentResolver().openInputStream(Uri.parse(params[1]));
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            if (is != null) {
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    buffer.write(data, 0, nRead);
                }
            }

            Response response = HttpRequestor.post(params[0], path, null, buffer.toByteArray());
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