package ipleiria.project.add.Utils;

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

import ipleiria.project.add.Dropbox.DropboxClientFactory;
import ipleiria.project.add.MEOCloud.Data.MEOMetadata;
import ipleiria.project.add.MEOCloud.Exceptions.HttpErrorException;
import ipleiria.project.add.MEOCloud.MEOCallback;
import ipleiria.project.add.MEOCloud.MEOCloudClient;
import ipleiria.project.add.MEOCloud.Tasks.MEOCreateFolderTree;
import ipleiria.project.add.Model.ApplicationData;
import ipleiria.project.add.Model.Criteria;
import ipleiria.project.add.Model.Dimension;
import ipleiria.project.add.Model.ItemFile;
import ipleiria.project.add.R;

import static ipleiria.project.add.Utils.PathUtils.TRASH_FOLDER;

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

    public static List<File> getLocalFiles(Context context){
        List<File> files = new LinkedList<>();
        for(Dimension dimension: ApplicationData.getInstance().getDimensions()){
            File dimensiondir = new File(context.getFilesDir().getAbsolutePath() + "/" + dimension.getReference());
            if(dimensiondir.isDirectory()){
                goThroughFolder(dimensiondir, files);
            }
        }
        Log.d(TAG, "Local files found: " + files);
        return files;
    }

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

    public static void moveFilesToNewDir(final Context context, List<ItemFile> files, String old) {
        if(NetworkState.isOnline(context)){
            for(ItemFile itemFile: files){
                final String newPath = PathUtils.getRemoteFilePath(itemFile);
                final String oldPath = old + "/" + itemFile.getFilename();

                if(MEOCloudClient.isClientInitialized()) {
                    String[] splitPath = newPath.substring(1, newPath.lastIndexOf("/")).split("/");
                    String dimensionPath = splitPath[0];
                    String areaPath = splitPath[1];
                    String criteriaPath = splitPath[2];

                    new MEOCreateFolderTree(new MEOCallback<MEOMetadata>() {
                        @Override
                        public void onComplete(MEOMetadata result) {
                            CloudHandler.moveFileMEO(oldPath, newPath);
                        }

                        @Override
                        public void onRequestError(HttpErrorException httpE) {
                        }

                        @Override
                        public void onError(Exception e) {
                        }
                    }).execute(dimensionPath, areaPath, criteriaPath);
                }
                if(DropboxClientFactory.isClientInitialized()){
                    CloudHandler.moveFileDropbox(oldPath, newPath);
                }
            }
        }else{
            for(ItemFile itemFile: files){
                renameFile(PathUtils.getLocalFilePath(context, itemFile.getFilename(), itemFile.getParent().getCriteria()),
                        PathUtils.getLocalFilePath(context, itemFile.getFilename(), itemFile.getParent().getCriteria()));
            }
        }
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

            double points = 0;
            // first 4 criteria have a special case with a 10 point limit
            for(int i = 0; i < 4; i++){
                Criteria criteria = ApplicationData.getInstance().getCriterias().get(i);
                points += criteria.getPoints();
                if(points >= 10){
                    points = 10;
                }
                sheet.getRow(criteria.getWriteCell().y)
                            .getCell(criteria.getWriteCell().x)
                            .setCellValue(points);
                System.out.println(sheet.getRow(criteria.getWriteCell().y)
                        .getCell(criteria.getWriteCell().x).getNumericCellValue());
            }
            for(int i = 4; i < ApplicationData.getInstance().getCriterias().size(); i++){
                Criteria criteria = ApplicationData.getInstance().getCriterias().get(i);
                if(criteria.getPoints() > 0) {
                    points = criteria.getPoints();
                    sheet.getRow(criteria.getWriteCell().y)
                            .getCell(criteria.getWriteCell().x)
                            .setCellValue(points);
                    System.out.println(sheet.getRow(criteria.getWriteCell().y)
                            .getCell(criteria.getWriteCell().x).getNumericCellValue());
                }
            }
            try (FileOutputStream stream = new FileOutputStream(file)) {
                wb.write(stream);
            }
            evaluator.evaluateAll();

            for(int i = 0; i < ApplicationData.getInstance().getCriterias().size(); i++) {
                Criteria criteria = ApplicationData.getInstance().getCriterias().get(i);
                if(criteria.getPoints() > 0) {
                    double value = sheet.getRow(criteria.getReadCell().y)
                            .getCell(criteria.getReadCell().x)
                            .getNumericCellValue();
                    criteria.setFinalPoints(value);
                    System.out.println("criteria: " + criteria.getRealReference() + " ;points: " + criteria.getFinalPoints());
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
                Log.e("LOCAL_FILE_COPY_OFFLIME", e.getMessage(), e);
            }
        }
        return file;
    }
}
