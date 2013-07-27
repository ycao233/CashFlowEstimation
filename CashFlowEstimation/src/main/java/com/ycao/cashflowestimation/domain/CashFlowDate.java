package com.ycao.cashflowestimation.domain;

import org.joda.time.DateMidnight;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ycao on 7/27/13.
 */
public class CashFlowDate {

    private DateMidnight date;

    private double calculatedCash;

    private double manualAdjustment;

    private List<Invoice> invoices;

    public CashFlowDate(DateMidnight date) {
        this.date = date;
        invoices = new ArrayList<Invoice>();
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

}
