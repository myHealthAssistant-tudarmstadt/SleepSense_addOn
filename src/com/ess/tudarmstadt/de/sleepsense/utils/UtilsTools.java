package com.ess.tudarmstadt.de.sleepsense.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.ess.tudarmstadt.de.sleepsense.systemmonitor.SensorsMeterService;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class UtilsTools {

	/**
	 * return the Unix timeStamp of given date also add to the DAY_OF_MONTH if
	 * there is an amountOfAddedDate
	 * 
	 * @param date
	 *            ex. 22-10-2013
	 * @param dateFormat
	 *            ex. dd-MM-yyyy
	 * @param hourOfDay
	 *            in 24hours format
	 * @param minute
	 * @param amountOfAddedDate
	 *            by default should be 0
	 * @return
	 */
	public static long timeToUnix(String date, String dateFormat,
			int hourOfDay, int minute, int amountOfAddedDate) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		Date keyDate;
		Calendar cal = Calendar.getInstance();
		try {
			keyDate = sdf.parse(date);
			cal.setTime(keyDate);
			cal.add(Calendar.DAY_OF_MONTH, amountOfAddedDate);
			cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
			cal.set(Calendar.MINUTE, minute);
		} catch (ParseException e) {
			e.printStackTrace();
			return -1;
		}

		// Log.e("timeToUnix", cal.getTime().toString());
		return (cal.getTimeInMillis() / 1000L);
	}

	public static void logDebug(String tag, String text) {
		String str = getCurrentDate(SensorsMeterService.timeFormat) + " " + tag
				+ ":\n" + text + "\n";
		writeStringToLogFile("SleepSense.log.txt", str);
	}

	public static String getCurrentDate(String format) {
		SimpleDateFormat sdfDate = new SimpleDateFormat(format);
		Date now = new Date();
		String strDate = sdfDate.format(now);
		return strDate;
	}

	public static boolean writeStringToLogFile(String outFileName, String text) {
		boolean returnCode = false;
		File exportDir = new File(Environment.getExternalStorageDirectory()
				+ "/SleepSense/data/", "");
		File file = new File(exportDir, outFileName);

		if (!exportDir.exists()) {
			exportDir.mkdirs();
		}

		try {
			if (!file.exists())
				file.createNewFile();
			FileWriter filewriter = new FileWriter(file, true);
			filewriter.append(text);
			filewriter.close();
			returnCode = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return returnCode;
	}

	public static void setAlarms(Context context) {
		WakeupAlarm wAlarm = new WakeupAlarm();
		WakeupAlarmOff offAlarm = new WakeupAlarmOff();
		Date dat = new Date();// initializes to now
		Calendar cal_now = Calendar.getInstance();
		cal_now.setTime(dat);

		// time when sleepDetector should start
		Calendar cal_alarm_on = Calendar.getInstance();
		cal_alarm_on.setTime(dat);
		cal_alarm_on.set(Calendar.HOUR_OF_DAY, 19);// set the alarm time
		cal_alarm_on.set(Calendar.MINUTE, 59);
		cal_alarm_on.set(Calendar.SECOND, 0);

		// time when sleepDetector should stop
		Calendar cal_alarm_off = Calendar.getInstance();
		cal_alarm_off.setTime(dat);
		cal_alarm_off.set(Calendar.HOUR_OF_DAY, 10);// set the alarm time
		cal_alarm_off.set(Calendar.MINUTE, 59);
		cal_alarm_off.set(Calendar.SECOND, 0);

		if (cal_now.after(cal_alarm_off) && cal_now.before(cal_alarm_on)) {
			// now is between 11:00 and 20:00
			cal_alarm_off.add(Calendar.DATE, 1); // off for the next day, not
													// today

		} else {
			if (cal_now.before(cal_alarm_off)) {
				// now is before 11:00
				cal_alarm_on.add(Calendar.DATE, -1); // using on-alarm from
														// yesterday
			} else if (cal_now.after(cal_alarm_on)) {
				// now is after 20:00
				cal_alarm_off.add(Calendar.DATE, 1); // using off-alarm for the
														// next day, not today
			}
		}

		wAlarm.setRepeatedAlarm(context, cal_alarm_on);
		offAlarm.setRepeatedAlarm(context, cal_alarm_off);
	}

	/** Check if current time is between 8pm and 11am+1day */
	public static boolean isServiceInRuntime() {
		Date dat = new Date();// initializes to now
		Calendar cal_now = Calendar.getInstance();
		cal_now.setTime(dat);

		// time when sleepDetector should start
		Calendar cal_begin_time = Calendar.getInstance();
		cal_begin_time.setTime(dat);
		cal_begin_time.set(Calendar.HOUR_OF_DAY, 19);// set the time
		cal_begin_time.set(Calendar.MINUTE, 59);
		cal_begin_time.set(Calendar.SECOND, 0);

		// time when the day end
		Calendar cal_day_end = Calendar.getInstance();
		cal_day_end.setTime(dat);
		cal_day_end.set(Calendar.HOUR_OF_DAY, 23);// set the time
		cal_day_end.set(Calendar.MINUTE, 59);
		cal_day_end.set(Calendar.SECOND, 59);

		// time when the day start
		Calendar cal_day_start = Calendar.getInstance();
		cal_day_start.setTime(dat);
		cal_day_start.set(Calendar.HOUR_OF_DAY, 0);// set the time
		cal_day_start.set(Calendar.MINUTE, 0);
		cal_day_start.set(Calendar.SECOND, 0);

		// time when sleepDetector should stop
		Calendar cal_end_time = Calendar.getInstance();
		cal_end_time.setTime(dat);
		cal_end_time.set(Calendar.HOUR_OF_DAY, 10);// set the time
		cal_end_time.set(Calendar.MINUTE, 59);
		cal_end_time.set(Calendar.SECOND, 0);

		if ((cal_now.after(cal_begin_time) && cal_now.before(cal_day_end))
				|| (cal_now.after(cal_day_start) && cal_now
						.before(cal_end_time))) {
			return true;
		} else
			return false;

	}

	public static void cancelAlarms(Context context) {
		WakeupAlarm wAlarm = new WakeupAlarm();
		WakeupAlarmOff offAlarm = new WakeupAlarmOff();

		wAlarm.cancelAlarm(context);
		offAlarm.cancelAlarm(context);

	}
}
