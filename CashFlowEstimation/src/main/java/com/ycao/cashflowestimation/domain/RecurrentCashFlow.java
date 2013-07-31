package com.ycao.cashflowestimation.domain;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ycao.cashflowestimation.dal.SQLiteConnector;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ycao on 7/27/13.
 */
public class RecurrentCashFlow {

    /**
     * payment schedule
     */
    public enum Schedule {
        WEEKLY, // pays out on Sunday
        MONTHLY, // pays out on first day of month
        YEARLY  // pays out jan 1
    }

    private Schedule schedule;
    private double amount;
    private String description;
    private long _id;

    public RecurrentCashFlow(Schedule schedule, double amount, String description) {
        this.schedule = schedule;
        this.amount = amount;
        this.description = description;
    }

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

    public long getId() {
        return _id;
    }

    public void setId(long _id) {
        this._id = _id;
    }

    public long persist(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(SQLiteConnector.RCFT_COL_SCHEDULE, this.getSchedule().toString());
        values.put(SQLiteConnector.RCFT_COL_AMOUNT, this.getAmount());
        values.put(SQLiteConnector.RCFT_COL_DESCRIPTION, this.getDescription());

        long id = db.insert(SQLiteConnector.RECURRENT_CASH_FLOW_TABLE, null, values);

        return id;
    }

    public static List<RecurrentCashFlow> getAllRecurrentCashFlow(SQLiteDatabase db) {
        List<RecurrentCashFlow> all = new ArrayList<RecurrentCashFlow>();
        Cursor cursor = db.query(SQLiteConnector.RECURRENT_CASH_FLOW_TABLE,
                            SQLiteConnector.RCFT_COLUMNS.toArray(new String[SQLiteConnector.RCFT_COLUMNS.size()]),
                            null, null, null, null, null);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            RecurrentCashFlow cashFlow = convertFromDBObject(cursor);
            all.add(cashFlow);
            cursor.moveToNext();
        }

        return all;
    }

    private static RecurrentCashFlow convertFromDBObject(Cursor cursor) {
        long id = cursor.getLong(0);
        String description = cursor.getString(1);
        Schedule schedule = Schedule.valueOf(cursor.getString(2));
        Double amount = cursor.getDouble(3);
        RecurrentCashFlow flow = new RecurrentCashFlow(schedule, amount, description);
        flow.setId(id);

        return flow;
    }

    public String toString() {
        return String.format("id: {%d} description: {%s} schedule: {%s} amount: {%f}",
                this.getId(), this.getDescription(), this.getSchedule(), this.getAmount());
    }
}
