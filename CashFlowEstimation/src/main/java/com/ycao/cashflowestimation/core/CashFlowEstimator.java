package com.ycao.cashflowestimation.core;

import com.ycao.cashflowestimation.domain.CashFlowDate;

import org.joda.time.DateMidnight;

/**
 * Created by ycao on 7/27/13.
 */
public class CashFlowEstimator {

    public CashFlowDate getNextCashFlowDate(CashFlowDate date) {
        CashFlowDate nextDay = new CashFlowDate(date.getDate().plusDays(1));
        return nextDay;
    }

}
