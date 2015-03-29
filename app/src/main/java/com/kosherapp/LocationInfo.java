package com.kosherapp;

import android.location.Location;

public interface LocationInfo {
	Location getAppLocation();

	Double getAppLatitude();

	Double getAppLongitude();

	Location getCurrentLocation();

	Double getCurrentLatitude();

	Double getCurrentLongitude();

	Boolean getUseCurrentLocation();
}
