package com.ycao.cashflowestimation.domain;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ycao.cashflowestimation.dal.SQLiteConnector;

import org.joda.time.DateMidnight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ycao on 7/29/13.
 */
public class PaymentInstallment {

    private final static String CLASS_NAME = PaymentInstallment.class.getName();

    private long _id;
    private DateMidnight date;
    private double amountDue;
    private boolean isPaid;
    private long invoiceId;

    public PaymentInstallment(DateMidnight date, double amountDue) {
        this._id = -1;
        this.date = date;
        this.amountDue = amountDue;
    }

    public boolean isInstallmentPaid() {
        return isPaid();
    }

    public void setInstallmentPaid(boolean isPaid) {
        this.setPaid(isPaid);
    }

    public DateMidnight getDate() {
        return date;
    }

    public double getAmountDue() {
        return amountDue;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public void setPaid(boolean paid) {
        isPaid = paid;
    }

    public long getInvoiceId() {
        return this.invoiceId;
    }

    public void setInvoiceId(long invoiceId) {
        this.invoiceId = invoiceId;
    }

    public void setId(long _id) {
        this._id = _id;
    }

    public long getId() {
        return _id;
    }

    public long persist(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(SQLiteConnector.PI_COL_INVOICE_ID, this.getInvoiceId());
        values.put(SQLiteConnector.PI_COL_AMOUNT_DUE, this.getAmountDue());
        values.put(SQLiteConnector.PI_COL_DATE, this.getDate().getMillis());

        long id = -1;
        if (this.getId() == -1) {
            id = db.insert(SQLiteConnector.PAYMENT_INSTALLMENT_TABLE, null, values);
            this.setId(id);
        } else {
            db.update(SQLiteConnector.PAYMENT_INSTALLMENT_TABLE, values, "_id=" + this.getId(), null);
        }
        Log.d(CLASS_NAME, "persisted " + this.toString());
        return id;
    }

    public String toString() {
        return "Due Date: "+getDate().toString()+" amount: "+getAmountDue();
    }

    //TODO: There is a bug -- vendor id + inv id should be unique
    public static List<PaymentInstallment> getAllPaymentsFor(SQLiteDatabase db, long invoiceId) {
        List<PaymentInstallment> all = new ArrayList<PaymentInstallment>();
        String selection = String.format("%s = ?", SQLiteConnector.PI_COL_INVOICE_ID);
        String[] range = new String[]{String.valueOf(invoiceId)};
        Cursor cursor = db.query(SQLiteConnector.PAYMENT_INSTALLMENT_TABLE,
                            SQLiteConnector.PAYMENT_INSTALLMENT_COLUMNS.toArray(new String[SQLiteConnector.PAYMENT_INSTALLMENT_COLUMNS.size()]),
                            selection, range, null, null, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            PaymentInstallment p = convertFromDBObject(cursor, invoiceId);
            if (p != null) {
                all.add(p);
            }
            cursor.moveToNext();
        }

        return all;
    }

    private static PaymentInstallment convertFromDBObject(Cursor cursor, long invoiceId) {
        long id = cursor.getLong(0);
        Long invId = cursor.getLong(1);
        if (invId != invoiceId) {
            Log.e(CLASS_NAME, "Invalid data returned: "+invId+" while I am looking for: "+invoiceId);
            return null;
        }
        DateMidnight dueDate = new DateMidnight(cursor.getLong(2));
        Double amountDue = cursor.getDouble(3);
        PaymentInstallment p = new PaymentInstallment(dueDate, amountDue);
        p.setId(id);
        p.setInvoiceId(invoiceId);

        return p;
    }

}
