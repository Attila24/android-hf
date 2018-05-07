package hf.thewalkinglife.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import hf.thewalkinglife.R;
import hf.thewalkinglife.db.StepDataDbManager;
import hf.thewalkinglife.model.StepData;

/**
 * Adapter for bridging step data between a cursor and the RecyclerView in the HistoryFragment.
 */
public class StepAdapter extends CursorRecyclerViewAdapter<StepAdapter.ViewHolder> {
    private static final String TAG = "StepAdapter";

    public StepAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    /**
     * Binds a given stepdata's values to the viewholder.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        final StepData stepData = StepDataDbManager.cursorToStepData(cursor);

        holder.stepDataDate.setText(stepData.date);
        holder.stepDataCount.setText(mContext.getString(R.string.stepdata_row_steps, String.valueOf(stepData.stepCount)));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_stepdata, parent, false);
        return new ViewHolder(view);
    }

    /**
     * A helper class that holds the two values of a stepdata row.
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.stepdata_date) TextView stepDataDate;
        @BindView(R.id.stepdata_count) TextView stepDataCount;

        ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
