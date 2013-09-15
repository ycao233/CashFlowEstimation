package com.ycao.cashflowestimation.domain;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ycao.cashflowestimation.dal.SQLiteConnector;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by ycao on 9/14/13.
 */
public abstract class Entity {

    protected long _id = -1;

    public long getId() {
        return _id;
    }

    public void setId(long id) {
        this._id = id;
    }

    public long persist(SQLiteConnector dbConn) {
        SQLiteDatabase db = dbConn.getWritableDatabase();
        ContentValues values = getContentValues(db);

        long id = this.getId();
        if (id == -1) {
            id = db.insert(getTableName(), null, values);
            this.setId(id);
        } else {
            db.update(getTableName(), values, "_id=" + id, null);
        }
        if (id == -1) {
            Log.d(getLogName(), "FAILED in persisting " + this.toString());
        } else {
            Log.d(getLogName(), "persisted " + this.toString());
            foreignObjectPersist(dbConn, this.getId());
        }

        return this.getId();
    }

    protected void foreignObjectPersist(SQLiteConnector dbConn, long id) {
        //no op for most tables
    }

    public <T extends Entity> T getById(SQLiteConnector dbConn, long id) {
        SQLiteDatabase db = dbConn.getReadableDatabase();
        if (id == -1) {
            return null;
        }
        String table = getTableName();
        String selection = String.format("%s = ?", SQLiteConnector.ID);
        String[] range =  new String[]{String.valueOf(id)};
        Cursor cursor = db.query(getTableName(), getColumns(),
                            selection, range, null, null, null);

        Log.d(getLogName(), "query by id: " + id);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            T t = convertFromDBObject(db, cursor);
            return t;
        }

        return null;
    }

    public <T extends Entity> List<T> getBySelection(SQLiteConnector dbConn, String selection, String[] range, String orderBy) {
        SQLiteDatabase db = dbConn.getReadableDatabase();
        List<T> all = new LinkedList<T>();

        Cursor cursor = db.query(getTableName(), getColumns(), selection, range, null, null, orderBy);
        Log.d(getLogName(), "query by: "+selection+" and range: "+(range == null ? null : range[0])+" ordered by: "+orderBy);

        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            T e = convertFromDBObject(db, cursor);
            all.add(e);
            cursor.moveToNext();
        }

        return all;
    }

    protected abstract String getTableName();

    protected abstract String[] getColumns();

    protected abstract String getLogName();

    protected abstract <T extends Entity> T convertFromDBObject(SQLiteDatabase db, Cursor cursor);

    protected abstract ContentValues getContentValues(SQLiteDatabase db);
}
