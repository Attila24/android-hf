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

public class StepAdapter extends CursorRecyclerViewAdapter<StepAdapter.ViewHolder> {
    private static final String TAG = "StepAdapter";

    public StepAdapter(Context context, Cursor cursor) {
        super(context, cursor);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, Cursor cursor) {
        final StepData stepData = StepDataDbManager.cursorToStepData(cursor);

        holder.stepDataDate.setText(stepData.date);
        holder.stepDataCount.setText(String.valueOf(stepData.stepCount));
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_stepdata, parent, false);
        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.stepdata_date) TextView stepDataDate;
        @BindView(R.id.stepdata_count) TextView stepDataCount;

        public ViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }
}
