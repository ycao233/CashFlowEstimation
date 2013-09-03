package com.ycao.cashflowestimation.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.inject.Inject;
import com.ycao.cashflowestimation.R;
import com.ycao.cashflowestimation.dal.SQLiteConnector;
import com.ycao.cashflowestimation.ui.InvoiceActivity;

import roboguice.fragment.RoboFragment;

/**
 * Created by ycao on 9/2/13.
 */
public class InvoiceSummaryFragment extends RoboFragment {

    private final static String CLASS_NAME = InvoiceSummaryFragment.class.getName();

    @Inject
    private SQLiteConnector sqlConn;

    private Button addInvoiceButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invoice_summary, container, false);

        addInvoiceButton = (Button) view.findViewById(R.id.add_invoice_button);
        addInvoiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newInvoice = new Intent(getActivity(), InvoiceActivity.class);
                startActivity(newInvoice);
            }
        });

        return view;
    }
}
