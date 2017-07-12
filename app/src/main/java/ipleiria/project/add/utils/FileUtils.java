package ipleiria.project.add.utils;

import android.util.Log;

import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STCalcMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.List;

import hugo.weaving.DebugLog;
import ipleiria.project.add.Application;
import ipleiria.project.add.data.model.Area;
import ipleiria.project.add.data.model.EvaluationPeriod;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.data.source.database.CategoryRepository;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.data.model.Criteria;
import ipleiria.project.add.data.model.Dimension;
import ipleiria.project.add.R;

/**
 * Created by Lisboa on 26-Apr-17.
 */

public class FileUtils {

    public static final String SHEET_FILENAME = "ficha_avaliacao.xlsx";
    public static final String DOC_FILENAME = "relatorio.txt";

    public static void generateReport() {
        try {
            SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy");
            int yearStart = Integer.parseInt(yearDateFormat.format(ItemsRepository.getInstance().getCurrentPeriod().getStartDate()));
            int yearEnd = Integer.parseInt(yearDateFormat.format(ItemsRepository.getInstance().getCurrentPeriod().getEndDate()));

            File file = new File(Application.getAppContext().getFilesDir(), DOC_FILENAME);
            Writer writer = new OutputStreamWriter(new FileOutputStream(file), "ISO8859_1");

            writer.append("Curriculum Vitae Detalhado - Avaliação de Desempenho \n");
            writer.append("\t ESTG - IPLEIRIA \n");
            writer.append("Nome do Avaliado: " + UserService.getInstance().getUser().getName() + "\n");
            writer.append("Categoria:  \n");
            writer.append("Unidade Orgânica: Escola Superior de Tecnologia e Gestão \n");
            writer.append("Departamento: Departamento de " + UserService.getInstance().getUser().getDepartment() + "\n");
            writer.append("Regime de contratação:  \n");
            writer.append("Período em avaliação: " + yearStart + " a " + yearEnd + "\n");

            for (Dimension dimension : CategoryRepository.getInstance().getDimensions()) {
                if (dimension.getNumberOfItems() > 0) {
                    writer.append(dimension.getReference() + ". " + dimension.getName() + "\n");

                    for (Area area : dimension.getAreas()) {
                        for (Criteria criteria : area.getCriterias()) {
                            if (criteria.getItems().size() > 0) {
                                writer.append(criteria.getRealReference() + " - \"" + criteria.getName() + "\"");

                                for (Item item : criteria.getItems()) {
                                    writer.append("\n\tDescriçao da atividade: " + "\n\t" + item.getDescription() + "\n");
                                }
                                writer.append("\n\t" + "Comprovativo Em anexo");
                                writer.append("\n\t" + "Pontuação " + criteria.getPoints() + "\n");
                            }
                        }

                    }
                }

            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @DebugLog
    public static void readExcel() {
        // part of poi-shadow - newer version incompatible with API 19(4.4)
        //System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl");
        //System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl");
        //System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl");

        try {
            File file = getExcelFile();
            InputStream inputStream = new FileInputStream(file);
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);

            //wb.setForceFormulaRecalculation(false);
            //wb.getCTWorkbook().getCalcPr().setCalcMode(STCalcMode.MANUAL);

            XSSFSheet sheet = wb.getSheetAt(0);

            User user = UserService.getInstance().getUser();
            List<Dimension> dimensions = CategoryRepository.getInstance().getDimensions();
            List<Criteria> criterias = CategoryRepository.getInstance().getCriterias();

            writeUserData(sheet, user, ItemsRepository.getInstance().getCurrentPeriod());
            writeDimensionWeights(sheet, user, dimensions);
            writeCategories(sheet, criterias);

            try (FileOutputStream stream = new FileOutputStream(file)) {
                wb.write(stream);
            }

            XSSFFormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
            evaluator.evaluateAll();

            for (int i = 0; i < criterias.size(); i++) {
                Criteria criteria = criterias.get(i);
                double value = sheet.getRow(criteria.getReadCell().y)
                        .getCell(criteria.getReadCell().x)
                        .getNumericCellValue();
                criteria.setFinalPoints(value);
            }

            // wb.close() - using older version of apache poi (due to 4.4 compatibility issues)
            // which doesn't have a close() method
            inputStream.close();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @DebugLog
    private static void writeUserData(XSSFSheet sheet, User user, EvaluationPeriod currentPeriod) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy");

        int yearStart = Integer.parseInt(yearDateFormat.format(currentPeriod.getStartDate()));
        int yearEnd = Integer.parseInt(yearDateFormat.format(currentPeriod.getEndDate()));
        int duration;
        if ((yearEnd - yearStart) * 12 > 0) {
            duration = (yearEnd - yearStart) * 12;
        } else {
            // default
            duration = 36;
        }

        sheet.getRow(2).getCell(7).setCellValue(duration);
        sheet.getRow(1).getCell(2).setCellValue(user.getName());
        sheet.getRow(2).getCell(2).setCellValue(user.getDepartment());
        String startDate = dateFormat.format(currentPeriod.getStartDate());
        String endDate = dateFormat.format(currentPeriod.getEndDate());
        sheet.getRow(2).getCell(10).setCellValue(startDate);
        sheet.getRow(2).getCell(13).setCellValue(endDate);
    }

    @DebugLog
    private static void writeCategories(XSSFSheet sheet, List<Criteria> criterias) {
        int points = 0;
        // first 4 criteria have a special case with a 10 point limit
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
            sheet.getRow(criteria.getWriteCell().y)
                    .getCell(criteria.getWriteCell().x)
                    .setCellValue(criteria.getWeights());
        }
    }

    @DebugLog
    private static void writeDimensionWeights(XSSFSheet sheet, User user, List<Dimension> dimensions){
        if (user.getDimensionWeightLimit(dimensions.get(0).getDbKey()) == 0) {
            sheet.getRow(6).getCell(5).setCellValue(dimensions.get(0).getWeight());
        } else {
            sheet.getRow(6).getCell(5).setCellValue(user.getDimensionWeightLimit(dimensions.get(0).getDbKey()));
        }

        if (user.getDimensionWeightLimit(dimensions.get(1).getDbKey()) == 0) {
            sheet.getRow(6).getCell(7).setCellValue(dimensions.get(1).getWeight());
        } else {
            sheet.getRow(6).getCell(7).setCellValue(user.getDimensionWeightLimit(dimensions.get(1).getDbKey()));
        }

        if (user.getDimensionWeightLimit(dimensions.get(2).getDbKey()) == 0) {
            sheet.getRow(6).getCell(10).setCellValue(dimensions.get(2).getWeight());
        } else {
            sheet.getRow(6).getCell(10).setCellValue(user.getDimensionWeightLimit(dimensions.get(2).getDbKey()));
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
