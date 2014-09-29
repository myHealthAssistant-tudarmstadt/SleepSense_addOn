package com.ess.tudarmstadt.de.sleepsense.utils;

import java.util.Calendar;
import java.util.Date;

import com.ess.tudarmstadt.de.sleepsense.R;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

			Toast.makeText(context.getApplicationContext(),
					"Booting Completed. Reset Alarms", Toast.LENGTH_LONG)
					.show();

			// Resetting the wake-up alarms
			UtilsTools.setAlarms(context.getApplicationContext());
		}
	}

}
