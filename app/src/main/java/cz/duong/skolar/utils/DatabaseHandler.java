package cz.duong.skolar.utils;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * Created by David on 10. 4. 2014.
 */
public class DatabaseHandler extends SQLiteAssetHelper {
    private static final String DB_NAME = "data.s3db";
    private static final int DB_VERSION = 1;

    private static DatabaseHandler instance;

    public static synchronized DatabaseHandler getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHandler(context);
        }

        return instance;
    }

    public DatabaseHandler(Context ctx) {
        super(ctx, DB_NAME, null, DB_VERSION);
    }
}
