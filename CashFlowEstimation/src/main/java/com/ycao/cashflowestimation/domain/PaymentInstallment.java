package com.ycao.cashflowestimation.domain;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ycao.cashflowestimation.dal.SQLiteConnector;

import org.joda.time.DateMidnight;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ycao on 7/29/13.
 */
public class PaymentInstallment extends Entity {

    private final static String CLASS_NAME = PaymentInstallment.class.getName();
    private final static PaymentInstallment accessor = new PaymentInstallment();

    private DateMidnight dueDate;
    private double amountDue;
    private boolean isPaid;
    private long invoiceId;

    public static PaymentInstallment getAccessor() {
        return accessor;
    }

    public boolean isInstallmentPaid() {
        return isPaid();
    }

    public void setInstallmentPaid(boolean isPaid) {
        this.setPaid(isPaid);
    }

    public DateMidnight getDueDate() {
        return dueDate;
    }

    public double getAmountDue() {
        return amountDue;
    }

    public void setDueDate(DateMidnight dueDate) {
        this.dueDate = dueDate;
    }

    public void setAmountDue(double amountDue) {
        this.amountDue = amountDue;
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

    @Override
    protected ContentValues getContentValues(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(SQLiteConnector.PI_COL_INVOICE_ID, this.getInvoiceId());
        values.put(SQLiteConnector.PI_COL_AMOUNT_DUE, this.getAmountDue());
        values.put(SQLiteConnector.PI_COL_DATE, this.getDueDate().getMillis());
        return values;
    }

    @Override
    protected String getTableName() {
        return SQLiteConnector.PAYMENT_INSTALLMENT_TABLE;
    }

    @Override
    protected String[] getColumns() {
        return SQLiteConnector.PAYMENT_INSTALLMENT_COLUMNS.toArray(
                new String[SQLiteConnector.PAYMENT_INSTALLMENT_COLUMNS.size()]);
    }

    @Override
    protected String getLogName() {
        return this.CLASS_NAME;
    }

    public List<PaymentInstallment> getAllPaymentsFor(SQLiteDatabase db, long invoiceId) {
        List<PaymentInstallment> all = new ArrayList<PaymentInstallment>();
        String selection = String.format("%s = ?", SQLiteConnector.PI_COL_INVOICE_ID);
        String[] range = new String[]{String.valueOf(invoiceId)};
        Cursor cursor = db.query(SQLiteConnector.PAYMENT_INSTALLMENT_TABLE,
                            SQLiteConnector.PAYMENT_INSTALLMENT_COLUMNS.toArray(new String[SQLiteConnector.PAYMENT_INSTALLMENT_COLUMNS.size()]),
                            selection, range, null, null, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            PaymentInstallment p = convertFromDBCursor(db, cursor);
            if (p != null) {
                all.add(p);
            }
            cursor.moveToNext();
        }

        return all;
    }

    @Override
    protected PaymentInstallment convertFromDBCursor(SQLiteDatabase db, Cursor cursor) {
        long id = cursor.getLong(0);
        Long invId = cursor.getLong(1);
        DateMidnight dueDate = new DateMidnight(cursor.getLong(2));
        Double amountDue = cursor.getDouble(3);
        PaymentInstallment p = new PaymentInstallment();
        p.setDueDate(dueDate);
        p.setAmountDue(amountDue);
        p.setId(id);
        p.setInvoiceId(invId);

        return p;
    }

    public String toString() {
        return "Due Date: "+ getDueDate().toString()+" amount: "+getAmountDue();
    }

}
