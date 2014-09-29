/* 
 * Copyright (C) 2014 TU Darmstadt, Hessen, Germany.
 * Department of Computer Science Databases and Distributed Systems
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.ess.tudarmstadt.de.sleepsense.utils;

import java.util.Calendar;

import com.ess.tudarmstadt.de.sleepsense.systemmonitor.SensorsMeterService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.text.format.DateFormat;
import android.util.Log;

public class WakeupAlarmOff extends BroadcastReceiver {
	private static final String TAG = WakeupAlarmOff.class.getSimpleName();
	private static final String intentAction = "com.ess.tudarmstadt.de.sleepsense.systemmonitor.WakeupAlarmOff.ALARM";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(intentAction)) {
			PowerManager pm = (PowerManager) context
					.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wl = pm.newWakeLock(
					PowerManager.PARTIAL_WAKE_LOCK, TAG);

			Log.e(TAG, "start wakeupAlarmOff!");
			wl.acquire();
			stopSleepDetectorService(context);
			wl.release();
		}
	}

	private void stopSleepDetectorService(Context context) {
//		Intent i = new Intent(context, NewSleepDetectorService.class);
		Intent i = new Intent(context, SensorsMeterService.class);
		context.stopService(i);
	}

	// public void setForFiveMinAlarm(Context context) {
	// Log.e(TAG, "alarm being set for once time for 5min");
	// AlarmManager am = (AlarmManager) context
	// .getSystemService(Context.ALARM_SERVICE);
	// Intent i = new Intent(context, WakeupAlarmOff.class);
	// PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
	// // set for 5min
	// long triggerAtMillis = System.currentTimeMillis() + (1000 * 60 * 5);
	// am.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
	// }

	public void setRepeatedAlarm(Context context, Calendar time) {
		Log.e(TAG, "off_alarm being set repeated for calender time at:"
				+ DateFormat.format(SensorsMeterService.timeFormat, time));
		
		Intent i = new Intent(context, WakeupAlarmOff.class);
		i.setAction(intentAction);
		i.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		PendingIntent pi = PendingIntent.getBroadcast(context, 1, i, 0);
		
		long triggerAtMillis = time.getTimeInMillis();
		long intervalMillis = 24 * 60 * 60 * 1000; // one day time
		
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtMillis,
				intervalMillis, pi);
	}

	public void cancelAlarm(Context context) {
		Log.e(TAG, "alarm being canceled");

		Intent i = new Intent(context, WakeupAlarmOff.class);
		i.setAction(intentAction);
		i.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
		
		PendingIntent sender = PendingIntent.getBroadcast(context, 1, i, 0);
		AlarmManager alarmManager = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(sender);
		stopSleepDetectorService(context);
		
	}

}