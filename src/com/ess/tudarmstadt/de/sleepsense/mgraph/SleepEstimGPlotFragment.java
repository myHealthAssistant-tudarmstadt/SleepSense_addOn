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

package com.ess.tudarmstadt.de.sleepsense.mgraph;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ess.tudarmstadt.de.sleepsense.R;
import com.ess.tudarmstadt.de.sleepsense.database.CookTraffic;
import com.ess.tudarmstadt.de.sleepsense.database.LocalTransformationDBMS;
import com.ess.tudarmstadt.de.sleepsense.database.TrafficData;
import com.ess.tudarmstadt.de.sleepsense.systemmonitor.SensorsMeterService;
import com.ess.tudarmstadt.de.sleepsense.utils.UtilsTools;
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewDataInterface;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.ValueDependentColor;

/**
 * @author HieuHa
 * 
 */
public class SleepEstimGPlotFragment extends Fragment {

	private static final String TAG = SleepEstimGPlotFragment.class
			.getSimpleName();
	private View rootView;

	// private ArrayList<GraphViewData> data_line;
	// private ArrayList<GraphViewData> data_bar;

	private static String sleepGrpTitle = "Sleep Pattern";
	private boolean sleepIsBar = true;

	public static final int SLEEP_THREHOLD = 26; // %

