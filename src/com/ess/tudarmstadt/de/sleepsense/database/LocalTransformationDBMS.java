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

package com.ess.tudarmstadt.de.sleepsense.database;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ess.tudarmstadt.de.sleepsense.systemmonitor.SensorsMeterService;

public class LocalTransformationDBMS {

	private static String TAG = "LocalTransformationDBMS";
	// private static boolean D = false;

	private SQLiteDatabase database;
	private LocalTransformationDB dbHelper;

	public LocalTransformationDBMS(Context context) {
		dbHelper = new LocalTransformationDB(context);
	}

	public void open() {
		database = dbHelper.getWritableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public long addSleepData(String date, double sleep, double wake,
			double prbly) {
		ContentValues values = new ContentValues();
		values.put(LocalTransformationDB.SLEEP_TIME_COLUMN_DATE, date);
		values.put(LocalTransformationDB.SLEEP_TIME_COLUMN_SLEEP, sleep);
		values.put(LocalTransformationDB.SLEEP_TIME_COLUMN_WAKE, wake);
		values.put(LocalTransformationDB.SLEEP_TIME_COLUMN_PRBLY, prbly);

		long insertId = database.insert(LocalTransformationDB.TABLE_SLEEP_TIME,
				null, values);
		Log.e(TAG,
				"insert sleep time at: " + insertId + " -- "
						+ values.toString());
		return insertId;
	}

	public ArrayList<TrafficData> getAllSleepData() {
		ArrayList<TrafficData> list = new ArrayList<TrafficData>();
		String q = "SELECT * FROM " + LocalTransformationDB.TABLE_SLEEP_TIME
				+ " ORDER BY " + LocalTransformationDB.SLEEP_TIME_COLUMN_DATE
				+ ";";
		Cursor cursor = database.rawQuery(q, null);
		if (cursor.moveToFirst()) {
			do {
				TrafficData trafficData = new TrafficData(
						cursor.getString(cursor
								.getColumnIndex(LocalTransformationDB.SLEEP_TIME_COLUMN_DATE)),
						-1,
						cursor.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SLEEP_TIME_COLUMN_SLEEP)),
						cursor.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SLEEP_TIME_COLUMN_WAKE)));
				trafficData
						.setExtra(cursor.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SLEEP_TIME_COLUMN_PRBLY)));
				trafficData
						.setTrafficId(cursor.getInt(cursor
								.getColumnIndex(LocalTransformationDB.SLEEP_TIME_COLUMN_ID)));

				list.add(trafficData);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return list;
	}

	public int updateRow(String table, ContentValues contents,
			String whereClause, String[] whereArgs) {
		return database.update(table, contents, whereClause, whereArgs);

	}

	public long deleteSleepDataWithId(int id) {
		String whereClause = LocalTransformationDB.SLEEP_TIME_COLUMN_ID
				+ " =? "
		// + "AND "
		// + LocalTransformationDB.SLEEP_TIME_COLUMN_DATE + " like ? "
		// + "AND "
		// + LocalTransformationDB.SLEEP_TIME_COLUMN_SLEEP + " =? "
		// + "AND "
		//
		;
		String[] whereArgs = new String[] { String.valueOf(id) };

		return database.delete(LocalTransformationDB.TABLE_SLEEP_TIME,
				whereClause, whereArgs);
	}

	public boolean addTraffic(String date, int trafficType, double xValue,
			double yValue) {

		// ContentValues values = new ContentValues();
		// values.put(LocalTransformationDB.COLUMN_DATE_TEXT, date);
		// values.put(LocalTransformationDB.COLUMN_TYPE, trafficType);
		// values.put(LocalTransformationDB.COLUMN_X_AXIS, xValue);
		// values.put(LocalTransformationDB.COLUMN_Y_AXIS, yValue);
		// long insertId = database.insert(
		// LocalTransformationDB.TABLE_TRAFFIC_MON, null, values);
		// // Log.e(TAG, "insert at: " + insertId);
		// return insertId != -1;
		return false;
	}

	public boolean addAllFour(String date, double timeValue, double accVal,
			double lightVal, double noiseVal, double sleepVal) {
		ContentValues values = new ContentValues();
		values.put(LocalTransformationDB.SENSOR_METER_COLUMN_DATE, date);
		values.put(LocalTransformationDB.SENSOR_METER_COLUMN_TIME, timeValue);
		values.put(LocalTransformationDB.SENSOR_METER_COLUMN_ACC, accVal);
		values.put(LocalTransformationDB.SENSOR_METER_COLUMN_LIGHT, lightVal);
		values.put(LocalTransformationDB.SENSOR_METER_COLUMN_SOUND, noiseVal);
		values.put(LocalTransformationDB.SENSOR_METER_COLUMN_SLEEP, sleepVal);
		long insertId = database.insert(
				LocalTransformationDB.TABLE_SENSOR_METER, null, values);
		Log.e(TAG, "insert to SensorMeter Table at: " + insertId + " :" + date
				+ "-" + timeValue);
		return insertId != -1;
	}

	public ArrayList<TrafficData> getAllTrafficFromDate(String date, int typeId) {
		ArrayList<TrafficData> list = new ArrayList<TrafficData>();

		String q = "SELECT * FROM "
				+ LocalTransformationDB.TABLE_SENSOR_METER
				// + ";";
				+ " where( " + LocalTransformationDB.SENSOR_METER_COLUMN_DATE
				+ " like '" + date + "%')" + " ORDER BY "
				+ LocalTransformationDB.SENSOR_METER_COLUMN_TIME + ";";
		Cursor cursor = database.rawQuery(q, null);
		if (cursor.moveToFirst()) {
			do {
				double timeAxis = cursor
						.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_TIME));
				double yAxis = 0.0d;
				if (typeId == SensorsMeterService.accId)
					yAxis = cursor
							.getDouble(cursor
									.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_ACC));
				else if (typeId == SensorsMeterService.lightId)
					yAxis = cursor
							.getDouble(cursor
									.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_LIGHT));
				else if (typeId == SensorsMeterService.soundId)
					yAxis = cursor
							.getDouble(cursor
									.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_SOUND));
				else if (typeId == SensorsMeterService.sleepId)
					yAxis = cursor
							.getDouble(cursor
									.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_SLEEP));

				TrafficData trafficData = new TrafficData(date, typeId,
						timeAxis, yAxis);

				trafficData
						.setTrafficId(cursor.getInt(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_ID)));
				list.add(trafficData);
			} while (cursor.moveToNext());
		}
		cursor.close();
		return list;
	}

	/**
	 * Get data from 20:00pm of date1 to 11:00am of date2
	 * 
	 * @param date1
	 * @param date2
	 * @param typeID
	 *            type of trafficData
	 * @return
	 */
	public ArrayList<TrafficData> getTrafficsBetweenDate(String date1,
			String date2, int typeId) {
		ArrayList<TrafficData> list = new ArrayList<TrafficData>();
		String q = "SELECT * FROM "
				+ LocalTransformationDB.TABLE_SENSOR_METER
				// + ";";
				+ " where " + "( "
				+ LocalTransformationDB.SENSOR_METER_COLUMN_DATE + " like '"
				+ date1 + "%' OR "
				+ LocalTransformationDB.SENSOR_METER_COLUMN_DATE + " like '"
				+ date2 + "%'" + ")" + " ORDER BY "
				+ LocalTransformationDB.SENSOR_METER_COLUMN_ID + ";";
		Cursor cursor = database.rawQuery(q, null);
		if (cursor.moveToFirst()) {
			do {
				Double clockTime = cursor
						.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_TIME));
				String clockDate = cursor
						.getString(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_DATE));
				// FIXME
				boolean isValidData = (date1.equals(date2)) ? (clockTime >= 20.0d && clockDate
						.equals(date1)) : (clockTime >= 20.0d && clockDate
						.equals(date1))
						|| (clockTime <= 11.0d && clockDate.equals(date2));

				if (isValidData) {
					double yAxis = 0.0d;
					if (typeId == SensorsMeterService.accId)
						yAxis = cursor
								.getDouble(cursor
										.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_ACC));
					else if (typeId == SensorsMeterService.lightId)
						yAxis = cursor
								.getDouble(cursor
										.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_LIGHT));
					else if (typeId == SensorsMeterService.soundId)
						yAxis = cursor
								.getDouble(cursor
										.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_SOUND));
					else if (typeId == SensorsMeterService.sleepId)
						yAxis = cursor
								.getDouble(cursor
										.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_SLEEP));

					TrafficData trafficData = new TrafficData(clockDate,
							typeId, clockTime, yAxis);

					trafficData
							.setTrafficId(cursor.getInt(cursor
									.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_ID)));
					list.add(trafficData);
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
		return list;
	}

	public ArrayList<SuperFourCoordinate> getAllFourAllDb() {
		// for recalculate sleepPby
		ArrayList<SuperFourCoordinate> sfc = new ArrayList<SuperFourCoordinate>();

		String q = "SELECT * FROM " + LocalTransformationDB.TABLE_SENSOR_METER
				+ " ORDER BY " + LocalTransformationDB.SENSOR_METER_COLUMN_TIME
				+ ";";
		Cursor cursor = database.rawQuery(q, null);
		if (cursor.moveToFirst()) {
			do {
				int id = cursor
						.getInt(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_ID));
				double acc = cursor
						.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_ACC));
				double light = cursor
						.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_LIGHT));
				double sound = cursor
						.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_SOUND));
				double sleep = cursor
						.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_SLEEP));

				SuperFourCoordinate val = new SuperFourCoordinate(
						SensorsMeterService.accId, SensorsMeterService.lightId,
						SensorsMeterService.soundId,
						SensorsMeterService.sleepId);

				val.setDb_id(id);
				val.setValue(SensorsMeterService.accId, acc);
				val.setValue(SensorsMeterService.lightId, light);
				val.setValue(SensorsMeterService.soundId, sound);
				val.setValue(SensorsMeterService.sleepId, sleep);

				sfc.add(val);
			} while (cursor.moveToNext());
		}
		cursor.close();

		return sfc;
	}

	public boolean recalcUpdateDb(int id, double sleepVal) {
		// update sleepPby after recalculate
		ContentValues values = new ContentValues();
		values.put(LocalTransformationDB.SENSOR_METER_COLUMN_SLEEP, sleepVal);

		long insertId = database.update(
				LocalTransformationDB.TABLE_SENSOR_METER, values,
				LocalTransformationDB.SENSOR_METER_COLUMN_ID + "=?",
				new String[] { String.valueOf(id) });

		Log.e(TAG, "recalc sleepPby; nrRows Effected " + insertId);
		return insertId != -1;
	}

	public String createCSVfileOfTrafficTable() {
		String csvHeader = "";
		String csvValues = "";

		for (int i = 0; i < LocalTransformationDB.COLUMNS_SENSOR_METER.length; i++) {
			if (csvHeader.length() > 0) {
				csvHeader += ",";
			}
			csvHeader += "\"" + LocalTransformationDB.COLUMNS_SENSOR_METER[i]
					+ "\"";
		}
		csvValues = csvHeader + "\n";
		String q = "SELECT * FROM " + LocalTransformationDB.TABLE_SENSOR_METER
				+ " ORDER BY " + LocalTransformationDB.SENSOR_METER_COLUMN_DATE
				+ ";";

		Cursor cursor = database.rawQuery(q, null);
		if (cursor.moveToFirst()) {
			do {
				csvValues += Integer
						.toString(cursor.getInt(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_ID)))
						+ ",";
				csvValues += cursor
						.getString(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_DATE))
						+ ",";
				csvValues += formatTimeDoubleToString(cursor
						.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_TIME)))
						+ ",";
				csvValues += cursor
						.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_ACC))
						+ ",";
				csvValues += cursor
						.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_LIGHT))
						+ ",";
				csvValues += cursor
						.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_SOUND))
						+ ",";
				csvValues += cursor
						.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_SLEEP))
						+ "\n";

			} while (cursor.moveToNext());
		}
		cursor.close();

		return csvValues;
	}

	// public String createCSVfileOfTrafficTable(){
	// String csvHeader = "";
	// String csvValues = "";
	//
	// for (int i = 0; i < LocalTransformationDB.COLUMNS_TRAFFIC_MON.length;
	// i++) {
	// if (csvHeader.length() > 0) {
	// csvHeader += ",";
	// }
	// csvHeader += "\"" + LocalTransformationDB.COLUMNS_TRAFFIC_MON[i] + "\"";
	// }
	// csvValues = csvHeader + "\n";
	// String q = "SELECT * FROM "
	// + LocalTransformationDB.TABLE_TRAFFIC_MON
	// + " ORDER BY " + LocalTransformationDB.COLUMN_TYPE + ";";
	//
	// Cursor cursor = database.rawQuery(q, null);
	// if (cursor.moveToFirst()) {
	// do {
	// csvValues +=
	// Integer.toString(cursor.getInt(cursor.getColumnIndex(LocalTransformationDB.COLUMN_ID)))
	// + ",";
	// csvValues += cursor.getString(cursor
	// .getColumnIndex(LocalTransformationDB.COLUMN_DATE_TEXT))
	// + ",";
	// csvValues += cursor.getInt(cursor
	// .getColumnIndex(LocalTransformationDB.COLUMN_TYPE))
	// + ",";
	// csvValues += formatTimeDoubleToString(cursor.getDouble(cursor
	// .getColumnIndex(LocalTransformationDB.COLUMN_X_AXIS)))
	// + ",";
	// csvValues += cursor.getDouble(cursor
	// .getColumnIndex(LocalTransformationDB.COLUMN_Y_AXIS))
	// + "\n";
	//
	// } while (cursor.moveToNext());
	// }
	// cursor.close();
	//
	// return csvValues;
	// }

	public String createCSVfileOfUserSleepDiaryTable() {
		String csvHeader = "";
		String csvValues = "";

		for (int i = 0; i < LocalTransformationDB.COLUMNS_SLEEP_TIME.length; i++) {
			if (csvHeader.length() > 0) {
				csvHeader += ",";
			}
			csvHeader += "\"" + LocalTransformationDB.COLUMNS_SLEEP_TIME[i]
					+ "\"";
		}
		csvValues = csvHeader + "\n";
		String q = "SELECT * FROM " + LocalTransformationDB.TABLE_SLEEP_TIME
				+ " ORDER BY " + LocalTransformationDB.SLEEP_TIME_COLUMN_DATE
				+ ";";

		Cursor cursor = database.rawQuery(q, null);
		if (cursor.moveToFirst()) {
			do {
				csvValues += Integer
						.toString(cursor.getInt(cursor
								.getColumnIndex(LocalTransformationDB.SLEEP_TIME_COLUMN_ID)))
						+ ",";
				csvValues += cursor
						.getString(cursor
								.getColumnIndex(LocalTransformationDB.SLEEP_TIME_COLUMN_DATE))
						+ ",";
				csvValues += formatTimeDoubleToString(cursor
						.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SLEEP_TIME_COLUMN_SLEEP)))
						+ ",";
				csvValues += formatTimeDoubleToString(cursor
						.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SLEEP_TIME_COLUMN_WAKE)))
						+ ",";
				csvValues += cursor
						.getDouble(cursor
								.getColumnIndex(LocalTransformationDB.SLEEP_TIME_COLUMN_PRBLY))
						+ "\n";

			} while (cursor.moveToNext());
		}
		cursor.close();

		return csvValues;
	}

	private String formatTimeDoubleToString(double timeDouble) {
		return new DecimalFormat("00.00").format(timeDouble).replaceAll("\\,",
				":");
	}

	public boolean addDateOfTraffic(String date, int trafficId) {

		ContentValues values = new ContentValues();
		values.put(LocalTransformationDB.COLUMN_TRAFFIC_ID, trafficId);
		values.put(LocalTransformationDB.COLUMN_DATE_TEXT, date);
		long insertId = database.insert(
				LocalTransformationDB.TABLE_DATE_TO_TRAFFIC, null, values);
		Log.e(TAG, "insert date:" + date + " at: " + insertId);
		return insertId != -1;
	}

	public ArrayList<String> getAllAvalDate() {
		ArrayList<String> list = new ArrayList<String>();
		String q = "SELECT * FROM "
				+ LocalTransformationDB.TABLE_DATE_TO_TRAFFIC + " ORDER BY "
				+ LocalTransformationDB.COLUMN_DATE_ID + ";";
		Cursor cursor = database.rawQuery(q, null);
		if (cursor.moveToFirst()) {
			do {
				String date = cursor
						.getString(cursor
								.getColumnIndex(LocalTransformationDB.COLUMN_DATE_TEXT));
				if (!list.contains(date))
					list.add(date);
			} while (cursor.moveToNext());
		}
		cursor.close();

		Collections.sort(list, new Comparator<String>() {

			@Override
			public int compare(String arg0, String arg1) {
				SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
				int compareResult = 0;
				try {
					Date arg0Date = format.parse(arg0);
					Date arg1Date = format.parse(arg1);
					compareResult = arg0Date.compareTo(arg1Date);
				} catch (ParseException e) {
					e.printStackTrace();
					compareResult = arg0.compareTo(arg1);
				}
				return compareResult;
			}
		});

		return list;
	}

	public void deleteAllTrafficRecords() {
		// drop and recreate table
		// database.execSQL("DROP TABLE IF EXISTS "
		// + LocalTransformationDB.TABLE_TRAFFIC_MON);
		// database.execSQL(LocalTransformationDB.TRAFFIC_MON_CREATE);
		// database.execSQL("DROP TABLE IF EXISTS "
		// + LocalTransformationDB.TABLE_DATE_TO_TRAFFIC);
		// database.execSQL(LocalTransformationDB.DATE_TO_TRAFFIC);
	}

	public int deleteAllTrafficFromDate(String date) {
		// database.delete(LocalTransformationDB.TABLE_DATE_TO_TRAFFIC,
		// LocalTransformationDB.COLUMN_DATE_TEXT + " like '" + date
		// + "%'", null);
		// return database.delete(LocalTransformationDB.TABLE_TRAFFIC_MON,
		// LocalTransformationDB.COLUMN_DATE_TEXT + " like '" + date
		// + "%'", null);
		return -1;
	}
}