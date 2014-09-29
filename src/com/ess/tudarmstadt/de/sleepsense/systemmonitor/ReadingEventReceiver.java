package com.ess.tudarmstadt.de.sleepsense.systemmonitor;

import java.util.ArrayList;

import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.Event;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.SensorReadingEvent;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.environmental.raw.AmbientLightEvent;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.physical.AccDeviceSensorEvent;
import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.events.sensorreadings.physical.AccSensorEventInG;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Event receiver implemented as a Android BroadcastReceiver for receiving
 * myHealthAssistant sensor reading events and Phone reading events
 */
public class ReadingEventReceiver extends BroadcastReceiver {

	private ArrayList<Double> accValues = new ArrayList<Double>();
	private ArrayList<Double> lightValues = new ArrayList<Double>();
	private boolean isRunning;
	
	public ReadingEventReceiver(){
		isRunning = false;
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {

		/* Get event type and the event itself */
		Event evt = intent.getParcelableExtra(Event.PARCELABLE_EXTRA_EVENT);
		String eventType = evt.getEventType();

		if (eventType.equals(SensorReadingEvent.ACCELEROMETER_ON_DEVICE)) {
			AccDeviceSensorEvent acc = (AccDeviceSensorEvent) evt;
			if (accValues == null) {
				accValues = new ArrayList<Double>();
			}
			double x = acc.x_mean;
			double y = acc.y_mean;
			double z = acc.z_mean;
			accValues.add(Math.sqrt(x * x + y * y + z * z));

			if (!isRunning) {
				Toast.makeText(context,
						"Running! You can close the app now",
						Toast.LENGTH_LONG).show();

				isRunning = true;
			}
		}

		if (eventType.equals(SensorReadingEvent.ACCELEROMETER_IN_G)) {
			AccSensorEventInG acc = (AccSensorEventInG) evt;
			if (accValues == null) {
				accValues = new ArrayList<Double>();
			}
			double x = acc.x_mean;
			double y = acc.y_mean;
			double z = acc.z_mean;
			accValues.add(Math.sqrt(x * x + y * y + z * z));
		}

		if (eventType.equals(SensorReadingEvent.AMBIENT_LIGHT)) {
			if (lightValues == null) {
				lightValues = new ArrayList<Double>();
			}
			AmbientLightEvent light = (AmbientLightEvent) evt;
			lightValues.add(Double.parseDouble(Float.toString(light
					.getAmbientLight())));

			if (!isRunning) {
				Toast.makeText(context,
						"Running! You can close the app now",
						Toast.LENGTH_LONG).show();

				isRunning = true;
			}
		}
	}

	public ArrayList<Double> getAccValues() {
		return accValues;
	}

	public void setAccValues(ArrayList<Double> accValues) {
		this.accValues = accValues;
	}

	public ArrayList<Double> getLightValues() {
		return lightValues;
	}

	public void setLightValues(ArrayList<Double> lightValues) {
		this.lightValues = lightValues;
	}

}
