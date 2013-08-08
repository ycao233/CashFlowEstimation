package com.ycao.cashflowestimation.ui.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ycao.cashflowestimation.R;
import com.ycao.cashflowestimation.domain.RecurrentCashFlow;

import java.util.List;

public class RecurrentCashFlowListAdapter extends BaseAdapter {
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
                            double amt = Double.parseDouble(String.valueOf(s));
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
                        flow.setSchedule(RecurrentCashFlow.Schedule.valueOf(((String) parent.getItemAtPosition(position)).toUpperCase()));
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