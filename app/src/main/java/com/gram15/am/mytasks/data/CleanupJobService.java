package com.gram15.am.mytasks.data;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by AMantovan on 21/08/2017.
 */

public class CleanupJobService extends JobService {
    private static final String TAG = CleanupJobService.class.getSimpleName();

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "Cleanup job started");
        new CleanupTask().execute(params);

        //Work is not yet complete
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        //No need to reschedule any jobs
        return false;
    }

    /* Handle access to the database on a background thread */
    private class CleanupTask extends AsyncTask<JobParameters, Void, JobParameters> {

        @Override
        protected JobParameters doInBackground(JobParameters... params) {
            String where = String.format("%s = ?", DbContract.TaskColumns.IS_COMPLETE);
            String[] args = {"1"};

            int count = getContentResolver().delete(DbContract.CONTENT_URI, where, args);
            Log.d(TAG, "Cleaned up " + count + " completed tasks");

            return params[0];
        }

        @Override
        protected void onPostExecute(JobParameters jobParameters) {
            //Notify that the work is now done
            jobFinished(jobParameters, false);
        }
    }
}
