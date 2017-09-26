package com.gram15.am.mytasks.data;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.gram15.am.mytasks.R;
import com.gram15.am.mytasks.TaskUtils;
import com.gram15.am.mytasks.views.TaskTitleTextView;

/**
 * Created by AMantovan on 19/08/2017.
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {

    /* Callback for list item click events */
    public interface OnItemClickListener {
        void onItemClick(View v, int position);

        void onItemToggled(boolean active, int position);
    }

    /* ViewHolder for task item */
    public class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TaskTitleTextView mNameView;
        public TextView mDateView;
        public ImageView mPriorityView;
        public CheckBox mCompletedCheckBox;
        public ImageView mAlarmView;
        public TextView mDetailsView;

        public TaskHolder(View itemView) {
            super(itemView);

            mNameView = (TaskTitleTextView) itemView.findViewById(R.id.tttv_description);
            mDateView = (TextView) itemView.findViewById(R.id.tv_date);
            mPriorityView = (ImageView) itemView.findViewById(R.id.iv_priority);
            mCompletedCheckBox = (CheckBox) itemView.findViewById(R.id.cb_completed);
            mAlarmView = (ImageView) itemView.findViewById(R.id.iv_date);
            mDetailsView = (TextView) itemView.findViewById(R.id.tv_description_detail);

            itemView.setOnClickListener(this);
            mCompletedCheckBox.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == mCompletedCheckBox) {
                completionToggled(this);
            } else {
                postItemClick(this);
            }
        }
    }

    private Cursor mCursor;
    private OnItemClickListener mOnItemClickListener;
    private Context mContext;

    public TaskAdapter(Cursor cursor) {
        mCursor = cursor;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    private void completionToggled(TaskHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemToggled(holder.mCompletedCheckBox.isChecked(), holder.getAdapterPosition());
        }
    }

    private void postItemClick(TaskHolder holder) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(holder.itemView, holder.getAdapterPosition());
        }
    }

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.list_item_task, parent, false);

        return new TaskHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TaskHolder holder, int position) {

        Task task = getItem(position);

        //Set values in the holder
        // set id
        holder.itemView.setTag(task.mId);
        // set iscomplete
        holder.mCompletedCheckBox.setChecked(task.mIsComplete);
        // set description
        holder.mNameView.setText(task.mDescription);
        // default state
        int state = TaskTitleTextView.NORMAL;
        if(task.mIsComplete){
            // task completed
            state = TaskTitleTextView.DONE;
        }
        else if(task.hasDueDate() && TaskUtils.isDateOverdue(task.mDueDateMillis)){
            // task overdue
            state = TaskTitleTextView.OVERDUE;
        }
        // set state
        holder.mNameView.setState(state);
        // set priority
        if(task.mIsPriority){
            holder.mPriorityView.setImageResource(R.drawable.ic_priority);
        }
        else{
            holder.mPriorityView.setImageResource(R.drawable.ic_not_priority);
        }
        // set due date
        if(task.hasDueDate()){
            holder.mDateView.setText(DateUtils.getRelativeTimeSpanString(mContext,task.mDueDateMillis));
            holder.mDateView.setVisibility(View.VISIBLE);
            holder.mAlarmView.setVisibility(View.VISIBLE);
        }
        else{
            holder.mDateView.setText(R.string.date_blank);
            holder.mDateView.setVisibility(View.GONE);
            holder.mAlarmView.setVisibility(View.GONE);
        }

        if(!task.mDetails.isEmpty()){
            holder.mDetailsView.setVisibility(View.VISIBLE);
        }
        else {
            holder.mDetailsView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return (mCursor != null) ? mCursor.getCount() : 0;
    }

    public Task getItem(int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Invalid item position requested");
        }

        return new Task(mCursor);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).mId;
    }

    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        notifyDataSetChanged();
    }
}
