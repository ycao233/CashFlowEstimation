package com.ycao.cashflowestimation.ui.fragment;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.ycao.cashflowestimation.R;
import com.ycao.cashflowestimation.core.CashFlowEstimator;
import com.ycao.cashflowestimation.dal.SQLiteConnector;
import com.ycao.cashflowestimation.domain.CashFlowDate;
import com.ycao.cashflowestimation.domain.Invoice;
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

        final TextView todayHeader = (TextView) view.findViewById(R.id.today_header_textview);
        todayHeader.setText("Today is " + today.getDate().toString("MM/dd/yyyy E"));

        final EditText todayCash = (EditText) view.findViewById(R.id.today_cash_editText);
        todayCash.setText(String.valueOf(today.getCalculatedCash()));

        final TextView todayDue = (TextView) view.findViewById(R.id.today_invoice_textView);
        todayDue.setText("(-" + String.valueOf(today.getTotalDue()) + ")");

        Button refresh = (Button) view.findViewById(R.id.refresh_button);

        final CashFlowEstimator threadSafeEstimator = estimator;

        cashEstimationList.setAdapter(new EstimationListAdapter(getActivity(), estimator.getSubsequentDates(today, 45, false)));

        /* listeners */
        cashEstimationList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastPos = firstVisibleItem + visibleItemCount;
                if ((lastPos == totalItemCount) && !loading) {
                    //I am at last element, and no one initailized loading new items
                    final CashFlowDate lastEstimation = (CashFlowDate) cashEstimationList.getAdapter().getItem(lastPos-1);
                    new CalculateMoreEstimations(15, threadSafeEstimator,
                            (EstimationListAdapter) (cashEstimationList.getAdapter())).execute();
                }
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_NAME, getActivity().MODE_PRIVATE);
                SharedPreferences.Editor editor = settings.edit();
                DateMidnight now = DateMidnight.now();
                editor.putLong(INIT_DATE, now.getMillis());
                float cashInput = Float.parseFloat(String.valueOf(todayCash.getText()));
                editor.putFloat(INIT_CASH_FLOW, cashInput);
                editor.commit();
                Log.d(CLASS_NAME, String.format("calculating using day: (%s), and cash amount: (%f)",
                        now.toString("MM/dd/yyyy"), cashInput));

                CashFlowDate today = new CashFlowDate(now);
                today.setCalculatedCash(cashInput);

                EstimationListAdapter adapter = (EstimationListAdapter) cashEstimationList.getAdapter();
                adapter.getItems().clear();
                adapter.getItems().addAll(estimator.getSubsequentDates(today, 45, false));

                adapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    private CashFlowDate getInitialCash() {
        final SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_NAME, getActivity().MODE_PRIVATE);
        estimator.setWeekdayIncome(settings.getFloat(WEEKDAY_INCOME, WEEKDAY_INCOME_DEFAULT));
        estimator.setWeekendIncome(settings.getFloat(WEEKEND_INCOME, WEEKEND_INCOME_DEFAULT));
        long firstDate = settings.getLong(INIT_DATE, DateMidnight.now().getMillis());
        float cash = settings.getFloat(INIT_CASH_FLOW, 0);
        CashFlowDate date = new CashFlowDate(new DateMidnight(firstDate));
        date.setInvoices(Invoice.getAllInvoiceInRange(sqlConn.getWritableDatabase(), date.getDate(), date.getDate()));
        date.setCalculatedCash(cash);

        return date;
    }

    private class CalculateMoreEstimations extends AsyncTask<Void, Void, Void> {
        private int days;
        private CashFlowEstimator estimator;
        private EstimationListAdapter adapter;

        public CalculateMoreEstimations(int days, CashFlowEstimator threadSafeEstimator,
                                        EstimationListAdapter adapter) {
            this.days = days;
            this.estimator = threadSafeEstimator;
            this.adapter = adapter;
        }

        @Override
        protected Void doInBackground(Void... params) {
            loading = true;
            CashFlowDate lastEstimation = (CashFlowDate) adapter.getItem(adapter.getCount()-1);
            List<CashFlowDate> nextDays = estimator.getSubsequentDates(lastEstimation, days, false);
            adapter.getItems().addAll(nextDays);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            adapter.notifyDataSetChanged();
            loading = false;
        }
    }
}
