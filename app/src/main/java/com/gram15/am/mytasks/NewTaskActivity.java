package com.gram15.am.mytasks;

import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.gram15.am.mytasks.data.DbContract;
import com.gram15.am.mytasks.data.TaskService;
import com.gram15.am.mytasks.views.DatePickerFragment;

import java.util.Calendar;

/**
 * Created by AMantovan on 21/08/2017.
 */

public class NewTaskActivity  extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener,
        View.OnClickListener {

    //Selected due date, stored as a timestamp
    private long mDueDate = Long.MAX_VALUE;

    private TextInputEditText mDescriptionView;
    private TextInputEditText mDetailsView;
    private SwitchCompat mPrioritySelect;
    private TextView mDueDateView;
    private static final String TASK_TITLE_KEY = "title_key";
    private static final String TASK_DETAILS_KEY = "details_key";
    private static final String TASK_DATE_KEY = "date_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        ActionBar actionBar = this.getSupportActionBar();
        // Sets the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mDescriptionView = (TextInputEditText) findViewById(R.id.tiet_input_description);
        mDetailsView = (TextInputEditText) findViewById(R.id.tiet_input_details);
        mPrioritySelect = (SwitchCompat) findViewById(R.id.sc_priority);
        mDueDateView = (TextView) findViewById(R.id.tv_date);
        View mSelectDate = findViewById(R.id.tv_date);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TASK_TITLE_KEY) ) {
                mDescriptionView.setText(savedInstanceState.getString(TASK_TITLE_KEY));
            }
            if (savedInstanceState.containsKey(TASK_DETAILS_KEY) ) {
                mDetailsView.setText(savedInstanceState.getString(TASK_DETAILS_KEY));
            }
            if (savedInstanceState.containsKey(TASK_DATE_KEY) ) {
                setDateSelection(savedInstanceState.getLong(TASK_DATE_KEY));
            }
        }

        mSelectDate.setOnClickListener(this);
        updateDateDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_new_task, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == R.id.action_save) {
            saveItem();
            return true;
        }
        else if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

    /* Manages the selected date value */
    public void setDateSelection(long selectedTimestamp) {
        mDueDate = selectedTimestamp;
        updateDateDisplay();
    }

    public long getDateSelection() {
        return mDueDate;
    }

    /* Click events on Due Date */
    @Override
    public void onClick(View v) {
        DatePickerFragment dialogFragment = new DatePickerFragment();
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
    }

    /* Date set events from dialog */
    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        //Set to noon on the selected day
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR_OF_DAY, 12);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);

        setDateSelection(c.getTimeInMillis());
    }

    private void updateDateDisplay() {
        if (getDateSelection() == Long.MAX_VALUE) {
            mDueDateView.setText(R.string.date_empty);
        } else {
            CharSequence formatted = DateUtils.getRelativeTimeSpanString(this, mDueDate);
            mDueDateView.setText(formatted);
        }
    }

    private void saveItem() {
        //Insert a new item
        ContentValues values = new ContentValues(4);
        values.put(DbContract.TaskColumns.DESCRIPTION, mDescriptionView.getText().toString());
        values.put(DbContract.TaskColumns.IS_PRIORITY, mPrioritySelect.isChecked() ? 1 : 0);
        values.put(DbContract.TaskColumns.IS_COMPLETE, 0);
        values.put(DbContract.TaskColumns.DUE_DATE, getDateSelection());
        values.put(DbContract.TaskColumns.DETAILS, mDetailsView.getText().toString());

        TaskService.insertNewTask(this, values);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TASK_TITLE_KEY, mDescriptionView.getText().toString());
        outState.putString(TASK_DETAILS_KEY, mDetailsView.getText().toString());
        outState.putLong(TASK_DATE_KEY, getDateSelection());

    }
}

