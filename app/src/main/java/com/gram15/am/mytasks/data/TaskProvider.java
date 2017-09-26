package com.gram15.am.mytasks.data;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by AMantovan on 21/08/2017.
 */

public class TaskProvider extends ContentProvider {
    private static final String TAG = TaskProvider.class.getSimpleName();

    private static final int CLEANUP_JOB_ID = 43;

    private static final int TASKS = 100;
    private static final int TASKS_WITH_ID = 101;

    private DbHelper mDbHelper;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        // content://com.gram15.am.mytasks/tasks
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY,
                DbContract.TABLE_TASKS,
                TASKS);

        // content://com.gram15.am.mytasks/tasks/id
        sUriMatcher.addURI(DbContract.CONTENT_AUTHORITY,
                DbContract.TABLE_TASKS + "/#",
                TASKS_WITH_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        manageCleanupJob();
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null; /* Not used */
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        // Get access to database (read-only for query)
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);
        Cursor returnCursor;

        // Query for the tasks, task by id and a default case
        switch (match)
        {
            // Query for the tasks
            case TASKS:
                returnCursor = db.query(
                        DbContract.TABLE_TASKS,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;

            // Query for task by id
            case TASKS_WITH_ID:
                String id = uri.getPathSegments().get(1);
                returnCursor = db.query(
                        DbContract.TABLE_TASKS,
                        projection,
                        DbContract.TaskColumns._ID + " = ?",
                        new String[]{id},
                        null,
                        null,
                        sortOrder
                );
                break;


            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown URI:" + uri);
        }

        // Set a notification URI on the Cursor and return that Cursor
        Context context = getContext();
        if (context != null){
            returnCursor.setNotificationUri(context.getContentResolver(), uri);
        }

        // Return the Cursor
        return returnCursor;

    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Get access to the database (to write new data to)
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the tasks
        int match = sUriMatcher.match(uri);
        Uri returnedUri; // URI to be returned

        switch (match) {
            case TASKS:
                // Insert new values into the database into tasks table
                long id = db.insert(DbContract.TABLE_TASKS, null, values);
                if ( id > 0 ) {
                    returnedUri = ContentUris.withAppendedId(DbContract.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        Context context = getContext();
        if (context != null){
            context.getContentResolver().notifyChange(uri, null);
        }
        // Return constructed uri (this points to the newly inserted row of data)
        return returnedUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Get access to the database (to update data to)
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the tasks
        int match = sUriMatcher.match(uri);
        Uri returnedUri; // URI to be returned

        int count = 0;
        switch (match) {
            case TASKS_WITH_ID:
                String id = uri.getPathSegments().get(1);
                count = db.update(DbContract.TABLE_TASKS
                        ,values
                        ,DbContract.TaskColumns._ID + "= ?"
                        ,new String[]{id}
                );
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri );
        }

        Context context = getContext();
        if(context != null){
            context.getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        switch (sUriMatcher.match(uri)) {
            case TASKS:
                //Rows aren't counted with null selection
                selection = (selection == null) ? "1" : selection;
                break;
            case TASKS_WITH_ID:
                long id = ContentUris.parseId(uri);
                selection = String.format("%s = ?", DbContract.TaskColumns._ID);
                selectionArgs = new String[]{String.valueOf(id)};
                break;
            default:
                throw new IllegalArgumentException("Illegal delete URI");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        int count = db.delete(DbContract.TABLE_TASKS, selection, selectionArgs);

        if (count > 0) {
            //Notify observers of the change
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return count;
    }

    /* Initiate a periodic job to clear out completed items */
    private void manageCleanupJob() {
        Log.d(TAG, "Scheduling cleanup job");
        JobScheduler jobScheduler = (JobScheduler) getContext()
                .getSystemService(Context.JOB_SCHEDULER_SERVICE);

        //Run the job approximately every hour
        long jobInterval = 3600000L;

        ComponentName jobService = new ComponentName(getContext(), CleanupJobService.class);
        JobInfo task = new JobInfo.Builder(CLEANUP_JOB_ID, jobService)
                .setPeriodic(jobInterval)
                .setPersisted(true)
                .build();

        if (jobScheduler.schedule(task) != JobScheduler.RESULT_SUCCESS) {
            Log.w(TAG, "Unable to schedule cleanup job");
        }
    }
}
