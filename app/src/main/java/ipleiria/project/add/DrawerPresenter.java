package ipleiria.project.add;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ipleiria.project.add.data.model.EvaluationPeriod;
import ipleiria.project.add.data.model.Item;
import ipleiria.project.add.data.source.FilesRepository;
import ipleiria.project.add.data.source.UserService;
import ipleiria.project.add.data.source.database.ItemsRepository;
import ipleiria.project.add.utils.FileUtils;

import static ipleiria.project.add.data.source.UserService.PERIOD_DATE_FORMAT;

/**
 * Created by Lisboa on 01-Jul-17.
 */

public class DrawerPresenter implements BaseContract.Presenter{

    private static final String TAG = "DRAWER_PRESENTER";

    private BaseContract.View baseView;

    private final ItemsRepository itemsRepository;
    private final FilesRepository filesRepository;
    private final UserService userService;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    private Date startDate;
    private Date endDate;

    @SuppressLint("SimpleDateFormat")
    DrawerPresenter(BaseContract.View baseView, ItemsRepository itemsRepository,
                    FilesRepository filesRepository, UserService userService){
        this.baseView = baseView;
        this.baseView.setPresenter(this);

        this.filesRepository = filesRepository;
        this.itemsRepository = itemsRepository;
        this.userService = userService;

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat(PERIOD_DATE_FORMAT);
    }

    // TODO: 01-Jul-17 make base presenter of all presenters with drawer view, where subscribe will set user info
    @Override
    public void subscribe() {
        baseView.setUserInfo(userService.getUser());
    }

    @Override
    public void unsubscribe() {

    }

    @Override
    public void exportFiles() {
        baseView.showProgressDialog();

        itemsRepository.getItems(false, new FilesRepository.Callback<List<Item>>() {
            @Override
            public void onComplete(List<Item> result) {
                final Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    public void run() {
                        FileUtils.readExcel();
                        FileUtils.generateReport();

                        handler.post(new Runnable() {
                            public void run() {
                                String username = userService.getUser().getName();

                                baseView.hideProgressDialog();

                                if(filesRepository.exportFiles(username)){
                                    baseView.showOnlineFilesExported();
                                }else{
                                    baseView.showOfflineFilesExported();
                                }

                            }
                        });

                    }
                };
                new Thread(runnable).start();
            }

            @Override
            public void onError(Exception e) {
                Log.d(TAG, e.getMessage(), e);
                baseView.hideProgressDialog();
            }
        });
    }

    @Override
    public void createPeriod() {
        EvaluationPeriod evaluationPeriod = new EvaluationPeriod();
        evaluationPeriod.setStartDate(startDate);
        evaluationPeriod.setEndDate(endDate);

        userService.getUser().addEvaluationPeriod(evaluationPeriod);
        userService.saveUserInfo();
    }

    @Override
    public boolean areDatesValid() {
        if(startDate == null || endDate == null){
            return false;
        }
        return startDate.compareTo(endDate) <= 0;
    }

    @Override
    public void setStartDate(int year, int month, int dayOfMonth) {
        setDate(year, month, dayOfMonth);
        startDate = calendar.getTime();

        //verify date comes before endDate
        if(endDate != null){
            if(startDate.compareTo(endDate) > 0){
                baseView.showDatesInvalid();
            }else{
                baseView.showDatesValid();
            }
        }
        baseView.setStartDateText(dateFormat.format(calendar.getTime()));
    }

    @Override
    public void setEndDate(int year, int month, int dayOfMonth) {
        setDate(year, month, dayOfMonth);
        endDate = calendar.getTime();

        //verify date comes after startDate
        if(startDate != null){
            if(endDate.compareTo(startDate) < 0){
                baseView.showDatesInvalid();
            }else{
                baseView.showDatesValid();
            }
        }
        baseView.setEndDateText(dateFormat.format(calendar.getTime()));
    }

    private void setDate(int year, int month, int dayOfMonth){
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
    }
}
