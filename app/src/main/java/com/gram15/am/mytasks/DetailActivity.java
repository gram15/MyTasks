package com.gram15.am.mytasks;

import android.app.DatePickerDialog;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gram15.am.mytasks.alarms.AlarmScheduler;
import com.gram15.am.mytasks.data.DbContract;
import com.gram15.am.mytasks.data.Task;
import com.gram15.am.mytasks.data.TaskService;
import com.gram15.am.mytasks.views.DatePickerFragment;
import com.gram15.am.mytasks.views.TaskTitleTextView;

/**
 * Created by AMantovan on 21/08/2017.
 */

public class DetailActivity extends AppCompatActivity implements
        DatePickerDialog.OnDateSetListener {

    private Task mCurrentTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);
        ActionBar actionBar = this.getSupportActionBar();
        // Set the action bar back button to look like an up button
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //Task must be passed to this activity as a valid provider Uri
        final Uri taskUri = getIntent().getData();


        if (savedInstanceState != null) {
            mCurrentTask = null;
            Long id = 0L;
            Long dueDate = 0L;
            String description = "";
            String details = "";
            boolean isComplete = false;
            boolean isPriority = false;
            if (savedInstanceState.containsKey(DbContract.TaskColumns._ID) ) {
                id = savedInstanceState.getLong(DbContract.TaskColumns._ID);
            }
            if (savedInstanceState.containsKey(DbContract.TaskColumns.DESCRIPTION) ) {
                description = savedInstanceState.getString(DbContract.TaskColumns.DESCRIPTION);
            }
            if (savedInstanceState.containsKey(DbContract.TaskColumns.DUE_DATE) ) {
                dueDate = savedInstanceState.getLong(DbContract.TaskColumns.DUE_DATE);
            }
            if (savedInstanceState.containsKey(DbContract.TaskColumns.IS_COMPLETE) ) {
                isComplete = savedInstanceState.getBoolean(DbContract.TaskColumns.IS_COMPLETE);
            }
            if (savedInstanceState.containsKey(DbContract.TaskColumns.IS_PRIORITY) ) {
                isPriority = savedInstanceState.getBoolean(DbContract.TaskColumns.IS_PRIORITY);
            }
            if (savedInstanceState.containsKey(DbContract.TaskColumns.DETAILS) ) {
                details = savedInstanceState.getString(DbContract.TaskColumns.DETAILS);
            }

            mCurrentTask = new Task(id,description,details,isComplete,isPriority,dueDate);
            updateUI();
        }
        else{
            new GetDetailsTask().execute(taskUri);

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete) {
            // delete item
            deleteItem();
            return true;
        }
        else if (id == R.id.action_reminder) {
            // show datepickerfragment
            showDatePickerFragment();
            return true;
        } else if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        long dateForSchedulation = TaskUtils.addHourToDate(year,month,day,TaskUtils.Constants.DEFAULT_HOUR_OF_REMINDER);

        if(!TaskUtils.isDateOverdue(dateForSchedulation)){
            Uri uri = ContentUris.withAppendedId(DbContract.CONTENT_URI, mCurrentTask.mId);
            AlarmScheduler.scheduleAlarm(this,dateForSchedulation,uri);
        }
        else {
            Toast.makeText(this, R.string.wrong_date_for_schedulation, Toast.LENGTH_LONG).show();
        }

    }

    /**
     * This method retrives task data by id
     * @param uri
     */
    private boolean getCurrentTask(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            mCurrentTask = new Task(cursor);
            return true;
        }else{
            return false;
        }
    }

    /**
     * This method updates the UI with current task data
     */
    private void updateUI(){
        if(mCurrentTask!=null) {
            TaskTitleTextView detailTextDescription = (TaskTitleTextView) findViewById(R.id.tttv_description);
            ImageView detailPriority = (ImageView) findViewById(R.id.iv_detail_priority);
            TextView detailDueDate = (TextView) findViewById(R.id.tv_detail_text_date);
            TextView detailsText = (TextView) findViewById(R.id.tv_detail_details);

            detailTextDescription.setText(mCurrentTask.mDescription);
            detailsText.setText(mCurrentTask.mDetails);

            // default state
            int state = TaskTitleTextView.NORMAL;
            if(mCurrentTask.mIsComplete){
                // task completed
                state = TaskTitleTextView.DONE;
            }
            else if(mCurrentTask.hasDueDate() && TaskUtils.isDateOverdue(mCurrentTask.mDueDateMillis)){
                // task overdue
                state = TaskTitleTextView.OVERDUE;
            }
            // set state
            detailTextDescription.setState(state);

            if (mCurrentTask.mIsPriority) {
                detailPriority.setImageResource(R.drawable.ic_priority);
            } else {
                detailPriority.setImageResource(R.drawable.ic_not_priority);
            }

            if (mCurrentTask.hasDueDate()) {
                detailDueDate.setText(DateUtils.getRelativeTimeSpanString(this, mCurrentTask.mDueDateMillis));

            } else {
                detailDueDate.setText(R.string.date_empty);
            }
        }

    }

    /**
     * This method deletes current task
     */
    private void deleteItem() {
        //delete an item
        if(mCurrentTask!=null){
            Uri uri = ContentUris.withAppendedId(DbContract.CONTENT_URI, mCurrentTask.mId);
            TaskService.deleteTask(this, uri);
            finish();

        }
    }

    private void showDatePickerFragment() {
        new DatePickerFragment().show(getSupportFragmentManager(), "DatePickerFragment");

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(DbContract.TaskColumns._ID, mCurrentTask.mId);
        outState.putString(DbContract.TaskColumns.DESCRIPTION, mCurrentTask.mDescription);
        outState.putLong(DbContract.TaskColumns.DUE_DATE, mCurrentTask.mDueDateMillis);
        outState.putBoolean(DbContract.TaskColumns.IS_COMPLETE, mCurrentTask.mIsComplete);
        outState.putBoolean(DbContract.TaskColumns.IS_PRIORITY, mCurrentTask.mIsPriority);
        outState.putString(DbContract.TaskColumns.DETAILS, mCurrentTask.mDetails);


    }

    /* Handle access to the database on a background thread */
    private class GetDetailsTask extends AsyncTask<Uri, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Uri... params) {
            boolean result = false;
            if(params!=null) {
                //get data from db
                result= getCurrentTask(params[0]);

            }
            return result;

        }

        @Override
        protected void onPostExecute(Boolean result) {

            // if success update the UI
            if(result){
                updateUI();
            }
        }
    }
}
