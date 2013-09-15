package com.ycao.cashflowestimation.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ycao.cashflowestimation.R;
import com.ycao.cashflowestimation.core.CashFlowDate;

import java.util.List;

/**
 * Created by ycao on 8/6/13.
 */
public class EstimationListAdapter extends BaseAdapter {
    private List<CashFlowDate> items;
    private LayoutInflater inflater;
    private Context context;

    public EstimationListAdapter(Context context, List<CashFlowDate> items) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final View entry = this.inflater.inflate(R.layout.list_item_estimation, null);
        TextView invoice = (TextView) entry.findViewById(R.id.invoice_on_day_textView);
        TextView cashAmount = (TextView) entry.findViewById(R.id.cash_textView);
        TextView date = (TextView) entry.findViewById(R.id.date_textView);
        final CashFlowDate d = (CashFlowDate) getItem(position);
        cashAmount.setText("$"+String.valueOf(d.getCalculatedCash()));
        invoice.setText("($"+d.getTotalDueOnThisDay()+")");
        String dateString = d.getDate().toString("MM/dd/yyyy\nE");
        date.setText(dateString);
        return entry;
    }

    public List<CashFlowDate> getItems() {
        return items;
    }
}
