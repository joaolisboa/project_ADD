package ipleiria.project.add.data.source;

import ipleiria.project.add.data.model.ItemFile;

/**
 * Created by Lisboa on 06-May-17.
 */

public class FilesRepository implements FilesDataSource {

    private static FilesRepository INSTANCE = null;

    public FilesRepository(){}

    public static FilesRepository getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FilesRepository();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }


    @Override
    public void deleteFile(ItemFile file) {

    }

    @Override
    public void permanenetlyDeleteFile(ItemFile file) {

    }

    @Override
    public void restoreFile(ItemFile file) {

    }

    @Override
    public void saveFile(ItemFile file) {

    }
}
