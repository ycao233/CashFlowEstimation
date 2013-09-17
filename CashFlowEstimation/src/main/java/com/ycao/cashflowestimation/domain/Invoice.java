package com.ycao.cashflowestimation.domain;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ycao.cashflowestimation.dal.SQLiteConnector;

import org.joda.time.DateMidnight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Represents an invoice
 *
 * Created by ycao on 7/27/13.
 */
public class Invoice extends Entity {

    private final static String CLASS_NAME = Invoice.class.getName();
    private final static Invoice accessor = new Invoice();

    private String invoiceNumber;
    private double credit;
    private String vendor;
    private DateMidnight date;
    private String notes;
    private List<PaymentInstallment> payments = new ArrayList<PaymentInstallment>();

    public static Invoice getAccessor() {
        return accessor;
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

    public DateMidnight getDate() {
        return date;
    }

    public void setDate(DateMidnight date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
            if (p.getDueDate().equals(date)) {
                due.add(p);
            }
        }

        return due;
    }

    public double getTotalDue() {
        double total = 0;
        for (PaymentInstallment p : getPayments()) {
            total += p.getAmountDue();
        }

        return total;
    }

    public void addPayment(PaymentInstallment p) {
        getPayments().add(p);
    }


    /**
     * beging / end inclusive
     *
     *
     * @param dbConn
     * @param begin
     * @param end
     * @return a list of invoices
     */
    public List<Invoice> getAllInvoiceInRange(SQLiteConnector dbConn, DateMidnight begin, DateMidnight end) {

        String selection = (begin == null && end == null) ? null :  String.format("%s >= ? AND %s <= ?", SQLiteConnector.INVOICE_COL_DATE, SQLiteConnector.INVOICE_COL_DATE);
        String[] range = (begin == null && end == null) ? null : new String[]{String.valueOf(begin.getMillis()), String.valueOf(end.getMillis())};
        List<Invoice> rangedInvoice =  getBySelection(dbConn, selection, range, SQLiteConnector.INVOICE_COL_DATE);
        sortByDueDay(rangedInvoice);

        return rangedInvoice;
    }

    protected Invoice convertFromDBObject(SQLiteDatabase db, Cursor cursor) {
        long id = cursor.getLong(0);
        String invNum = cursor.getString(1);
        String vendor = cursor.getString(2);
        DateMidnight invDate = new DateMidnight(cursor.getLong(3));
        Double credit = cursor.getDouble(4);
        String notes = cursor.getString(5);
        Invoice i = new Invoice();
        i.setInvoiceNumber(invNum);
        i.setId(id);
        i.setVendor(vendor);
        i.setDate(invDate);
        i.setCredit(credit);
        i.setNotes(notes);

        i.getPayments().addAll(PaymentInstallment.getAccessor().getAllPaymentsFor(db, i.getId()));
        Log.d(getLogName(), "got: "+i);
        return i;
    }

    public void sortByDueDay(List<Invoice> invoices) {
        Collections.sort(invoices, new Comparator<Invoice>() {
            @Override
            public int compare(Invoice a, Invoice b) {
                return (a.getPayments().get(0).getDueDate().compareTo(b.getPayments().get(0).getDueDate()));
            }
        });
    }



    @Override
    protected ContentValues getContentValues(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(SQLiteConnector.INVOICE_COL_CREDIT, 0);
        values.put(SQLiteConnector.INVOICE_COL_NUMBER, this.getInvoiceNumber());
        values.put(SQLiteConnector.INVOICE_COL_VENDOR, this.getVendor());
        values.put(SQLiteConnector.INVOICE_COL_DATE, this.getDate().getMillis());
        values.put(SQLiteConnector.INVOICE_COL_NOTE, this.getNotes());

        return values;
    }

    @Override
    protected void foreignObjectPersist(SQLiteConnector dbConn, long id) {
        if (id == -1) {
            return;
        }

        for (PaymentInstallment payment : this.getPayments()) {
            payment.setInvoiceId(id);
            payment.persist(dbConn);
        }
    }

    @Override
    protected String getTableName() {
        return SQLiteConnector.INVOICE_TABLE;
    }

    @Override
    protected String[] getColumns() {
        return SQLiteConnector.INVOICE_COLUMNS.toArray(new String[SQLiteConnector.INVOICE_COLUMNS.size()]);
    }

    @Override
    protected String getLogName() {
        return CLASS_NAME;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String toString() {
        return String.format("%d: invoice number: %s, vendor: %s, total due: %f", this.getId(),
                this.getInvoiceNumber(), this.getVendor(), this.getTotalDue());
    }
}
