package com.ycao.cashflowestimation.dal;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.ycao.cashflowestimation.domain.RecurrentCashFlow;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ycao on 7/29/13.
 */
@Singleton
public class SQLiteConnector extends SQLiteOpenHelper {

    private static final String CLASS_NAME = SQLiteConnector.class.getName();

    public static final String DB_NAME = "cash_flow_sqlite";
    public static final int DB_VERSION = 1;

    public static final String ID = "_id";

    /* invoices */
    public static final String INVOICE_TABLE = "INVOICE";
    public static final String INVOICE_COL_NUMBER = "INVOICE_NUM";
    public static final String INVOICE_COL_VENDOR = "VENDOR";
    public static final String INVOICE_COL_CREDIT = "CREDIT";
    public static final String INVOICE_COL_DATE = "DATE";
    public static final List<String> INVOICE_COLUMNS = Arrays.asList(ID, INVOICE_COL_NUMBER, INVOICE_COL_VENDOR, INVOICE_COL_DATE, INVOICE_COL_CREDIT);

    private static final String CREATE_INVOICE = String.format("create table %s " +
            "(%s integer primary key autoincrement, " +
            "%s text not null UNIQUE, " +
            "%s text not null, " +
            "%s integer not null, " +
            "%s real);",
            INVOICE_TABLE,
            ID,
            INVOICE_COL_NUMBER,
            INVOICE_COL_VENDOR,
            INVOICE_COL_DATE,
            INVOICE_COL_CREDIT);
    private static final String CREATE_INVOICE_TABLE_INDEX = String.format("CREATE INDEX invoice_date_idx on %s(%s);",
            INVOICE_TABLE,
            INVOICE_COL_DATE);

    /* payment installment */
    public static final String PAYMENT_INSTALLMENT_TABLE = "PAYMENT_INSTALLMENT";
    public static final String PI_COL_DATE = "DATE";
    public static final String PI_COL_AMOUNT_DUE = "AMOUNT_DUE";
    public static final String PI_COL_PAID = "PAID";
    public static final String PI_COL_INVOICE_ID = "INVOICE_ID";
    public static final List<String> PAYMENT_INSTALLMENT_COLUMNS = Arrays.asList(ID, PI_COL_INVOICE_ID, PI_COL_DATE, PI_COL_AMOUNT_DUE, PI_COL_PAID);

    private static final String CREATE_PAYMENT_INSTALLMENT = String.format("create table %s " +
            "(%s integer primary key autoincrement, " +
            "%s integer not null, " +
            "%s integer not null, " +
            "%s real not null, " +
            "%s integer, " +
            "FOREIGN KEY (%s) REFERENCES %s(%s));",
            PAYMENT_INSTALLMENT_TABLE,
            ID,
            PI_COL_INVOICE_ID,
            PI_COL_DATE,
            PI_COL_AMOUNT_DUE,
            PI_COL_PAID,
            PI_COL_INVOICE_ID, INVOICE_TABLE, ID);
    private static final String CREATE_PI_TABLE_INDEX = String.format("CREATE INDEX fk_inv_id_idx on %s(%s);",
            PAYMENT_INSTALLMENT_TABLE,
            PI_COL_INVOICE_ID);

    /* recurring cashflow */
    public static final String RECURRENT_CASH_FLOW_TABLE = "RECUR_FLOW_TABLE";
    public static final String RCFT_COL_DESCRIPTION = "DESCRIPTION";
    public static final String RCFT_COL_SCHEDULE = "SCHEDULE";
    public static final String RCFT_COL_AMOUNT = "AMOUNT";
    public static final List<String> RCFT_COLUMNS = Arrays.asList(ID, RCFT_COL_DESCRIPTION, RCFT_COL_SCHEDULE, RCFT_COL_AMOUNT);

    private static final String CREATE_RECURRENT_CASH_FLOW = String.format("create table %s " +
            "(%s integer primary key autoincrement," +
            "%s text not null UNIQUE," +
            "%s text not null," +
            "%s real not null);",
            RECURRENT_CASH_FLOW_TABLE,
            ID,
            RCFT_COL_DESCRIPTION,
            RCFT_COL_SCHEDULE,
            RCFT_COL_AMOUNT);

    @Inject
    public SQLiteConnector(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(CLASS_NAME, "Creating sqlite databases.");
        db.execSQL(CREATE_INVOICE);
        Log.i(CLASS_NAME, "Created invoice table with sql cmd: " + CREATE_INVOICE);
        db.execSQL(CREATE_INVOICE_TABLE_INDEX);
        Log.i(CLASS_NAME, "Created invoice table index with sql cmd: " + CREATE_INVOICE_TABLE_INDEX);

        db.execSQL(CREATE_PAYMENT_INSTALLMENT);
        Log.i(CLASS_NAME, "Created payment intallment table with sql cmd: " + CREATE_PAYMENT_INSTALLMENT);
        db.execSQL(CREATE_PI_TABLE_INDEX);
        Log.i(CLASS_NAME, "Created payment intallment table index sql cmd: " + CREATE_PI_TABLE_INDEX);


        db.execSQL(CREATE_RECURRENT_CASH_FLOW);
        Log.i(CLASS_NAME, "Created recurrent cash flow table with sql cmd: " + CREATE_RECURRENT_CASH_FLOW);
    }

    public void open() {
        this.getWritableDatabase();
        Log.i(CLASS_NAME, "opened database: "+this.getDatabaseName());
    }

    public void bootstrapData() {
        RecurrentCashFlow salary = new RecurrentCashFlow(RecurrentCashFlow.Schedule.WEEKLY, 300 * 7, "salary");
        salary.persist(this.getWritableDatabase());
        RecurrentCashFlow rent = new RecurrentCashFlow(RecurrentCashFlow.Schedule.MONTHLY, 250 * 30, "rent");
        rent.persist(this.getWritableDatabase());
        RecurrentCashFlow bonus = new RecurrentCashFlow(RecurrentCashFlow.Schedule.WEEKLY, 300 * 7, "bonus");
        bonus.persist(this.getWritableDatabase());
        RecurrentCashFlow tax = new RecurrentCashFlow(RecurrentCashFlow.Schedule.YEARLY, 400 * 365, "tax");
        tax.persist(this.getWritableDatabase());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
    }

}
