package com.gram15.am.mytasks.data;
import android.database.Cursor;

import static com.gram15.am.mytasks.data.DbContract.getColumnInt;
import static com.gram15.am.mytasks.data.DbContract.getColumnLong;
import static com.gram15.am.mytasks.data.DbContract.getColumnString;

/**
 * Created by AMantovan on 19/08/2017.
 * Helpful entity model for holding attributes related to a task.
 */

public class Task {

    /* Constants for missing data */
    public static final long NO_DATE = Long.MAX_VALUE;
    public static final long NO_ID = -1;

    //Unique identifier in database
    public long mId;
    //Task description
    public final String mDescription;
    //Marked if task is done
    public final boolean mIsComplete;
    //Optional due date for the task
    public final long mDueDateMillis;
    //Task details
    public final String mDetails;
    //Marked if task is priority
    public final int mPriorityLevel;

    /**
     * Creates a new Task from discrete items, does not set id
     */
    public Task(String description,String details, boolean isComplete, boolean isPriority, long dueDateMillis,int priorityLevel) {
        this.mId = NO_ID; //Not set
        this.mDescription = description;
        this.mIsComplete = isComplete;
        this.mDueDateMillis = dueDateMillis;
        this.mDetails = details;
        this.mPriorityLevel = priorityLevel;
    }

    /**
     * Creates a new Task from discrete items, sets id
     */
    public Task(long id,String description,String details, boolean isComplete, boolean isPriority, long dueDateMillis,int priorityLevel) {
        this.mId = id; //set id
        this.mDescription = description;
        this.mIsComplete = isComplete;
        this.mDueDateMillis = dueDateMillis;
        this.mDetails = details;
        this.mPriorityLevel = priorityLevel;
    }

    /**
     * Creates a new Task with no due date
     */
    public Task(String description, String details, boolean isComplete, boolean isPriority,int priorityLevel) {
        this(description, details, isComplete, isPriority, NO_DATE,priorityLevel);
    }

    /**
     * Creates a new task from a database Cursor
     */
    public Task(Cursor cursor) {
        this.mId = getColumnLong(cursor, DbContract.TaskColumns._ID);
        this.mDescription = getColumnString(cursor, DbContract.TaskColumns.DESCRIPTION);
        this.mIsComplete = getColumnInt(cursor, DbContract.TaskColumns.IS_COMPLETE) == 1;
        this.mDueDateMillis = getColumnLong(cursor, DbContract.TaskColumns.DUE_DATE);
        this.mDetails = getColumnString(cursor, DbContract.TaskColumns.DETAILS);
        this.mPriorityLevel = getColumnInt(cursor,DbContract.TaskColumns.PRIORITY_LEVEL);
    }

    /**
     * Return true if a due date has been set on this task.
     */
    public boolean hasDueDate() {
        return this.mDueDateMillis != Long.MAX_VALUE;
    }

}
