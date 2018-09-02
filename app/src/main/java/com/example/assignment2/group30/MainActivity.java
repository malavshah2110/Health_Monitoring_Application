package com.example.assignment2.group30;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.assignment2.group30.data.SensorDataContract;
import com.example.assignment2.group30.data.SensorDataHelper;
import com.example.assignment2.group30.model.SensorData;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    /*
    * For plotting the graph we have used the GraphView library.
    * First of all we have declared all the private global variables
    */

    // Declaring the graph view object:
    private GraphView graph;

    // Variables for buttons:
    private Button run_Button, stop_Button, upload_button, download_button;

    //Handler for managing threads
    private Handler handler;

    //Thread that plots the graph
    private Runnable graphThread;

    //Random variable
    private Random mRandom = new Random();

    private int xValue = 31;

    //This LineGraphSeries plots a line graph in the graphview
    private LineGraphSeries<DataPoint> zDataSeries;
    private LineGraphSeries<DataPoint> yDataSeries;
    private LineGraphSeries<DataPoint> xDataSeries;

    //Boolean variable to check whether the data is being
    private boolean graphState = true;

    //List of data points. DataPoints contain x,y co-ordinates that will be plotted on the graph
    private List<DataPoint> zDataPointList;
    private List<DataPoint> xDataPointList;
    private List<DataPoint> yDataPointList;


    private EditText et_PatientId, et_Age, et_PatientName;

    private RadioGroup rg_Sex;

    private RadioButton rb_Male, rb_Female;

    private ArrayList<Float> zValues;
    private ArrayList<Float> yValues;
    private ArrayList<Float> xValues;

    private Intent startSenseService;
    private SQLiteDatabase db;
    private String tableName = "";
    private SensorDataHelper sensorDataHelper;

    private static String uploadURL = "http://impact.asu.edu/CSE535Spring18Folder/UploadToServer.php";
    private static String downloadURL = "http://impact.asu.edu/CSE535Spring18Folder/group30.db";
    //private static String uploadURL = "http://192.168.0.5:8080/lan";

    HttpURLConnection conn = null;
    DataOutputStream dos = null;
    String lineEnd = "\r\n";
    String hyphens = "--";
    String boundary = "*****";
    byte[] buffer;
    int bytesAvailable, bufferSize, bytesRead;
    int maxBuffer = 1 * 1024 * 1024;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_Age = findViewById(R.id.age);
        et_PatientId = findViewById(R.id.patientId);
        et_PatientName = findViewById(R.id.patientName);

        rg_Sex = findViewById(R.id.radioGroup_Sex);
        rb_Female = findViewById(R.id.femaleRButton);
        rb_Male = findViewById(R.id.maleRButton);

        run_Button = findViewById(R.id.run_graph);
        stop_Button = findViewById(R.id.stop_graph);
        upload_button = findViewById(R.id.uploadButton);
        download_button = findViewById(R.id.downloadButton);

        graph = findViewById(R.id.graph);

        zDataPointList = new ArrayList<>();
        yDataPointList = new ArrayList<>();
        xDataPointList = new ArrayList<>();
        yDataSeries = new LineGraphSeries<>(generateRandomData());
        xDataSeries = new LineGraphSeries<>(generateRandomData());
        zDataSeries = new LineGraphSeries<>(generateRandomData());

        zDataSeries.setTitle("Z axis");
        xDataSeries.setTitle("X axis");
        yDataSeries.setTitle("Y axis");
        //setting properties for graph
        zDataSeries.setColor(Color.GREEN);
        yDataSeries.setColor(Color.RED);
        xDataSeries.setColor(Color.BLUE);

        graph.addSeries(xDataSeries);
        graph.addSeries(yDataSeries);
        graph.addSeries(zDataSeries);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(30);
        graph.getViewport().setMinY(-15);
        graph.getViewport().setMaxY(15);

        //setting labels for X and Y axis
        graph.getGridLabelRenderer().setHorizontalAxisTitle("Time (Seconds)");
        graph.getGridLabelRenderer().setVerticalAxisTitle("Heart Rate");
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        //setting the background color for the graph
        graph.getViewport().setBackgroundColor(Color.BLACK);
        handler = new Handler();

        zValues = new ArrayList<>();
        yValues = new ArrayList<>();
        xValues = new ArrayList<>();

        startSenseService = new Intent(MainActivity.this, sensorHandlerClass.class);


        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, 1);
        }


        /*onclick listener for run button
        * whenever the run button is pressed we call the graphControl method by passing
        * false. This helps us to control the situation when run button is pressed
        * multiple times. If the boolean value was not passed then generate data method would be
        * called multiple times and that would speed up the graph.
        *
        * */
        run_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                graphControl(true);
            }
        });

        /*onclick listener for run button
        * whenever the stop button is pressed we call the graphControl method by passing
        * false. This helps us to control the situation when stop button is pressed
        * multiple times. If the boolean value was not passed then generate data method would be
        * called multiple times and that would speed up the graph.
        *
        * */
        stop_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                graphControl(false);
            }
        });

        upload_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (graphState) { // If service is stopped
                    SendDatabaseToServer sendDatabaseToServer = new SendDatabaseToServer();
                    sendDatabaseToServer.execute("");
                } else {
                    // If service is running
                    Toast.makeText(getApplicationContext(), "Stop the graph to upload", Toast.LENGTH_SHORT).show();
                }

            }
        });

        download_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validatePatientFields()) {
                    if (graphState) {
                        DownloadDatabase downloadDatabase = new DownloadDatabase();
                        downloadDatabase.execute("");
                    } else {
                        Toast.makeText(getApplicationContext(), "Stop the graph to download", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });


        /*Thread to plot the graph on the UI thread
        * as we want to constantly provide the values to the graph and plot it
        * on the UI thread because we don't want to increase the load on UI thread
        * and crash the app in the process
        * */
        graphThread = new Runnable() {
            @Override
            public void run() {
                //TODO
                DataPoint zAppendDataPoint;
                DataPoint xAppendDataPoint;
                DataPoint yAppendDataPoint;
                if (zValues.size() == 0) {
                    zAppendDataPoint = new DataPoint(xValue, 5.0);
                    xAppendDataPoint = new DataPoint(xValue, 5.0);
                    yAppendDataPoint = new DataPoint(xValue, 5.0);
                    zDataPointList.add(zAppendDataPoint);
                    xDataPointList.add(xAppendDataPoint);
                    yDataPointList.add(yAppendDataPoint);
                } else {
                    float xVal = xValues.remove(0);
                    float yVal = yValues.remove(0);
                    float zVal = zValues.remove(0);
                    Log.d("Values", "x: " + xVal + " y: " + yVal + " z: " + zVal);
                    zAppendDataPoint = new DataPoint(xValue, zVal);
                    xAppendDataPoint = new DataPoint(xValue, xVal);
                    yAppendDataPoint = new DataPoint(xValue, yVal);

                    SensorData sensorData = new SensorData();
                    sensorData.setTimestamp(new Date().toString());
                    sensorData.setxValue(xVal);
                    sensorData.setyValue(yVal);
                    sensorData.setzValue(zVal);

                    sensorDataHelper.storeSensorValues(sensorData);

                    zDataPointList.add(zAppendDataPoint);
                    xDataPointList.add(xAppendDataPoint);
                    yDataPointList.add(yAppendDataPoint);
                }
                xValue += 1;
                zDataSeries.appendData(zAppendDataPoint, true, 30);
                yDataSeries.appendData(yAppendDataPoint, true, 30);
                xDataSeries.appendData(xAppendDataPoint, true, 30);
                handler.postDelayed(this, 170);
            }
        };

    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessageReceiver,
                        new IntentFilter("accel-values"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            zValues.add(intent.getExtras().getFloat("z"));
            yValues.add(intent.getExtras().getFloat("y"));
            xValues.add(intent.getExtras().getFloat("x"));
        }
    };

    @Override
    protected void onPause() {
        // Unregister since the activity is not visible
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    /* This parametrized method is to control the graph
    *  The boolean flag makes sure that graph runs when run button is clicked and
    *  stops when stop button is clicked
    *  */
    private void graphControl(boolean executionFlag) {


        if (validatePatientFields()) {

            try {
                String storage_folder = "/Android/Data/CSE535_ASSIGNMENT2";

                File f = new File(Environment.getExternalStorageDirectory(), storage_folder);
                if (!f.exists()) {
                    f.mkdirs();
                    Log.d("success", "file created");
                }
                sensorDataHelper = new SensorDataHelper(this);

            } catch (Exception e) {

                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }


            if (executionFlag) {
                if (graphState) {
                    if (xValue > 32) {
                        zDataSeries.resetData(regenerateRandomData(2));
                        xDataSeries.resetData(regenerateRandomData(0));
                        yDataSeries.resetData(regenerateRandomData(1));
                    }
                    //TODO
                    Bundle b = new Bundle();
                    b.putString("s", "st");
                    startSenseService.putExtras(b);
                    startService(startSenseService);
                    handler.postDelayed(graphThread, 1000);
                    Log.d("State", "started");
                    graphState = false;
                }
            } else {
                if (!graphState) {
                    handler.removeCallbacks(graphThread);
                    stopService(startSenseService);
                    zValues.clear();
                    xValues.clear();
                    yValues.clear();
                    DataPoint[] dataPoints = new DataPoint[1];
                    dataPoints[0] = new DataPoint(0, 0);
                    zDataSeries.resetData(dataPoints);
                    yDataSeries.resetData(dataPoints);
                    xDataSeries.resetData(dataPoints);
                    Log.d("State", "stopped");
                    graphState = true;
                }
            }
        }

    }

    //provided in library documentation
    private DataPoint[] generateRandomData() {
        int count = 31;
        zDataPointList.clear();
        xDataPointList.clear();
        yDataPointList.clear();
        DataPoint[] values = new DataPoint[count];
        for (int i = 0; i < count; i++) {
            double x = i;
            double y = mRandom.nextDouble() * 5.0;
            DataPoint v = new DataPoint(x, y);
            values[i] = v;
        }
        for (int i = 0; i < count; i++) {
            zDataPointList.add(values[i]);
            xDataPointList.add(values[i]);
            yDataPointList.add(values[i]);
        }
        return values;
    }

    /* This method provides the previous
    *  values to the graph when the run button is pressed again
    *  and starts the graph again for the new random values
    *  */
    private DataPoint[] regenerateRandomData(int axis) {
        String xVal = xValue + "";
        int count = Integer.parseInt(xVal);
        DataPoint[] values = new DataPoint[count];
        if (axis == 0) {
            for (int i = 0; i < count; i++) {
                values[i] = xDataPointList.get(i);
            }
        } else if (axis == 1) {
            for (int i = 0; i < count; i++) {
                values[i] = yDataPointList.get(i);
            }
        } else if (axis == 2) {
            for (int i = 0; i < count; i++) {
                values[i] = zDataPointList.get(i);
            }
        }

        return values;
    }

    private boolean validatePatientFields() {
        String name = et_PatientName.getText().toString();
        String id = et_PatientId.getText().toString();
        String age = et_Age.getText().toString();
        int seletedId = rg_Sex.getCheckedRadioButtonId();
        RadioButton seletedRb = findViewById(seletedId);


        if (name.equals("") || id.equals("") || age.equals("") || seletedId == -1) {
            Toast.makeText(this, "Please Select all fields", Toast.LENGTH_SHORT).show();
            return false;
        } else {
            String sex = seletedRb.getText().toString();
            tableName = name+"_"+id+"_"+age+"_"+sex;
            SensorDataContract.SensorDataTable.TABLE_NAME = tableName;
            return true;
        }

    }

    public void generateDownloadedData() {
        String filePath = Environment.getExternalStorageDirectory()
                + File.separator + "Android"
                + File.separator + "data"
                + "/CSE535_ASSIGNMENT2_DOWN"
                + File.separator + "group30.db";
        db = SQLiteDatabase.openDatabase(filePath, null, SQLiteDatabase.OPEN_READONLY);
        Log.d("op", "opn");

        try {
            zDataSeries.resetData(regenerateRandomData(2));
            xDataSeries.resetData(regenerateRandomData(0));
            yDataSeries.resetData(regenerateRandomData(1));
            Cursor data = db.rawQuery("Select * from " + tableName + " order by timestamp desc limit 10;", null);
            Log.d("table",tableName);

            data.moveToFirst();
            for (int i = 0; i < 10; i++) {
                Log.d("Values", data.getDouble(1) + " " + data.getDouble(2) + " " + data.getDouble(3));
                xDataSeries.appendData(new DataPoint(xValue, data.getDouble(1)), true, 30);
                yDataSeries.appendData(new DataPoint(xValue, data.getDouble(2)), true, 30);
                zDataSeries.appendData(new DataPoint(xValue, data.getDouble(3)), true, 30);
                zDataPointList.add(new DataPoint(xValue, data.getDouble(3)));
                yDataPointList.add(new DataPoint(xValue, data.getDouble(2)));
                xDataPointList.add(new DataPoint(xValue, data.getDouble(1)));
                data.moveToNext();
                xValue++;
            }
            Toast.makeText(getApplicationContext(), "Table found successfully", Toast.LENGTH_LONG).show();
        } catch (SQLiteException e) {
            Log.d("excpetion",e.toString());
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(), "No table found for this person", Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    class SendDatabaseToServer extends AsyncTask<String, Void, String> {

        String response_string = "";

        protected String doInBackground(String... strings) {
            //For getting locally Stored data
            File file = new File(Environment.getExternalStorageDirectory()
                    + File.separator + "Android"
                    + File.separator + "data"
                    + "/CSE535_ASSIGNMENT2"
                    + File.separator + "group30.db");

            //For File to be upload on server
            String filePath = Environment.getExternalStorageDirectory()
                    + File.separator + "Android"
                    + File.separator + "data"
                    + "/CSE535_ASSIGNMENT2"
                    + File.separator + "group30.db";


            int size = (int) file.length();
            byte[] bytes = new byte[size];

            try {

                //Creating URL Object
                URL url = new URL(uploadURL);

                //Setting headers for HTTP POST request
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + "*****");
                conn.setRequestProperty("uploaded_file", filePath);

                //Output stream to upload
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(hyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\"" +
                        filePath + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                //For Writing locally stored data to server
                FileInputStream fileInputStream = new FileInputStream(file);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBuffer);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                //Uploading all the data from locally stored file
                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBuffer);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                dos.writeBytes(lineEnd);
                dos.writeBytes(hyphens + boundary + hyphens + lineEnd);

                //Getting the response from Server
                int responseCode = conn.getResponseCode();

                if (responseCode == 200) {
                    //successful POST request
                    response_string = conn.getResponseMessage();
                } else {
                    //In case of error
                    Log.d("Problem--------------->", "Request didn't get through");
                }


            }catch (FileNotFoundException e){
                // If database not found
                response_string = "No database found";
            }catch (Exception e) {
                //Any other exception
                e.printStackTrace();
            }
            return response_string;
        }


        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("OK")) {
                Toast.makeText(MainActivity.this, "Database uploaded successfully", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_LONG).show();
            }
        }
    }

    class DownloadDatabase extends AsyncTask<String, Void, String> {


        @Override
        protected String doInBackground(String... strings) {

            InputStream inputStream = null;
            //Ot=utoutStream to store the downloaded file content to file
            FileOutputStream fileOutputStream = null;
            HttpURLConnection connection = null;
            String response = "Downloaded";

            try {

                String storage_folder = "/Android/Data/CSE535_ASSIGNMENT2_DOWN";

                //File location to store the downloaded database file
                File f = new File(Environment.getExternalStorageDirectory(), storage_folder);
                if (!f.exists()) {
                    f.mkdirs();
                    Log.d("success", "file created");
                }

                URL url = new URL(downloadURL);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                inputStream = connection.getInputStream();
                fileOutputStream = new FileOutputStream(Environment.getExternalStorageDirectory()
                        + File.separator + "Android"
                        + File.separator + "data"
                        + "/CSE535_ASSIGNMENT2_DOWN"
                        + File.separator + "group30.db");

                byte[] fileData = new byte[4096];
                long total = 0;
                int count = 0;
                //reading the file bytes to store it in the path specified above
                while ((count = inputStream.read(fileData)) != -1) {
                    total += count;
                    fileOutputStream.write(fileData, 0, count);
                }
            } catch (SQLiteException e) {
                e.printStackTrace();
                response = e.getMessage();
            } catch (Exception e) {
                e.printStackTrace();
                response = e.getMessage();

            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connection.disconnect();
            }
            return response;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s.equals("Downloaded")) {
                //method to generate the latest 10 points from the downloaded database to graph
                generateDownloadedData();
            }
        }
    }


}