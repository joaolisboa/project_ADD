package ipleiria.project.add.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.dropbox.DropboxClientFactory;
import ipleiria.project.add.meocloud.data.MEOMetadata;
import ipleiria.project.add.meocloud.exceptions.HttpErrorException;
import ipleiria.project.add.meocloud.MEOCallback;
import ipleiria.project.add.meocloud.MEOCloudClient;
import ipleiria.project.add.meocloud.tasks.MEOCreateFolderTree;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.data.model.ItemFile;
import ipleiria.project.add.R;

import static ipleiria.project.add.utils.PathUtils.TRASH_FOLDER;

/**
 * Created by Lisboa on 26-Apr-17.
 */

public class FileUtils {

    private static final String TAG  ="FILE_UTILS";

    public static void copyFileToLocalDir(Context context, Uri src, Criteria criteria){
        String filename = UriHelper.getFileName(context, src);
        String path = PathUtils.getLocalFilePath(context, filename, criteria);
        File dir = new File(path.substring(0, path.lastIndexOf("/")));
        if(!dir.exists()){
            dir.mkdirs();
        }
        File destFile = new File(path);
        try {
            InputStream is = context.getContentResolver().openInputStream(src);
            FileOutputStream outStream = new FileOutputStream(destFile);

            int nRead;
            byte[] data = new byte[16384];

            if (is != null) {
                while ((nRead = is.read(data, 0, data.length)) != -1) {
                    outStream.write(data, 0, nRead);
                }
            }
            outStream.close();
            is.close();
        }catch(IOException e){
            Log.e("LOCAL_FILE_COPY_OFFLIME", e.getMessage(), e);
        }
    }

    public static void deleteFile(String path){
        File file = new File(path);
        if(file.exists()){
            boolean success = file.delete();
            Log.d("FILE_ACTION", "file delete successful? " + success);
        }
    }

    public static void renameFile(String from, String to){
        File src = new File(from);
        if(src.exists()) {
            File dir = new File(to.substring(0, to.lastIndexOf("/")));
            if(!dir.exists()){
                dir.mkdirs();
            }
            File dest = new File(to);
            boolean success = src.renameTo(dest);
            Log.d("FILE_ACTION", "file rename successful: " + success);
        }
    }

    /*public static List<File> getLocalFiles(Context context){
        List<File> files = new LinkedList<>();
        for(Dimension dimension: ApplicationData.getInstance().getDimensions()){
            File dimensiondir = new File(context.getFilesDir().getAbsolutePath() + "/" + dimension.getReference());
            if(dimensiondir.isDirectory()){
                goThroughFolder(dimensiondir, files);
            }
        }
        Log.d(TAG, "Local files found: " + files);
        return files;
    }*/

    private static void goThroughFolder(File dir, List<File> files){
        for(File fileInDir: dir.listFiles()){
            if(fileInDir.isDirectory()){
                goThroughFolder(fileInDir, files);
            }else{
                files.add(fileInDir);
            }
        }
    }

    public static File getUserThumbnail(Context context){
        return new File(context.getFilesDir() + "/user_thumb.jpg");
    }

    public static List<File> getLocalDeletedFiles(Context context) {
        List<File> files = new LinkedList<>();
        File trashDir = new File(context.getFilesDir() + TRASH_FOLDER);
        if(!trashDir.exists()){
            trashDir.mkdirs();
            if(trashDir.list() != null && trashDir.list().length == 0){
                Collections.addAll(files, trashDir.listFiles());
            }
        }
        Log.d(TAG, "Local deleted files found: " + files);
        return files;
    }

    public static void readExcel(Context context) {
        System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        try {
            File file = getExcelFile(context);
            InputStream inputStream = new FileInputStream(file);
            Workbook wb = new XSSFWorkbook(inputStream);
            wb.setForceFormulaRecalculation(true);
            FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            Sheet sheet = wb.getSheetAt(0);

            int points = 0;
            // first 4 criteria have a special case with a 10 point limit
            List<Criteria> criterias = CategoryRepository.getInstance().getCriterias();
            for(int i = 0; i < 4; i++){
                Criteria criteria = criterias.get(i);
                points += criteria.getPoints();
                if(points >= 10){
                    points = 10;
                }
                sheet.getRow(criteria.getWriteCell().y)
                            .getCell(criteria.getWriteCell().x)
                            .setCellValue(points);
            }
            for(int i = 4; i < criterias.size(); i++){
                Criteria criteria = criterias.get(i);
                if(criteria.getPoints() > 0) {
                    sheet.getRow(criteria.getWriteCell().y)
                            .getCell(criteria.getWriteCell().x)
                            .setCellValue(criteria.getPoints());
                }
            }
            try (FileOutputStream stream = new FileOutputStream(file)) {
                wb.write(stream);
            }
            evaluator.evaluateAll();

            for(int i = 0; i < criterias.size(); i++) {
                Criteria criteria = criterias.get(i);
                if(criteria.getPoints() > 0) {
                    double value = sheet.getRow(criteria.getReadCell().y)
                            .getCell(criteria.getReadCell().x)
                            .getNumericCellValue();
                    criteria.setFinalPoints(value);
                }
            }

            wb.close();
            inputStream.close();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static File getExcelFile(Context context){
        File file = new File(context.getFilesDir().getAbsolutePath() + "/ficha_avaliacao.xlsx");
        if(!file.exists()) {
            try {
                InputStream is = context.getResources().openRawResource(R.raw.ficha_avaliacao);
                FileOutputStream outStream = new FileOutputStream(file);

                int nRead;
                byte[] data = new byte[16384];

                if (is != null) {
                    while ((nRead = is.read(data, 0, data.length)) != -1) {
                        outStream.write(data, 0, nRead);
                    }
                }
                outStream.close();
                is.close();
            } catch (IOException e) {
                Log.e("LOCAL_FILE_COPY_OFFLINE", e.getMessage(), e);
            }
        }
        return file;
    }
}
