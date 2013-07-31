package com.ycao.cashflowestimation.domain;

import org.joda.time.DateMidnight;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an invoice
 *
 * Created by ycao on 7/27/13.
 */
public class Invoice {

    private long _id;

    private String invoiceNumber;

    private double credit;

    private String vendor;

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
}
