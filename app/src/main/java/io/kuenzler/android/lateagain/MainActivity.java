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
import android.text.Editable;
import android.text.method.KeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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
 * @version 0.6
 * @date 09.11.15 22:00 cleanup + doc
 */
public class MainActivity extends AppCompatActivity {

    private RequestLoop mReqLoop;
    private PropertiesManager mPm;
    private String[] mOldLocations;

    //private AutoCompleteTextView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        setDateTimeNow(null);
        mPm = new PropertiesManager(this);
        //mPm.setFromKey("locationHistory", "Eching,Feldmoching,Westkreuz");
        mOldLocations = mPm.getFromKey("locationHistory").split(";");
        final AutoCompleteTextView startView = (AutoCompleteTextView) findViewById(R.id.t_start);
        final AutoCompleteTextView destView = (AutoCompleteTextView) findViewById(R.id.t_dest);
        updateDropdown();
        startView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    updateDropdown();
                    startView.showDropDown();
                }
            }
        });
        destView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    updateDropdown();
                    destView.showDropDown();
                }
            }
        });
        startView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                   search(null);
                }
                return true;
            }
        });
        destView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    search(null);
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setDateTimeNow(null);
        EditText startField, destField;
        startField = (EditText) findViewById(R.id.t_start);
        destField = (EditText) findViewById(R.id.t_dest);
        startField.setText(mPm.getFromKey("lastStart"));
        destField.setText(mPm.getFromKey("lastDestination"));
        Log.v("LateAgain", "+ ON RESUME +");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v("LateAgain", "- ON PAUSE -");
    }

    /**
     *
     */
    private void updateDropdown() {
        final AutoCompleteTextView startView = (AutoCompleteTextView) findViewById(R.id.t_start);
        final AutoCompleteTextView destView = (AutoCompleteTextView) findViewById(R.id.t_dest);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, removeDuplicates(mOldLocations));
        startView.setAdapter(adapter);
        destView.setAdapter(adapter);
    }

    /**
     * Creates ongoign notification with countdown
     *
     * @param time      countdown time HH.mm.ss
     * @param departure departure time HH.mm
     * @param type
     */
    public void createNotification(String time, String departure, String type) {
        if (time.equals("--alert--")) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancelAll();
        } else {
            //Intent intent = new Intent(this, MainActivity.class);
            Intent intent = new Intent();
            PendingIntent pIntent = PendingIntent.getActivity(this, 1, intent, 0);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.clock);

            if (time.equals("00:00:00")) {
                Notification noti = new Notification.Builder(this)
                        .setContentTitle("Departure now!")
                        .setContentText("00:00:00" + " remaining.")
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
                        .setContentTitle("Next Departure at " + departure + " (" + type + ")")
                        .setContentText(time + " remaining.")
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
     * @param view
     */
    public void setDateTimeNow(View view) {
        setDateTime(Calendar.getInstance());
    }

    /**
     * @param hour
     * @param minute
     */
    public void setTime(int hour, int minute) {
        TextView l_time = (TextView) findViewById(R.id.l_time);
        l_time.setText(hour + ":" + minute);
    }

    /**
     * @param day
     * @param month
     * @param year
     */
    public void setDate(int day, int month, int year) {
        TextView l_date = (TextView) findViewById(R.id.l_date);
        l_date.setText(day + "." + month + "." + year);
    }

    /**
     * @param cal
     */
    public void setDateTime(Calendar cal) {
        int hour, minute, day, month, year;
        String date, time;
        Calendar calendar;
        TextView l_date = (TextView) findViewById(R.id.l_date);
        TextView l_time = (TextView) findViewById(R.id.l_time);
        Button b_now = (Button) findViewById(R.id.b_now);

        calendar = Calendar.getInstance();
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);

        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);

        setTime(hour, minute);
        setDate(day, month, year);
    }

    /**
     * @param view
     */
    public void search(View view) {
        EditText startField, destField;
        String start, dest;
        startField = (EditText) findViewById(R.id.t_start);
        destField = (EditText) findViewById(R.id.t_dest);
        start = startField.getText().toString().trim();
        dest = destField.getText().toString().trim();

        mPm.setFromKey("lastDestination", dest);
        mPm.setFromKey("lastStart", start);

        if (start.isEmpty() || dest.isEmpty()) {
            showToast("miep miep, no emtpy fields!");
        } else {
            mReqLoop = new RequestLoop(this);
            ArrayList<Departure> deps = null;
            //while (deps == null) {
            deps = mReqLoop.getDepartures(start, dest);
            //}
            if (deps == null) {
                return;
            }
            String[] depsString = new String[deps.size()];
            Departure dep;
            for (int i = 0; i < deps.size(); i++) {
                dep = deps.get(i);
                depsString[i] = dep.getTimeStart() + " (" + dep.getDelay() + ") " + dep.getType();
            }
            DeparturesDLV dlv = new DeparturesDLV(this, depsString);
            dlv.showdialog();
        }
    }

    /**
     * @param index
     */
    public void startCountdown(int index) {
        mReqLoop.setDepartureIndex(index);
        mReqLoop.start();
    }

    /**
     * @param view
     */
    public void stopAll(View view) {
        mReqLoop.interrupt();
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
        //TODO race condition
    }

    /**
     * @param message
     */
    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * @param whichLoc
     * @param location
     */
    public void setAlternativeLocation(int whichLoc, String location) {
        EditText field;
        if (whichLoc == 0) {
            field = (EditText) findViewById(R.id.t_start);
            field.setText(location);
        } else if (whichLoc == 1) {
            field = (EditText) findViewById(R.id.t_dest);
            field.setText(location);
        }
        mReqLoop.setAlternativeLocation(whichLoc, location);
        search(null);
    }

    /**
     * @param locations
     * @param whichLoc
     */
    public void getAlternativeLocations(ArrayList<String> locations, int whichLoc) {
        String[] locArray = locations.toArray(new String[locations.size()]);
        LocationsDLV ld = new LocationsDLV(this, locArray, whichLoc);
        ld.showdialog();
    }

    public void clearFields(View view) {
        EditText startField, destField;
        startField = (EditText) findViewById(R.id.t_start);
        destField = (EditText) findViewById(R.id.t_dest);
        startField.setText("");
        destField.setText("");
    }

    /**
     * @param start
     * @param dest
     */
    public void setCorrectedLocations(String start, String dest) {
        EditText startField, destField;
        startField = (EditText) findViewById(R.id.t_start);
        destField = (EditText) findViewById(R.id.t_dest);
        try {
            startField.setText(start);
            destField.setText(dest);
        } catch (Exception e) {
            //TODO happens when started in loop thread
            Log.e("Wrong thread ex", "see todo");
        }
        String oldLocations = mPm.getFromKey("locationHistory");
        String[] oldLocationsParsed = oldLocations.split(",");
        oldLocationsParsed = removeDuplicates(oldLocationsParsed);
        oldLocations = start.trim() + ";" + dest.trim();
        for (String loc : oldLocationsParsed) {
            if (loc.equalsIgnoreCase(start) || loc.equalsIgnoreCase(dest)) {
                //
            } else {
                oldLocations += ";" + loc.trim();
            }
        }
        this.mOldLocations = oldLocations.split(";");
        mPm.setFromKey("lastStart", start);
        mPm.setFromKey("lastDestination", dest);
        mPm.setFromKey("locationHistory", oldLocations);
    }

    /**
     * @param arr
     * @return
     */
    public String[] removeDuplicates(String[] arr) {
        ArrayList<String> arrClean = new ArrayList<String>();
        for (String s : arr) {
            s = s.trim();
            if (!arrClean.contains(s))
                arrClean.add(s);
        }
        String[] result = arrClean.toArray(new String[arrClean.size()]);
        return result;
    }
}

