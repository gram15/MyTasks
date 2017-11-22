package com.gram15.am.mytasks.data;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by AMantovan on 19/08/2017.
 */

public class DbContract {
    //Database schema information
    public static final String TABLE_TASKS = "tasks";

    public static final class TaskColumns implements BaseColumns {
        //Task description
        public static final String DESCRIPTION = "description";
        //Completed marker
        public static final String IS_COMPLETE = "is_complete";
        //Completion date (can be null)
        public static final String DUE_DATE = "due_date";
        //Task details
        public static final String DETAILS = "details";
        //Task details
        public static final String PRIORITY_LEVEL = "priority_level";
    }

    //Unique authority string for the content provider
    public static final String CONTENT_AUTHORITY = "com.gram15.am.mytasks";

    /* Sort order constants */
    //Priority first, Completed last, the rest by date
    public static final String DEFAULT_SORT = String.format("%s ASC, %s DESC, %s ASC",
            TaskColumns.IS_COMPLETE, TaskColumns.PRIORITY_LEVEL, TaskColumns.DUE_DATE);

    //Completed last, then by date, followed by priority
    public static final String DATE_SORT = String.format("%s ASC, %s ASC, %s DESC",
            TaskColumns.IS_COMPLETE, TaskColumns.DUE_DATE, TaskColumns.PRIORITY_LEVEL);

    //Base content Uri for accessing the provider
    public static final Uri CONTENT_URI = new Uri.Builder().scheme("content")
            .authority(CONTENT_AUTHORITY)
            .appendPath(TABLE_TASKS)
            .build();

    /* Helpers to retrieve column values */
    public static String getColumnString(Cursor cursor, String columnName) {
        return cursor.getString( cursor.getColumnIndex(columnName) );
    }

    public static int getColumnInt(Cursor cursor, String columnName) {
        return cursor.getInt( cursor.getColumnIndex(columnName) );
    }

    public static long getColumnLong(Cursor cursor, String columnName) {
        return cursor.getLong( cursor.getColumnIndex(columnName) );
    }
}

