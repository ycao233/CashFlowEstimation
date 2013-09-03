package com.ycao.cashflowestimation.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Inject;
import com.ycao.cashflowestimation.R;
import com.ycao.cashflowestimation.dal.SQLiteConnector;

import roboguice.fragment.RoboFragment;

/**
 * Created by ycao on 9/2/13.
 */
public class InvoiceSummaryFragment extends RoboFragment {

    private final static String CLASS_NAME = InvoiceSummaryFragment.class.getName();

    @Inject
    private SQLiteConnector sqlConn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invoice_summary, container, false);
        return view;
    }
}
