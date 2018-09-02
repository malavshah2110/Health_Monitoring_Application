package com.example.assignment2.group30.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.example.assignment2.group30.model.SensorData;

import java.io.File;

/**
 * Created by Dishank on 3/5/2018.
 */

public class SensorDataHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "group30.db";

    public static final int DATABASE_VERSION = 1;

    public SensorDataHelper(Context context) {
        super(context, Environment.getExternalStorageDirectory()
                + File.separator + "Android"
                + File.separator + "data"
                + "/CSE535_ASSIGNMENT2"
                + File.separator
                + DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE = "CREATE TABLE " + SensorDataContract.SensorDataTable.TABLE_NAME + "(" +
                SensorDataContract.SensorDataTable.COLUMN_TIMESTAMP + " TEXT," +
                SensorDataContract.SensorDataTable.COLUMN_XVALUE + " REAL," +
                SensorDataContract.SensorDataTable.COLUMN_YVALUE + " REAL," +
                SensorDataContract.SensorDataTable.COLUMN_ZVALUE + " REAL" + ")";
        sqLiteDatabase.execSQL(CREATE_TABLE);

    }

    public void storeSensorValues(SensorData sensorData){
        System.out.println("Inserting Values................====>");
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(SensorDataContract.SensorDataTable.COLUMN_TIMESTAMP, sensorData.getTimestamp());
        contentValues.put(SensorDataContract.SensorDataTable.COLUMN_XVALUE, sensorData.getxValue());
        contentValues.put(SensorDataContract.SensorDataTable.COLUMN_YVALUE, sensorData.getyValue());
        contentValues.put(SensorDataContract.SensorDataTable.COLUMN_ZVALUE, sensorData.getzValue());
        Log.d("insert tbl",SensorDataContract.SensorDataTable.TABLE_NAME);
        db.insert(SensorDataContract.SensorDataTable.TABLE_NAME, null, contentValues);
        db.close();
//        SQLiteDatabase db2 = getWritableDatabase();
//        Cursor c = db2.rawQuery("select * from "+SensorDataContract.SensorDataTable.TABLE_NAME+ " ;", null);
//        c.moveToFirst();
//        System.out.println(c.getString(0));
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + SensorDataContract.SensorDataTable.TABLE_NAME);

        onCreate(sqLiteDatabase);
    }
}
