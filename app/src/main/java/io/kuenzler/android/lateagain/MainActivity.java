package io.kuenzler.android.lateagain;

import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
//import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import io.kuenzler.android.lateagain.control.PropertiesManager;
import io.kuenzler.android.lateagain.control.RequestLoop;
import io.kuenzler.android.lateagain.model.Departure;
import io.kuenzler.android.lateagain.view.DeparturesDLV;
import io.kuenzler.android.lateagain.view.LocationsDLV;

/**
 * @author Leonhard Künzler
 * @version 0.7
 */
public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView mStartView;
    private AutoCompleteTextView mDestView;
    private TextView mDateView;
    //private final TextView mTimeView = (TextView) findViewById(R.id.l_time);
    //private final Button mNowButton = (Button) findViewById(R.id.b_now);

    private RequestLoop mReqLoop;
    private PropertiesManager mPm;
    private String[] mOldLocations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mStartView = (AutoCompleteTextView) findViewById(R.id.t_start);
        mDestView = (AutoCompleteTextView) findViewById(R.id.t_line);
        mDateView = (TextView) findViewById(R.id.t_date);

        /*setDateTimeNow(null);
        mPm = new PropertiesManager(this);
        mOldLocations = mPm.getFromKey("locationHistory").split(";");
        updateDropdown();
        initLocationView(mStartView);
        initLocationView(mDestView);

        String[] lines = {"Bus", "Str", "U", "S"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, removeDuplicates(lines));
        mDestView.setAdapter(adapter);*/
    }

    protected void onStart() {
        super.onStart();
        setDateTimeNow(null);
        mPm = new PropertiesManager(this);
        mOldLocations = mPm.getFromKey("locationHistory").split(";");
        updateDropdown();
        initLocationView(mStartView);
        initLocationView(mDestView);

        String[] lines = {"Bus", "Str", "U", "S"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, removeDuplicates(lines));
        mDestView.setAdapter(adapter);
    }

    /**
     * @param actv AutoCompliteTextView to init
     */
    private void initLocationView(final AutoCompleteTextView actv) {
        actv.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    updateDropdown();
                    actv.showDropDown();
                }
            }
        });
        actv.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    search(null);
                }
                return true;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setDateTimeNow(null);
    }

    /**
     * Updates dropdown dialogs with current oldLocations
     */
    private void updateDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, removeDuplicates(mOldLocations));
        mStartView.setAdapter(adapter);
    }

    /**
     * Creates ongoign notification with countdown
     *
     * @param time      countdown time HH.mm.ss
     * @param departure departure time HH.mm
     * @param type      transportation type (s,bus,u)
     */
    public void createNotification(String time, String departure, String type, String platform) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 1, intent, 0);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.clock);

        if (time.equals("00:00:00")) {
            Notification noti = new Notification.Builder(this)
                    .setContentTitle(type + " departs now on platform " + platform + "!")
                    .setContentText("00:00:00" + " remaining. (Platrform " + platform + ")")
                    .setTicker("Train should be there")
                    .setNumber(1)
                    .setLargeIcon(bitmap)
                    //.setSmallIcon(R.drawable.clock)
                    .setSmallIcon(R.drawable.train)
                    .setContentIntent(pIntent)
                    .setOngoing(false)
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            noti.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(0, noti);

        } else {
            Notification noti = new Notification.Builder(this)
                    .setContentTitle("Next " + type + " at " + departure)
                    .setContentText(time + " remaining. (Platform " + platform + ")")
                    .setTicker("Countdown started to " + departure)
                    .setNumber(1)
                    .setLargeIcon(bitmap)
                    //.setSmallIcon(R.drawable.clock)
                    .setSmallIcon(R.drawable.train)
                    .setContentIntent(pIntent)
                    //.addAction(0, "<< Prev", pIntent)
                    //.addAction(0, "Stop", pIntent)
                    //.addAction(0, "Next >>", pIntent)
                    .setOngoing(true)
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            noti.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(0, noti);
        }

        if (time.endsWith("0:02:00")) {

            final Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            new Thread() {

                public void run() {
                    try {
                        v.vibrate(200);
                        sleep(300);
                        v.vibrate(200);
                        sleep(300);
                        v.vibrate(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }


    /**
     * Cancel all notifications and exit
     *
     * @param view just to call from click
     */
    public void exit(View view) {
        NotificationManager notifManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
        finish();
        System.exit(0);
    }

    /**
     * Opens Date dialog and sets date
     *
     * @param view just to call from click
     */
    public void openDialogDate(View view) {
        Calendar mcurrentTime = Calendar.getInstance();
        int day = mcurrentTime.get(Calendar.DAY_OF_YEAR);
        int month = mcurrentTime.get(Calendar.MONTH);
        int year = mcurrentTime.get(Calendar.YEAR);
        DatePickerDialog mDatePicker;
        mDatePicker = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker datePicker, int sday, int smonth, int syear) {
                setDate(sday, smonth, syear);
            }
        }, year, month, day);
        mDatePicker.setTitle("Select Date");
        mDatePicker.show();
    }

    /**
     * Opens Time dialog and sets time
     *
     * @param view just to call from click
     */
    public void openDialogTime(View view) {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                setTime(selectedHour, selectedMinute);
            }
        }, hour, minute, true);
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    /**
     * Sets actual time
     *
     * @param view action view
     */
    public void setDateTimeNow(View view) {
        setDateTime(Calendar.getInstance());
    }

    /**
     * @param hour   hour as int
     * @param minute minute as int
     */
    public void setTime(int hour, int minute) {
        String fullTime = hour + ":" + minute;
        //TODOt_time.setText(fullTime);
    }

    /**
     * @param day   day as int
     * @param month month as int
     * @param year  year as int
     *              //TODO use calendar or date
     */
    public void setDate(int day, int month, int year) {
        String fullDate = day + "." + month + "." + year;
        mDateView.setText(fullDate);
    }

    /**
     * @param cal calendar with date to set
     */
    public void setDateTime(Calendar cal) {
        int hour, minute, day, month, year;

        Calendar calendar;
        if (cal != null) {
            calendar = cal;
        } else {
            calendar = Calendar.getInstance();
        }
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);

        setTime(hour, minute);
        setDate(day, month, year);
    }

    /**
     * @param view view called the action
     */
    public void search(View view) {
        stopAll(view);
        String start, dest;
        start = mStartView.getText().toString().trim();
        dest = mDestView.getText().toString().trim();

        //mPm.setFromKey("lastLine", dest);
        mPm.setFromKey("lastStart", start);

        if (start.isEmpty()) {
            showToast("Start must not be empty!");
            //} else if (start.equalsIgnoreCase(dest)) {
            //    showToast("No equal locations");
        } else {
            checkLine(dest);
            mReqLoop = new RequestLoop(this);
            ArrayList<Departure> deps;
            deps = mReqLoop.getDepartures(start, dest); //Dest empty for now
            if (deps == null) {
                return;
            }
            String[] depsString = new String[deps.size()];
            Departure dep;
            for (int i = 0; i < deps.size(); i++) {
                dep = deps.get(i);
                depsString[i] = dep.getTimeStart() + " (" + dep.getDelay() + ") " + dep.getType() + " to " + dep.getLocDestination();
            }
            DeparturesDLV dlv = new DeparturesDLV(this, depsString);
            dlv.showdialog();
        }
    }

    private void checkLine(String unfixedLine) {
        String line = unfixedLine.toLowerCase().trim();
        boolean recognized = false;
        if (line.isEmpty()) {
            recognized = true;
        }
        if (line.contains("bus")) {
            recognized = true;
        }
        if (line.contains("bu")) {
            recognized = true;
        }
        if (line.contains("b")) {
            recognized = true;
        }
        if (line.contains("str")) {
            recognized = true;
        }
        if (line.contains("st")) {
            recognized = true;
        }
        if (line.contains("s")) {
            recognized = true;
        }
        if (line.contains("u")) {
            recognized = true;
        }
        if (line.contains("v")) {
            recognized = true;
        }
        try {
            Integer.parseInt(line);
            recognized = true;
        } catch (NumberFormatException e) {
            // not an integer!
        }

        if (!recognized) {
            showToast("Your Line is not recognized. Try something like: S, S1, 1 , bus, bu, 650, str, u");
        }
    }

    /**
     * @param index index of desired departure
     */
    public void startCountdown(int index) {
        mReqLoop.setDepartureIndex(index);
        mReqLoop.start();
    }

    /**
     * @param view view that called the action
     */
    public void stopAll(View view) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        if (mReqLoop == null || !mReqLoop.isAlive()) {
            return;
        }
        mReqLoop.interrupt();
        mNotificationManager.cancelAll();
        //TODO race condition
    }

    /**
     * @param message message to toast
     */
    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * @param whichLoc 0=start,1=dest
     * @param location Location as String
     */
    public void setAlternativeLocation(int whichLoc, String location) {
        EditText field;
        if (whichLoc == 0) {
            field = (EditText) findViewById(R.id.t_start);
            field.setText(location);
        } else if (whichLoc == 1) {
            field = (EditText) findViewById(R.id.t_line);
            field.setText(location);
        }
        mReqLoop.setAlternativeLocation(whichLoc, location);
        search(null);
    }

    /**
     * @param locations List of alternative locations for dialog
     * @param whichLoc  0=start,1=dest
     */
    public void getAlternativeLocations(ArrayList<String> locations, int whichLoc) {
        String[] locArray = locations.toArray(new String[locations.size()]);
        LocationsDLV ld = new LocationsDLV(this, locArray, whichLoc);
        ld.showdialog();
    }

    /**
     * Clears Location selection textfields
     *
     * @param view view that called the action
     */
    public void clearFields(View view) {
        mStartView.setText("");
        mDestView.setText("");
    }

    /**
     * @param start Start location
     * @param dest  Dest location
     */
    public void setCorrectedLocations(String start, String dest) {
        EditText startField, destField;
        startField = (EditText) findViewById(R.id.t_start);
        destField = (EditText) findViewById(R.id.t_line);
        try {
            startField.setText(start);
            //destField.setText(dest);
        } catch (Exception e) {
            //TODO happens when started in loop thread
            Log.e("Wrong thread ex", "see todo");
        }
        String oldLocations = mPm.getFromKey("locationHistory");
        String[] oldLocationsParsed = oldLocations.split(",");
        oldLocationsParsed = removeDuplicates(oldLocationsParsed);
        oldLocations = start.trim();
        for (String loc : oldLocationsParsed) {
            if (!loc.equalsIgnoreCase(start) || !loc.equalsIgnoreCase(dest)) {
                oldLocations += ";" + loc.trim();
            }
        }
        this.mOldLocations = oldLocations.split(";");
        mPm.setFromKey("lastStart", start);
        mPm.setFromKey("lastDestination", dest);
        mPm.setFromKey("locationHistory", oldLocations);
    }

    /**
     * @param arr String array with duplicates
     * @return String array without duplicates
     */
    public String[] removeDuplicates(String[] arr) {
        ArrayList<String> arrClean = new ArrayList<>();
        for (String s : arr) {
            s = s.trim();
            if (!arrClean.contains(s)) {
                arrClean.add(s);
            }
        }
        return arrClean.toArray(new String[arrClean.size()]);
    }
}

