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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class LocalTransformationDB extends SQLiteOpenHelper {
	
	private static final String TAG = LocalTransformationDB.class.getName();
	
	// TRAFFIC MONITORING
	public static final String COLUMN_DATE_ID = "dateId";
	public static final String TABLE_TRAFFIC_MON_ = "local_traffic_monitoring";
	public static final String TABLE_DATE_TO_TRAFFIC = "local_date_to_traffic";
	public static final String COLUMN_ID ="id";
	public static final String COLUMN_X_AXIS = "time_value";
	public static final String COLUMN_Y_AXIS = "type_value";
	public static final String COLUMN_DATE_TEXT = "date_value";
	public static final String COLUMN_TRAFFIC_ID = "trafficId";
	public static final String COLUMN_TYPE = "trafficType";
	
	public static final String TABLE_SLEEP_TIME ="sleep_date";
	public static final String SLEEP_TIME_COLUMN_ID ="id";
	public static final String SLEEP_TIME_COLUMN_DATE ="date";
	public static final String SLEEP_TIME_COLUMN_SLEEP ="sleep";
	public static final String SLEEP_TIME_COLUMN_WAKE ="wake";
	public static final String SLEEP_TIME_COLUMN_PRBLY ="probability";
	
	public static final String TABLE_SENSOR_METER ="local_sensor_traffic_mon";
	public static final String SENSOR_METER_COLUMN_ID ="id";
	public static final String SENSOR_METER_COLUMN_DATE ="date";
	public static final String SENSOR_METER_COLUMN_TIME ="time_value";
	public static final String SENSOR_METER_COLUMN_ACC ="acc_meter";
	public static final String SENSOR_METER_COLUMN_LIGHT ="light_meter";
	public static final String SENSOR_METER_COLUMN_SOUND ="noise_meter";
	public static final String SENSOR_METER_COLUMN_SLEEP ="sleep_meter";
	
	public static final String DATABASE_NAME = "transformations.db";
	public static final int DATABASE_VERSION = 4;
	
	public static final String[] COLUMNS_TRAFFIC_MON = {COLUMN_ID, COLUMN_DATE_TEXT, COLUMN_TYPE,
		COLUMN_X_AXIS, COLUMN_Y_AXIS};
	public static final String[] COLUMNS_SLEEP_TIME = {SLEEP_TIME_COLUMN_ID, SLEEP_TIME_COLUMN_DATE, SLEEP_TIME_COLUMN_SLEEP,
		SLEEP_TIME_COLUMN_WAKE, SLEEP_TIME_COLUMN_PRBLY};
	public static final String[] COLUMNS_SENSOR_METER = {SENSOR_METER_COLUMN_ID, SENSOR_METER_COLUMN_DATE, SENSOR_METER_COLUMN_TIME,
		SENSOR_METER_COLUMN_ACC, SENSOR_METER_COLUMN_LIGHT, SENSOR_METER_COLUMN_SOUND, SENSOR_METER_COLUMN_SLEEP };

	public static final String TRAFFIC_MON_CREATE = "CREATE TABLE "
			+ TABLE_TRAFFIC_MON_ + " (" + 
			COLUMN_ID +	" INTEGER PRIMARY KEY AUTOINCREMENT, " + 
			COLUMN_DATE_TEXT+ " TEXT, " +
			COLUMN_TYPE+ " TEXT, " +
			COLUMN_X_AXIS+	" REAL, " + 
			COLUMN_Y_AXIS+	" REAL);";
	
	public static final String CREATE_TABLE_DATE_TO_TRAFFIC = "CREATE TABLE "
			+ TABLE_DATE_TO_TRAFFIC + " (" + 
			COLUMN_DATE_ID +	" INTEGER PRIMARY KEY AUTOINCREMENT, " + 
			COLUMN_TRAFFIC_ID+ " INTEGER, " +
			COLUMN_DATE_TEXT+ " TEXT);";
	
	public static final String CREATE_SLEEP_TABLE = "CREATE TABLE "
			+ TABLE_SLEEP_TIME + " (" + 
			SLEEP_TIME_COLUMN_ID +	" INTEGER PRIMARY KEY AUTOINCREMENT, " + 
			SLEEP_TIME_COLUMN_DATE+ " TEXT, " +
			SLEEP_TIME_COLUMN_SLEEP+ " REAL," +
			SLEEP_TIME_COLUMN_WAKE+ " REAL," +
			SLEEP_TIME_COLUMN_PRBLY+ " REAL);";

	public static final String CREATE_SENSOR_METER_TABLE = "CREATE TABLE "
			+ TABLE_SENSOR_METER + " (" + 
			SENSOR_METER_COLUMN_ID +	" INTEGER PRIMARY KEY AUTOINCREMENT, " + 
			SENSOR_METER_COLUMN_DATE+ " TEXT, " +
			SENSOR_METER_COLUMN_TIME+ " REAL," +
			SENSOR_METER_COLUMN_ACC+ " REAL," +
			SENSOR_METER_COLUMN_LIGHT+ " REAL," +
			SENSOR_METER_COLUMN_SOUND+ " REAL," +
			SENSOR_METER_COLUMN_SLEEP+ " REAL);";
	
	public LocalTransformationDB(Context context) {
		super(context, DATABASE_NAME,null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		Log.i(TAG, "creating database tabelle");
//		database.execSQL(TRAFFIC_MON_CREATE);
		database.execSQL(CREATE_TABLE_DATE_TO_TRAFFIC);
		database.execSQL(CREATE_SLEEP_TABLE);
		database.execSQL(CREATE_SENSOR_METER_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		Log.e(TAG, "Upgrading database from version "+ oldVersion+ " to "+ newVersion);
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_TRAFFIC_MON_);
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_DATE_TO_TRAFFIC);
//		database.execSQL("DROP TABLE IF EXISTS " + TABLE_SLEEP_TIME);
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_SENSOR_METER);
//		onCreate(database);

		database.execSQL(CREATE_TABLE_DATE_TO_TRAFFIC);
		database.execSQL(CREATE_SENSOR_METER_TABLE);

//		if (oldVersion < 3){
//			// move data from TABLE_TRAFFIC_MON_ to SENSOR_METER_TABLE_CREATE
//			
//			String q = "SELECT * FROM "
//					+ TABLE_TRAFFIC_MON_
//					+ " ORDER BY " + LocalTransformationDB.COLUMN_TYPE + ";";
//			
//			Cursor cursor = database.rawQuery(q, null);
//			ArrayList<TrafficData> old_list = new ArrayList<TrafficData>(
//					cursor.getCount() * 4);
//			if (cursor.moveToFirst()) {
//				do {
//					TrafficData trafficData = new TrafficData(
//							cursor.getString(cursor
//									.getColumnIndex(LocalTransformationDB.COLUMN_DATE_TEXT)),
//							cursor.getInt(cursor
//									.getColumnIndex(LocalTransformationDB.COLUMN_TYPE)),
//							cursor.getDouble(cursor
//									.getColumnIndex(LocalTransformationDB.COLUMN_X_AXIS)),
//							cursor.getDouble(cursor
//									.getColumnIndex(LocalTransformationDB.COLUMN_Y_AXIS)));
//					trafficData.setTrafficId(cursor.getInt(cursor.getColumnIndex(LocalTransformationDB.COLUMN_ID)));
//					old_list.add(trafficData);
//				} while (cursor.moveToNext());
//			}
//			cursor.close();
//
//			for (TrafficData trfD : old_list){
//				if (trfD.getTrafficId() == NewSleepDetectorService.accId){
//					ContentValues values = new ContentValues();
//					values.put(LocalTransformationDB.SENSOR_METER_COLUMN_DATE, trfD.getTrafficDate());
//					values.put(LocalTransformationDB.SENSOR_METER_COLUMN_TIME, trfD.getxValue());
//					values.put(LocalTransformationDB.SENSOR_METER_COLUMN_ACC, trfD.getyValue());
//					
//					long id = database.insert(TABLE_SENSOR_METER, null, values);
//					Log.e(TAG, "insert upgrade rowID=" + id);
//				} else {
//					String whereClause = SENSOR_METER_COLUMN_DATE  + " like ? "
//							+ "AND "
//							+ SENSOR_METER_COLUMN_TIME + " =? ";
//					
//					String[] whereArgs = new String[] {trfD.getTrafficDate(), String.valueOf(trfD.getxValue())};
//					
//					if (trfD.getTrafficId() == NewSleepDetectorService.lightId){
//						ContentValues values = new ContentValues();
//						values.put(LocalTransformationDB.SENSOR_METER_COLUMN_LIGHT, trfD.getyValue());
//						int i = database.update(TABLE_SENSOR_METER, values, whereClause, whereArgs);
//						Log.e(TAG, "upgrade, nr of rows updated:" + i);
//					} 
//					else if (trfD.getTrafficId() == NewSleepDetectorService.soundId){
//						ContentValues values = new ContentValues();
//						values.put(LocalTransformationDB.SENSOR_METER_COLUMN_SOUND, trfD.getyValue());
//						int i = database.update(TABLE_SENSOR_METER, values, whereClause, whereArgs);
//						Log.e(TAG, "upgrade, nr of rows updated:" + i);
//					} 
//					else if (trfD.getTrafficId() == NewSleepDetectorService.sleepId){
//						ContentValues values = new ContentValues();
//						values.put(LocalTransformationDB.SENSOR_METER_COLUMN_SLEEP, trfD.getyValue());
//						int i = database.update(TABLE_SENSOR_METER, values, whereClause, whereArgs);
//						Log.e(TAG, "upgrade, nr of rows updated:" + i);
//					}
//				}
//			}
//		}
	}
}