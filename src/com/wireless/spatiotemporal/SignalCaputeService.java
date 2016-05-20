package com.wireless.spatiotemporal;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoWcdma;
import android.telephony.CellLocation;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthWcdma;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

public class SignalCaputeService extends Service implements LocationListener {

	protected static final String LOG_TAG = "MainActivity";
	private static final int NETWORK_2G = 0;
	private static final int NETWORK_3G = 1;
	private static final int NETWORK_4G = 2;
	public static String SERVER_API_URL = "http://ec2-52-10-41-20.us-west-2.compute.amazonaws.com:8080/PlayStoreApis/Offers/";
	// public static String SERVER_API_URL =
	// "http://localhost:8080/PlayStoreApis/Offers/";

	private TelephonyManager mTelephonyManager;
	private LocationManager mLocationManager;

	private int receivedsignal;
	private int signaltype;
	private String deviceId;
	private Location location;
	private int cellid;
	private int celllac;
	protected boolean isAdded = true;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		mTelephonyManager.listen(mPhoneStateListener,
				PhoneStateListener.LISTEN_DATA_ACTIVITY
						| PhoneStateListener.LISTEN_CELL_INFO
						| PhoneStateListener.LISTEN_CELL_LOCATION
						| PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		deviceId = mTelephonyManager.getDeviceId();
		registerReceiver(new ConnectivityChangeReceiver(), new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	PhoneStateListener mPhoneStateListener = new PhoneStateListener() {

		@Override
		public void onCellInfoChanged(List<CellInfo> cellInfo) {
			super.onCellInfoChanged(cellInfo);

		}

		@Override
		public void onCellLocationChanged(CellLocation cell) {
			super.onCellLocationChanged(cell);
			GsmCellLocation loc = (GsmCellLocation) cell;
			cellid = loc.getCid();
			celllac = loc.getLac();
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
			try {
				String message = "";
				Intent intent = new Intent("wireless");
				Log.i(LOG_TAG, "onSignalStrengthsChanged: " + signalStrength);

				String type = getNetworkType(getApplicationContext());
				for (final CellInfo info : mTelephonyManager.getAllCellInfo()) {
					if (info instanceof CellInfoGsm) {
						final CellSignalStrengthGsm gsm = ((CellInfoGsm) info)
								.getCellSignalStrength();
						receivedsignal = gsm.getAsuLevel();
						signaltype = NETWORK_2G;
						message = "Network type - 2G, Signalstrength: "
								+ receivedsignal;
						break;
					} else if (info instanceof CellInfoCdma) {
						final CellSignalStrengthCdma cdma = ((CellInfoCdma) info)
								.getCellSignalStrength();
						receivedsignal = cdma.getAsuLevel();
						signaltype = NETWORK_3G;
						message = "Network type - 3G, Signalstrength: "
								+ receivedsignal;
						break;
					} else if (info instanceof CellInfoLte) {
						final CellSignalStrengthLte lte = ((CellInfoLte) info)
								.getCellSignalStrength();
						//String[] lteString = lte.toString().split(" ");
						receivedsignal = lte.getAsuLevel();
						//receivedsignal = Integer.valueOf(lteString[1].split("=")[1]);
						signaltype = NETWORK_4G;
						message = "Network type - 4G, Signalstrength: "
								+ receivedsignal;
						break;
					} else if (info instanceof CellInfoWcdma) {
						final CellSignalStrengthWcdma wcdma = ((CellInfoWcdma) info)
								.getCellSignalStrength();
						receivedsignal = wcdma.getAsuLevel();
						signaltype = NETWORK_3G;
						message = "Network type - 3G, Signalstrength: "
								+ receivedsignal;
						break;
					} else {
						throw new Exception("Unknown type of cell signal!");
					}
				}
				
				/*if (signalStrength.isGsm()) {
					if(signalStrength.getGsmSignalStrength() < 99) {
						Log.i(LOG_TAG,
								"onSignalStrengthsChanged: getGsmBitErrorRate "
										+ signalStrength.getGsmBitErrorRate());
						Log.i(LOG_TAG,
								"onSignalStrengthsChanged: getGsmSignalStrength "
										+ signalStrength.getGsmSignalStrength());
						receivedsignal = signalStrength.getGsmSignalStrength();
					} else {
						String s = String.valueOf(signalStrength.getClass().getMethod("getLevel", null).invoke(signalStrength, null));
						receivedsignal = Integer.valueOf(s);
					}
					signaltype = NETWORK_2G;
					message = "Network type - 2G, Signalstrength: "
							+ receivedsignal;
				} else if (signalStrength.getCdmaDbm() > 0) {
					Log.i(LOG_TAG, "onSignalStrengthsChanged: getCdmaDbm "
							+ signalStrength.getCdmaDbm());
					Log.i(LOG_TAG, "onSignalStrengthsChanged: getCdmaEcio "
							+ signalStrength.getCdmaDbm());
					receivedsignal = signalStrength.getEvdoDbm();
					signaltype = NETWORK_3G;
					message = "Network type - 3G, Signalstrength in dBm: "
							+ receivedsignal;
				} else if(signalStrength.getEvdoDbm() > 0){
					Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoDbm "
							+ signalStrength.getEvdoDbm());
					Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoEcio "
							+ signalStrength.getEvdoEcio());
					Log.i(LOG_TAG, "onSignalStrengthsChanged: getEvdoSnr "
							+ signalStrength.getEvdoSnr());
					receivedsignal = signalStrength.getEvdoDbm();
					signaltype = NETWORK_3G;
					message = "Network type - 3G, Signalstrength in dBm: "
							+ receivedsignal;
				} else {
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
										"onSignalStrengthsChanged: "
												+ mthd.getName() + " "
												+ mthd.invoke(signalStrength));
	
								if (mthd.getName().equals("getLteSignalStrength")) {
									receivedsignal = (Integer) mthd
											.invoke(signalStrength);
									message = "Network type - 4G, Signalstrength: "
											+ receivedsignal;
									signaltype = NETWORK_4G;
									break;
								}
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
				}*/

				if (isAdded) {
					new AddConnectivityLogTask().execute();
					isAdded = false;
				}
				intent.putExtra("message", message);
				LocalBroadcastManager.getInstance(SignalCaputeService.this)
						.sendBroadcast(intent);
				Log.i(LOG_TAG, "onSignalStrengthsChanged: " + signalStrength);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};

	private class AddConnectivityLogTask extends AsyncTask<Void, Void, Void> {
		private static final String TAG = "OffersFetcher";
		public final String SERVER_URL = SERVER_API_URL + "addConnectivityLog?";
		private String currentDateandTime;
		private SimpleDateFormat sdf;

		@Override
		protected Void doInBackground(Void... params) {
			try {
				// Create an HTTP client
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
						.permitAll().build();
				StrictMode.setThreadPolicy(policy);
				HttpClient client = new DefaultHttpClient();
				sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
				currentDateandTime = sdf.format(new Date());
				getLocationUpdate();
				double latitude = 0;
				double longitude = 0;
				if (null != location) {
					latitude = location.getLatitude();
					longitude = location.getLongitude();
				}
				String url = SERVER_URL + "mac=" + deviceId
						+ "&receivedsignal=" + receivedsignal
						+ "&receivedtime=" + currentDateandTime + "&location="
						+ latitude + "," + longitude + "&signaltype="
						+ signaltype
						+ "&cellid=" + cellid + 
						"&celllac=" + celllac;
				 
				Log.d(TAG, url);
				HttpGet post = new HttpGet(url);
				// Perform the request and check the status code
				HttpResponse response = client.execute(post);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					StringBuilder value = new StringBuilder();
					BufferedReader reader = null;
					String line = null;
					try {
						reader = new BufferedReader(new InputStreamReader(
								content));
						while (null != (line = reader.readLine()))
							value.append(line);
						int status = response.getStatusLine().getStatusCode();
						if (status == HttpStatus.SC_OK) {
							Log.d(TAG, "Successfully logged");
						} else {
							Log.d(TAG, "Logging unsuccessful");
						}

					} catch (Exception ex) {
						Log.e(TAG, "Failed to parse JSON due to: " + ex);
					} finally {
						reader.close();
					}
				} else {
					Log.e(TAG, "Server responded with status code: "
							+ statusLine.getStatusCode());
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void v) {
			super.onPostExecute(v);
			isAdded = true;
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		this.location = location;
	}

	@Override
	public void onProviderDisabled(String provider) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	private void getLocationUpdate() {
		try {
			boolean isGPSEnabled = mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);

			// getting network status
			boolean isNetworkEnabled = mLocationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

			if (!isGPSEnabled && !isNetworkEnabled) {
				// no network provider is enabled
			} else {
				if (isNetworkEnabled) {
					Log.d("activity", "LOC Network Enabled");
					if (mLocationManager != null) {
						location = mLocationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							Log.d("activity", "LOC by Network");
						}
					}
				}
				// if GPS Enabled get lat/long using GPS Services
				if (isGPSEnabled) {
					if (location == null) {
						Log.d("activity", "RLOC: GPS Enabled");
						if (mLocationManager != null) {
							location = mLocationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								Log.d("activity", "RLOC: loc by GPS");
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getNetworkType(Context context) {
		TelephonyManager mTelephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		int networkType = mTelephonyManager.getNetworkType();
		switch (networkType) {
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_EDGE:
		case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_1xRTT:
		case TelephonyManager.NETWORK_TYPE_IDEN:
			return "2G";
		case TelephonyManager.NETWORK_TYPE_UMTS:
		case TelephonyManager.NETWORK_TYPE_EVDO_0:
		case TelephonyManager.NETWORK_TYPE_EVDO_A:
		case TelephonyManager.NETWORK_TYPE_HSDPA:
		case TelephonyManager.NETWORK_TYPE_HSUPA:
		case TelephonyManager.NETWORK_TYPE_HSPA:
		case TelephonyManager.NETWORK_TYPE_EVDO_B:
		case TelephonyManager.NETWORK_TYPE_EHRPD:
		case TelephonyManager.NETWORK_TYPE_HSPAP:
			return "3G";
		case TelephonyManager.NETWORK_TYPE_LTE:
			return "4G";
		default:
			return "Unknown";
		}
	}

	class ConnectivityChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			debugIntent(intent, "grokkingandroid");
		}

		private void debugIntent(Intent intent, String tag) {
			Log.v(tag, "action: " + intent.getAction());
			Log.v(tag, "component: " + intent.getComponent());
			Bundle extras = intent.getExtras();
			if (extras != null) {
				for (String key : extras.keySet()) {
					Log.v(tag, "key [" + key + "]: " + extras.get(key));
				}
			} else {
				Log.v(tag, "no extras");
			}
		}
	}
}
