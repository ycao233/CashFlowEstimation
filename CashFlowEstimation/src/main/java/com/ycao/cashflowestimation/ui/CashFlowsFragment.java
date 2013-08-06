package com.ycao.cashflowestimation.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.ycao.cashflowestimation.R;
import com.ycao.cashflowestimation.dal.SQLiteConnector;
import com.ycao.cashflowestimation.domain.RecurrentCashFlow;

import java.util.List;

import roboguice.fragment.RoboFragment;

import static com.ycao.cashflowestimation.ui.MainActivity.*;

/**
 * Created by ycao on 8/4/13.
 */
public class CashFlowsFragment extends RoboFragment {

    private static final String CLASS_NAME = CashFlowsFragment.class.getName();

    @Inject private SQLiteConnector sqlConn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cash_flows, container, false);
        EditText weekdayIncome = (EditText) view.findViewById(R.id.weekday_editText);
        EditText weekendIncome = (EditText) view.findViewById(R.id.weekend_editText);

        SharedPreferences settings = getActivity().getSharedPreferences(APP_NAME, getActivity().MODE_PRIVATE);
        weekdayIncome.setText(String.valueOf(settings.getFloat(WEEKDAY_INCOME, WEEKDAY_INCOME_DEFAULT)));
        weekendIncome.setText(String.valueOf(settings.getFloat(WEEKEND_INCOME, WEEKEND_INCOME_DEFAULT)));

        List<RecurrentCashFlow> allOutFlow = RecurrentCashFlow.getAllRecurrentCashFlow(sqlConn.getWritableDatabase());
        ListView outflow = (ListView) view.findViewById(R.id.recurrent_cashflow_list);
        outflow.setAdapter(new RecurrentCashFlowListAdapter(getActivity(), allOutFlow));

        return view;
    }

    static class RecurrentCashFlowListAdapter extends BaseAdapter {
        private List<RecurrentCashFlow> outflows;
        private LayoutInflater inflater;
        private Context context;

        public RecurrentCashFlowListAdapter(Context context, List<RecurrentCashFlow> outflows) {
            this.inflater = LayoutInflater.from(context);
            this.context = context;
            this.outflows = outflows;
        }

        @Override
        public int getCount() {
            return outflows.size();
        }

        @Override
        public Object getItem(int position) {
            if (position < outflows.size()) {
                return outflows.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            RecurrentCashFlow f = (RecurrentCashFlow) getItem(position);
            if (f != null) {
                return f.getId();
            }

            return -1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final RecurrentCashFlow flow = (RecurrentCashFlow) getItem(position);
            View entry = this.inflater.inflate(R.layout.list_item_cash_flow, null);
            if (flow != null) {
                TextView name = (TextView) entry.findViewById(R.id.outflow_name_textView);
                EditText amount = (EditText) entry.findViewById(R.id.outflow_amount_editText);
                Spinner scheduleSpinner = (Spinner) entry.findViewById(R.id.schedule_spinner);

                name.setText(flow.getDescription());

                /* amount adapter */
                amount.setText(String.valueOf(flow.getAmount()));
                amount.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        try {
                            double amt =  Double.parseDouble(String.valueOf(s));
                            flow.setAmount(amt);
                        } catch (NumberFormatException e) {
                            Toast.makeText(context, "Please enter a valid amount", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                /* spinner adapter */
                ArrayAdapter spinnerAdapter = (ArrayAdapter) scheduleSpinner.getAdapter();
                scheduleSpinner.setSelection(spinnerAdapter.getPosition(flow.getSchedule().capitalize()));
                scheduleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        flow.setSchedule(RecurrentCashFlow.Schedule.valueOf(((String)parent.getItemAtPosition(position)).toUpperCase()));
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
            return entry;
        }

        public List<RecurrentCashFlow> getOutflows() {
            return this.outflows;
        }
    }
}
