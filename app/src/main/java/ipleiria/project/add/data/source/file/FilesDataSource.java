package ipleiria.project.add.data.source.file;

import android.net.Uri;

import java.io.File;
import java.util.List;

import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.EvaluationPeriod;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.data.model.PendingFile;

/**
 * Created by Lisboa on 09-Dec-17.
 */

public interface FilesDataSource {

    List<PendingFile> getPendingFiles();

    File createImageFile();

    void setCurrentPeriod(EvaluationPeriod evaluationPeriod);

    void moveFile(ItemFile file, Criteria newCriteria);

    void deleteFile(ItemFile fileToDelete);

    void permanentlyDeleteFile(ItemFile file);

    void restoreFile(ItemFile fileToRestore);

    void saveFile(ItemFile file, Uri uri);

    void movePendingFile(PendingFile file, Item item, Criteria criteria);
}
