package com.ycao.cashflowestimation.dal;

import android.util.Log;

import com.couchbase.cblite.CBLServer;
import com.google.inject.Singleton;

import java.io.IOException;

/**
 * Created by ycao on 7/27/13.
 */
@Singleton
public class CouchBaseConnector {

    public static final String DB_NAME = "cash_flow_cblite";

    // startup a couchbase lite instance
    public void startCouchbase(String filesDir) {
        try {
            CBLServer server = new CBLServer(filesDir);
            server.getDatabaseNamed(DB_NAME);
        } catch (IOException e) {
            Log.e("MainActivity", "Error starting TDServer", e);
        }
        Log.d("MainActivity", "couchbase lite started");
    }


}
