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

package com.ess.tudarmstadt.de.sleepsense.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

import com.ess.tudarmstadt.de.sleepsense.R;
import com.ess.tudarmstadt.de.sleepsense.systemmonitor.SensorsMeterService;
import com.ess.tudarmstadt.de.sleepsense.utils.BootReceiver;
import com.ess.tudarmstadt.de.sleepsense.utils.UtilsTools;

import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.AbstractChannel;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.Event;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.management.Subscription;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.management.Unsubscription;

/**
 * 
 * @author HieuHa
 *
 */
public class SleepDetectorPreference extends PreferenceActivity {

	private static final String TAG = SleepDetectorPreference.class
			.getSimpleName();
	private Context context;
	private SharedPreferences pref;
	private boolean startAny;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		context = getApplicationContext();
		pref = PreferenceManager.getDefaultSharedPreferences(context);

		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new MyPreferenceFragment())
				.commit();
		startAny = false;
	}

	public class MyPreferenceFragment extends PreferenceFragment {
		public MyPreferenceFragment() {
			// Just an empty constructor!
		}

		@Override
		public void onCreate(final Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			addPreferencesFromResource(R.xml.detector_confix);

			SwitchPreference time_frame = (SwitchPreference) getPreferenceManager()
					.findPreference(getString(R.string.settings_time_frame));
			time_frame
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							pref.edit()
									.putBoolean(
											getString(R.string.settings_time_frame),
											(Boolean) newValue).commit();
							setAlarm((Boolean) newValue);
							return true;
						}
					});

			SwitchPreference waiting_period = (SwitchPreference) getPreferenceManager()
					.findPreference(getString(R.string.settings_waiting_period));
			waiting_period
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							pref.edit()
									.putBoolean(
											getString(R.string.settings_waiting_period),
											(Boolean) newValue).commit();
							return true;
						}
					});

			SwitchPreference acc_sensor = (SwitchPreference) getPreferenceManager()
					.findPreference(getString(R.string.settings_acc_sensor));
			acc_sensor
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							// subcribe(
							// SensorReadingEvent.ACCELEROMETER_ON_DEVICE,
							// (Boolean) newValue);
							pref.edit()
									.putBoolean(
											getString(R.string.settings_acc_sensor),
											(Boolean) newValue).commit();
							startAny = (Boolean) newValue;
							return true;
						}

					});

			SwitchPreference ambient_light_sensor = (SwitchPreference) getPreferenceManager()
					.findPreference(
							getString(R.string.settings_ambient_light_sensor));
			ambient_light_sensor
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							// subcribe(SensorReadingEvent.AMBIENT_LIGHT,
							// (Boolean) newValue);
							pref.edit()
									.putBoolean(
											getString(R.string.settings_ambient_light_sensor),
											(Boolean) newValue).commit();
							startAny = (Boolean) newValue;
							return true;
						}

					});

			SwitchPreference noise_sensor = (SwitchPreference) getPreferenceManager()
					.findPreference(getString(R.string.settings_noise_sensor));
			noise_sensor
					.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

						@Override
						public boolean onPreferenceChange(
								Preference preference, Object newValue) {
							pref.edit()
									.putBoolean(
											getString(R.string.settings_noise_sensor),
											(Boolean) newValue).commit();
							startAny = (Boolean) newValue;
							return true;
						}
					});
		}
	}

	@Override
	public void onStop() {
		Log.e(TAG, "onStop");

		// else {
		// if (startAny){
		// wAlarm.startSleepDetectorService(context);
		// } else {
		// wAlarm.stopSleepDetectorService(context);
		// }
		// }
		super.onStop();
	}

	private void subcribe(String eventType, boolean enable) {
		if (enable) {
			Subscription sub = new Subscription(TAG, getTimestamp(), TAG,
					getPackageName(), eventType);

			// publish subscription
			publishEvent(sub, AbstractChannel.MANAGEMENT);
		} else {
			Unsubscription sub = new Unsubscription(TAG, getTimestamp(), TAG,
					getPackageName(), eventType);

			// publish subscription
			publishEvent(sub, AbstractChannel.MANAGEMENT);
		}

	}

	private void publishEvent(Event event, String channel) {
		Intent i = new Intent();
		// add event
		i.putExtra(Event.PARCELABLE_EXTRA_EVENT_TYPE, event.getEventType());
		i.putExtra(Event.PARCELABLE_EXTRA_EVENT, event);

		// set channel
		i.setAction(channel);

		// set receiver package
		i.setPackage("de.tudarmstadt.dvs.myhealthassistant.myhealthhub");

		// sent intent
		this.sendBroadcast(i);
		// mAdapter.notifyDataSetChanged();
	}

	private String getTimestamp() {
		return (String) android.text.format.DateFormat.format(
				SensorsMeterService.timeFormat, new java.util.Date());
	}

	private void setAlarm(Boolean on) {
		ComponentName receiver = new ComponentName(context, BootReceiver.class);
		PackageManager pm = context.getPackageManager();

		if (on) {
			// for rebooting the device
			pm.setComponentEnabledSetting(receiver,
			        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
			        PackageManager.DONT_KILL_APP);
			
			UtilsTools.setAlarms(context);
			

		} else {
			UtilsTools.cancelAlarms(context);
			
			// turn off the bootReceiver Broadcast
			pm.setComponentEnabledSetting(receiver,
			        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
			        PackageManager.DONT_KILL_APP);
		}
	}
}
