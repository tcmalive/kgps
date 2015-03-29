package com.kosherapp.util;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import com.kosherapp.Common;

public class Maps {
	public static Boolean showMap(Activity activity, String address) {
		String methodInfo = "<Maps.showMap(Activity, String):Boolean>";

		Common.Log(methodInfo, String.format("address: %s", address));
		
		try {
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri
				.parse(String.format("geo:0,0?q=%s", address)));
			activity.startActivity(intent);
		} catch (Exception e) {
			Common.Log(methodInfo, Common.getString(e));
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static String[] getAddress(Context context, double latitude,
		double longitude) {
		String methodInfo = "<Maps.getAddress(Context, double, double):String[]>";
		String[] streetAddress = new String[] {};
		Geocoder geocoder = new Geocoder(context);
		
		Common.Log(methodInfo, String
			.format("latitude: %s", String.valueOf(latitude)));
		Common.Log(methodInfo, String.format("longitude: %s", String
			.valueOf(longitude)));

		List<Address> addresses;
		try {
			addresses = geocoder.getFromLocation(latitude, longitude, 1);
		} catch (IOException e) {
			Common.Log(methodInfo, Common.getString(e));
			return streetAddress;
		}
		if (addresses == null) {
			Common.Log(methodInfo, String.format("addresses == null: %s", String
				.valueOf(addresses == null)));
			return streetAddress;
		}
		if (addresses.size() == 0) {
			Common.Log(methodInfo, String.format("addresses.size() == 0: %s", String
				.valueOf(addresses.size() == 0)));
			return streetAddress;
		}

		Address address = addresses.get(0);

		int maxAddressLineIndex = address.getMaxAddressLineIndex();
		streetAddress = new String[maxAddressLineIndex + 1];

		for (int i = 0; i <= maxAddressLineIndex; i++) {
			streetAddress[i] = address.getAddressLine(i);
		}

		return streetAddress;
	}

	public static final int	TWO_MINUTES	= 1000 * 60 * 2;

	/**
	 * Determines whether one Location reading is better than the current Location
	 * fix
	 * 
	 * @param location
	 *         The new Location that you want to evaluate
	 * 
	 * @param currentBestLocation
	 *         The current Location fix, to which you want to compare the new one
	 */

	public static boolean isBetterLocation(Location location,
		Location currentBestLocation) {
		if (location == null) {
			return false;
		}

		if (currentBestLocation == null) {

			// A new location is always better than no location

			return true;

		}

		// Check whether the new location fix is newer or older

		long timeDelta = location.getTime() - currentBestLocation.getTime();

		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;

		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;

		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use
		// the new location

		// because the user has likely moved

		if (isSignificantlyNewer) {

			return true;

			// If the new location is more than two minutes older, it must be
			// worse

		} else if (isSignificantlyOlder) {

			return false;

		}

		// Check whether the new location fix is more or less accurate

		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation
			.getAccuracy());

		boolean isLessAccurate = accuracyDelta > 0;

		boolean isMoreAccurate = accuracyDelta < 0;

		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider

		boolean isFromSameProvider = isSameProvider(location.getProvider(),

		currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy

		if (isMoreAccurate) {

			return true;

		} else if (isNewer && !isLessAccurate) {

			return true;

		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {

			return true;

		}

		return false;

	}

	/** Checks whether two providers are the same */

	private static boolean isSameProvider(String provider1, String provider2) {

		if (provider1 == null) {

			return provider2 == null;

		}

		return provider1.equals(provider2);

	}

}
