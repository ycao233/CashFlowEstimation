package com.ycao.cashflowestimation.ui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.ycao.cashflowestimation.R;
import com.ycao.cashflowestimation.dal.SQLiteConnector;
import com.ycao.cashflowestimation.domain.Invoice;
import com.ycao.cashflowestimation.domain.PaymentInstallment;
import com.ycao.cashflowestimation.domain.Vendor;

import org.joda.time.DateMidnight;
import org.joda.time.format.DateTimeFormat;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.zip.Inflater;

import roboguice.activity.RoboFragmentActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;


/**
 * Created by ycao on 9/3/13.
 */
@ContentView(R.layout.activity_invoice)
public class InvoiceActivity extends RoboFragmentActivity {

    private static final String CLASS_NAME = InvoiceActivity.class.getName();

    public static int PICTURE_REQUEST = 100;
    public static final String EMPTY = "";

    @Inject
    private SQLiteConnector sqlConn;

    private Button invDatePicker;
    private Button dueDatePicker;

    @InjectView(R.id.inv_number_edittext)
    private EditText invNumberInput;

    @InjectView(R.id.due_amount_edittext)
    private EditText dueAmountInput;

    @InjectView(R.id.note_edittext)
    private EditText notesInput;

    private Button saveButton;
    private Button cancelButton;
    private ImageView image;
    private TextView pictureLabel;
    private Button addVendorButton;

    private Spinner vendorSpinner;
    private ArrayAdapter<String> allVendors;

    private Invoice currInvoice;

    private Vibrator vibrator;

    private View.OnClickListener nullOp = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //do nothing
        }
    };

    private View.OnClickListener startCamera = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            image.setOnClickListener(nullOp);
            pictureLabel.setOnClickListener(nullOp);
            //cue for the customer so they know they pressed button
            vibrator.vibrate(50);
            startCameraForInvoicePicture();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        getActionBar().hide();

        invDatePicker = (Button) findViewById(R.id.choose_inv_date_edittext);
        setupOnClickDatePickerListener(invDatePicker);

        dueDatePicker = (Button) findViewById(R.id.due_date_edittext);
        setupOnClickDatePickerListener(dueDatePicker);

        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        saveButton = (Button) findViewById(R.id.save_invoice_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validInputs()) {
                    Invoice invoice = getInvoice();
                    long id = invoice.persist(sqlConn);
                    if (id == -1) {
                        Toast.makeText(InvoiceActivity.this, getString(R.string.invoice_empty_input), Toast.LENGTH_SHORT).show();
                    } else {
                        Intent result = new Intent();
                        result.putExtra("_id", id);
                        setResult(RESULT_OK, result);
                        finish();
                    }
                } else {
                    Toast.makeText(InvoiceActivity.this, getString(R.string.invoice_empty_input), Toast.LENGTH_SHORT).show();
                }
            }
        });

        image = (ImageView) findViewById(R.id.icon_imageview);
        pictureLabel = (TextView) findViewById(R.id.picture_label);

        //image.setOnClickListener(startCamera);
        pictureLabel.setOnClickListener(startCamera);

        vendorSpinner = (Spinner) findViewById(R.id.vendor_spinner);
        List<String> allVendorsList = Vendor.getAccessor().getAllVendorNames(sqlConn);
        allVendors = new ArrayAdapter(this, R.layout.spinner_item, allVendorsList);
        vendorSpinner.setAdapter(allVendors);

        addVendorButton = (Button) findViewById(R.id.add_vendor_button);
        addVendorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddVendorDialog();
            }
        });

        long id = getIntent().getLongExtra(SQLiteConnector.ID, -1);
        if (id != -1) {
            currInvoice = Invoice.getAccessor().getById(sqlConn, id);
            int pos = allVendorsList.indexOf(currInvoice.getVendor().getName());
            vendorSpinner.setSelection(pos > 0 ? pos : 0);
            invNumberInput.setText(currInvoice.getInvoiceNumber());
            notesInput.setText(currInvoice.getNotes());
            invDatePicker.setText(currInvoice.getDate().toString("MM/dd/yyyy"));
            PaymentInstallment p = currInvoice.getPayments().get(0);
            dueDatePicker.setText(p.getDueDate().toString("MM/dd/yyyy"));
            dueAmountInput.setText(String.valueOf(p.getAmountDue()));
        }
    }

    private void showAddVendorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View vendorAddDialog = inflater.inflate(R.layout.dialog_add_vendor, null);
        builder
            .setView(vendorAddDialog)
            .setPositiveButton(R.string.save, null)
            .setNegativeButton(R.string.cancel, null);
        final AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText name = (EditText) vendorAddDialog.findViewById(R.id.vendor_name_edittext);
                EditText phone = (EditText) vendorAddDialog.findViewById(R.id.phone_number_edittext);
                EditText credit = (EditText) vendorAddDialog.findViewById(R.id.credit_edittext);
                if (empty(name.getText())) {
                    Toast.makeText(InvoiceActivity.this, getString(R.string.invoice_empty_input), Toast.LENGTH_SHORT).show();
                } else {
                    Vendor v = new Vendor();
                    v.setName(name.getText().toString());
                    v.setCredit(empty(credit.getText()) ? 0 : Double.parseDouble(credit.getText().toString()));
                    v.setPhone(empty(phone.getText()) ? "" : phone.getText().toString());
                    long id = v.persist(sqlConn);
                    if (id == -1) {
                        Toast.makeText(InvoiceActivity.this, "Failed to create new vendor", Toast.LENGTH_SHORT).show();
                    } else {
                        allVendors.add(v.getName());
                        allVendors.notifyDataSetChanged();
                        int pos = allVendors.getPosition(v.getName());
                        vendorSpinner.setSelection(pos);
                    }
                    dialog.dismiss();
                }
            }
        });
    }

    private void startCameraForInvoicePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, PICTURE_REQUEST);
    }

    private File getImageFile() {
        // TODO: picture management
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (PICTURE_REQUEST == requestCode) {
            if (resultCode == RESULT_OK && data != null) {
                Bundle extras = data.getExtras();
                Bitmap icon = (Bitmap) extras.get("data");
                image.setImageBitmap(icon);
                pictureLabel.setVisibility(View.INVISIBLE);
            } else {
                image.setOnClickListener(startCamera);
                pictureLabel.setOnClickListener(startCamera);
            }
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
        invoice.setVendor(Vendor.getAccessor().getByName(sqlConn, vendorSpinner.getSelectedItem().toString()));
        invoice.setDate(getDate(invDatePicker));
        invoice.setNotes(notesInput.getText().toString());
        List<PaymentInstallment> payments = invoice.getPayments();
        PaymentInstallment p = payments.size() > 0 ? payments.get(0) : new PaymentInstallment();
        p.setDueDate(getDate(dueDatePicker));
        p.setAmountDue(Double.parseDouble(dueAmountInput.getText().toString()));
        if (payments.size() == 0) {
            invoice.addPayment(p);
        }
        return invoice;
    }

    private DateMidnight getDate(Button dueDatePicker) {
        String date = dueDatePicker.getText().toString();
        return DateMidnight.parse(date, DateTimeFormat.forPattern("MM/dd/yyyy"));
    }

    private boolean validInputs() {
        return !( empty(vendorSpinner.getSelectedItem().toString())
                || empty(invNumberInput.getText())
                || empty(dueAmountInput.getText()));
    }

    private boolean empty(Editable s) {
        return s == null || s.toString().trim().length() == 0;
    }

    private boolean empty(String s) {
        return s == null || s.trim().length() == 0;
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
