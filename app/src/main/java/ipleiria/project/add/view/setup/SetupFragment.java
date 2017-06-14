package ipleiria.project.add.view.setup;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import ipleiria.project.add.R;
import ipleiria.project.add.data.model.EvaluationPeriod;
import ipleiria.project.add.data.model.User;
import ipleiria.project.add.data.source.UserService;

import static ipleiria.project.add.data.source.UserService.USER_DATA_KEY;

/**
 * Created by J on 06/06/2017.
 */

public class SetupFragment extends Fragment {


    private Date startDate_;
    private Date endDate_;
    private String name_;
    private String department_;
    public SetupFragment() {
    }

    public static SetupFragment newInstance() {
        return new SetupFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.setup_frag, container, false);

        final EditText startDate = (EditText) root.findViewById(R.id.startDate);
        final EditText endDate = (EditText) root.findViewById(R.id.endDate);
        final EditText name = (EditText) root.findViewById(R.id.name);
        final String myFormat = "dd-MM-yyyy";
        final Spinner department = (Spinner) root.findViewById(R.id.department);
        final Calendar myCalendar = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat(myFormat);
        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(USER_DATA_KEY,0);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, month);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        startDate_ = myCalendar.getTime();
                        startDate.setText(sdf.format(myCalendar.getTime()));
                    }
                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, month);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                        endDate_ = myCalendar.getTime();
                        endDate.setText(sdf.format(myCalendar.getTime()));
                    }
                }, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH))
                        .show();
            }
        });

        Button createButton = (Button) root.findViewById(R.id.create_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name_ = name.getText().toString();
                User user = UserService.getInstance().getUser();
                EvaluationPeriod evaluationPeriod = new EvaluationPeriod();
                evaluationPeriod.setStartDate(startDate_);
                evaluationPeriod.setEndDate(endDate_);
                department_ = department.getSelectedItem().toString().toUpperCase();
                user.setDepartment(department_);
                user.addEvaluationPeriod(evaluationPeriod);
                user.setName(name_);
                UserService.getInstance().saveUserInfo();
                sharedPreferences.edit().putBoolean("my_first_time", false).apply();
                getActivity().finish();

            }
        });

        return root;
    }

}
