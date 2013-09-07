package com.ycao.cashflowestimation.domain;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ycao.cashflowestimation.dal.SQLiteConnector;

import org.joda.time.DateMidnight;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an invoice
 *
 * Created by ycao on 7/27/13.
 */
public class Invoice {

    private final String CLASS_NAME = Invoice.class.getName();
    private long _id;

    private String invoiceNumber;
    private double credit;
    private String vendor;
    private DateMidnight date;
    private List<PaymentInstallment> payments;

    public Invoice(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
        payments = new ArrayList<PaymentInstallment>();
    }

    /**
     * unique identifier of this invoice
     */
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    /**
     * credit with the vendor
     */
    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    /**
     *  vendor name
     */
    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    /**
     * paid?
     */
    public boolean isPaidInFull() {
        for (PaymentInstallment p : getPayments()) {
            if (!p.isInstallmentPaid()) {
                return false;
            }
        }

        return true;
    }

    /**
     * due dates
     */
    public List<PaymentInstallment> getPayments() {
        return payments;
    }

    /**
     * money due on a certain day
     */
    public List<PaymentInstallment> getPaymentDueOn(DateMidnight date) {
        List<PaymentInstallment> due = new ArrayList<PaymentInstallment>();
        for (PaymentInstallment p : getPayments()) {
            if (p.getDate().equals(date)) {
                due.add(p);
            }
        }

        return due;
    }

    public void addPayment(PaymentInstallment p) {
        getPayments().add(p);
    }

    public long persist(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(SQLiteConnector.INVOICE_COL_CREDIT, 0);
        values.put(SQLiteConnector.INVOICE_COL_NUMBER, this.getInvoiceNumber());
        values.put(SQLiteConnector.INVOICE_COL_VENDOR, this.getVendor());
        values.put(SQLiteConnector.INVOICE_COL_DATE, this.getVendor());

        long id = -1;
        if (this.getId() == -1) {
            id = db.insert(SQLiteConnector.INVOICE_TABLE, null, values);
            this.setId(id);
        } else {
            db.update(SQLiteConnector.INVOICE_TABLE, values, "_id=" + this.getId(), null);
        }
        Log.d(CLASS_NAME, "persisted " + this.toString());

        for (PaymentInstallment payment : getPayments()) {

        }
        return id;
    }

    public long getId() {
        return _id;
    }

    public void setId(long id) {
        this._id = id;
    }

    public String toString() {
        return String.format("invoice number: %s, vendor: %s", this.getInvoiceNumber(),
                this.getVendor());
    }
}
