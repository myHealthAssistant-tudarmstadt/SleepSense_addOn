package com.ess.tudarmstadt.de.sleepsense.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Environment;
import android.util.Log;

/** 
 * Export/Import the database to/from SD card
 * @author HieuHa
 *
 */
public class DbExportImport {

	public static final String TAG = DbExportImport.class.getName();

	/** Directory that files are to be read from and written to **/
	protected static final File DATABASE_DIRECTORY = new File(
			Environment.getExternalStorageDirectory() + "/SleepSense/database/",
			"");

	private static final String fileName = "sleepSense.db";
	/** File path of Db to be imported **/
	protected static final File IMPORT_FILE = new File(DATABASE_DIRECTORY,
			fileName);

	public static final String PACKAGE_NAME = "com.hieuha.sleepdetector";
	public static final String DATABASE_NAME = LocalTransformationDB.DATABASE_NAME;
	private static final File DATA_DIRECTORY_DATABASE = new File(
			Environment.getDataDirectory() + "/data/" + PACKAGE_NAME
					+ "/databases/" + DATABASE_NAME);

	/**
	 * Saves the application database to the export directory under [fileName]
	 **/
	protected static boolean exportDb() {
		if (!SdIsPresent())
			return false;

		File dbFile = DATA_DIRECTORY_DATABASE;

		File exportDir = DATABASE_DIRECTORY;
		File file = new File(exportDir, fileName);

		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}

