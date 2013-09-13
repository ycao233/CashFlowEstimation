package com.ycao.cashflowestimation.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ycao.cashflowestimation.R;
import com.ycao.cashflowestimation.domain.Invoice;

import java.util.List;

/**
 * Created by ycao on 9/13/13.
 */
public class InvoiceListAdapter extends BaseAdapter {
    private List<Invoice> items;
    private LayoutInflater inflater;
    private Context context;

    public InvoiceListAdapter(Context context, List<Invoice> items) {
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
        return ((Invoice) getItem(position)).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Invoice i = (Invoice) getItem(position);
        final View entry = this.inflater.inflate(R.layout.list_item_invoice, null);
        TextView vendor = (TextView) entry.findViewById(R.id.vendor_name_textview);
        TextView invNum = (TextView) entry.findViewById(R.id.invoice_num_textview);
        TextView invAmount = (TextView) entry.findViewById(R.id.invoice_amount_textview);
        TextView dueDate = (TextView) entry.findViewById(R.id.duedate_textview);

        vendor.setText(i.getVendor());
        invNum.setText(i.getInvoiceNumber());
        invAmount.setText("$"+i.getTotalDue());
        dueDate.setText(i.getPayments().get(0).getDate().toString("MM/dd/YYYY"));
        return entry;
    }
}
