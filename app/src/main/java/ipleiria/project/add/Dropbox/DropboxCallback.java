package ipleiria.project.add.Dropbox;

import com.dropbox.core.v2.files.Metadata;

/**
 * Created by Lisboa on 06-May-17.
 */

public interface DropboxCallback<I> {

    void onComplete(I result);

    void onError(Exception e);

}
