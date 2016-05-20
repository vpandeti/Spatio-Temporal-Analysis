package com.wireless.spatiotemporal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {

	protected static final String LOG_TAG = "MainActivity";
	TelephonyManager mTelephonyManager;
	TextView mParameterTextView, mValueTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startService(new Intent(getApplicationContext(),
				SignalCaputeService.class));
		mParameterTextView = (TextView) findViewById(R.id.parameter);
		mValueTextView = (TextView) findViewById(R.id.value);
		mValueTextView.setMovementMethod(new ScrollingMovementMethod());
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mMessageReceiver, new IntentFilter("wireless"));
	}

	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String message = intent.getStringExtra("message");
			message += "\n";
			mValueTextView.append(message);
			Log.d("receiver", "Got message: " + message);
		}
	};

	PhoneStateListener mPhoneStateListener = new PhoneStateListener() {

		@Override
		public void onCellInfoChanged(List<CellInfo> cellInfo) {
			super.onCellInfoChanged(cellInfo);

		}

		@Override
		public void onCellLocationChanged(CellLocation location) {
			super.onCellLocationChanged(location);
		}

		@Override
		public void onDataActivity(int direction) {
			super.onDataActivity(direction);
		}

		@Override
		public void onDataConnectionStateChanged(int state, int networkType) {
			super.onDataConnectionStateChanged(state, networkType);
		}

		@Override
		public void onDataConnectionStateChanged(int state) {
			super.onDataConnectionStateChanged(state);
		}

		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
			super.onServiceStateChanged(serviceState);
		}

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			super.onSignalStrengthsChanged(signalStrength);

			Log.i(LOG_TAG, "onSignalStrengthsChanged: " + signalStrength);
			if (signalStrength.isGsm()) {
				Log.i(LOG_TAG, "onSignalStrengthsChanged: getGsmBitErrorRate "
						+ signalStrength.getGsmBitErrorRate());
				Log.i(LOG_TAG,
						"onSignalStrengthsChanged: getGsmSignalStrength "
								+ signalStrength.getGsmSignalStrength());
			} else if (signalStrength.getCdmaDbm() > 0) {
				Log.i(LOG_TAG, "onSignalStrengthsChanged: getCdmaDbm "
						+ signalStrength.getCdmaDbm());
				Log.i(LOG_TAG, "onSignalStrengthsChanged: getCdmaEcio "
						+ signalStrength.getCdmaEcio());
				// mValueTextView.setText("Cdma Dbm = " +
				// signalStrength.getCdmaDbm() + ", Ecio = " +
				// signalStrength.getCdmaEcio());
			} else {
				Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoDbm "
						+ signalStrength.getEvdoDbm());
				Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoEcio "
						+ signalStrength.getEvdoEcio());
				Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoSnr "
						+ signalStrength.getEvdoSnr());
			}

			// Reflection code starts from here
			try {
				Method[] methods = android.telephony.SignalStrength.class
						.getMethods();
				for (Method mthd : methods) {
					if (mthd.getName().equals("getLteSignalStrength")
							|| mthd.getName().equals("getLteRsrp")
							|| mthd.getName().equals("getLteRsrq")
							|| mthd.getName().equals("getLteRssnr")
							|| mthd.getName().equals("getLteCqi")) {
						Log.i(LOG_TAG,
								"onSignalStrengthsChanged: " + mthd.getName()
										+ " " + mthd.invoke(signalStrength));
						mValueTextView.setText("Lte Dbm = "
								+ mthd.invoke(signalStrength));
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(
				mMessageReceiver);
		super.onDestroy();
	}

}
