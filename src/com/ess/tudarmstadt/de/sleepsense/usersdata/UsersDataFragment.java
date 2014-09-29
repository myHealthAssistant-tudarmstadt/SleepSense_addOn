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

package com.ess.tudarmstadt.de.sleepsense.usersdata;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import android.R.color;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;

import com.ess.tudarmstadt.de.sleepsense.database.LocalTransformationDB;
import com.ess.tudarmstadt.de.sleepsense.database.LocalTransformationDBMS;
import com.ess.tudarmstadt.de.sleepsense.database.TrafficData;
import com.ess.tudarmstadt.de.sleepsense.utils.UtilsTools;
import com.ess.tudarmstadt.de.sleepsense.R;

/**
 * User diary list
 * @author HieuHa
 *
 */
public class UsersDataFragment extends ListFragment {

	private static final String TAG = UsersDataFragment.class.getSimpleName();
	private static final int POS_TAG = 1 + 2 << 24;
	private View rootView;
	private ArrayAdapter<TrafficData> mArrayAdapter;
	private ArrayList<TrafficData> sleepData;
	public static final String dateFormat = "dd-MM-yyyy";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_list_user_data,
				container, false);

		Log.e(TAG, ": onCreateView");

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);

		LocalTransformationDBMS db = new LocalTransformationDBMS(getActivity()
				.getApplicationContext());
		db.open();
		sleepData = db.getAllSleepData();

		// Sort the time of sleepData in decimal format, e.g 13:32 means 13,32
		Collections.sort(sleepData, new Comparator<TrafficData>() {

			@Override
			public int compare(TrafficData arg0, TrafficData arg1) {
				SimpleDateFormat format = new SimpleDateFormat(
						"dd-MM-yyyy_hh:mm");
				int compareResult = 0;
				try {
					Date arg0Date = format.parse(arg0.getTrafficDate()
							+ "_"
							+ new DecimalFormat("00.00").format(
									arg0.getxValue()).replaceAll("\\,", ":"));
					Date arg1Date = format.parse(arg1.getTrafficDate()
							+ "_"
							+ new DecimalFormat("00.00").format(
									arg1.getxValue()).replaceAll("\\,", ":"));
					compareResult = arg0Date.compareTo(arg1Date);
				} catch (ParseException e) {
					e.printStackTrace();
					compareResult = arg0.getTrafficDate().compareTo(
							arg1.getTrafficDate());
				}
				return compareResult;
			}
		});

		db.close();

		mArrayAdapter = new mArrayAdapter(getActivity(), getActivity()
				.getApplicationContext());
		mArrayAdapter.addAll(sleepData);

		setListAdapter(mArrayAdapter);
		Button addEntry = (Button) rootView.findViewById(R.id.btn_add_item);
		addEntry.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				addEntryRow();
			}
		});

	}

	@Override
	public void onDestroy() {
		Log.e(TAG, "onDestroy");
		super.onDestroy();
	}

	private class mArrayAdapter extends ArrayAdapter<TrafficData> {
		private LayoutInflater mInflater;
		private Context context;

		public mArrayAdapter(Activity activity, Context ctx) {
			super(ctx, 0);
			this.mInflater = (LayoutInflater) LayoutInflater.from(activity);
			this.context = ctx;
		}

		HashMap<Integer, Double> sleepPrbly;
		HashMap<Double, Integer> sl2;

		public void calcPrbly() {
			sleepPrbly = new HashMap<Integer, Double>();

			ArrayList<Double> timeSleep = new ArrayList<Double>();
			ArrayList<Double> timeWake = new ArrayList<Double>();
			double firstSleepTime = Double.MAX_VALUE;
			double lastWakeTime = 0.0d;
			if (getCount() > 0) {
				for (int i = 0; i < getCount(); i++) {
					// populate the list of sleep time
					double aSleep = Math.floor(getItem(i).getxValue() * 10.0) / 10.0;
					double aWake = Math.floor(getItem(i).getyValue() * 10.0) / 10.0;
					timeSleep.add(aSleep);
					timeWake.add(aWake);

					if (aSleep < firstSleepTime)
						firstSleepTime = aSleep;
					if (aWake > lastWakeTime)
						lastWakeTime = aWake;
				}
			} else {
				return;
			}

			sl2 = new HashMap<Double, Integer>();
			double x = firstSleepTime;
			while (x < lastWakeTime) {
				int count = 0;
				for (int j = 0; j < timeSleep.size(); j++) {
					if (x >= timeSleep.get(j)) {
						count++;
					}
				}
				for (int z = 0; z < timeWake.size(); z++) {
					if (x >= timeWake.get(z)) {
						count--;
					}
				}
				sl2.put(x, count);

				x = Math.round((x + 0.10) * 100.00) / 100.00;

				if (x >= Math.floor(x) + 0.60)
					x = Math.floor(x + 1.00); // increase by one hour if x ->
												// minute 60
			}

			Log.e(TAG, sl2.toString());
			int hitsSize = sl2.size();
			for (int i = 0; i < getCount(); i++) {
				double slp = Math.floor(getItem(i).getxValue() * 10.0) / 10.0;
				double wke = Math.floor(getItem(i).getyValue() * 10.0) / 10.0;

				ArrayList<Integer> countList = new ArrayList<Integer>();

				for (Entry<Double, Integer> e : sl2.entrySet()) {
					if (e.getKey() >= slp && e.getKey() <= wke) {
						countList.add(e.getValue());
					}
				}

				double sum = 0.0d;
				if (!countList.isEmpty()) {
					for (int j = 0; j < countList.size(); j++) {
						sum += countList.get(j);
					}
					sum = sum / countList.size();
				}
				sleepPrbly.put(i,
						Math.round(sum / hitsSize * 100.0 * 10.0) / 10.0);
			}
			updatePrblyOnDb();
		}

		private void updatePrblyOnDb() {
			LocalTransformationDBMS db = new LocalTransformationDBMS(context);
			db.open();
			for (int i = 0; i < getCount(); i++) {
				TrafficData trfD = getItem(i);
				int id = trfD.getTrafficId();
				double sPy = sleepPrbly.get(i);

				ContentValues values = new ContentValues();
				values.put(LocalTransformationDB.SLEEP_TIME_COLUMN_DATE,
						trfD.getTrafficDate());
				values.put(LocalTransformationDB.SLEEP_TIME_COLUMN_SLEEP,
						trfD.getxValue());
				values.put(LocalTransformationDB.SLEEP_TIME_COLUMN_WAKE,
						trfD.getyValue());
				values.put(LocalTransformationDB.SLEEP_TIME_COLUMN_PRBLY, sPy);

				int rowUpdated = db.updateRow(
						LocalTransformationDB.TABLE_SLEEP_TIME, values,
						LocalTransformationDB.SLEEP_TIME_COLUMN_ID + " =?",
						new String[] { String.valueOf(id) });
				Log.e(TAG, "number of row updated:" + rowUpdated);
			}
			db.close();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final View view;
			if (convertView == null) {
				view = mInflater.inflate(R.layout.personal_profile_row, parent,
						false);
			} else {
				view = convertView;
			}

			final TrafficData key = getItem(position);

			view.setTag(POS_TAG, position);
			TextView tv_sleepDate = (TextView) view
					.findViewById(R.id.per_row_date);
			TextView tv_sleep = (TextView) view
					.findViewById(R.id.per_row_sleep);
			TextView tv_wake = (TextView) view.findViewById(R.id.per_row_wake);
			TextView tv_hours = (TextView) view
					.findViewById(R.id.per_row_hours);
			TextView tv_pbly = (TextView) view.findViewById(R.id.per_row_pby);

			// convert (double) hour.mm to hour:mm
			double x = key.getxValue();
			double y = key.getyValue();
			String sleep = new DecimalFormat("00.00").format(x).replaceAll(
					"\\,", ":");
			String wake = new DecimalFormat("00.00").format(y).replaceAll(
					"\\,", ":");

			// calculate the sleep hours
			int increasingDate = (x > y)? 1 : 0;
			long sleepUnix = UtilsTools.timeToUnix(key.getTrafficDate(),
					dateFormat, (int) Math.round(x - (x % 1)),
					(int) (Math.round((x % 1) * 100)), 0);
			
			long wakeUnix = UtilsTools.timeToUnix(key.getTrafficDate(),
					dateFormat, (int) Math.round(y - (y % 1)),
					(int) (Math.round((y % 1) * 100)), increasingDate);
			
			long sleepHoursInSec = wakeUnix - sleepUnix;
			double sleepHours = sleepHoursInSec / 3600.0;
			
			double fractionPart = sleepHours % 1;
			double integralPart = Math.floor(sleepHours - fractionPart);
						
			String sleepHoursTxt = new DecimalFormat("00.00").format(integralPart + (fractionPart * 60)/100).replaceAll(
					"\\,", ":");

			tv_sleepDate.setText(key.getTrafficDate());
			tv_sleep.setText(sleep);
			tv_wake.setText(wake);
			tv_hours.setText(sleepHoursTxt);

			String pbly = String.valueOf(key.getExtra()) + "%";
			tv_pbly.setText(pbly);

			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					view.setBackgroundColor(color.holo_red_light);
					showPopupMenu(view);
				}
			});

			return view;
		}

		private void showPopupMenu(final View v) {
			int pos = (Integer) v.getTag(POS_TAG);
			final TrafficData key = getItem(pos);
			PopupMenu popupMenu = new PopupMenu(this.getContext(), v);
			popupMenu.getMenuInflater().inflate(R.menu.popupmenu_userdata,
					popupMenu.getMenu());

			// v.setBackground(null);
			popupMenu
					.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

						@Override
						public boolean onMenuItemClick(MenuItem item) {
							switch (item.getItemId()) {
							case R.id.per_edit:
								editRow(key);
								v.setBackground(null);
								break;
							case R.id.per_del:
								deleteRow(key);
								v.setBackground(null);
								break;
							}
							return true;
						}
					});
			popupMenu.show();
		}
	}

	private void addEntryRow() {
		idToUpdate = -1;
		FragmentManager fm = this.getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment prev = fm.findFragmentByTag("datePicker");
		if (prev != null) {
			ft.remove(prev);
		}

		DialogFragment newFragment = new DatePickerFragment();
		newFragment.show(fm, "datePicker");

	}

	private int idToUpdate;

	private void editRow(TrafficData key) {
		try {
			calendarPicker = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			Date keyDate = sdf.parse(key.getTrafficDate());
			Calendar cal = Calendar.getInstance();
			cal.setTime(keyDate);

			calendarPicker.set(Calendar.YEAR, cal.get(Calendar.YEAR));
			calendarPicker.set(Calendar.MONTH, cal.get(Calendar.MONTH));
			calendarPicker.set(Calendar.DAY_OF_MONTH,
					cal.get(Calendar.DAY_OF_MONTH));

		} catch (ParseException e) {
			e.printStackTrace();
		}
		idToUpdate = key.getTrafficId();
		sleep = key.getxValue();
		wake = key.getyValue();
		showTimePicker(true);
	}

	private void deleteRow(TrafficData key) {
		Log.e(TAG, "delete trafficData:" + key.toString());
		LocalTransformationDBMS db = new LocalTransformationDBMS(getActivity()
				.getApplicationContext());
		db.open();
		long row = db.deleteSleepDataWithId(key.getTrafficId());

		Log.e(TAG, "delete entry at: " + row);

		db.close();
		if (row > 0) {
			mArrayAdapter.remove(key);
			if (!sleepData.remove(key)) {
				Log.e(TAG, "can't remove here! (399)");
			}
			((com.ess.tudarmstadt.de.sleepsense.usersdata.UsersDataFragment.mArrayAdapter) mArrayAdapter)
					.calcPrbly();
			mArrayAdapter.notifyDataSetChanged();
		}
	}

	private void showTimePicker(boolean isPickSleep) {
		FragmentManager fm = this.getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		Fragment prev = fm.findFragmentByTag("timePicker");
		if (prev != null) {
			ft.remove(prev);
		}

		DialogFragment newFragment = new TimePickerFragment(isPickSleep);

		newFragment.show(fm, "timePicker");
	}

	private Calendar calendarPicker = Calendar.getInstance();
	private double sleep = 0.0d;
	private double wake = 0.0d;

	private void addToDb() {
		String date = String.valueOf(DateFormat.format("dd-MM-yyyy",
				calendarPicker));

		LocalTransformationDBMS db = new LocalTransformationDBMS(getActivity()
				.getApplicationContext());
		db.open();
		if (idToUpdate > -1) {
			ContentValues values = new ContentValues();
			values.put(LocalTransformationDB.SLEEP_TIME_COLUMN_SLEEP, sleep);
			values.put(LocalTransformationDB.SLEEP_TIME_COLUMN_WAKE, wake);
			values.put(LocalTransformationDB.SLEEP_TIME_COLUMN_PRBLY, 0.0d);

			int rowUpdated = db.updateRow(
					LocalTransformationDB.TABLE_SLEEP_TIME, values,
					LocalTransformationDB.SLEEP_TIME_COLUMN_ID + " =?",
					new String[] { String.valueOf(idToUpdate) });

			Log.e(TAG, "number of rows edited:" + rowUpdated);

			for (TrafficData trD : sleepData) {
				if (trD.getTrafficId() == idToUpdate) {
					trD.setxValue(sleep);
					trD.setyValue(wake);
				}
			}
		} else {
			db.addSleepData(date, sleep, wake, 0.0d);
			sleepData.add(new TrafficData(date, -1, sleep, wake));
		}
		db.close();

		Collections.sort(sleepData, new Comparator<TrafficData>() {

			@Override
			public int compare(TrafficData arg0, TrafficData arg1) {
				SimpleDateFormat format = new SimpleDateFormat(
						"dd-MM-yyyy_hh:mm");
				int compareResult = 0;
				try {
					Date arg0Date = format.parse(arg0.getTrafficDate()
							+ "_"
							+ new DecimalFormat("00.00").format(
									arg0.getxValue()).replaceAll("\\,", ":"));
					Date arg1Date = format.parse(arg1.getTrafficDate()
							+ "_"
							+ new DecimalFormat("00.00").format(
									arg1.getxValue()).replaceAll("\\,", ":"));
					compareResult = arg0Date.compareTo(arg1Date);
				} catch (ParseException e) {
					e.printStackTrace();
					compareResult = arg0.getTrafficDate().compareTo(
							arg1.getTrafficDate());
				}
				return compareResult;
			}
		});

		mArrayAdapter.clear();
		mArrayAdapter.addAll(sleepData);
		((com.ess.tudarmstadt.de.sleepsense.usersdata.UsersDataFragment.mArrayAdapter) mArrayAdapter)
				.calcPrbly();

		mArrayAdapter.notifyDataSetChanged();

		// clear picker
		calendarPicker = Calendar.getInstance();
		sleep = 0.0d;
		wake = 0.0d;
		idToUpdate = -1;

	}

	@SuppressLint("ValidFragment")
	public class DatePickerFragment extends DialogFragment implements
			DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			int year = calendarPicker.get(Calendar.YEAR);
			int month = calendarPicker.get(Calendar.MONTH);
			int day = calendarPicker.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			calendarPicker = Calendar.getInstance();
			calendarPicker.set(Calendar.YEAR, year);
			calendarPicker.set(Calendar.MONTH, month);
			calendarPicker.set(Calendar.DAY_OF_MONTH, day);

			showTimePicker(true); // pick the sleep time
		}
	}

	public class TimePickerFragment extends DialogFragment implements
			TimePickerDialog.OnTimeSetListener {
		private boolean isPickSleep;

		public TimePickerFragment(boolean pickSleep) {
			this.isPickSleep = pickSleep;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);

			// Create a new instance of TimePickerDialog and return it
			TimePickerDialog dialog = new TimePickerDialog(getActivity(), this,
					hour, minute, true);

			dialog.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

			if (isPickSleep)
				dialog.setTitle("set sleep time");
			else
				dialog.setTitle("set wake time");

			return dialog;
		}

		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			if (isPickSleep) {
				sleep = hourOfDay + minute * 0.01;
				showTimePicker(false); // pick the wake time
			} else {
				wake = hourOfDay + minute * 0.01;
				addToDb();
			}

		}
	}
}