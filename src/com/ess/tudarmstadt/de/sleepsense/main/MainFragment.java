package com.ess.tudarmstadt.de.sleepsense.main;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ess.tudarmstadt.de.sleepsense.R;
import com.ess.tudarmstadt.de.sleepsense.database.DbExportImport;
import com.ess.tudarmstadt.de.sleepsense.database.LocalTransformationDBMS;
import com.ess.tudarmstadt.de.sleepsense.mgraph.GraphPlotFragment;
import com.ess.tudarmstadt.de.sleepsense.mgraph.SleepEstimGPlotFragment;
import com.ess.tudarmstadt.de.sleepsense.usersdata.UsersDataFragment;
import com.ess.tudarmstadt.de.sleepsense.utils.UtilsTools;

import de.tudarmstadt.dvs.myhealthassistant.myhealthhub.IMyHealthHubRemoteService;

public class MainFragment extends FragmentActivity {
	public static final String TAG = MainFragment.class.getSimpleName();

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	// Activity results

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	// for connecting to the remote service of myHealthHub
	private Intent myHealthHubIntent;
	private boolean connectedToHMM;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_health_hub);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// connectToMhh();
		
		// WakeupAlarm wa = new WakeupAlarm();
		// wa.setRepeAlarm(getApplicationContext());

		DbExportImport.importDateTableIntoDb(getApplicationContext());
		boolean finish = DbExportImport
				.importSleepDiaryIntoDb(getApplicationContext());
		if (finish)
			DbExportImport.importSensorMetersIntoDb(getApplicationContext());
		
		
	}

	/** Establish connection to myHealthHub Service */
	private void connectToMhh() {
		myHealthHubIntent = new Intent(
				IMyHealthHubRemoteService.class.getName());
		this.bindService(myHealthHubIntent, myHealthAssistantRemoteConnection,
				Context.BIND_AUTO_CREATE);

	}

	private ServiceConnection myHealthAssistantRemoteConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Toast.makeText(getApplicationContext(),
					"Connected to myHealthAssistant", Toast.LENGTH_LONG).show();
			connectedToHMM = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.d(MainFragment.class.getSimpleName(), "I am disconnected.");
			connectedToHMM = false;
		}
	};

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {

			Fragment fragment;

			switch (position) {
			case 0:
				fragment = new SleepEstimGPlotFragment();
				return fragment;
			case 1:
				fragment = new GraphPlotFragment();
				return fragment;
			case 2:
				fragment = new UsersDataFragment();
				return fragment;
			default:
				return null;

			}
		}

		@Override
		public int getCount() {
			// Show 2 total pages.
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case 0:
				return "Sleep Estimate";
			case 1:
				return "Debug Graph";
			case 2:
				return "User Sleep Diary";
			}
			return null;
		}
	}

	/** Menu creation */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		
		SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		boolean one_time_calc = pref.getBoolean("one_time_recalculation_of_database", false);
		if (!one_time_calc){
			menu.findItem(R.id.action_once_recalc).setVisible(false); // FIXME if needed
		} else{
			menu.findItem(R.id.action_once_recalc).setVisible(false);
		}
		return super.onCreateOptionsMenu(menu);
	}

	/** Menu methods */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		// Preferences:
		case R.id.action_settings:
			Intent i = new Intent(getApplicationContext(),
					SleepDetectorPreference.class);
			startActivity(i);
			return true;

			// export db to file
		case R.id.action_export:
			return backupDatabaseCSV();
			
		case R.id.action_once_recalc:
			// TODO if need
			return true;
			
		}

		return false;
	}

	private Boolean backupDatabaseCSV() {
		Log.e(TAG, "backup CSV file Of TrafficTable");
		Boolean isSuccess = false;

		LocalTransformationDBMS dbAdapter = new LocalTransformationDBMS(
				this.getApplicationContext());
		dbAdapter.open();
		String csvTrafficTable = dbAdapter.createCSVfileOfTrafficTable();
		String csvSleepTable = dbAdapter.createCSVfileOfUserSleepDiaryTable();
		dbAdapter.close();
//		Log.e(TAG, csvTrafficTable);
//		Log.e(TAG, "==========");
//		Log.e(TAG, csvSleepTable);

		isSuccess = UtilsTools.writeStringToLogFile("SensorMonitorRecords.txt", csvTrafficTable);

		isSuccess &= UtilsTools.writeStringToLogFile("UserSleepDiary.txt", csvSleepTable);

		if (isSuccess){
			Toast.makeText(this.getApplicationContext(),
					"write to file successfull!", Toast.LENGTH_SHORT).show();
		} else
			Toast.makeText(this.getApplicationContext(),
					"Write to file failed! ", Toast.LENGTH_SHORT)
					.show();
		return isSuccess;
	}

	// private boolean importCsvFile(String filePath) {
	// boolean returnCode = false;
	// File file = new File(Environment.getExternalStorageDirectory()
	// + "/SleepSense/database", filePath);
	// BufferedReader br = null;
	// try {
	//
	// if (file.exists()) {
	// br = new BufferedReader(new FileReader(file));
	// String line;
	// while ((line = br.readLine()) != null) {
	//
	// // use comma as separator
	// String[] country = line.split(",");
	//
	// }
	//
	// FileWriter filewriter = new FileWriter(file, true);
	// filewriter.close();
	// Toast.makeText(this.getApplicationContext(),
	// "write to file successfull: ", Toast.LENGTH_SHORT)
	// .show();
	// returnCode = true;
	// } else {
	// Toast.makeText(this.getApplicationContext(),
	// "File not exists!", Toast.LENGTH_SHORT).show();
	// }
	// } catch (IOException e) {
	// e.printStackTrace();
	// }finally {
	// if (br != null) {
	// try {
	// br.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }
	// return returnCode;
	// }

	@Override
	public void onDestroy() {
		if (connectedToHMM) {
			this.unbindService(myHealthAssistantRemoteConnection);
			connectedToHMM = false;
		}

		if (myHealthHubIntent != null) {
			this.stopService(myHealthHubIntent);
		}
		super.onDestroy();
	}
}
