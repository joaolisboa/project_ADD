package ipleiria.project.add.view.setup;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import ipleiria.project.add.R;
import ipleiria.project.add.view.main.MainActivity;

/**
 * Created by J on 06/06/2017.
 */

public class SetupFragment extends Fragment implements SetupContract.View {

    private SetupContract.Presenter setupPresenter;

    private EditText startDate;
    private EditText endDate;
    private EditText nameText;
    private Spinner departmentSelect;

    private TextView dateError;

    private DatePickerDialog startDatePicker;
    private DatePickerDialog endDatePicker;

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

        startDate = (EditText) root.findViewById(R.id.startDate);
        endDate = (EditText) root.findViewById(R.id.endDate);
        nameText = (EditText) root.findViewById(R.id.name);
        departmentSelect = (Spinner) root.findViewById(R.id.department);
        dateError = (TextView) root.findViewById(R.id.date_error);

        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDatePicker.show();
            }
        });

        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endDatePicker.show();
            }
        });

        Button createButton = (Button) root.findViewById(R.id.create_button);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameText.getText().toString();
                String department = departmentSelect.getSelectedItem().toString().toUpperCase();

                if(setupPresenter.isInputValid(name)){
                    setupPresenter.onCreateClick(name, department);
                    startActivity(new Intent(getContext(), MainActivity.class));
                    getActivity().finish();
                }
            }
        });

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupPresenter.setupCalendar();
    }


    @Override
    public void createDatePickers(int year, int month, int dayOfMonth) {
        startDatePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                setupPresenter.setStartDate(year, month, dayOfMonth);
            }
        }, year, month, dayOfMonth);
        endDatePicker = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                setupPresenter.setEndDate(year, month, dayOfMonth);
            }
        }, year, month, dayOfMonth);
    }

    @Override
    public void showNoNetworkError() {
        new AlertDialog.Builder(getContext())
                .setTitle("No internet")
                .setMessage("In order to continue an internet connection is required")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(getContext(), SetupActivity.class));
                        getActivity().finish();
                    }
                })
                .show();
    }

    @Override
    public void setPresenter(SetupContract.Presenter presenter) {
        setupPresenter = presenter;
    }

    @Override
    public void showStartDateInvalid() {
        startDate.setError("Start date must come before end date");
        dateError.setVisibility(View.VISIBLE);
    }

    @Override
    public void showEndDateInvalid() {
        endDate.setError("End date must come after start date");
        dateError.setVisibility(View.VISIBLE);
    }

    @Override
    public void setNameError() {
        nameText.setError("Name field is empty");
    }

    @Override
    public void showDatesValid() {
        //only one needs to be verified and if one is correct so is the other
        startDate.setError(null);
        endDate.setError(null);
        dateError.setVisibility(View.GONE);
    }

    @Override
    public void setNameValid() {
        nameText.setError(null);
    }

    @Override
    public void setStartDateText(String date) {
        startDate.setText(date);
    }

    @Override
    public void setEndDateText(String date) {
        endDate.setText(date);
    }

}
