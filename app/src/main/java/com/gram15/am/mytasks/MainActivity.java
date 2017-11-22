package com.gram15.am.mytasks;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.gram15.am.mytasks.data.DbContract;
import com.gram15.am.mytasks.data.Task;
import com.gram15.am.mytasks.data.TaskAdapter;
import com.gram15.am.mytasks.data.TaskService;

public class MainActivity extends AppCompatActivity implements

        LoaderManager.LoaderCallbacks<Cursor>,
        TaskAdapter.OnItemClickListener,
        View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener{

    // Constants for logging and referring to a unique loader
    private static final String MAIN_ACTIVITY_TAG = MainActivity.class.getSimpleName();

    private TaskAdapter mAdapter;
    private static final int TASK_LOADER_ID = 0;
    private String sortBy;
    private MyContentObserver myContentObserver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
            setContentView(R.layout.activity_main);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            mAdapter = new TaskAdapter(null);
            mAdapter.setOnItemClickListener(this);

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            recyclerView.setHasFixedSize(true);
            recyclerView.setAdapter(mAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            setupSharedPreferences();

            myContentObserver = new MyContentObserver(new Handler());

            this.getApplicationContext()
                    .getContentResolver()
                    .registerContentObserver(
                            DbContract.CONTENT_URI, true,
                            myContentObserver);

            getSupportLoaderManager().initLoader(TASK_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Click events in Floating Action Button */
    @Override
    public void onClick(View v) {
        Intent intent = new Intent(this, NewTaskActivity.class);
        startActivity(intent);
    }

    /**
     * Init sharedPreferences
     */
    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadSortByPreferences(sharedPreferences);
        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Sets sort by variable based on settings value
     * @param sharedPreferences
     */
    private void loadSortByPreferences(SharedPreferences sharedPreferences) {
        sortBy = sharedPreferences.getString(getString(R.string.pref_sortBy_key)
                ,getString(R.string.pref_sortBy_default))
                .equals(getString(R.string.pref_sortBy_default)) ? DbContract.DEFAULT_SORT : DbContract.DATE_SORT;

    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_sortBy_key))) {
            loadSortByPreferences(sharedPreferences);
            refreshData();
        }
    }
    /**
     * Instantiates and returns a new AsyncTaskLoader with the given ID.
     * This loader will return task data as a Cursor or null if an error occurs.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle loaderArgs) {
        return  new AsyncTaskLoader<Cursor>(this) {

            // Initialize a Cursor that will hold all the task data
            Cursor mTaskData = null;

            // This method is called when a loader first starts loading data
            @Override
            protected void onStartLoading() {
                if (mTaskData != null) {
                    // Delivers previously loaded data immediately
                    deliverResult(mTaskData);
                } else {
                    // Force new load
                    forceLoad();
                }
            }

            // This method loading asynchronous data
            @Override
            public Cursor loadInBackground() {
                // Query and load all task data in the background order by priority settings (DEFAULT or DUEDATE)
                try {
                    return getContentResolver().query(DbContract.CONTENT_URI,
                            null,
                            null,
                            null,
                            sortBy);

                } catch (Exception e) {
                    Log.e(MAIN_ACTIVITY_TAG, "Failed to asynchronously load data.");
                    e.printStackTrace();
                    return null;
                }
            }

            // This method sends the result of the load, a Cursor, to the registered listener
            public void deliverResult(Cursor data) {
                mTaskData = data;
                super.deliverResult(data);
            }
        };

    }

    /**
     * Called when a previously created loader has finished its load.
     *
     * @param loader The Loader that has finished.
     * @param data The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update the data that the adapter uses to create ViewHolders
        mAdapter.swapCursor(data);
    }


    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.
     * onLoaderReset removes any references this activity had to the loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    /* Click events in RecyclerView items */
    @Override
    public void onItemClick(View v, int position) {
        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
        Uri currentTaskUri = ContentUris.withAppendedId(DbContract.CONTENT_URI, mAdapter.getItem(position).mId);
        intent.setData(currentTaskUri);
        startActivity(intent);
    }

    @Override
    public void onItemToggled(boolean active, int position) {
        Task task = mAdapter.getItem(position);
        updateItem(task);

    }

    private void updateItem(final Task task) {
        //Update an item
        Uri uri = ContentUris.withAppendedId(DbContract.CONTENT_URI, task.mId);
        ContentValues values = new ContentValues(4);
        values.put(DbContract.TaskColumns.DESCRIPTION, task.mDescription);
        values.put(DbContract.TaskColumns.IS_COMPLETE, !task.mIsComplete);
        values.put(DbContract.TaskColumns.DUE_DATE, task.mDueDateMillis);
        values.put(DbContract.TaskColumns.DETAILS, task.mDetails);
        TaskService.updateTask(this,uri, values);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(myContentObserver);
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    class MyContentObserver extends ContentObserver {
        public MyContentObserver(Handler h) {
            super(h);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            refreshData();

        }
    }

    /**
     * Reloads data
     */
    private  void refreshData(){
        getSupportLoaderManager().restartLoader(TASK_LOADER_ID, null, this);
    }
}
