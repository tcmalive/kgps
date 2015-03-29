/**
 * 
 */
package com.kosherapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * @author Josh
 * 
 */
public class LocationMapView extends Activity {

	/**
	 * 
	 */

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(Common.LOG_TAG, "in location map view");
		setContentView(R.layout.mapview);
		Log.d(Common.LOG_TAG, "setContentView");

		Intent data = getIntent();
		double longitude = data.getDoubleExtra(Common.INTENT_KEY_LOCATION_LONGITUDE,
			Double.MIN_VALUE);
		double latitude = data.getDoubleExtra(Common.INTENT_KEY_LOCATION_LATITUDE,
			Double.MIN_VALUE);
		Log.d(Common.LOG_TAG, "longitude: " + String.valueOf(longitude));
		Log.d(Common.LOG_TAG, "latitude: " + String.valueOf(latitude));

		// data.get

		Log.d(Common.LOG_TAG, "DrivingDirectionsFactory");
		Log.d(Common.LOG_TAG, ".driveTo");

		// mapView = (MapView) findViewById(R.id.mapview1);
		// mc = mapView.getController();
		//
		// String coordinates[] = { "40.747778", "-73.985556" };
		// double lat = Double.parseDouble(coordinates[0]);
		// double lng = Double.parseDouble(coordinates[1]);
		//
		// GeoPoint p = new GeoPoint((int) (lat * 1E6), (int) (lng * 1E6));
		//
		// mc.animateTo(p);
		// mc.setZoom(17);
		// mapView.invalidate();
	}

}
