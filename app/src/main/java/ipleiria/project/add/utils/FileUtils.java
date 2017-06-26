package ipleiria.project.add.utils;

import android.util.Log;

import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import ipleiria.project.add.Application;
import ipleiria.project.add.data.model.Area;
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

    public static void generateNote(String sFileName) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy");
            int yearStart = Integer.parseInt(yearDateFormat.format(ItemsRepository.getInstance().getCurrentPeriod().getStartDate()));
            int yearEnd = Integer.parseInt(yearDateFormat.format(ItemsRepository.getInstance().getCurrentPeriod().getEndDate()));
            File file = new File(Application.getAppContext().getFilesDir(), sFileName);
            FileWriter writer = new FileWriter(file);
            writer.append("Curriculum Vitae Detalhado - Avaliação de Desempenho \n");
            writer.append("\t ESTG - IPLEIRIA \n");
            writer.append("Nome do Avaliado: " + UserService.getInstance().getUser().getName()+"\n");
            writer.append("Categoria:  \n");
            writer.append("Unidade Orgânica: Escola Superior de Tecnologia e Gestão \n");
            writer.append("Departamento: Departamento de " + UserService.getInstance().getUser().getDepartment()+"\n");
            writer.append("Regime de contratação:  \n");
            writer.append("Período em avaliação: " + yearStart +" a " +yearEnd +"\n");

            for (Dimension dimension : CategoryRepository.getInstance().getDimensions()) {
                if(dimension.getNumberOfItems() > 0) {
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
        } catch (
                IOException e)

        {
            e.printStackTrace();
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
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat yearDateFormat = new SimpleDateFormat("yyyy");

            int yearStart = Integer.parseInt(yearDateFormat.format(ItemsRepository.getInstance().getCurrentPeriod().getStartDate()));
            int yearEnd = Integer.parseInt(yearDateFormat.format(ItemsRepository.getInstance().getCurrentPeriod().getEndDate()));
            int duration = 0;
            if( yearEnd - yearStart * 12 > 0) {
                duration = yearEnd - yearStart * 12;
            }else{
                duration = 12;
            }
            sheet.getRow(2).getCell(7).setCellValue(duration);
            sheet.getRow(1).getCell(2).setCellValue(UserService.getInstance().getUser().getName());
            sheet.getRow(2).getCell(2).setCellValue(UserService.getInstance().getUser().getDepartment());
            String startDate = dateFormat.format(ItemsRepository.getInstance().getCurrentPeriod().getStartDate());
            String endDate = dateFormat.format(ItemsRepository.getInstance().getCurrentPeriod().getEndDate());
            sheet.getRow(2).getCell(10).setCellValue(startDate);
            sheet.getRow(2).getCell(13).setCellValue(endDate);

            List<Dimension> dimensions = CategoryRepository.getInstance().getDimensions();
            User user = UserService.getInstance().getUser();
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
                sheet.getRow(criteria.getWriteCell().y)
                        .getCell(criteria.getWriteCell().x)
                        .setCellValue(criteria.getWeights());


            }
            try (FileOutputStream stream = new FileOutputStream(file)) {
                wb.write(stream);
            }
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
