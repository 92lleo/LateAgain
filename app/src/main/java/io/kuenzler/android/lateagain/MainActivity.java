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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.ContentViewEvent;
import com.crashlytics.android.answers.SearchEvent;

import io.fabric.sdk.android.Fabric;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;

import io.kuenzler.android.lateagain.control.DateCalculator;
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

    private int mOldDelay;
    private boolean ongoingNotification = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        this.getApplication();
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
                                              try {
                                                  if (hasFocus) {
                                                      updateDropdown();
                                                      actv.showDropDown();
                                                  }
                                              } catch (WindowManager.BadTokenException e) {
                                                  Crashlytics.log(e.toString());
                                                  //TODO: log inconsistence
                                              } catch (Exception e){
                                                  Crashlytics.log(e.toString());
                                              }
                                          }
                                      }

        );
        actv.setOnKeyListener(new View.OnKeyListener()

                              {
                                  @Override
                                  public boolean onKey(View v, int keyCode, KeyEvent event) {
                                      if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                                              (keyCode == KeyEvent.KEYCODE_ENTER)) {
                                          search(null);
                                      }
                                      return true;
                                  }
                              }

        );
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
     * @param dep       transportation type (s,bus,u)
     */
    public void createNotification(String time, String departure, Departure dep) {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(this, 1, intent, 0);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.clock);
        String title, text;

        String[] times = time.split(":");
        boolean isInPast = false;
        for (String s : times) {
            try {
                int x = Integer.parseInt(s);
                if (x < 0) {
                    isInPast = true;
                }
            } catch (NumberFormatException e) {
                isInPast = true;
            }
        }
        title = "";
        if (isInPast) {
            title = (dep.getType() + " is already gone");
            Answers.getInstance().logContentView(new ContentViewEvent()
                    .putContentName("Countdown finnished")
                    .putContentType("to " + dep.getLocStart())
                    .putContentId("1"));
        } else if (time.equals("00:00:00")) {
            isInPast = true;
            if (dep.getPlatform().equals("-")) {
                title = (dep.getType() + " departs now!");
            } else {
                title = (dep.getType() + " departs now on platform " + dep.getPlatform() + "!");
            }
        }
        text = dep.getType() + " to " + dep.getLocDestination() + " at " + dep.getTimeStart();
        if (isInPast) {
            Notification noti = new Notification.Builder(this)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setTicker("Train should be there")
                    .setNumber(1)
                    .setLargeIcon(bitmap)
                    .setSmallIcon(R.drawable.train)
                    .setContentIntent(pIntent)
                    .setOngoing(false)
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            noti.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(0, noti);

        } else {
            times = time.split(":");
            if (!times[0].equals("00")) {
                time = times[0] + ":" + times[1] + ":" + times[2];
            } else if (!times[1].equals("00")) {
                time = times[1] + ":" + times[2];
            } else {
                time = times[2] + "s";
            }
            text = dep.getType() + " to " + dep.getLocDestination() + " at " + dep.getTimeStart() + " (" + dep.getDelay() + ")";
            if (dep.getPlatform().equals("-")) {
                title = time + " left to get to " + dep.getType();
            } else {
                title = time + " left to get to platform " + dep.getPlatform();
            }
            Notification noti = new Notification.Builder(this)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setTicker(text)
                    .setNumber(1)
                    .setLargeIcon(bitmap)
                    //.setSmallIcon(R.drawable.clock)
                    .setSmallIcon(R.drawable.train)
                    .setContentIntent(pIntent)
                    //.addAction(0, "<< Prev", pIntent)
                    //.addAction(0, "Stop", pIntent)
                    //.addAction(0, "Next >>", pIntent)
                    .setOngoing(ongoingNotification)
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            noti.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(0, noti);
        }

        int newDelay = -1;
        try {
            newDelay = Integer.parseInt(dep.getDelay());
        } catch (NumberFormatException e) {
            //ignore
        }
        if (mOldDelay >= 0 && newDelay != mOldDelay) {
            final Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
            new Thread() {

                public void run() {
                    try {
                        v.vibrate(400);
                        sleep(300);
                        v.vibrate(100);
                        sleep(200);
                        v.vibrate(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
        mOldDelay = newDelay;
        if (time.endsWith("02:00")) {

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
        stopAll(null);
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
        Answers.getInstance().logSearch(new SearchEvent()
                .putQuery("Searched: " + start + ", " + dest));
        mOldDelay = -1;
        //mPm.setFromKey("lastLine", dest); depr. -> dest eq line
        mPm.setFromKey("lastStart", start);

        if (start.isEmpty()) {
            showToast("Start must not be empty!");
            //} else if (start.equalsIgnoreCase(dest)) {
            //    showToast("No equal locations");
        } else {
            checkLine(dest);
            mReqLoop = new RequestLoop(this);
            ArrayList<Departure> deps;
            String date, time, filter; //TODO: date,time & filter hard for now
            date = DateCalculator.getCurrentDate();
            time = DateCalculator.getCurrentTime();
            filter = "1111111111000000";
            deps = mReqLoop.getDepartures(date, time, start, dest, filter); //TODO: dest to line
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

    private void expandNotificationBar() {
        try {

            @SuppressWarnings("ResourceType")  //works
                    Object service = getSystemService("statusbar");
            Class<?> statusbarManager = Class.forName("android.app.StatusBarManager");
            Method expand = statusbarManager.getMethod("expandNotificationsPanel");
            expand.invoke(service);
        } catch (Exception e) {
            //
        }
    }

    private void checkLine(String unfixedLine) {
        String[] recognizedStrings = {"bus", "bu", "b", "str", "st", "s", "u", "v", "re", "ic"};
        String line = unfixedLine.toLowerCase().trim();
        boolean recognized = false;
        if (line.isEmpty()) {
            recognized = true;
        } else {
            for (String rs : recognizedStrings) {
                if (line.contains(rs)) {
                    recognized = true;
                }
            }
            if (line.isEmpty()) {
                recognized = true;
            }
            try {
                Integer.parseInt(line);
                recognized = true;
            } catch (NumberFormatException e) {
                // not an integer, continue
            }
        }

        if (!recognized) {
            showToast("Your line is not recognized. Try something like: S, S1, 1 , bus, bu, 650, str, u, re");
        }
    }

    /**
     * @param index index of desired departure
     */
    public void startCountdown(int index) {
        CheckBox cb = (CheckBox) findViewById(R.id.cb_ongoing);
        ongoingNotification = cb.isSelected();
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.snackbarCoordinatorLayout);
        final Snackbar snackbar = Snackbar
                .make(coordinatorLayout, "Startet new countdown...", Snackbar.LENGTH_LONG);
        snackbar.setAction("Show", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandNotificationBar();
                snackbar.dismiss();
                //moveTaskToBack(true); //homebutton alternative
                startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME));
            }
        });
        snackbar.show();

        mReqLoop.setDepartureIndex(index);
        mReqLoop.start();
    }

    /**
     * @param view view that called the action
     */
    public void stopAll(View view) {
        ongoingNotification = false;
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

