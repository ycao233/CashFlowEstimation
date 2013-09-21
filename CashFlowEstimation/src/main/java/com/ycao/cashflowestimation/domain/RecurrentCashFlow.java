package com.ycao.cashflowestimation.domain;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ycao.cashflowestimation.dal.SQLiteConnector;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ycao on 7/27/13.
 */
public class RecurrentCashFlow extends Entity {

    private static final String CLASS_NAME = RecurrentCashFlow.class.getName();
    private static final RecurrentCashFlow accessor = new RecurrentCashFlow();

    public static RecurrentCashFlow getAccessor() {
        return accessor;
    }

    /**
     * payment schedule
     */
    public enum Schedule {
        WEEKLY, // pays out on Sunday
        MONTHLY, // pays out on first day of month
        YEARLY; // pays out jan 1

        private static final List<String> AVAILABLE_SCHEDULES = Arrays.asList(WEEKLY.name(), MONTHLY.name(), YEARLY.name());
        public static List<String> getAvailableSchedules() {
            return AVAILABLE_SCHEDULES;
        }

        public String capitalize() {
            return this.name().charAt(0) + this.name().toLowerCase().substring(1);
        }
    }

    private Schedule schedule;
    private double amount;
    private String description;

    public Schedule getSchedule() {
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

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    @Override
    protected String getTableName() {
        return SQLiteConnector.RECURRENT_CASH_FLOW_TABLE;
    }

    @Override
    protected String[] getColumns() {
        return SQLiteConnector.RCFT_COLUMNS.toArray(new String[SQLiteConnector.RCFT_COLUMNS.size()]);
    }

    @Override
    protected String getLogName() {
        return this.CLASS_NAME;
    }

    @Override
    protected ContentValues getContentValues(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(SQLiteConnector.RCFT_COL_SCHEDULE, this.getSchedule().toString());
        values.put(SQLiteConnector.RCFT_COL_AMOUNT, this.getAmount());
        values.put(SQLiteConnector.RCFT_COL_DESCRIPTION, this.getDescription());
        return values;
    }

    public List<RecurrentCashFlow> getAllRecurrentCashFlow(SQLiteConnector dbConn) {
        List<RecurrentCashFlow> all = getBySelection(dbConn, null, null, null);
        return all;
    }

    @Override
    protected RecurrentCashFlow convertFromDBCursor(SQLiteDatabase db, Cursor cursor) {
        long id = cursor.getLong(0);
        String description = cursor.getString(1);
        Schedule schedule = Schedule.valueOf(cursor.getString(2));
        Double amount = cursor.getDouble(3);
        RecurrentCashFlow flow = new RecurrentCashFlow();
        flow.setSchedule(schedule);
        flow.setAmount(amount);
        flow.setDescription(description);
        flow.setId(id);

        return flow;
    }

    public String toString() {
        return String.format("id: {%d} description: {%s} schedule: {%s} amount: {%f}",
                this.getId(), this.getDescription(), this.getSchedule(), this.getAmount());
    }
}
