package ipleiria.project.add.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFNum;
import org.apache.poi.xwpf.usermodel.XWPFNumbering;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTNumFmt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ipleiria.project.add.Application;
import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.source.MEOCloudService;
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

    private static final String TAG = "FILE_UTILS";

    public static void copyFileToLocalDir(Context context, Uri src, Criteria criteria) {
        String filename = UriHelper.getFileName(context, src);
        String path = PathUtils.getLocalFilePath(context, filename, criteria);
        File dir = new File(path.substring(0, path.lastIndexOf("/")));
        if (!dir.exists()) {
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
        } catch (IOException e) {
            Log.e("LOCAL_FILE_COPY_OFFLIME", e.getMessage(), e);
        }
    }

    public static void deleteFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            boolean success = file.delete();
            Log.d("FILE_ACTION", "file delete successful? " + success);
        }
    }

    public static void renameFile(String from, String to) {
        File src = new File(from);
        if (src.exists()) {
            File dir = new File(to.substring(0, to.lastIndexOf("/")));
            if (!dir.exists()) {
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

    private static void goThroughFolder(File dir, List<File> files) {
        for (File fileInDir : dir.listFiles()) {
            if (fileInDir.isDirectory()) {
                goThroughFolder(fileInDir, files);
            } else {
                files.add(fileInDir);
            }
        }
    }

    public static File getUserThumbnail(Context context) {
        return new File(context.getFilesDir() + "/user_thumb.jpg");
    }

    public static List<File> getLocalDeletedFiles(Context context) {
        List<File> files = new LinkedList<>();
        File trashDir = new File(context.getFilesDir() + TRASH_FOLDER);
        if (!trashDir.exists()) {
            trashDir.mkdirs();
            if (trashDir.list() != null && trashDir.list().length == 0) {
                Collections.addAll(files, trashDir.listFiles());
            }
        }
        Log.d(TAG, "Local deleted files found: " + files);
        return files;
    }

    public static void generateNote(String sFileName) {
        try {
            File file = new File(Application.getAppContext().getFilesDir(), sFileName);
            FileWriter writer = new FileWriter(file);

            for (Dimension dimension : CategoryRepository.getInstance().getDimensions()) {

                writer.append(dimension.getReference() + ". " + dimension.getName() + "\n");

                for (Area area : dimension.getAreas()) {
                    for (Criteria criteria : area.getCriterias()) {
                        if (criteria.getItems().size() > 0) {
                            writer.append(criteria.getRealReference() + " - \"" + criteria.getName() + "\"");

                            for (Item item : criteria.getItems()) {
                                writer.append("\n\tDescriçao da atividade: " +"\n\t"+ item.getDescription() + "\n");


                            }
                            writer.append("\n\t"+ "Comprovativo Em anexo"  );
                            writer.append("\n\t"+ "Pontuação " + criteria.getPoints() + "\n");
                        }
                    }

                }

            }

            writer.flush();
            writer.close();
        } catch (
                IOException e)

        {
            e.printStackTrace();
        }
    }

    public static void readExcel(Criteria criteria){
        try {
            File file = getExcelFile();
            InputStream inputStream = new FileInputStream(file);
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            wb.setForceFormulaRecalculation(true);
            XSSFFormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            XSSFSheet sheet = wb.getSheetAt(0);

            int points = 0;
            if(criteria.getDimension().getReference() == 1 && criteria.getReference() <= 4){
                points += criteria.getWeights();
                if (points >= 10) {
                    points = 10;
                }
                sheet.getRow(criteria.getWriteCell().y)
                        .getCell(criteria.getWriteCell().x)
                        .setCellValue(points);
            }else{
                if (criteria.getWeights() > 0) {
                    sheet.getRow(criteria.getWriteCell().y)
                            .getCell(criteria.getWriteCell().x)
                            .setCellValue(criteria.getWeights());
                }

                try (FileOutputStream stream = new FileOutputStream(file)) {
                    wb.write(stream);
                }
                evaluator.evaluateFormulaCell(sheet.getRow(criteria.getReadCell().y)
                        .getCell(criteria.getReadCell().x));

                if (criteria.getWeights() > 0) {
                    double value = sheet.getRow(criteria.getReadCell().y)
                            .getCell(criteria.getReadCell().x)
                            .getNumericCellValue();
                    criteria.setFinalPoints(value);
                }
            }

            inputStream.close();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public static void readExcel() {
        // part of poi-shadow - newer version incompatible with API 19(4.4)
        //System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        //System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        //System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        try {
            File file = getExcelFile();
            InputStream inputStream = new FileInputStream(file);
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            wb.setForceFormulaRecalculation(true);
            XSSFFormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            XSSFSheet sheet = wb.getSheetAt(0);

            int points = 0;
            // first 4 criteria have a special case with a 10 point limit
            List<Criteria> criterias = CategoryRepository.getInstance().getCriterias();
            for (int i = 0; i < 4; i++) {
                Criteria criteria = criterias.get(i);
                points += criteria.getWeights();
                if (points >= 10) {
                    points = 10;
                }
                sheet.getRow(criteria.getWriteCell().y)
                        .getCell(criteria.getWriteCell().x)
                        .setCellValue(points);
            }
            for (int i = 4; i < criterias.size(); i++) {
                Criteria criteria = criterias.get(i);
                if (criteria.getWeights() > 0) {
                    sheet.getRow(criteria.getWriteCell().y)
                            .getCell(criteria.getWriteCell().x)
                            .setCellValue(criteria.getWeights());
                }
            }
            try (FileOutputStream stream = new FileOutputStream(file)) {
                wb.write(stream);
            }
            evaluator.evaluateAll();

            for (int i = 0; i < criterias.size(); i++) {
                Criteria criteria = criterias.get(i);
                if (criteria.getWeights() > 0) {
                    double value = sheet.getRow(criteria.getReadCell().y)
                            .getCell(criteria.getReadCell().x)
                            .getNumericCellValue();
                    criteria.setFinalPoints(value);
                }
            }

            // wb.close() - using older version of apache poi (due to 4.4 compatibility issues)
            // which doesn't have a close() method
            inputStream.close();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static File getExcelFile() {
        File file = new File(Application.getAppContext().getFilesDir(), "ficha_avaliacao.xlsx");
        if (!file.exists()) {
            try {
                InputStream is = Application.getAppContext().getResources().openRawResource(R.raw.ficha_avaliacao);
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
