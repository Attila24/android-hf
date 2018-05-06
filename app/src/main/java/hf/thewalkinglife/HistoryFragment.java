package hf.thewalkinglife;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.BindView;
import butterknife.ButterKnife;
import hf.thewalkinglife.adapter.StepAdapter;
import hf.thewalkinglife.db.LoadStepsTask;
import hf.thewalkinglife.db.StepDataDbManager;

public class HistoryFragment extends Fragment implements LoadStepsTask.StepsLoaderFragment {

    @BindView(R.id.step_data_list) RecyclerView stepDataList;

    private StepAdapter adapter;
    private StepDataDbManager dbManager;
    private LoadStepsTask loadStepsTask;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbManager = StepsApplication.getDbManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (loadStepsTask != null) {
            loadStepsTask.cancel(false);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adapter != null && adapter.getCursor() != null) {
            adapter.getCursor().close();
        }
    }


    private void refreshList() {
        if (loadStepsTask != null) {
            loadStepsTask.cancel(false);
        }
        loadStepsTask = new LoadStepsTask(this, dbManager);
        loadStepsTask.execute();
    }

    @Override
    public void setSteps(Cursor steps) {
        adapter = new StepAdapter(getContext(), steps);
        stepDataList.setAdapter(adapter);
    }
}
