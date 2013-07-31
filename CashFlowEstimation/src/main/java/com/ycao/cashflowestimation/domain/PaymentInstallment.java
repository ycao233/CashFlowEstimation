package com.ycao.cashflowestimation.domain;

import org.joda.time.DateMidnight;

/**
 * Created by ycao on 7/29/13.
 */
public class PaymentInstallment {
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
}
