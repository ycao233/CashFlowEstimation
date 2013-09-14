package com.ycao.cashflowestimation.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.google.inject.Inject;
import com.ycao.cashflowestimation.R;
import com.ycao.cashflowestimation.dal.SQLiteConnector;
import com.ycao.cashflowestimation.domain.Invoice;
import com.ycao.cashflowestimation.ui.InvoiceActivity;
import com.ycao.cashflowestimation.ui.adapter.InvoiceListAdapter;

import roboguice.fragment.RoboFragment;

/**
 * Created by ycao on 9/2/13.
 */
public class InvoiceSummaryFragment extends RoboFragment {

    private final static String CLASS_NAME = InvoiceSummaryFragment.class.getName();

    @Inject
    private SQLiteConnector sqlConn;

    private Button addInvoiceButton;
    private int addRequest = 0;

    private InvoiceListAdapter invoiceListAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invoice_summary, container, false);

        addInvoiceButton = (Button) view.findViewById(R.id.add_invoice_button);
        addInvoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newInvoice = new Intent(getActivity(), InvoiceActivity.class);
                startActivityForResult(newInvoice, addRequest);
            }
        });

        final ListView invoiceList = (ListView) view.findViewById(R.id.invoices_listView);
        invoiceListAdapter = new InvoiceListAdapter(getActivity(), Invoice.getAccessor().getAllInvoiceInRange(sqlConn, null, null));
        invoiceList.setAdapter(invoiceListAdapter);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (addRequest == requestCode) {
            if (resultCode == InvoiceActivity.CREATED) {
                invoiceListAdapter.notifyDataSetChanged();
            }
        }
    }
}
