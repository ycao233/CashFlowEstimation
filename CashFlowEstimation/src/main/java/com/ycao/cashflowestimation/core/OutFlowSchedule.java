package com.ycao.cashflowestimation.core;

import java.util.List;

/**
 * Created by ycao on 7/27/13.
 */
public class OutFlowSchedule {

    private RecurrentPayment bonus;
    private RecurrentPayment salary;
    private RecurrentPayment rent;
    private List<RecurrentPayment> others;

    /**
     * payment schedule
     */
    public enum SCHEDULE {
        WEEKLY, // pays out on Sunday
        MONTHLY, // pays out on first day of month
        YEARLY  // pays out jan 1
    }

    public static class RecurrentPayment {
        private SCHEDULE schedule;
        private double amount;
        private String description;

        public RecurrentPayment(SCHEDULE schedule, double amount) {
            this.schedule = schedule;
            this.amount = amount;
        }

        public SCHEDULE getSchedule() {
            return schedule;
        }

        public double getAmount() {
            return amount;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }
}
