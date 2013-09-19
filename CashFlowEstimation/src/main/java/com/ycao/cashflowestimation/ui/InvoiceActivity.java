package com.ycao.cashflowestimation.ui;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.ycao.cashflowestimation.R;
import com.ycao.cashflowestimation.dal.SQLiteConnector;
import com.ycao.cashflowestimation.domain.Invoice;
import com.ycao.cashflowestimation.domain.PaymentInstallment;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;

import java.util.Calendar;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


/**
 * Created by ycao on 9/3/13.
 */
@ContentView(R.layout.activity_invoice)
public class InvoiceActivity extends RoboFragmentActivity {

    public static int CANCELLED = 0;
    public static int CREATED = 1;

    @Inject
    private SQLiteConnector sqlConn;

    private Button invDatePicker;
    private Button dueDatePicker;

    @InjectView(R.id.vendor_edittext)
    private EditText vendorId;

    @InjectView(R.id.inv_number_edittext)
    private EditText invNumberInput;

    @InjectView(R.id.due_amount_edittext)
    private EditText dueAmountInput;

    @InjectView(R.id.note_edittext)
    private EditText notesInput;

    private Button saveButton;
    private Button cancelButton;

    private Invoice currInvoice;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getActionBar().hide();

        invDatePicker = (Button) findViewById(R.id.choose_inv_date_edittext);
        setupOnClickDatePickerListener(invDatePicker);

        dueDatePicker = (Button) findViewById(R.id.due_date_edittext);
        setupOnClickDatePickerListener(dueDatePicker);

        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(CANCELLED);
                finish();
            }
        });

        saveButton = (Button) findViewById(R.id.save_invoice_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validInputs()) {
                    //TODO: add ability to retrieve existing invoice later on
                    Invoice invoice = getInvoice();
                    long id = invoice.persist(sqlConn);
                    if (id == -1) {
                        Toast.makeText(InvoiceActivity.this, getString(R.string.invoice_empty_input), Toast.LENGTH_SHORT).show();
                    } else {
                        /* TODO */
                        Intent result = new Intent();
                        result.putExtra("_id", id);
                        setResult(CREATED, result);
                        finish();
                    }
                } else {
                    Toast.makeText(InvoiceActivity.this, getString(R.string.invoice_empty_input), Toast.LENGTH_SHORT).show();
                }
            }
        });

        long id = getIntent().getLongExtra(SQLiteConnector.ID, -1);
        if (id != -1) {
            currInvoice = Invoice.getAccessor().getById(sqlConn, id);
            vendorId.setText(currInvoice.getVendor());
            invNumberInput.setText(currInvoice.getInvoiceNumber());
            notesInput.setText(currInvoice.getNotes());
            invDatePicker.setText(currInvoice.getDate().toString("MM/DD/YYYY"));
            PaymentInstallment p = currInvoice.getPayments().get(0);
            dueDatePicker.setText(p.getDueDate().toString("MM/DD/YYYY"));
            dueAmountInput.setText(String.valueOf(p.getAmountDue()));
        }
    }

    private Invoice getInvoice() {
        Invoice invoice;
        if (currInvoice == null) {
           invoice = new Invoice();
        } else {
            invoice = currInvoice;
        }
        invoice.setInvoiceNumber(invNumberInput.getText().toString());
        invoice.setCredit(invoice.getCredit());
        invoice.setVendor(vendorId.getText().toString());
        invoice.setDate(getDate(invDatePicker));
        invoice.setNotes(notesInput.getText().toString());
        PaymentInstallment p = new PaymentInstallment();
        p.setDueDate(getDate(dueDatePicker));
        p.setAmountDue(Double.parseDouble(dueAmountInput.getText().toString()));
        invoice.addPayment(p);
        return invoice;
    }

    private DateMidnight getDate(Button dueDatePicker) {
        String date = dueDatePicker.getText().toString();
        return DateMidnight.parse(date, DateTimeFormat.forPattern("MM/dd/yyyy"));
    }

    private boolean validInputs() {
        return !(empty(vendorId.getText())
                || empty(invNumberInput.getText())
                || empty(dueAmountInput.getText()));
    }

    private boolean empty(Editable s) {
        return s == null || s.toString().trim().length() == 0;
    }
    private void setupOnClickDatePickerListener(final TextView view) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment(view);
                newFragment.show(getSupportFragmentManager(), "Pick a Date");
            }
        });
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private TextView v;

        public DatePickerFragment(TextView v) {
            this.v = v;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            v.setText((monthOfYear + 1) + "/" + dayOfMonth + "/" + year);
        }
    }
}
