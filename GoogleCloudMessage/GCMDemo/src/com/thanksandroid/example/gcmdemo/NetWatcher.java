package com.thanksandroid.example.gcmdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetWatcher extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //here, check that the network connection is available. If yes, start your service. If not, stop your service.
       ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
       NetworkInfo info = cm.getActiveNetworkInfo();
       if (info != null) {
           if (info.isConnected()) {
        	   Intent openMainActivity= new Intent(context, MainActivity.class);
               openMainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               context.startActivity(openMainActivity);
           }
       }
    }
}