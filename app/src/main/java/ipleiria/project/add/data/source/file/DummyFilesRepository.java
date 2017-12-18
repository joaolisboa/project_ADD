package ipleiria.project.add.data.source.file;

import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.EvaluationPeriod;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.model.PendingFile;

/**
 * Created by Lisboa on 09-Dec-17.
 */

public class DummyFilesRepository extends MasterFilesRepository implements FilesDataSource {

    private static final String TAG = "DUMMY_FILES_REPO";

    private List<PendingFile> pendingFiles;

    public DummyFilesRepository(){
        pendingFiles = new ArrayList<>();
        populatePendingFiles();
    }

    private void populatePendingFiles() {
        pendingFiles.add(new PendingFile(new ItemFile("test.jpg"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("email.eml"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("test.jpg"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("email.eml"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("test.jpg"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("email.eml"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("test.jpg"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("email.eml"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("test.jpg"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("email.eml"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("test.jpg"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("email.eml"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("test.jpg"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("email.eml"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("test.jpg"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("email.eml"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("email.eml"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("email.eml"), "test"));
        pendingFiles.add(new PendingFile(new ItemFile("email.eml"), "test"));
    }

    @Override
    public List<PendingFile> getPendingFiles() {
        return pendingFiles;
    }

    @Override
    public void setCurrentPeriod(EvaluationPeriod evaluationPeriod) {

    }

    @Override
    public void moveFile(ItemFile file, Criteria newCriteria) {

    }

    @Override
    public void deleteFile(ItemFile fileToDelete) {

    }

    @Override
    public void permanentlyDeleteFile(ItemFile file) {

    }

    @Override
    public void restoreFile(ItemFile fileToRestore) {

    }

    @Override
    public void saveFile(ItemFile file, Uri uri) {

    }

    @Override
    public void movePendingFile(PendingFile file, Item item, Criteria criteria) {

    }
}
