package com.example.assignment2.group30.data;

import android.provider.BaseColumns;


/**
 * Created by Dishank on 3/5/2018.
 */

public class SensorDataContract {



    public static final class SensorDataTable implements BaseColumns{

        public static String TABLE_NAME = "default";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_XVALUE = "xValue";
        public static final String COLUMN_YVALUE = "yValue";
        public static final String COLUMN_ZVALUE = "zValue";


    }
}
