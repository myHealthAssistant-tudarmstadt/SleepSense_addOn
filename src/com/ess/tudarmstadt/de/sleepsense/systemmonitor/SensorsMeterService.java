package com.ess.tudarmstadt.de.sleepsense.systemmonitor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.ess.tudarmstadt.de.sleepsense.database.LocalTransformationDBMS;
import com.ess.tudarmstadt.de.sleepsense.database.SuperFourCoordinate;
import com.ess.tudarmstadt.de.sleepsense.main.MainFragment;
import com.ess.tudarmstadt.de.sleepsense.utils.UtilsTools;
import com.ess.tudarmstadt.de.sleepsense.IRemoteService;
import com.ess.tudarmstadt.de.sleepsense.R;

import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.services.SystemMonitor;

/**
 * This is main service which listen to change in 
 * Accelerometer, Light meter and Noise recorder
 * @author HieuHa
 * 
 */
public class SensorsMeterService extends Service implements SensorEventListener {
	public static final String timeFormat = "yyyy-MM-dd_kk:mm:ss";

	private static final String TAG = SensorsMeterService.class.getSimpleName();
	private static int A_SECOND_IN_MILLIS = 1000;
	private static final double accThreshold = 0.01;
	private static final double noiseThreshold = 0.5;
	private static final double lightThreshold = 0.5;

	private static Handler soundScheduleHandler;
	private static Handler sensorScheduleHandler;
	private Handler mServiceHandler;
	private SoundMeter mSoundMeter;
	private ArrayList<Double> sounds_per_min;
	public static final int accId = Sensor.TYPE_ACCELEROMETER;
	public static final int lightId = Sensor.TYPE_LIGHT;
	public static final int soundId = 777;
	public static final int sleepId = 888;

	private ArrayList<SuperFourCoordinate> list_sensor_data;

	private int maxSizeToDB;
	private int waiting_period;
	private int batLvlCounter;
	private int soundMeterDelay;

	private int batteryPercent;
	private BroadcastReceiver batteryScreenReceiver;

	private boolean isRunning;
	boolean enNoiseable;
	boolean enAccable;
	boolean enLightable;

	// for receiving reading events:
	// private ReadingEventReceiver myReadingReceiver;
	private SensorManager mSensorManager;

