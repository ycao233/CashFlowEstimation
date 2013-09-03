package com.ycao.cashflowestimation.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.inject.Inject;
import com.ycao.cashflowestimation.R;
import com.ycao.cashflowestimation.dal.SQLiteConnector;
import com.ycao.cashflowestimation.domain.RecurrentCashFlow;
import com.ycao.cashflowestimation.ui.adapter.RecurrentCashFlowListAdapter;
import com.ycao.cashflowestimation.utils.Constants;

import java.util.List;

import roboguice.fragment.RoboFragment;

/**
 * Created by ycao on 8/4/13.
 */
public class CashFlowsFragment extends RoboFragment {

    private static final String CLASS_NAME = CashFlowsFragment.class.getName();

    @Inject private SQLiteConnector sqlConn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_flows, container, false);
        final EditText weekdayIncome = (EditText) view.findViewById(R.id.weekday_editText);
        final EditText weekendIncome = (EditText) view.findViewById(R.id.weekend_editText);

        final SharedPreferences settings = getActivity().getSharedPreferences(Constants.APP_NAME, getActivity().MODE_PRIVATE);
        weekdayIncome.setText(String.valueOf(settings.getFloat(Constants.WEEKDAY_INCOME, Constants.WEEKDAY_INCOME_DEFAULT)));
        weekendIncome.setText(String.valueOf(settings.getFloat(Constants.WEEKEND_INCOME, Constants.WEEKEND_INCOME_DEFAULT)));

        List<RecurrentCashFlow> allOutFlow = RecurrentCashFlow.getAllRecurrentCashFlow(sqlConn.getWritableDatabase());
        final ListView outflow = (ListView) view.findViewById(R.id.recurrent_cashflow_list);
        outflow.setAdapter(new RecurrentCashFlowListAdapter(getActivity(), allOutFlow));

        Button estimateButton = (Button) view.findViewById(R.id.estimate_button);
        estimateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = settings.edit();
                editor.putFloat(Constants.WEEKDAY_INCOME, Float.parseFloat(String.valueOf(weekdayIncome.getText())));
                editor.putFloat(Constants.WEEKEND_INCOME, Float.parseFloat(String.valueOf(weekendIncome.getText())));

                editor.commit();

                List<RecurrentCashFlow> cashflows = ((RecurrentCashFlowListAdapter) outflow.getAdapter()).getOutflows();
                for (RecurrentCashFlow cashflow : cashflows) {
                    cashflow.persist(sqlConn.getWritableDatabase());
                }

                getActivity().getActionBar().setSelectedNavigationItem(0);
            }
        });

        return view;
    }
}