		try {
			file.createNewFile();
			copyFile(dbFile, file);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	// /**
	// * Replaces current database with the IMPORT_FILE if import database is
	// * valid and of the correct type
	// **/
	// protected static boolean restoreDb() {
	// if (!SdIsPresent())
	// return false;
	//
	// File exportFile = DATA_DIRECTORY_DATABASE;
	// File importFile = IMPORT_FILE;
	//
	// if (!checkDbIsValid(importFile))
	// return false;
	//
	// if (!importFile.exists()) {
	// Log.d(TAG, "File does not exist");
	// return false;
	// }
	//
	// try {
	// exportFile.createNewFile();
	// copyFile(importFile, exportFile);
	// return true;
	// } catch (IOException e) {
	// e.printStackTrace();
	// return false;
	// }
	// }

	/** Imports the sleep diary **/
	public static boolean importSleepDiaryIntoDb(Context ctx) {
		if (!SdIsPresent())
			return false;

		File importFile = IMPORT_FILE;

		if (!checkDbIsValid(importFile, LocalTransformationDB.TABLE_SLEEP_TIME,
				LocalTransformationDB.COLUMNS_SLEEP_TIME))
			return false;

		try {
			SQLiteDatabase sqlDb = SQLiteDatabase.openDatabase(
					importFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);

			Cursor cursor = sqlDb.query(true,
					LocalTransformationDB.TABLE_SLEEP_TIME, null, null, null,
					null, null, null, null);

			LocalTransformationDBMS dbAdapter = new LocalTransformationDBMS(ctx);
			dbAdapter.open();

			// Adds all items in cursor to current database
			if (cursor.moveToFirst()) {
				do {
					String date = cursor
							.getString(cursor
									.getColumnIndex(LocalTransformationDB.SLEEP_TIME_COLUMN_DATE));
					double sleep = cursor
							.getDouble(cursor
									.getColumnIndex(LocalTransformationDB.SLEEP_TIME_COLUMN_SLEEP));
					double wake = cursor
							.getDouble(cursor
									.getColumnIndex(LocalTransformationDB.SLEEP_TIME_COLUMN_WAKE));
					double prbly = cursor
							.getDouble(cursor
									.getColumnIndex(LocalTransformationDB.SLEEP_TIME_COLUMN_PRBLY));

					dbAdapter.addSleepData(date, sleep, wake, prbly);

				} while (cursor.moveToNext());
			}

			sqlDb.close();
			cursor.close();
			dbAdapter.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/** Imports the date table **/
	public static boolean importDateTableIntoDb(Context ctx) {
		if (!SdIsPresent())
			return false;

		File importFile = IMPORT_FILE;

		if (!checkDbIsValid(importFile,
				LocalTransformationDB.TABLE_DATE_TO_TRAFFIC, new String[] {
						LocalTransformationDB.COLUMN_DATE_ID,
						LocalTransformationDB.COLUMN_TRAFFIC_ID,
						LocalTransformationDB.COLUMN_DATE_TEXT }))
			return false;

		try {
			SQLiteDatabase sqlDb = SQLiteDatabase.openDatabase(
					importFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);

			Cursor cursor = sqlDb.query(true,
					LocalTransformationDB.TABLE_DATE_TO_TRAFFIC, null, null,
					null, null, null, null, null);

			LocalTransformationDBMS dbAdapter = new LocalTransformationDBMS(ctx);
			dbAdapter.open();

			// Adds all items in cursor to current database
			if (cursor.moveToFirst()) {
				do {
					String date = cursor
							.getString(cursor
									.getColumnIndex(LocalTransformationDB.COLUMN_DATE_TEXT));
					dbAdapter.addDateOfTraffic(date, -1);

				} while (cursor.moveToNext());
			}

			sqlDb.close();
			cursor.close();
			dbAdapter.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/** Imports the sensor meter table **/
	public static boolean importSensorMetersIntoDb(Context ctx) {
		if (!SdIsPresent())
			return false;

		File importFile = IMPORT_FILE;

		if (!checkDbIsValid(importFile,
				LocalTransformationDB.TABLE_SENSOR_METER,
				LocalTransformationDB.COLUMNS_SENSOR_METER))
			return false;

		try {
			SQLiteDatabase sqlDb = SQLiteDatabase.openDatabase(
					importFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);

			Cursor cursor = sqlDb.query(true,
					LocalTransformationDB.TABLE_SENSOR_METER, null, null, null,
					null, null, null, null);

			LocalTransformationDBMS dbAdapter = new LocalTransformationDBMS(ctx);
			dbAdapter.open();

			// Adds all items in cursor to current database
			if (cursor.moveToFirst()) {
				do {

					String date = cursor
							.getString(cursor
									.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_DATE));

					double time = cursor
							.getDouble(cursor
									.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_TIME));

					double acc_meter = cursor
							.getDouble(cursor
									.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_ACC));
					double light_meter = cursor
							.getDouble(cursor
									.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_LIGHT));
					double sound_meter = cursor
							.getDouble(cursor
									.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_SOUND));
					double sleep_estim = cursor
							.getDouble(cursor
									.getColumnIndex(LocalTransformationDB.SENSOR_METER_COLUMN_SLEEP));

					dbAdapter.addAllFour(date, time, acc_meter, light_meter,
							sound_meter, sleep_estim);
				} while (cursor.moveToNext());
			}
			sqlDb.close();
			cursor.close();
			dbAdapter.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Given an SQLite database file, this checks if the file is a valid SQLite
	 * database and that it contains all the columns represented by
	 * DbAdapter.ALL_COLUMN_KEYS
	 **/
	protected static boolean checkDbIsValid(File db, String table,
			String[] columns) {
		if (!db.exists())
			return false;
		try {
			SQLiteDatabase sqlDb = SQLiteDatabase.openDatabase(db.getPath(),
					null, SQLiteDatabase.OPEN_READONLY);

			Cursor cursor = sqlDb.query(true, table, null, null, null, null,
					null, null, null);

			// ALL_COLUMN_KEYS should be an array of keys of essential columns.
			// Throws exception if any column is missing
			for (String s : columns) {
				cursor.getColumnIndexOrThrow(s);
			}

			sqlDb.close();
			cursor.close();
		} catch (IllegalArgumentException e) {
			Log.d(TAG, "Database valid but not the right type");
			e.printStackTrace();
			return false;
		} catch (SQLiteException e) {
			Log.d(TAG, "Database file is invalid.");
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			Log.d(TAG, "checkDbIsValid encountered an exception");
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private static void copyFile(File src, File dst) throws IOException {
		FileInputStream srcInStr = new FileInputStream(src);
		FileOutputStream dstInStr = new FileOutputStream(dst);
		FileChannel inChannel = srcInStr.getChannel();
		FileChannel outChannel = dstInStr.getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
			if (srcInStr != null)
				srcInStr.close();
			if (dstInStr != null)
				dstInStr.close();
		}
	}

	/** Returns whether an SD card is present and writable **/
	public static boolean SdIsPresent() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}
}