	private IRemoteService.Stub coreRemoteServiceStub = new IRemoteService.Stub() {

		@Override
		public void doServiceTask() throws RemoteException {
			Log.i(TAG, "hello I'm a service");
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return coreRemoteServiceStub;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.e(TAG, "onCreate!");

		// For connect from mHH
		// myReadingReceiver = new ReadingEventReceiver();
		// IntentFilter inFil = new IntentFilter();
		// inFil.addAction(SensorReadingEvent.ACCELEROMETER_ON_DEVICE);
		// inFil.addAction(SensorReadingEvent.ACCELEROMETER_IN_G);
		// inFil.addAction(SensorReadingEvent.AMBIENT_LIGHT);
		// this.registerReceiver(myReadingReceiver, inFil);

		// IntentFilter battFil = new IntentFilter();
		// battFil.addAction(Intent.ACTION_SCREEN_ON);
		// battFil.addAction(Intent.ACTION_SCREEN_OFF);
		// battFil.addAction(Intent.ACTION_BATTERY_CHANGED);
		//
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Toast.makeText(this, "Sensor Meter Service starting",
				Toast.LENGTH_SHORT).show();
		Log.e(TAG, "Sensor Meter Service starting");
		UtilsTools.logDebug(TAG, "Sensor Meter Service starting");

		IntentFilter battFil = new IntentFilter();
		battFil.addAction(Intent.ACTION_SCREEN_ON);
		battFil.addAction(Intent.ACTION_SCREEN_OFF);
		battFil.addAction(Intent.ACTION_BATTERY_CHANGED);

		batteryScreenReceiver = new BatScreenReceiver();
		batteryPercent = 0;
		isRunning = false;
		this.registerReceiver(batteryScreenReceiver, battFil);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		if (!pm.isScreenOn()) {
			startSensorMeters();
		}

		return systemNotice();
	}

	private int systemNotice() {
		int t = START_STICKY; // START_REDELIVER_INTENT
		Log.e(SystemMonitor.class.getSimpleName(),
				"call me redundant BABY!  onStartCommand service");

		int myID = android.os.Process.myPid();
		// The intent to launch when the user clicks the expanded notification
		Intent intent = new Intent(this, MainFragment.class);
		// Intent intent = new Intent();
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendIntent = PendingIntent
				.getActivity(this, 0, intent, 0);

		Notification notice = new Notification.Builder(getApplicationContext())
				.setSmallIcon(R.drawable.ic_launcher)
				.setWhen(System.currentTimeMillis())
				.setContentTitle(getPackageName())
				.setContentText("Sensors're running")
				.setContentIntent(pendIntent).build();

		notice.flags |= Notification.FLAG_AUTO_CANCEL;
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(myID, notice);

		startForeground(myID, notice);
		return t;
	}

	// private void connectToMyHH() {
	// FIXME if needed
	// }

	private class BatScreenReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Log.e(TAG, "screen's off");
				startSensorMeters();
			} else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
				Log.e(TAG, "screen's on");
				stopSensorMeters(false);
			} else if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				batteryPercent = (level * 100) / scale;
			}
		}
	}

	@Override
	public void onDestroy() {
		Log.e(TAG, "onDestroy");
		stopSensorMeters(false);

		// unregister the screen checker
		this.unregisterReceiver(batteryScreenReceiver);

		// force start service again if the service still in runtime
		if (UtilsTools.isServiceInRuntime()) {
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			boolean enSensorMeter = pref.getBoolean(getApplicationContext()
					.getString(R.string.settings_time_frame), false);
			if (enSensorMeter) {
				mServiceHandler = new Handler();
				mServiceHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						forceStartService();
					}
				}, 65 * 1000);
			}
		}
		// stop foreground service
		this.stopForeground(true);
		super.onDestroy();
	}


	private void forceStartService() {
		PowerManager pm = (PowerManager) this.getApplicationContext()
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, TAG);

		wl.acquire();
		UtilsTools.logDebug(TAG, "force-start Service");
		Intent i = new Intent(this.getApplicationContext(),
				SensorsMeterService.class);
		this.startService(i);
		wl.release();
	}

	private double accPby, waitPby, ligPby, nosiPby;

	private void startSensorMeters() {
		if (!isRunning) {
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());

			enNoiseable = pref.getBoolean(
					getApplicationContext().getString(
							R.string.settings_noise_sensor), false);
			enAccable = pref.getBoolean(
					getApplicationContext().getString(
							R.string.settings_acc_sensor), false);
			enLightable = pref.getBoolean(
					getApplicationContext().getString(
							R.string.settings_ambient_light_sensor), false);

			// divide the probability threshold
			// based on those are enabled
			waitPby = 0.5d;
			int c = 0;
			if (enAccable)
				c = c + 1;
			if (enLightable)
				c = c + 1;
			if (enNoiseable)
				c = c + 1;

			if (c > 0)
				accPby = ligPby = nosiPby = 0.5 / c;
			else
				this.stopSelf();

			Log.e(TAG, "Start Analysis! - options:" + c);
			UtilsTools.logDebug(TAG, "Start Analysis! - options:" + c);
			
			// FIXME if Needed:
			// maximum time waited before data's being stored to db
			maxSizeToDB = 10;
			batLvlCounter = 0;
			waiting_period = 45; // minutes
			list_sensor_data = new ArrayList<SuperFourCoordinate>(10);

			// start noise recorder
			if (enNoiseable) {
				Log.e(TAG, "start audio recorder");
				soundScheduleHandler = new Handler();
				soundMeterDelay = Math.round(A_SECOND_IN_MILLIS * 15); // getAmplitude
																		// 4
				// times per min

				sounds_per_min = new ArrayList<Double>(soundMeterDelay
						* maxSizeToDB);

				mSoundMeter = new SoundMeter();
				mSoundMeter.start();
				mSoundTask.run();
			}

			// start acc and light Meters
			mSensorManager = (SensorManager) getApplicationContext()
					.getSystemService(Context.SENSOR_SERVICE);
			if (enAccable) {
				Sensor mSensor = mSensorManager
						.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
				mSensorManager.registerListener(this, mSensor,
						SensorManager.SENSOR_DELAY_NORMAL);
			}

			if (enLightable) {
				Sensor mSensor = mSensorManager
						.getDefaultSensor(Sensor.TYPE_LIGHT);
				mSensorManager.registerListener(this, mSensor,
						SensorManager.SENSOR_DELAY_NORMAL);
			}

			if (enLightable || enAccable) {
				sensorScheduleHandler = new Handler();
				sensorMeasureTask.run();

			}
		}
	}

	private void stopSensorMeters(boolean toRestart) {
		if (isRunning) {
			Log.e(TAG, "Stop Analysis!");

			if (soundScheduleHandler != null) {
				soundScheduleHandler.removeCallbacks(mSoundTask);
			}
			if (mSoundMeter != null) {
				mSoundMeter.stop();
			}

			if (sensorScheduleHandler != null) {
				sensorScheduleHandler.removeCallbacks(sensorMeasureTask);
			}

			if (mSensorManager != null) {
				mSensorManager.unregisterListener(this);
			}

			if (list_sensor_data.size() > 0)
				addAllListsToDb();

			String t = getTimestamp() + "; Battery at: " + batteryPercent
					+ "%\n";
			t += "---------------------\n";
			UtilsTools.writeStringToLogFile("SystemBatteryMonitor.txt", t);

			isRunning = false;
			
			if (toRestart){
				startSensorMeters();
			}
		}
	}

	private Runnable mSoundTask = new Runnable() {
		public void run() {
			if (mSoundMeter != null)
				sounds_per_min.add(mSoundMeter.getAmplitude());

			soundScheduleHandler.postDelayed(mSoundTask, soundMeterDelay);

		}
	};

	private double getMean(ArrayList<Double> values) {
		double mean = 0.0d;
		int n = values.size();
		for (double b : values) {
			mean += b;
		}
		mean = mean / n;
		return mean;
	}

	private double getVariance(ArrayList<Double> values) {
		double variance = 0.0d;
		int n = values.size();
		double mean = getMean(values);
		for (double b : values) {
			variance += (mean - b) * (mean - b);
		}
		variance = variance / n;
		return variance;
	}

	/**
	 * in case of sensors go to sleep after stand by on some devices
	 */
	private void forceRestartSensorMeters() {
		// use partial wake lock to stop and restart the sensor
		PowerManager pm = (PowerManager) this.getApplicationContext()
				.getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wl = pm.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, TAG);

		UtilsTools.logDebug(TAG, "Force restart SensorMeter!");
		wl.acquire();
		stopSensorMeters(true);
		wl.release();
	}

	private Runnable sensorMeasureTask = new Runnable() {

		@Override
		public void run() {
			// run each minute to calculate the variance of gathering
			// data

			// check if sensors still acquire data
			if (isRunning) {
				if ((enAccable && (accValues == null || accValues.size() == 0))
						|| (enLightable && (lightValues == null || lightValues
								.size() == 0))
						|| (enNoiseable && (sounds_per_min == null || sounds_per_min
								.size() == 0))) {
					forceRestartSensorMeters();
					return;
				}

				String timeStamp = getTimestamp();
				SuperFourCoordinate fourMeter = new SuperFourCoordinate(accId,
						lightId, soundId, sleepId);
				fourMeter.setTimeAxis(timeStamp);

				double sleepPby = 0.0d;

				if (accValues != null && accValues.size() > 0) {
					double lastMinAccValue = getVariance(accValues);
					fourMeter.setValue(accId, lastMinAccValue);

					if (lastMinAccValue < accThreshold) {
						sleepPby = sleepPby + accPby;
						if (waiting_period > 0) {
							waiting_period--;
						} else if (waiting_period == 0) {
							sleepPby = sleepPby + waitPby;
						}
					} else {
						// reset if motion detect
						waiting_period = 45;
						sleepPby = 0;
					}
					// clear old data
					accValues.clear();
				}

				if (lightValues != null && lightValues.size() > 0) {
					double lastMinLightValue = getMean(lightValues);
					fourMeter.setValue(lightId, lastMinLightValue);

					if (lastMinLightValue < lightThreshold)
						sleepPby = sleepPby + ligPby;
					// clear old data
					lightValues.clear();
				}

				if (sounds_per_min != null && sounds_per_min.size() > 0) {
					double lastMinNoiseValue = getVariance(sounds_per_min);
					fourMeter.setValue(soundId, lastMinNoiseValue);

					if (lastMinNoiseValue < noiseThreshold)
						sleepPby = sleepPby + nosiPby;
					// clear old data
					sounds_per_min.clear();
				}

				fourMeter.setValue(sleepId, sleepPby);

				list_sensor_data.add(fourMeter);
				// store list to database when the size grow enough
				if (list_sensor_data.size() > maxSizeToDB) {
					addAllListsToDb();
				}

				// write the batteryLevel to file each 30min
				batLvlCounter++;
				if (batLvlCounter > 30) {
					String t = timeStamp + "; Battery at: " + batteryPercent
							+ "%\n";
					UtilsTools.writeStringToLogFile("SystemBatteryMonitor.txt",
							t);
					batLvlCounter = 0;
				}

				/* log each minute */
				Log.e(TAG, "min reading: light= " + fourMeter.getValue(lightId)
						+ "; Acc= " + fourMeter.getValue(accId) + "; Noise= "
						+ fourMeter.getValue(soundId) + "; SleepPrbl="
						+ sleepPby);
			}
			
			sensorScheduleHandler.postDelayed(sensorMeasureTask,
					A_SECOND_IN_MILLIS * 60);
		}
	};

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {

	}

	ArrayList<Double> accValues;
	ArrayList<Double> lightValues;

	@Override
	public void onSensorChanged(SensorEvent event) {

		int mSensorType = event.sensor.getType();
		if (mSensorType == Sensor.TYPE_ACCELEROMETER) {
			double val = (Math.sqrt(event.values[0] * event.values[0]
					+ event.values[1] * event.values[1] + event.values[2]
					* event.values[2]));

			if (accValues == null)
				accValues = new ArrayList<Double>();

			accValues.add(val);

			if (!isRunning) {
				Toast.makeText(
						getApplicationContext(),
						"SleepSense Debug: Running! You can close the app now!!",
						Toast.LENGTH_SHORT).show();

				isRunning = true;
			}
		} else if (mSensorType == Sensor.TYPE_LIGHT) {

			if (lightValues == null)
				lightValues = new ArrayList<Double>();

			lightValues
					.add(Double.parseDouble(String.valueOf(event.values[0])));

			if (!isRunning) {
				Toast.makeText(
						getApplicationContext(),
						"SleepSense Debug: Running! You can close the app now!!",
						Toast.LENGTH_SHORT).show();

				isRunning = true;
			}
		}
	}

	private void addAllListsToDb() {
		ArrayList<String> dateToAdd = new ArrayList<String>();
		LocalTransformationDBMS transformationDB = new LocalTransformationDBMS(
				getApplicationContext());
		transformationDB.open();
		ArrayList<String> databaseListData = transformationDB.getAllAvalDate();

		// add to traffic table
		for (SuperFourCoordinate f : list_sensor_data) {
			String date = getDayFromDate(f.getTimeAxis(), timeFormat,
					"dd-MM-yyyy");
			double timeDouble = convertTimeToDouble(f.getTimeAxis(),
					timeFormat, "kk.mm");

			transformationDB.addAllFour(date, timeDouble, f.getValue(accId),
					f.getValue(lightId), f.getValue(soundId),
					f.getValue(sleepId));

			if (!databaseListData.contains(date) && !dateToAdd.contains(date)) {
				dateToAdd.add(date);
			}
		}

		// clear lists
		list_sensor_data.clear();

		for (String s : dateToAdd) {
			transformationDB.addDateOfTraffic(s, -1);
		}
		transformationDB.close();
	}

	private String getDayFromDate(String timeOfMeasurement, String dateFormat,
			String applyPattern) {
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

		try {
			Date today = sdf.parse(timeOfMeasurement);
			sdf.applyPattern(applyPattern);

			return sdf.format(today);

		} catch (ParseException e) {
			e.printStackTrace();
			Log.e(TAG, e.toString());
		}
		return "";
	}

	/** it actually just converts for example 13:45 to 13.45;
	 *  Be careful when changing this implementation, the 
	 *  whole thing is built on this (graphs, database)
	 * 
	 **/
	private static double convertTimeToDouble(String fullTime,
			String dateFormat, String applyPattern) {
		SimpleDateFormat fullDate = new SimpleDateFormat(dateFormat);
		SimpleDateFormat timeDate = new SimpleDateFormat(applyPattern);

		try {
			Date now = fullDate.parse(fullTime);
			String strDate = timeDate.format(now);
			double parseDate = Double.parseDouble(strDate);
			if (parseDate >= 24.00d)
				return parseDate - 24;
			return parseDate;

		} catch (ParseException e) {
			Log.e(TAG, e.toString());
			e.printStackTrace();
		}

		return Double.parseDouble("00.00");
	}

	/**
	 * Returns the current time
	 * 
	 * @return timestamp
	 */
	private String getTimestamp() {
		return (String) android.text.format.DateFormat.format(timeFormat,
				new java.util.Date());
	}
}
