package com.ycao.cashflowestimation.ui.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.inject.Inject;
import com.ycao.cashflowestimation.R;
import com.ycao.cashflowestimation.dal.SQLiteConnector;
import com.ycao.cashflowestimation.domain.Invoice;
import com.ycao.cashflowestimation.ui.InvoiceActivity;
import com.ycao.cashflowestimation.ui.adapter.InvoiceListAdapter;

import java.util.List;

import roboguice.fragment.RoboFragment;

/**
 * Created by ycao on 9/2/13.
 */
public class InvoiceSummaryFragment extends RoboFragment {

    private final static String CLASS_NAME = InvoiceSummaryFragment.class.getName();

    @Inject
    private SQLiteConnector sqlConn;

    private Button addInvoiceButton;
    private final static int ADD_REQUEST = 0;


    private InvoiceListAdapter invoiceListAdapter;

    private List<Invoice> allInvoices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invoice_summary, container, false);

        addInvoiceButton = (Button) view.findViewById(R.id.add_invoice_button);
        addInvoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newInvoice = new Intent(getActivity(), InvoiceActivity.class);
                startActivityForResult(newInvoice, ADD_REQUEST);
            }
        });

        final ListView invoiceList = (ListView) view.findViewById(R.id.invoices_listView);
        allInvoices =  Invoice.getAccessor().getAllInvoiceInRange(sqlConn, null, null);
        invoiceListAdapter = new InvoiceListAdapter(getActivity(), allInvoices);
        invoiceList.setAdapter(invoiceListAdapter);
        invoiceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Invoice i = (Invoice) invoiceListAdapter.getItem(position);
                Intent editInvoice = new Intent(getActivity(), InvoiceActivity.class);
                editInvoice.putExtra(SQLiteConnector.ID, i.getId());
                startActivityForResult(editInvoice, ADD_REQUEST);
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (ADD_REQUEST == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d(CLASS_NAME, "Adding invoice...");
                long id = data.getLongExtra(SQLiteConnector.ID, -1);
                if (id != -1) {
                    Invoice i = Invoice.getAccessor().getById(sqlConn, id);
                    updateInvoice(i);
                }
            }
        }
    }

    private void updateInvoice(Invoice invoice) {
        if (invoiceListAdapter != null) {
            boolean cloned = false;
            List<Invoice> all = invoiceListAdapter.getItems();
            for (Invoice i : all) {
                if (i.getId() == invoice.getId()) {
                    i.setVendor(invoice.getVendor());
                    i.clone(invoice);
                    cloned = true;
                    break;
                }
            }
            if (!cloned) {
                all.add(invoice);
            }
            Invoice.getAccessor().sortByDueDay(all);
            allInvoices = all;
            invoiceListAdapter.setItems(allInvoices);
            invoiceListAdapter.notifyDataSetChanged();
        }
    }

}
