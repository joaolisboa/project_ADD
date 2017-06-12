package ipleiria.project.add.data.model;

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

    @Override
    public String toString() {
        return (startDate.getYear() + 1900) + " - " + (endDate.getYear() + 1900);
    }
}
