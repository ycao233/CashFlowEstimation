package com.ycao.cashflowestimation.core;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ycao.cashflowestimation.dal.SQLiteConnector;
import com.ycao.cashflowestimation.domain.Invoice;
import com.ycao.cashflowestimation.domain.RecurrentCashFlow;

import org.joda.time.DateMidnight;
import org.joda.time.DateTimeConstants;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ycao on 7/27/13.
 */
@Singleton
public class CashFlowEstimator {

    @Inject
    private SQLiteConnector sqlConn;

    private float weekdayIncome;
    private float weekendIncome;

    public CashFlowDate getNextCashFlowDate(CashFlowDate date) {
        List<Invoice> invoices = Invoice.getAccessor().getAllInvoiceInRange(sqlConn, date.getDate(), date.getDate());
        return getNextCashFlowDate(date, getRecurrentExpense(), invoices);
    }

    public List<CashFlowDate> getSubsequentDates(CashFlowDate start, int numOfDays, boolean inclusive) {
        List<CashFlowDate> dates = new LinkedList<CashFlowDate>();
        Expense expense = getRecurrentExpense();
        List<Invoice> invoices = Invoice.getAccessor().getAllInvoiceInRange(sqlConn, start.getDate(), start.getDate().plusDays(numOfDays));
        CashFlowDate curr = start;
        while (numOfDays-- > 0) {
            curr = getNextCashFlowDate(curr, expense, invoices);
            dates.add(curr);
        }

        if (inclusive) {
            dates.add(0, start);
        }

        return dates;
    }

    private Expense getRecurrentExpense() {
        List<RecurrentCashFlow> allOutFlow = RecurrentCashFlow.getAccessor().getAllRecurrentCashFlow(sqlConn);
        float weeklyExpense = getExpenses(allOutFlow, RecurrentCashFlow.Schedule.WEEKLY);
        float monthlyExpense = getExpenses(allOutFlow, RecurrentCashFlow.Schedule.MONTHLY);
        float yearlyExpense = getExpenses(allOutFlow, RecurrentCashFlow.Schedule.YEARLY);
        Expense expense = new Expense(weeklyExpense, monthlyExpense, yearlyExpense);

        return expense;
    }

    private CashFlowDate getNextCashFlowDate(CashFlowDate date, Expense expense, List<Invoice> invoices) {
        CashFlowDate nextDay = new CashFlowDate(date.getDate().plusDays(1));
        nextDay.setInvoices(getInvoicesOn(invoices, nextDay.getDate()));

        double cash = date.getCalculatedCash();
        if (nextDay.getDate().getDayOfWeek() < DateTimeConstants.SATURDAY) {
            cash += weekdayIncome;
        } else {
            cash += weekendIncome;
        }

        if (nextDay.getDate().getDayOfWeek() == 1) {
            cash -= expense.getWeeklyExpense();
        }

        if (nextDay.getDate().getDayOfMonth() == 1) {
            cash -= expense.getMonthlyExpense();
        }

        if (nextDay.getDate().getDayOfYear() == 1) {
            cash -= expense.getYearlyExpense();
        }

        cash -= nextDay.getTotalDueOnThisDay();

        nextDay.setCalculatedCash(cash);
        return nextDay;
    }

    private List<Invoice> getInvoicesOn(List<Invoice> invoices, DateMidnight date) {
        List<Invoice> dueOn = new LinkedList<Invoice>();
        for (Invoice i : invoices) {
            if (i.getPaymentDueOn(date).size() > 0) {
                dueOn.add(i);
            }
        }

        return dueOn;
    }

    private float getExpenses(List<RecurrentCashFlow> allOutFlow, RecurrentCashFlow.Schedule freq) {
        float total = 0;
        for (RecurrentCashFlow out : allOutFlow) {
            if (out.getSchedule() == freq) {
                total += out.getAmount();
            }
        }

        return total;
    }

    public CashFlowDate getTodayCashFromPast(CashFlowDate past) {
        DateMidnight today = DateMidnight.now();
        if (past.getDate().isAfter(today)) {
            return null;
        }

        Expense expense = getRecurrentExpense();
        CashFlowDate curr = past;
        List<Invoice> invoices = Invoice.getAccessor().getAllInvoiceInRange(sqlConn, past.getDate(), today);
        while (curr.getDate().isBefore(today)) {
            curr = getNextCashFlowDate(curr, expense, invoices);
        }

        return curr;
    }

    public float getWeekdayIncome() {
        return weekdayIncome;
    }

    public void setWeekdayIncome(float weekdayIncome) {
        this.weekdayIncome = weekdayIncome;
    }

    public float getWeekendIncome() {
        return weekendIncome;
    }

    public void setWeekendIncome(float weekendIncome) {
        this.weekendIncome = weekendIncome;
    }

    static class Expense {
        private float weeklyExpense;
        private float monthlyExpense;
        private float yearlyExpense;

        public Expense(float weeklyExpense, float monthlyExpense, float yearlyExpense) {
            this.weeklyExpense = weeklyExpense;
            this.monthlyExpense = monthlyExpense;
            this.yearlyExpense = yearlyExpense;
        }

        public float getWeeklyExpense() {
            return weeklyExpense;
        }

        public void setWeeklyExpense(float weeklyExpense) {
            this.weeklyExpense = weeklyExpense;
        }

        public float getMonthlyExpense() {
            return monthlyExpense;
        }

        public void setMonthlyExpense(float monthlyExpense) {
            this.monthlyExpense = monthlyExpense;
        }

        public float getYearlyExpense() {
            return yearlyExpense;
        }

        public void setYearlyExpense(float yearlyExpense) {
            this.yearlyExpense = yearlyExpense;
        }
    }
}
