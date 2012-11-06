package com.trackme.phonecomp.service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;

public class TrackMePhoneCompsService extends Service {

	private LocationManager locManager;
	private MyLocationListener mll;
	private Location currentBestLoc = null;
	private Geocoder gc;
	private List<Address> addresses = null;

	private List<Location> locs;

	private URL trackMeURL;
	private HttpURLConnection httpConn;
	private OutputStream outStr;

	private SmsManager sm;

	private Calendar cal;
	private String currentDateTimeString = "";

	private final int START_HR = 21;
	private final int STOP_HR = 24;
	private final long UPDATE_RATE = 600000;
	private final int MIN_DISP = 2;
	//	private final String PHONE_NO_DAD_IDEA = "8888945734";
	//	private final String PHONE_NO_DAD_BSNL = "9422005051";
	//private final String PHONE_NO_MOM = "8605110519";
	private final String PHONE_NO_BRO = "7709984290";
	//	private final String PHONE_NO_TUS = "9975257269";

	@Override
	public void onCreate() {
		cal = Calendar.getInstance();
		gc = new Geocoder(this);
		mll = new MyLocationListener();
		sm = SmsManager.getDefault();
		locs = new ArrayList<Location>();

		String location_context = Context.LOCATION_SERVICE;
		locManager = (LocationManager)getSystemService(location_context);

		try {
			trackMeURL = new URL("http://track-me.co.in/serve");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);

		currentBestLoc = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(currentBestLoc != null){
			int lt = (int)currentBestLoc.getLatitude();
			int lo = (int)currentBestLoc.getLongitude();
			if(lt==21 && lo==82)
				currentBestLoc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}else{
			currentBestLoc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}

		if(currentBestLoc != null){
			if(currentBestLoc.getAccuracy()<2000.0){
				currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
				sendLocationHTTP(currentBestLoc);
				if(cal.get(Calendar.HOUR_OF_DAY) >= START_HR && cal.get(Calendar.HOUR_OF_DAY) <= STOP_HR){
					sendLocationSMS(currentBestLoc);
				}
			}
		}

		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_RATE, MIN_DISP, mll);
		locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_RATE, MIN_DISP, mll);

		return START_STICKY;
	}

	@Override
	public void onStart(Intent intent, int startId) {

	}

	public class MyLocationListener implements LocationListener{

		@Override
		public void onLocationChanged(Location location) {
			if(location.getProvider().equalsIgnoreCase(LocationManager.GPS_PROVIDER)){
				if((int)location.getLatitude()!=21 && (int)location.getLatitude()!=82)
					locs.add(location);
			} else{
				locs.add(location);
			}

			if(location.getProvider().equalsIgnoreCase(LocationManager.NETWORK_PROVIDER)){
				synchronized (locs)
				{
					currentBestLoc = getBestLocation(locs);
					currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
					sendLocationHTTP(currentBestLoc);
					if(cal.get(Calendar.HOUR_OF_DAY) >= START_HR && cal.get(Calendar.HOUR_OF_DAY) <= STOP_HR){
						sendLocationSMS(currentBestLoc);
					}
					locs.clear();
				}
			}
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
	}

	/** Determines whether one Location reading is better than the current Location fix
	 * @param location  The new Location that you want to evaluate
	 * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	 */
	private boolean isBetterLocation(Location location1, Location location2) {
		if (location1 == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location2.getAccuracy() - location1.getAccuracy());
		return accuracyDelta <= 0;
	}

	private synchronized Location getBestLocation(List<Location> locs){
		if(locs.size() == 1)
			return locs.get(0);

		Location tmp = locs.get(0);
		for (int i = 0; i < locs.size()-1; ) {
			if(isBetterLocation(tmp,locs.get(++i)))
				tmp = locs.get(i);
			else
				tmp = locs.get(i-1);
		}
		return tmp;
	}

	private void sendLocationHTTP(Location location){
		String s = location.getLatitude() + "/" + location.getLongitude() + "/" + currentDateTimeString;
		try {
			httpConn = (HttpURLConnection)trackMeURL.openConnection();
			httpConn.setDoOutput(true);
			httpConn.setChunkedStreamingMode(s.getBytes().length);
			outStr = httpConn.getOutputStream();
			outStr.write(s.getBytes());
			outStr.flush();
			outStr.close();
			httpConn.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	private void sendLocationSMS(Location location){
		try {
			addresses = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			if(addresses.size() >= 0){
				String msg = "Tushar is/was near " + addresses.get(0).getAddressLine(0) + 
						"," + addresses.get(0).getAddressLine(1) + 
						"," + addresses.get(0).getAddressLine(2) + 
						" at " + currentDateTimeString; //+ " loc provider: " + location.getProvider() + " i=" + ++i;
				//sm.sendTextMessage(PHONE_NO_DAD, null, msg , null, null);
				//sm.sendTextMessage(PHONE_NO_MOM, null, msg , null, null);
				sm.sendTextMessage(PHONE_NO_BRO, null, msg , null, null);
				//sm.sendTextMessage(PHONE_NO_TUS, null, msg , null, null);				
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		locManager.removeUpdates(mll);
	}
}