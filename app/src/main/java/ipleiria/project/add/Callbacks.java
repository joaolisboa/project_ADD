package ipleiria.project.add;

import ipleiria.project.add.data.source.file.FilesRepository;

/**
 * Created by Lisboa on 13-Jun-17.
 */

public class Callbacks {

    public interface BaseCallback<I> {

        void onComplete(I result);

    }

    public interface Callback<I> extends FilesRepository.BaseCallback<I> {

        void onError(Exception e);
    }

    public interface ServiceCallback<I>{

        void onMEOComplete(I result);

        void onMEOError();

        void onDropboxComplete(I result);

        void onDropboxError();

    }

}
