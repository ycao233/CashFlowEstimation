package com.ycao.cashflowestimation.domain;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.ycao.cashflowestimation.dal.SQLiteConnector;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by ycao on 9/21/13.
 */
public class Vendor extends Entity {

    private final static String CLASS_NAME = Vendor.class.getName();
    private final static Vendor accessor = new Vendor();

    private String name;
    private String phone;
    private double credit;

    public static Vendor getAccessor() {
        return accessor;
    }

    @Override
    protected String getTableName() {
        return SQLiteConnector.VENDOR_TABLE;
    }

    @Override
    protected String[] getColumns() {
        return SQLiteConnector.VENDOR_COLUMNS.toArray(new String[SQLiteConnector.VENDOR_COLUMNS.size()]);
    }

    @Override
    protected String getLogName() {
        return CLASS_NAME;
    }

    @Override
    protected Vendor convertFromDBCursor(SQLiteDatabase db, Cursor cursor) {
        long id = cursor.getLong(0);
        String name = cursor.getString(1);
        double credit = cursor.getDouble(2);
        String phone = cursor.getString(3);
        Vendor v = new Vendor();
        v.setId(id);
        v.setName(name);
        v.setCredit(credit);
        v.setPhone(phone);

        Log.d(getLogName(), "got: " + v);
        return v;
    }

    @Override
    protected ContentValues getContentValues(SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        values.put(SQLiteConnector.VENDOR_COL_NAME, this.getName());
        values.put(SQLiteConnector.VENDOR_COL_CREDIT, this.getCredit());
        values.put(SQLiteConnector.VENDOR_COL_PHONE, this.getPhone());

        return values;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public List<String> getAllVendorNames(SQLiteConnector dbConn) {
        List<String> allNames = new LinkedList<String>();
        List<Vendor> all = getAll(dbConn);
        for (Vendor v : all) {
            allNames.add(v.getName());
        }

        return allNames;
    }

    public Vendor getByName(SQLiteConnector dbConn, String name) {
        String selection = SQLiteConnector.VENDOR_COL_NAME + " = ?";
        String[] range = new String[]{name};
        List<Vendor> vendors =  getBySelection(dbConn, selection, range, null);
        if (vendors.size() > 0) {
            return vendors.get(0);
        }

        return null;
    }

    public String toString() {
        return String.format("%s (%s)", getName(), getPhone());
    }
}
