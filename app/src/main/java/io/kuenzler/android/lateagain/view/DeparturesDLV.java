package io.kuenzler.android.lateagain.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import io.kuenzler.android.lateagain.MainActivity;
import io.kuenzler.android.lateagain.R;

/**
 * @author Leonhard Künzler
 * @version 0.2
 * @date 15.11.03 01:00
 */
public class DeparturesDLV implements OnItemClickListener {

    MainActivity mMain;
    ListView list;
    Dialog listDialog;
    String[] val;

    /**
     * @param main
     * @param departures
     */
    public DeparturesDLV(MainActivity main, String[] departures) {
        mMain = main;
        val = departures;
    }

    /**
     *
     */
    public void showdialog() {
        listDialog = new Dialog(mMain);
        listDialog.setTitle("Select Departure");
        LayoutInflater li = (LayoutInflater) mMain.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = li.inflate(R.layout.list, null, false);
        listDialog.setContentView(v);
        listDialog.setCancelable(true);
        //there are a lot of settings, for dialog, check them all out!

        ListView list1 = (ListView) listDialog.findViewById(R.id.listview);
        list1.setOnItemClickListener(this);
        list1.setAdapter(new ArrayAdapter<String>(mMain, android.R.layout.simple_list_item_1, val));
        //now that the dialog is set up, it's time to show it
        listDialog.show();
    }

    /**
     * @param arg0
     * @param arg1
     * @param arg2
     * @param arg3
     */
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        listDialog.cancel();
        mMain.startCountdown(arg2);
    }
}