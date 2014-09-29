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

import android.content.Intent;
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
import com.jjoe64.graphview.BarGraphView;
import com.jjoe64.graphview.CustomLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

/**
 * Create a graph of light, motion, noise and sleep estimated from sensors 
 * @author HieuHa
 *
 */
public class GraphPlotFragment extends Fragment {

	private static final String TAG = GraphPlotFragment.class.getSimpleName();
	private View rootView;

	private ArrayList<String> avalDate;
	// private ArrayList<GraphViewData> data_line;
	// private ArrayList<GraphViewData> data_bar;

	public static String lightGrpTitle = "Light Var per min";
	private int lightGrpType = SensorsMeterService.lightId;
	private boolean lightIsBar = false;

	public static String motionGrpTitle = "Motion Var per min";
	private int motionGrpType = SensorsMeterService.accId;
	private boolean motionIsBar = false;

	private static String sleepGrpTitle = "Sleep Pattern";
	private int sleepGrpType = SensorsMeterService.sleepId;
	private boolean sleepIsBar = false;

	private static String noiseGrpTitle = "Noise Var per min";
	private int noiseGrpType = SensorsMeterService.soundId;
	private boolean noiseIsBar = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_with_graph, container,
				false);

		Log.e(TAG, ": onCreateView");

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);

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

		LinearLayout layout_light = (LinearLayout) rootView
				.findViewById(R.id.light_graph);
		LinearLayout layout_motion = (LinearLayout) rootView
				.findViewById(R.id.motion_graph);

		layout_light.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// openBig();
			}
		});
		layout_motion.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// openBig();
			}
		});
	}

	/** Open this on Landscape mode **/
	private void openBig() {
		Intent i = new Intent(this.getActivity().getApplicationContext(),
				GraphPlotBigActivity.class);
		String date = ((TextView) rootView.findViewById(R.id.at_date))
				.getText().toString();
		i.putExtra("Timy", date);
		i.putExtra("lineGraphTitle", lightGrpTitle);
		i.putExtra("lineGraphType", lightGrpType);
		i.putExtra("isFirstBarType", lightIsBar);
		i.putExtra("barGraphTitle", motionGrpTitle);
		i.putExtra("barGraphType", motionGrpType);
		i.putExtra("isSecondBarType", motionIsBar);
		this.getActivity().startActivity(i);

	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	// change date when user press back or forth button
	private void dateBackAndForth(boolean back) {
		TextView atDate = (TextView) rootView.findViewById(R.id.at_date);
		String newDate = atDate.getText().toString();
		if (back) {
			newDate = getDate(-1, atDate.getText().toString());
		} else {
			newDate = getDate(1, atDate.getText().toString());
		}

		redrawCharts(newDate);
		atDate.setText(newDate);
	}

	private void clearAllCharts() {
		// clearChart(firstGrpTitle);
		// clearChart(secondGrpTitle);
	}

	// private void clearChart(String title) {
	// GraphViewData[] dataList = new GraphViewData[1];
	// dataList[0] = new GraphViewData(0.0, 0.0);
	// GraphViewSeries gvs_series = new GraphViewSeries(dataList);
	//
	// if (title.equals(firstGrpTitle)) {
	// createGraph(firstGrpTitle, gvs_series, R.id.light_graph, firstIsBar);
	// data_line = new ArrayList<GraphView.GraphViewData>();
	// }
	// if (title.equals(secondGrpTitle)) {
	// createGraph(secondGrpTitle, gvs_series, R.id.motion_graph, secondIsBar);
	// data_bar = new ArrayList<GraphView.GraphViewData>();
	// }
	// }
	//
	private void redrawCharts(String newDate) {
		TextView atDate = (TextView) rootView.findViewById(R.id.at_date);
		atDate.setText(newDate);

		// update data according to date from database
		ArrayList<GraphViewData> light_data = updateTrafficOnDate(newDate,
				lightGrpType);
		ArrayList<GraphViewData> motion_data = updateTrafficOnDate(newDate,
				motionGrpType);
		ArrayList<GraphViewData> noise_data = updateTrafficOnDate(newDate,
				noiseGrpType);
		ArrayList<GraphViewData> sleep_data = updateTrafficOnDate(newDate,
				sleepGrpType);

		// populate missing data if the lengths are not the same
		if (light_data.size() < sleep_data.size()) {
			for (int i = sleep_data.size() - light_data.size(); i < sleep_data
					.size(); i++) {
				light_data.add(new GraphViewData(sleep_data.get(i).getX(), 0));

			}
		}
		if (motion_data.size() < sleep_data.size()) {
			for (int i = sleep_data.size() - motion_data.size(); i < sleep_data
					.size(); i++) {
				motion_data.add(new GraphViewData(sleep_data.get(i).getX(), 0));

			}
		}

		drawChart(light_data, lightGrpTitle, R.id.light_graph, lightIsBar);
		drawChart(motion_data, motionGrpTitle, R.id.motion_graph, motionIsBar);
		drawChart(noise_data, noiseGrpTitle, R.id.noise_graph, noiseIsBar);
		drawChart(sleep_data, sleepGrpTitle, R.id.sleep_pattern, sleepIsBar);

	}

	private LocalTransformationDBMS transformationDB;

	private ArrayList<GraphView.GraphViewData> updateTrafficOnDate(String date,
			int type) {
		String nextDay = getDate(1, date);
		// initialize database
		this.transformationDB = new LocalTransformationDBMS(getActivity()
				.getApplicationContext());
		transformationDB.open();
		ArrayList<TrafficData> list = transformationDB.getTrafficsBetweenDate(
				date, nextDay, type);
		ArrayList<GraphView.GraphViewData> data = new ArrayList<GraphView.GraphViewData>();
		for (TrafficData t : list) {
			// for the time range from 19.59 to 23.59 add 100 to double data
			// and for the time range from 0.00 to 10.59 add 200 to double data,
			// by that we make sure the time line begin at 19.59 and end at
			// 10.59 of the next day FIXME: it could be nicer to convert to system time in milli 
			double x_axis = t.getxValue();
			if (t.getxValue() < 11.0)
				x_axis += 200;
			else if (t.getxValue() >= 19.59)
				x_axis += 100;

			GraphViewData x = new GraphViewData(x_axis, t.getyValue());
			data.add(x);
		}
		transformationDB.close();

		return data;
	}

	private void clearDb() {
		// clear DBs of a today date
		// initialize database
		this.transformationDB = new LocalTransformationDBMS(getActivity()
				.getApplicationContext());
		transformationDB.open();
		transformationDB.deleteAllTrafficRecords();
		transformationDB.close();
	}

	private void drawChart(ArrayList<GraphViewData> data_line, String title,
			int graphR_id, boolean isBar) {
		if (data_line.size() > 0) {

			GraphViewData[] dataList = new GraphViewData[data_line.size()];
			for (int i = 0; i < data_line.size(); i++) {
				dataList[i] = data_line.get(i);
			}
			GraphViewSeries gvs_series = new GraphViewSeries(dataList);
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
			// graphView.setVerticalLabels(new String[] { "high", "mid", "low"
			// });
			// graphView.setManualYAxisBounds(11.0d, 9.0d);
		} else
			graphView = new LineGraphView(
					getActivity().getApplicationContext(), graphTitle);

		graphView.setCustomLabelFormatter(new CustomLabelFormatter() {
			@Override
			public String formatLabel(double axis_value, boolean isValueX) {
				if (isValueX) {
					// X-Axis
					// decompose x_axis from adding up before
					double value = axis_value;
					if (axis_value >= 200) {
						value = axis_value - 200;
					} else if (axis_value > 0)
						value = axis_value - 100;

					// make sure not have smth like 4:60 or 11:83 time frame!
					double whole = value;
					double fractionalPart = value % 1;
					double integralPart = value - fractionalPart;
					if (fractionalPart >= 0.60) {
						whole = integralPart + 1.0d + (fractionalPart - 0.60);
					}
					// convert (double) hour.mm to hour:mm
					return new DecimalFormat("00.00").format(whole).replaceAll(
							"\\,", ":");
				} else {
					// Y-Axis
					return new DecimalFormat("#0.00").format(axis_value);
				}
			}
		});

		// add data
		graphView.addSeries(series);
		graphView.setScrollable(false);
		// optional - activate scaling / zooming
		// graphView.setScalable(true);
		// optional - legend
		// graphView.setShowLegend(true);

		if (Rid == R.id.sleep_pattern) {
			graphView.setManualYAxisBounds(1.0d, 0.0d);
		}
		graphView.getGraphViewStyle().setNumVerticalLabels(4);
		graphView.getGraphViewStyle().setNumHorizontalLabels(0); // AUTO
		graphView.getGraphViewStyle().setTextSize(17f);
		graphView.getGraphViewStyle().setVerticalLabelsAlign(Align.CENTER);
		graphView.getGraphViewStyle().setVerticalLabelsWidth(80);

		LinearLayout layout = (LinearLayout) rootView.findViewById(Rid);
		layout.removeAllViews();
		layout.addView(graphView);
		rootView.postInvalidate();
	}

	/**
	 * 
	 * @param i
	 *            0, 1 or -1
	 * @return current, next or prev date
	 */
	private String getDate(int i, String currentDate) {
		avalDate = new ArrayList<String>();
		this.transformationDB = new LocalTransformationDBMS(getActivity()
				.getApplicationContext());
		transformationDB.open();
		avalDate = transformationDB.getAllAvalDate();
		transformationDB.close();
		if (avalDate.isEmpty()) {
			Log.e(TAG, "no data available!!");
			avalDate.add(getCurrentDate());
		}

		int x = avalDate.indexOf(currentDate);

		if (i == -2) {
			Calendar c = Calendar.getInstance();
			int hour_of_day = c.get(Calendar.HOUR_OF_DAY);
			if (hour_of_day < 20 && avalDate.size() >= 2)
				return avalDate.get(avalDate.size() - 2);
			
			return avalDate.get(avalDate.size() - 1); // last date (special
														// case)
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