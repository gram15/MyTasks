package com.gram15.am.mytasks.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.gram15.am.mytasks.R;
import com.gram15.am.mytasks.TaskUtils;

/**
 * Created by AMantovan on 21/08/2017.
 */

public class DbHelper extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "tasks.db";
        private static final int DATABASE_VERSION = 2;

        private static final String SQL_CREATE_TABLE_TASKS = String.format("CREATE TABLE %s"
                        +" (%s INTEGER PRIMARY KEY AUTOINCREMENT, %s TEXT, %s INTEGER, %s INTEGER,%s TEXT, %s INTEGER)",
                DbContract.TABLE_TASKS,
                DbContract.TaskColumns._ID,
                DbContract.TaskColumns.DESCRIPTION,
                DbContract.TaskColumns.IS_COMPLETE,
                DbContract.TaskColumns.DUE_DATE,
                DbContract.TaskColumns.DETAILS,
                DbContract.TaskColumns.PRIORITY_LEVEL
        );

        private final Context mContext;

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SQL_CREATE_TABLE_TASKS);
            //loadDemoTask(db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + DbContract.TABLE_TASKS);
            //updateTableTasks(db);
            onCreate(db);
        }

        private void loadDemoTask(SQLiteDatabase db) {
            ContentValues values = new ContentValues();
            values.put(DbContract.TaskColumns.DESCRIPTION, mContext.getResources().getString(R.string.demo_task_description));
            values.put(DbContract.TaskColumns.IS_COMPLETE, 0);
            values.put(DbContract.TaskColumns.DUE_DATE, Long.MAX_VALUE);
            values.put(DbContract.TaskColumns.DETAILS, mContext.getResources().getString(R.string.demo_task_details));
            values.put(DbContract.TaskColumns.PRIORITY_LEVEL, TaskUtils.Constants.PRIORITY_lOW);

            db.insertOrThrow(DbContract.TABLE_TASKS, null, values);
        }

        // TODO inserire metodo per fare update dati vecchi con priorità booleano
        private void updateTableTasks(SQLiteDatabase db){

        }
    }
