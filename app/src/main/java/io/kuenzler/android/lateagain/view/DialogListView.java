package io.kuenzler.android.lateagain.view;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

import io.kuenzler.android.lateagain.MainActivity;
import io.kuenzler.android.lateagain.R;

public class DialogListView implements OnItemClickListener{

    MainActivity mMain;
    ListView list;
    Dialog listDialog;
    String[] val = {"sunday","monday","tuesday","thrusday","friday","wednesday","march"};
    public DialogListView(MainActivity main, String[] departures){
        mMain = main;
        val = departures;
    }

    public void showdialog()
    {
        listDialog = new Dialog(mMain);
        listDialog.setTitle("Select Departure");
        LayoutInflater li = (LayoutInflater) mMain.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = li.inflate(R.layout.list, null, false);
        listDialog.setContentView(v);
        listDialog.setCancelable(true);
        //there are a lot of settings, for dialog, check them all out!

        ListView list1 = (ListView) listDialog.findViewById(R.id.listview);
        list1.setOnItemClickListener(this);
        list1.setAdapter(new ArrayAdapter<String>(mMain,android.R.layout.simple_list_item_1, val));
        //now that the dialog is set up, it's time to show it
        listDialog.show();
    }

    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
    {
        listDialog.cancel();
        mMain.startCountdown(arg2);
    }
}