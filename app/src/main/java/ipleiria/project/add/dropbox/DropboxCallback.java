package ipleiria.project.add.dropbox;

/**
 * Created by Lisboa on 06-May-17.
 */

public interface DropboxCallback<I> {

    void onComplete(I result);

    void onError(Exception e);

}
