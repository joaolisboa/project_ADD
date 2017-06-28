package ipleiria.project.add.data.model;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Lisboa on 12-Jun-17.
 */

public class EvaluationPeriod {

    private String dbKey;
    private Date startDate;
    private Date endDate;

    public EvaluationPeriod(){

    }

    public EvaluationPeriod(String dbKey) {
        this.dbKey = dbKey;
    }

    public String getDbKey() {
        return dbKey;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setDbKey(String dbKey) {
        this.dbKey = dbKey;
    }

    public String toStringPath(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        String startYear = String.valueOf(calendar.get(Calendar.YEAR));
        calendar.setTime(endDate);
        String endYear = String.valueOf(calendar.get(Calendar.YEAR));
        return startYear + "_" + endYear;
    }

    @Override
    @SuppressLint("SimpleDateFormat")
    public String toString() {
        SimpleDateFormat year = new SimpleDateFormat("yyyy");
        return (year.format(startDate) + " - " + year.format(endDate));
    }

    @Override
    @SuppressLint("SimpleDateFormat")
    public boolean equals(Object obj) {
        if(obj instanceof EvaluationPeriod){
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            if(format.format(startDate).equals(format.format((((EvaluationPeriod)obj)).getStartDate()))){
                return true;
            }
            if(format.format(endDate).equals(format.format((((EvaluationPeriod)obj)).getEndDate()))){
                return true;
            }
        }
        return super.equals(obj);
    }
}
