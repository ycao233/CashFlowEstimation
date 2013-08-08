package com.ycao.cashflowestimation.ui;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.google.inject.Inject;
import com.ycao.cashflowestimation.R;
import com.ycao.cashflowestimation.core.CashFlowEstimator;
import com.ycao.cashflowestimation.dal.SQLiteConnector;
import com.ycao.cashflowestimation.domain.CashFlowDate;
import com.ycao.cashflowestimation.ui.adapter.EstimationListAdapter;
import com.ycao.cashflowestimation.utils.Constants;

import org.joda.time.DateMidnight;

import java.util.List;

import roboguice.fragment.RoboFragment;

import static com.ycao.cashflowestimation.utils.Constants.INIT_CASH_FLOW;
import static com.ycao.cashflowestimation.utils.Constants.INIT_DATE;
import static com.ycao.cashflowestimation.utils.Constants.WEEKDAY_INCOME;
import static com.ycao.cashflowestimation.utils.Constants.WEEKDAY_INCOME_DEFAULT;
import static com.ycao.cashflowestimation.utils.Constants.WEEKEND_INCOME;
import static com.ycao.cashflowestimation.utils.Constants.WEEKEND_INCOME_DEFAULT;

/**
 * Created by ycao on 8/6/13.
 */
public class EstimationFragment extends RoboFragment {

    // used to communicate between threads
    private volatile boolean loading = false;

    private final static String CLASS_NAME = EstimationFragment.class.getName();

    @Inject
    private SQLiteConnector sqlConn;

    @Inject
    private CashFlowEstimator estimator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_estimation, container, false);
        Log.d(CLASS_NAME, "inflated view");
        final ListView cashEstimationList = (ListView) view.findViewById(R.id.cash_estimation_listView);
        CashFlowDate firstDay = getInitialCash();
        CashFlowDate today = estimator.getTodayCashFromPast(firstDay);
        final List<CashFlowDate> estimatedList = estimator.getSubsequentDates(today, 45, true);

        final CashFlowEstimator threadSafeEstimator = estimator;

        /* listview */
        cashEstimationList.setAdapter(new EstimationListAdapter(getActivity(), estimatedList));
        cashEstimationList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastPos = firstVisibleItem + visibleItemCount;
                Log.d(CLASS_NAME, "last position is: "+lastPos+ ", firstVisibleItem: "+firstVisibleItem+", visibleItemCount: "+visibleItemCount);
                if ((lastPos == totalItemCount) && !loading) {
                    //I am at last element, and no one initailized loading new items
                    final CashFlowDate lastEstimation = (CashFlowDate) cashEstimationList.getAdapter().getItem(lastPos-1);
                    new CalculateMoreEstimations(15, lastEstimation, threadSafeEstimator,
                            estimatedList, (BaseAdapter) (cashEstimationList.getAdapter())).execute();
                }
            }
        });

        return view;
    }

    private CashFlowDate getInitialCash() {
        final SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_NAME, getActivity().MODE_PRIVATE);
        estimator.setWeekdayIncome(settings.getFloat(WEEKDAY_INCOME, WEEKDAY_INCOME_DEFAULT));
        estimator.setWeekendIncome(settings.getFloat(WEEKEND_INCOME, WEEKEND_INCOME_DEFAULT));
        long firstDate = settings.getLong(INIT_DATE, DateMidnight.now().getMillis());
        long cash = settings.getLong(INIT_CASH_FLOW, 0);
        CashFlowDate date = new CashFlowDate(new DateMidnight(firstDate));
        date.setCalculatedCash(cash);

        return date;
    }

    private class CalculateMoreEstimations extends AsyncTask<Void, Void, Void> {
        private int days;
        private CashFlowDate lastEstimation;
        private CashFlowEstimator estimator;
        private List<CashFlowDate> estimatedList;
        private BaseAdapter adapter;

        public CalculateMoreEstimations(int days, CashFlowDate lastEstimation,
                    CashFlowEstimator threadSafeEstimator, List<CashFlowDate> estimatedList, BaseAdapter adapter) {
            this.days = days;
            this.lastEstimation = lastEstimation;
            this.estimator = threadSafeEstimator;
            this.estimatedList = estimatedList;
            this.adapter = adapter;
        }

        @Override
        protected Void doInBackground(Void... params) {
            loading = true;
            List<CashFlowDate> nextDays = estimator.getSubsequentDates(lastEstimation, days, false);
            estimatedList.addAll(nextDays);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            adapter.notifyDataSetChanged();
            loading = false;
        }
    }
}