	private HashMap<Double, Integer> sleepPrbly;
	private ArrayList<TrafficData> sleepDiary;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.sleep_estim_graph_fragment,
				container, false);

		Log.e(TAG, ": onCreateView");

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);

		// initialize probability
		calcPrbly();

		Button cookBtn = (Button) rootView.findViewById(R.id.cook_up);
		cookBtn.setVisibility(View.GONE);
		cookBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CookTraffic mCook = new CookTraffic(getActivity()
						.getApplicationContext());
				mCook.cookUp();
			}
		});

		Button refrBtn = (Button) rootView.findViewById(R.id.refresh_btn);
		refrBtn.setVisibility(View.GONE);
		refrBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				redrawCharts(getCurrentDate());
			}
		});

		Button clearDB = (Button) rootView.findViewById(R.id.clear_all);
		clearDB.setVisibility(View.GONE);
		clearDB.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// clearAllCharts();
				// clearDb();

			}
		});

		// data_line = new ArrayList<GraphViewData>();
		// data_bar = new ArrayList<GraphViewData>();

		TextView atDate = (TextView) rootView.findViewById(R.id.at_date);
		atDate.setText(getCurrentDate());

		Button backBtn = (Button) rootView.findViewById(R.id.date_back);
		backBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dateBackAndForth(true);
			}
		});

		Button forthBtn = (Button) rootView.findViewById(R.id.date_next);
		forthBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dateBackAndForth(false);

			}
		});

		redrawCharts(getDate(-2, getCurrentDate()));

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	private void calcPrbly() {
		sleepPrbly = new HashMap<Double, Integer>();
		LocalTransformationDBMS transformationDB = new LocalTransformationDBMS(
				getActivity().getApplicationContext());
		transformationDB.open();
		sleepDiary = transformationDB.getAllSleepData();
		transformationDB.close();

		ArrayList<Double> timeSleep = new ArrayList<Double>();
		ArrayList<Double> timeWake = new ArrayList<Double>();
		double firstSleepTime = Double.MAX_VALUE;
		double lastWakeTime = 0.0d;
		if (!sleepDiary.isEmpty()) {
			for (int i = 0; i < sleepDiary.size(); i++) {
				// populate the list of sleep time
				double aSleep = Math
						.floor(sleepDiary.get(i).getxValue() * 10.0) / 10.0;
				double aWake = Math.floor(sleepDiary.get(i).getyValue() * 10.0) / 10.0;
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
			sleepPrbly.put(x, count);
			x = nextTenMinOf(x);
		}

		Log.e(TAG, sleepPrbly.toString());
	}

	private void clearAllCharts() {
		// clearChart(firstGrpTitle);
		// clearChart(secondGrpTitle);
	}

	private void clearDb() {
		// clear all DBs
		// initialize database
		LocalTransformationDBMS transformationDB = new LocalTransformationDBMS(
				getActivity().getApplicationContext());
		transformationDB.open();
		transformationDB.deleteAllTrafficRecords();
		transformationDB.close();
	}

	private void dateBackAndForth(boolean back) {
		TextView atDate = (TextView) rootView.findViewById(R.id.at_date);
		String newDate = atDate.getText().toString();
		if (back) {
			newDate = getDate(-1, atDate.getText().toString());
		} else {
			newDate = getDate(1, atDate.getText().toString());
		}

		redrawCharts(newDate);
	}

	private void redrawCharts(String newDate) {
		// update data according to date from database
		ArrayList<GraphViewData> sleep_data = updateTrafficOnDate(newDate,
				SensorsMeterService.sleepId);

		TextView atDate = (TextView) rootView.findViewById(R.id.at_date);
		atDate.setText(newDate);
		drawChart(sleep_data, sleepGrpTitle, R.id.sleep_pattern, sleepIsBar);

		double aSleep = 0.0d;
		double aWake = 0.0d;
		if (!sleepDiary.isEmpty()) {
			for (int i = 0; i < sleepDiary.size(); i++) {
				String aDate = sleepDiary.get(i).getTrafficDate();
				if (aDate.equals(newDate)){
					aSleep = Math
							.floor(sleepDiary.get(i).getxValue() * 10.0) / 10.0;
					aWake = Math.floor(sleepDiary.get(i).getyValue() * 10.0) / 10.0;
					
					if (aSleep < 20.0 && i < sleepDiary.size() - 2){ //FIXME
						aSleep = Math
								.floor(sleepDiary.get(i+1).getxValue() * 10.0) / 10.0;
						aWake = Math.floor(sleepDiary.get(i+1).getyValue() * 10.0) / 10.0;
					}
					
				}
			}
		}
		TextView sleepTruth = (TextView) rootView.findViewById(R.id.sleep_truth_txt);
		sleepTruth.setText(new DecimalFormat("00.00").format(aSleep).replaceAll(
							"\\,", ":") + " - " + new DecimalFormat("00.00").format(aWake).replaceAll(
							"\\,", ":"));
	}

	private ArrayList<GraphView.GraphViewData> updateTrafficOnDate(String date,
			int type) {
		String nextDay = getDate(1, date);

		// initialize database
		LocalTransformationDBMS transformationDB = new LocalTransformationDBMS(
				getActivity().getApplicationContext());
		transformationDB.open();
		ArrayList<TrafficData> sensorSleepEstimate = transformationDB
				.getTrafficsBetweenDate(date, nextDay, type);
		// close database
		transformationDB.close();

		// initialize graph data (x-axis is time line & y-axis is value of sleep
		// in percent)
		ArrayList<GraphViewData> data = new ArrayList<GraphViewData>();

		// sensor estimate each minute - each TrafficData - the percent of sleep
		// probability
		// for 45min long with no motion from the device, so each minute add up
		// 10/4.5 percentage
		double addP = 0.0d;
		for (TrafficData t : sensorSleepEstimate) {
			if (t.getyValue() >= 0.4) // no motion
				addP += 10.0 / 4.5;
			else
				addP = 0.0;

			int slpPrbSize = sleepPrbly.size();
			double ex = 0.0d;
			for (Entry<Double, Integer> e : sleepPrbly.entrySet()) {
				if (t.getxValue() >= e.getKey()
						&& t.getxValue() < nextTenMinOf(e.getKey())) {
					ex = e.getValue();
				}
			}
			addP = (addP + ((ex / slpPrbSize) * 100)) / 2;
			
			if (addP > 100) {
				addP = 100;
			}
			// for the time range from 19.59 to 23.59 add 100 to double data
			// and for the time range from 0.00 to 10.59 add 200 to double data,
			// by that we make sure the time line begin at 19.59 and end at
			// 10.59
			// of the next day
			double x_axis = t.getxValue();
			if (t.getxValue() < 11.0)
				x_axis += 200;
			else if (t.getxValue() >= 19.59)
				x_axis += 100;

			GraphViewData x = new GraphViewData(x_axis, addP);
			data.add(x);
			Log.e(TAG, "y-Val=" + t.getyValue() + " --> " + addP);
		}

		return data;
	}

	/**
	 * return the next 10min of time convert in double (00.00)
	 * 
	 * @param time
	 * @return
	 */
	private double nextTenMinOf(double time) {
		double x = Math.round((time + 0.10) * 100.00) / 100.00;

		if (x >= Math.floor(x) + 0.60)
			// increase by one hour if x -> minute 60
			x = Math.floor(x + 1.00);

		return x;
	}

	private void drawChart(ArrayList<GraphViewData> data_line, String title,
			int graphR_id, boolean isBar) {
		if (data_line.size() > 0) {
			GraphViewData[] dataList = new GraphViewData[data_line.size()];
			for (int i = 0; i < data_line.size(); i++) {
				dataList[i] = data_line.get(i);
			}
			GraphViewSeriesStyle seriesStyle = new GraphViewSeriesStyle();
			seriesStyle.setValueDependentColor(new ValueDependentColor() {
				@Override
				public int get(GraphViewDataInterface data) {
					// the higher the more green 
//					return Color.rgb((int) ((75 / 255) * ((data.getY() / 50))),
//							(int) (255 * (data.getY() / 100)), 50);
					
					// the higher the more green (better color)
					 float hue = Float.valueOf(String.valueOf(data.getY() *
					 120 / SLEEP_THREHOLD));
					 return Color.HSVToColor(new float[]{hue, 1f, 0.5f});


					// if (data.getY() > SLEEP_THREHOLD){
					// return Color.GREEN;
					// }
					// else if (data.getY() > 30)
					// return Color.YELLOW;
					// return Color.RED;
				}
			});

			GraphViewSeries gvs_series = new GraphViewSeries("Sleep Pattern",
					seriesStyle, dataList);

			createGraph(title, gvs_series, graphR_id, isBar);
		} else {
			// clear Chart
			GraphViewData[] dataList = new GraphViewData[1];
			dataList[0] = new GraphViewData(0.0, 0.0);
			GraphViewSeries gvs_series = new GraphViewSeries(dataList);
			createGraph(title, gvs_series, graphR_id, isBar);
		}
	}

	private void createGraph(String graphTitle, GraphViewSeries series,
			final int Rid, boolean isBarChart) {
		GraphView graphView = null;

		if (isBarChart) {
			graphView = new BarGraphView(getActivity().getApplicationContext(),
					graphTitle);
			// ((BarGraphView) graphView).setColWidth(1.09f);
			// graphView.setVerticalLabels(new String[] { "high", "mid", "low"
			// });
			// graphView.setManualYAxisBounds(11.0d, 9.0d);
		} else {
			graphView = new LineGraphView(
					getActivity().getApplicationContext(), graphTitle);

		}
		// add data
		graphView.addSeries(series);
		// graphView.setScrollable(true);
		// graphView.setViewPort(0, 23);
		// optional - activate scaling / zooming
		// graphView.setScalable(true);
		// optional - legend
		// graphView.setShowLegend(true);

		graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
			@Override
			public String formatLabel(double axis_value, boolean isValueX) {
				if (isValueX) {
					// X-Axis
					// decompose x_axis from adding up before
					double value = axis_value;
					if (axis_value >= 200) {
						value = axis_value - 200;
					} else if (axis_value >= 100)
						value = axis_value - 100;

					// convert (double) hour.mm to hour:mm
					// make sure not have smth like 4:60 or 11:83 in the time frame!
					double whole = value; // FIXME
					double fractionalPart = value % 1;
					double integralPart = value - fractionalPart;
					if (fractionalPart >= 0.60) {
						whole = integralPart + 1.0d + (fractionalPart - 0.60);
					}
					if (whole % 1 > 0.60){
						String str = "This's weird!: axisVal=" + axis_value + "\n whole=" + whole
								+ "\n value=" + value + "\n fractional=" + fractionalPart + "\n";
						UtilsTools.logDebug(TAG, str);
						
					}

					return new DecimalFormat("00.00").format(whole).replaceAll(
							"\\,", ":");
				} else {
					// Y-Axis
					return ""; //new DecimalFormat("#0.0").format(axis_value);
				}
			}
		});

		graphView.getGraphViewStyle().setNumVerticalLabels(4);
		graphView.getGraphViewStyle().setNumHorizontalLabels(0); //AUTO
		graphView.getGraphViewStyle().setTextSize(17f);
		graphView.getGraphViewStyle().setVerticalLabelsAlign(Align.CENTER);
		graphView.getGraphViewStyle().setVerticalLabelsWidth(0);
		graphView.getGraphViewStyle().setGridColor(Color.WHITE);
		
		
		LinearLayout layout = (LinearLayout) rootView.findViewById(Rid);
		layout.removeAllViews();
		layout.addView(graphView);
		rootView.postInvalidate();
		layout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				openBig();
			}
		});
		
	}

	private void openBig() {
		Intent i = new Intent(this.getActivity().getApplicationContext(),
				SleepEstimGPlotBigActivity.class);
		String date = ((TextView) rootView.findViewById(R.id.at_date))
				.getText().toString();
		i.putExtra("Timy", date);
		i.putExtra("lineGraphTitle", sleepGrpTitle);
		i.putExtra("lineGraphType", SensorsMeterService.sleepId);
		i.putExtra("isFirstBarType", sleepIsBar);
		this.getActivity().startActivity(i);
	}

	/**
	 * 
	 * @param i
	 *            0, 1 or -1
	 * @return current, next or prev date
	 */
	private String getDate(int i, String currentDate) {
		ArrayList<String> avalDate = new ArrayList<String>();
		LocalTransformationDBMS transformationDB = new LocalTransformationDBMS(
				getActivity().getApplicationContext());
		transformationDB.open();
		avalDate = transformationDB.getAllAvalDate();
		transformationDB.close();
		if (avalDate.isEmpty()) {
			Log.e(TAG, "no data available!!");
			avalDate.add(getCurrentDate());
		}
		int x = avalDate.indexOf(currentDate);

		if (i == -2){
			Calendar c = Calendar.getInstance();
			int hour_of_day = c.get(Calendar.HOUR_OF_DAY);
			if (hour_of_day < 20 && avalDate.size() >= 2)
				return avalDate.get(avalDate.size() - 2);
			
			return avalDate.get(avalDate.size() - 1); // last date (special case)
		}
		
		if (x >= 0) {
			if (i == 1 && x < avalDate.size() - 1)
				return avalDate.get(x + i);

			if (i == -1 && x > 0)
				return avalDate.get(x + i);
		} else {
			if (i == -1) {
				return avalDate.get(avalDate.size() - 1); // last date
			}
			if (i == -1) {
				return avalDate.get(0); // last date
			}
		}
		
		return currentDate;

	}

	private static String getCurrentDate() {
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy");
		Date now = new Date();
		String strDate = sdfDate.format(now);
		return strDate;
	}

	@Override
	public void onStop() {
		super.onStop();
	}
}