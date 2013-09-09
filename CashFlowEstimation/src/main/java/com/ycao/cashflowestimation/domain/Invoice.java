package com.ycao.cashflowestimation.domain;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ycao.cashflowestimation.dal.SQLiteConnector;

import org.joda.time.DateMidnight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents an invoice
 *
 * Created by ycao on 7/27/13.
 */
public class Invoice {

    private final static String CLASS_NAME = Invoice.class.getName();
    private long _id = -1;

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
        values.put(SQLiteConnector.INVOICE_COL_DATE, this.getDate().getMillis());

        long id = -1;
        if (this.getId() == -1) {
            id = db.insert(SQLiteConnector.INVOICE_TABLE, null, values);
            this.setId(id);
        } else {
            db.update(SQLiteConnector.INVOICE_TABLE, values, "_id=" + this.getId(), null);
        }
        Log.d(CLASS_NAME, "persisted " + this.toString());

        for (PaymentInstallment payment : getPayments()) {
            payment.setInvoiceId(id);
            payment.persist(db);
        }

        return id;
    }

    /**
     * beging / end inclusive
     *
     * @param begin
     * @param end
     * @return a list of invoices
     */
    public static List<Invoice> getAllInvoiceInRange(SQLiteDatabase db, DateMidnight begin, DateMidnight end) {
        List<Invoice> rangedInvoice = new LinkedList<Invoice>();
        String selection = String.format("%s >= ? AND %s <= ?", SQLiteConnector.INVOICE_COL_DATE, SQLiteConnector.INVOICE_COL_DATE);
        String[] range = new String[]{String.valueOf(begin.getMillis()), String.valueOf(end.getMillis())};
        Cursor cursor = db.query(SQLiteConnector.INVOICE_TABLE,
                            SQLiteConnector.INVOICE_COLUMNS.toArray(new String[SQLiteConnector.INVOICE_COLUMNS.size()]),
                            selection, range, null, null, SQLiteConnector.INVOICE_COL_DATE);

        Log.d(CLASS_NAME, "query by: "+selection+" "+range[0]+" "+range[1] );
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            Invoice i = convertFromDBObject(db, cursor);
            rangedInvoice.add(i);
            cursor.moveToNext();
        }

        return rangedInvoice;
    }

    private static Invoice convertFromDBObject(SQLiteDatabase db, Cursor cursor) {
        long id = cursor.getLong(0);
        String invNum = cursor.getString(1);
        String vendor = cursor.getString(2);
        DateMidnight invDate = new DateMidnight(cursor.getLong(3));
        Double credit = cursor.getDouble(4);
        Invoice i = new Invoice(invNum);
        i.setId(id);
        i.setVendor(vendor);
        i.setDate(invDate);
        i.setCredit(credit);

        i.getPayments().addAll(PaymentInstallment.getAllPaymentsFor(db, i.getId()));
        Log.d(CLASS_NAME, "got: "+i);
        return i;
    }

    public long getId() {
        return _id;
    }

    public void setId(long id) {
        this._id = id;
    }

    public String toString() {
        return String.format("%d: invoice number: %s, vendor: %s", this.getId(),
                this.getInvoiceNumber(), this.getVendor());
    }

    public DateMidnight getDate() {
        return date;
    }

    public void setDate(DateMidnight date) {
        this.date = date;
    }
}
