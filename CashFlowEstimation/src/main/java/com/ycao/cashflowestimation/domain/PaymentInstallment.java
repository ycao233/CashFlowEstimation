package com.ycao.cashflowestimation.domain;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ycao.cashflowestimation.dal.SQLiteConnector;

import org.joda.time.DateMidnight;

/**
 * Created by ycao on 7/29/13.
 */
public class PaymentInstallment {

    private final String CLASS_NAME = PaymentInstallment.class.getName();

    private long _id;
    private DateMidnight date;
    private double amountDue;
    private boolean isPaid;
    private long invoiceId;

    public PaymentInstallment(DateMidnight date, double amountDue) {
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
}
