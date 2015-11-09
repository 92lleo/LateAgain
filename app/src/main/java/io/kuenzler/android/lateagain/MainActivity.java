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
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import io.kuenzler.android.lateagain.control.RequestLoop;
import io.kuenzler.android.lateagain.model.Departure;
import io.kuenzler.android.lateagain.view.DialogListView;

public class MainActivity extends AppCompatActivity {

    private final static String TAG = "LateAgain";
    private RequestLoop mReqLoop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setDateTimeNow(null);
    }

    @Override
    public void onResume() {
        super.onResume();
        setDateTimeNow(null);
        Log.v(TAG, "+ ON RESUME +");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "- ON PAUSE -");
    }

    /**
     * @param time
     * @param departure
     */
    public void createNotification(String time, String departure) {

        if (time.equals("--alert--")) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancelAll();
        } else {

            Intent intent = new Intent(this, MainActivity.class);
            PendingIntent pIntent = PendingIntent.getActivity(this, 1, intent, 0);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.clock);

            Notification noti = new Notification.Builder(this)
                    .setContentTitle("Next Departure at " + departure)
                    .setContentText(time + " remaining.")
                    .setTicker("Countdown started to "+departure)
                    .setNumber(1)
                    .setLargeIcon(bitmap)
                    //.setSmallIcon(R.drawable.clock)
                    .setSmallIcon(R.drawable.train)
                    .setContentIntent(pIntent)
                    .addAction(0, "<< Prev", pIntent)
                    .addAction(0, "Stop", pIntent)
                    .addAction(0, "Next >>", pIntent)
                    .setOngoing(true)
                    .build();
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            // hide the notification after its selected
            noti.flags |= Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(0, noti);

            if (time.endsWith("00")) {

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
     * @param view
     */
    public void exit(View view) {
        NotificationManager notifManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notifManager.cancelAll();
        finish();
        System.exit(0);
    }

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
     * @param view
     */
    public void setDateTimeNow(View view) {
        setDateTime(Calendar.getInstance());
    }

    public void setTime(int hour, int minute) {
        TextView l_time = (TextView) findViewById(R.id.l_time);
        l_time.setText(hour + ":" + minute);
    }

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
    public void showDialog(View view) {

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

        if (start.isEmpty() || dest.isEmpty()) {
            showToast("miep miep, no emtpy fields!");
        } else {
            mReqLoop = new RequestLoop(this);
            ArrayList<Departure> deps = mReqLoop.getDepartures(start, dest);
            String[] depsString = new String[deps.size()];
            Departure dep;
            for (int i = 0; i < deps.size(); i++) {
                dep = deps.get(i);
                depsString[i] = dep.getTimeStart() + " (" + dep.getDelay() + ")";
            }
            DialogListView dlv = new DialogListView(this, depsString);
            dlv.showdialog();
        }
    }

    public void startCountdown(int index){
        mReqLoop.setDepartureIndex(index);
        mReqLoop.start();
    }

    public void stopAll(View view){
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
}

