package com.ycao.cashflowestimation.core;

import android.util.Log;

import com.google.inject.Inject;
import com.ycao.cashflowestimation.dal.SQLiteConnector;
import com.ycao.cashflowestimation.domain.Invoice;
import com.ycao.cashflowestimation.domain.PaymentInstallment;

import org.joda.time.DateMidnight;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by ycao on 7/27/13.
 */
public class CashFlowDate {

    private final static String CLASS_NAME = CashFlowDate.class.getName();

    private DateMidnight date;

    private double calculatedCash;

    private double manualAdjustment;

    private List<Invoice> invoices;

    public CashFlowDate(DateMidnight date, SQLiteConnector sqlConn) {
        this.date = date;
        invoices = new LinkedList<Invoice>();

        //TODO: Make this nested select later
        String selection = SQLiteConnector.PI_COL_DATE + " = ?";
        String[] range = new String[]{ String.valueOf(date.getMillis()) };
        List<PaymentInstallment> paymentInstallments =
                PaymentInstallment.getAccessor().getBySelection(sqlConn, selection, range, null);

        Set<Long> invoiceIds = getInvoiceIds(paymentInstallments);

        for (Long id : invoiceIds) {
            Invoice i = Invoice.getAccessor().getById(sqlConn, id);
            invoices.add(i);
        }
    }

    private Set<Long> getInvoiceIds(List<PaymentInstallment> paymentInstallments) {
        Set<Long> ids = new HashSet<Long>();
        for (PaymentInstallment p : paymentInstallments) {
            ids.add(p.getInvoiceId());
        }

        return ids;
    }

    /**
     * on which date this estimation is made
     */
    public DateMidnight getDate() {
        return date;
    }

    /**
     * available cash on this date
     */
    public double getCalculatedCash() {
        return calculatedCash;
    }

    public void setCalculatedCash(double calculatedCash) {
        this.calculatedCash = calculatedCash;
    }

    /**
     * manual adjustment
     *
     * if you know you will have income in the future, or you have other payments
     */
    public double getManualAdjustment() {
        return manualAdjustment;
    }

    public void setManualAdjustment(double manualAdjustment) {
        this.manualAdjustment = manualAdjustment;
    }

    /**
     * invoices adjustment
     */
    public List<Invoice> getInvoices() {
        return invoices;
    }

    public double getTotalDueOnThisDay() {
        double total = 0;
        for(Invoice invoice : getInvoices()) {
            for (PaymentInstallment p : invoice.getPaymentDueOn(getDate())) {
                total += p.getAmountDue();
            }
        }

        return total;
    }

    public String toString() {
        return String.format("date: (%s), estimated cash: %(f), total due: %(f)", getDate().toString("dd/MM/yyyy"), getCalculatedCash(), getTotalDueOnThisDay()) +
                String.format("\ndebug: day of week: (%d), day of month: (%d), day of year: (%d)", getDate().getDayOfWeek(), getDate().getDayOfMonth(), getDate().getDayOfYear());
    }

    public void setInvoices(List<Invoice> invoices) {
        this.invoices = invoices;
    }
}
