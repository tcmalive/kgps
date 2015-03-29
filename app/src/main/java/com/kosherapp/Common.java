package com.kosherapp;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Spinner;

public class Common {

	final static public String	PREFS_NAME																																= "KosherAppPrefs";
	final static public String	PREFS_KEY_EMAIL																											= "email";
	final static public String	PREFS_KEY_STATE																											= "state";
	final static public String	PREFS_KEY_VERSION																									= "appversion";
	final static public String	PREFS_KEY_IS_REGISTERED																			= "isRegistered";
	final static public String	PREFS_KEY_RESTAURANTS_LOAD_ID													= "restaurantsLoadId";
	final static public String	PREFS_KEY_MIKVEHS_LOAD_ID																	= "mikvehsLoadId";
	final static public String	PREFS_KEY_MINYAN_LOAD_ID																		= "minyanLoadId";
	final static public String	PREFS_KEY_LOYALTY_LOAD_ID																	= "loyaltyLoadId";
	final static public String	PREFS_KEY_DB_UPDATE_DATE																		= "dbUpdateDate";
	final static public String	PREFS_KEY_DB_UPDATING																					= "dbUpdating";
	final static public String	PREFS_KEY_ENHANCED_DISCOUNTS														= "enhancedDiscounts";
	final static public String	PREFS_KEY_NOTIFICATION_DISCOUNTS										= "deepDiscounts";
	final static public String	PREFS_KEY_AD_MILLIS																							= "adMillis";
	final static public String	PREFS_KEY_NOTIFICATION_MILLIS													= "notificationMillis";
	final static public String	PREFS_KEY_USE_CURRENT_LOCATION												= "useCurrentLocation";
	final static public String	PREFS_KEY_LOCATION_LONGITUDE														= "locationLongitude";
	final static public String	PREFS_KEY_LOCATION_LATITUDE															= "locationLatitude";
	final static public String	PREFS_KEY_LOCATION_ADDRESS																= "locationAddress";
	final static public String	PREFS_KEY_LIST_TYPE_VALUES																= "listTypeValues";

	public final static String	GOOGLE_API_HTTP																											= "https://maps.googleapis.com/maps/api/geocode/json?";

	public final static String	INTENT_KEY_SKUS																											= "skus";
	public final static String	INTENT_KEY_NAME_IDS																							= "nameIds";
	public final static String	INTENT_KEY_MANAGEDS																							= "manageds";

	public static final String	LOG_TAG																																			= "josh";
	public static final String	INTENT_KEY_EMAIL																										= "email";
	public static final String	INTENT_KEY_STATE																										= "state";
	public static final String	INTENT_KEY_DEVICE_ID																						= "deviceId";
	public static final String	INTENT_KEY_REGISTRATION_TYPE														= "registrationType";
	public static final String	INTENT_KEY_LOCATION_NAME																		= "restaurantName";
	public static final String	INTENT_KEY_LOCATION_ADDRESS															= "restaurantAddress";
	public static final String	INTENT_KEY_LOCATION_PHONENUMBER											= "restaurantPhonenumber";
	public static final String	INTENT_KEY_LOCATION_DISTANCE														= "restaurantDistance";
	public static final String	INTENT_KEY_LOCATION_DISCOUNT														= "restaurantDiscount";
	public static final String	INTENT_KEY_LOCATION_ADVERTISEMENT									= "restaurantAdvertisement";
	public static final String	INTENT_KEY_LOCATION_IS_CURRENT_LOCATION			= "isCurrentLocation";
	public static final String	INTENT_KEY_LOCATION_LONGITUDE													= "longitude";
	public static final String	INTENT_KEY_LOCATION_LATITUDE														= "latitude";
	public static final String	INTENT_KEY_LOCATION_FOOD_TYPE													= "foodType";
	public static final String	INTENT_KEY_CURRENT_LOCATION_LONGITUDE					= "currentLocationLongitude";
	public static final String	INTENT_KEY_CURRENT_LOCATION_LATITUDE						= "currentLocationLatitude";

	public static final String	INTENT_KEY_START_LONGITUDE																= "startLongitude";
	public static final String	INTENT_KEY_START_LATITUDE																	= "startLatitude";
	public static final String	INTENT_KEY_END_LONGITUDE																		= "endLongitude";
	public static final String	INTENT_KEY_END_LATITUDE																			= "endLatitude";

	public static final String	INTENT_KEY_NAME_FILTER																				= "nameFilter";
	public static final String	INTENT_KEY_DISTANCE_FILTER																= "distanceFilter";
	public static final String	INTENT_KEY_DISCOUNT_FILTER																= "discountFilter";
	public static final String	INTENT_KEY_FOODTYPE_FILTER																= "foodTypeFilter";

	public static final String	INTENT_KEY_SETTINGS_ENHANCED_DISCOUNT					= "enhancedDiscountSettings";
	public static final String	INTENT_KEY_SETTINGS_DEEP_DISCOUNT									= "deepDiscountSettings";

	public static final int				REQUEST_CODE_REGISTRATION																	= 12345;
	public static final int				REQUEST_CODE_GOOGLE_MAPS_CODE													= 12346;
	public static final int				REQUEST_CODE_LOCATION_CHOOSER													= 12347;
	public static final int				REQUEST_CODE_FILTER_CHOOSER															= 12348;
	public static final int				REQUEST_CODE_DIRECTION_MAP_VIEW											= 12349;
	public static final int				REQUEST_CODE_SETTINGS_VIEW																= 12350;
	public static final int				REQUEST_CODE_NOTIFICATION_VIEW												= 12351;
	public static final int				REQUEST_CODE_MENU_PAGE																				= 12352;
	public static final int				REQUEST_CODE_LOYALTY_CARDS																= 12353;
	public static final int				REQUEST_CODE_LOYALTY_REDEEM_SCREEN								= 12354;

	public static final String	REGISTRATION_STATUS_ACTIVE																= "active";
	public static final String	REGISTRATION_STATUS_EXPIRED															= "expired";
	public static final String	REGISTRATION_STATUS_NOT_SUBSCRIBER								= "not_subscriber";

	public static final String	REGISTRATION_SERVICE_STATUS_SUCCESS							= "success";
	public static final String	REGISTRATION_SERVICE_STATUS_FAILURE							= "failure";

	public static final int				REGISTRATION_TYPE_UNNECESSARY													= -1;
	public static final int				REGISTRATION_TYPE_REGISTER																= 0;
	public static final int				REGISTRATION_TYPE_REPEAT_REGISTER									= 1;

	public static final String	INTENT_KEY_END																												= "endAddress";

	public static final String	INTENT_KEY_TIP_TEXT																							= "tipText";

	public static final String	LIST_TYPE_RESTAURANT																						= "restaurant";

	public static final String	LIST_TYPE_MIKVEH																										= "mikvah";

	public static final String	LIST_TYPE_MINYAN																										= "minyan";

	public static final String	LIST_TYPE_LOYALTY																									= "loyaltyPlaces";

	public static final String	INTENT_KEY_LOCATION_TYPE																		= "locationType";

	public static final String	INTENT_KEY_IS_REGISTERED																		= "isRegistered";

	public static final long			ONE_DAY_IN_MILLIS																									= 86400000;

	public static final String	INTENT_KEY_NOTIFICATION_DESCRIPTION							= "notificationText";

	public static final String	INTENT_KEY_NOTIFICATION_HYPERLINK_TEXT				= "notificationHyperlinkText";

	public static final String	INTENT_KEY_NOTIFICATION_CLICKED											= "notificationClicked";

	public static final String	INTENT_KEY_LOCATION_WEBSITE															= "locationWebsite";

	public static final String	INTENT_KEY_LISTTYPE																							= "listType";

	public static final String	INTENT_KEY_USE_CURRENT_LOCATION											= "useCurrentLocation";

	public static final String	INTENT_KEY_NOTIFICATION_MILLIS												= "notificationMillis";

	public static final String	INTENT_KEY_AD_MILLIS																						= "adMillis";

	public static final String	INTENT_KEY_NOTIFICATION_DISCOUNTS_ENABLED	= "notificationDiscountsEnabled";

	public static final String	INTENT_KEY_ENHANCED_DISCOUNTS_ENABLED					= "enhancedDiscountsEnabled";

	public static final String	INTENT_KEY_APP_LONGITUDE																		= "appLongitude";

	public static final String	INTENT_KEY_APP_LATITUDE																			= "appLatitude";
	public static final String	INTENT_KEY_LOYALTY_ID																					= "loyaltyId";

	public static final long			AD_MILLIS_DEFAULT																									= 20000;																																															// two
	public static final long			NOTIFICATION_MILLIS_DEFAULT															= 3600000;																																													// 1
	public static final String	INTENT_KEY_LOYALTYCARD_NAME															= "loyaltyCardName";
	public static final String	INTENT_KEY_LOYALTYCARD_DESCRIPTION								= "loyaltyCardDescription";
	public static final String	INTENT_KEY_LOYALTYCARD_CLICKS													= "loyaltyCardClicks";
	public static final String	INTENT_KEY_LOYALTYCARD_CLICKSNEEDED							= "loyaltyCardClicksNeeded";
	public static final String	INTENT_KEY_LOYALTYCARD_REDEMPTIONS								= "loyaltyCardRedemptions";
	public static final String	INTENT_KEY_LOYALTYCARD_DEFAULTPUNCHAMT				= "loyaltyCardDefaultPunchAmt";
	public static final String	INTENT_KEY_REDEEM_ACTION																		= "loyaltyRedeemAction";
	public static final String	LOYALTY_REDEEM_ACTION_PUNCH															= "loyaltyRedeemActionPunch";
	public static final String	LOYALTY_REDEEM_ACTION_REDEEM														= "loyaltyRedeemActionRedeem";
	public static final String	INTENT_KEY_LOYALTY_ITEM_ID																= "loyaltyItemId";
	public static final String	INTENT_KEY_PUNCH_RESULT																			= "punchResult";
	public static final String	INTENT_KEY_REDEEM_RESULT																		= "redeemResult";

	public static boolean isValidEmailAddress(String emailAddress) {
		String expression = "^[\\w\\.=-]+@[\\w\\.-]+\\.[\\w]{2,3}$";
		CharSequence inputStr = emailAddress;
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		return matcher.matches();
	}

	public static String getDeviceId(Context baseContext,
		ContentResolver contentResolver) {
		final TelephonyManager tm = (TelephonyManager) baseContext
			.getSystemService(Context.TELEPHONY_SERVICE);
		final String tmDevice, tmSerial, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = ""
			+ android.provider.Settings.Secure.getString(contentResolver,
				android.provider.Settings.Secure.ANDROID_ID);
		UUID deviceUuid = new UUID(androidId.hashCode(),
			((long) tmDevice.hashCode() << 32) | tmSerial.hashCode());
		String deviceId = deviceUuid.toString();

		return deviceId;
	}

	public static double Round(double Rval, int Rpl) {
		double p = (double) Math.pow(10, Rpl);
		Rval = Rval * p;
		double tmp = Math.round(Rval);
		return (double) tmp / p;

	}

	public static double Distance(double Latitude1, double Longitude1,
		double Latitude2, double Longitude2) {
		double dDistance = 0; // Double.MinValue;

		double dLat1InRad = Latitude1 * (Math.PI / 180.0);
		double dLong1InRad = Longitude1 * (Math.PI / 180.0);
		double dLat2InRad = Latitude2 * (Math.PI / 180.0);
		double dLong2InRad = Longitude2 * (Math.PI / 180.0);

		double dLongitude = dLong2InRad - dLong1InRad;
		double dLatitude = dLat2InRad - dLat1InRad;

		// Intermediate result a.
		double a = Math.pow(Math.sin(dLatitude / 2.0), 2.0) + Math.cos(dLat1InRad)
			* Math.cos(dLat2InRad) * Math.pow(Math.sin(dLongitude / 2.0), 2.0);

		// Intermediate result c (great circle distance in Radians).
		double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));

		// Distance.
		// const Double kEarthRadiusMiles = 3956.0;
		double kEarthRadiusKms = 6376.5;
		dDistance = kEarthRadiusKms * c;

		return dDistance;

	}

	public static int getSpinnerChildPosition(Spinner spinner, String text) {
		String methodInfo = "<Common.getSpinnerChildPosition(Spinner, String):int>";
		Common.Log(methodInfo, String.format("text: %s", text));

		if (text == null) {
			return -1;
		}
		if (text.equals("")) {
			return -1;
		}

		text = text.toLowerCase(Locale.US);
		Common.Log(methodInfo, String.format("text: %s", text));
		if (spinner == null) {
			return -1;
		}
		if (spinner.getCount() == 0) {
			return -1;
		}

		int pos = -1;

		for (int i = 0; i < spinner.getCount(); i++) {
			Common.Log(methodInfo, String.format("i: %s", String.valueOf(i)));
			String tvText = (String) spinner.getItemAtPosition(i);
			if (tvText == null)
				return -1;
			tvText = tvText.replaceAll("\\s", "");
			tvText = tvText.toLowerCase(Locale.US);
			Common.Log(methodInfo, String.format("tvText: %s", tvText));
			if (tvText.equals(text)) {
				pos = i;
				break;
			}
		}

		return pos;
	}

	public static String getString(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String stacktrace = sw.toString();
		return stacktrace;
	}

	public static void showSimpleAlertDialog(Context context, String title,
		String message) {
		AlertDialog.Builder adb = new AlertDialog.Builder(context);
		adb.setTitle(title);
		adb.setMessage(message);
		adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// Action for 'Ok' Button
			}
		});

		adb.show();
	}

	public static void Log(String methodInfo, String message) {
		Log.d(Common.LOG_TAG, String.format("%s %s", methodInfo, message));
	}

	public static void LogMethodStart(String methodInfo) {
		String message = "START";
		Log.d(Common.LOG_TAG,
			String.format("%s %s %s", methodInfo, message, (new Date()).toString()));
	}

	public static void LogMethodEnd(String methodInfo) {
		String message = "END";
		Log.d(Common.LOG_TAG,
			String.format("%s %s %s", methodInfo, message, (new Date()).toString()));
	}

	public static Boolean gotoHyperlink(String hyperlinkText, Activity activity) {
		String methodInfo = "<Common.gotoHyperlink(String, Activity):void>";
		if (hyperlinkText == null) {
			return false;
		}
		if (hyperlinkText.equals("")) {
			Common.Log(
				methodInfo,
				String.format("hyperlinkText.equals(\"\"): %s",
					String.valueOf(hyperlinkText.equals(""))));
			return false;
		}

		Common.Log(methodInfo, String.format("hyperlink: %s", hyperlinkText));

		Intent browserIntent = new Intent(Intent.ACTION_VIEW,
			Uri.parse(hyperlinkText));
		try {
			activity.startActivity(browserIntent);
		} catch (ActivityNotFoundException e) {
			Common.Log(methodInfo, Common.getString(e));
			return false;
		}
		return true;
	}

	public static boolean hasActiveInternetConnection(Context context,
		Activity activity) {
		String methodInfo = "<Common.hasActiveInternetConnection(Context, Activity):boolean>";
		Common.Log(methodInfo, "START");

		if (isNetworkAvailable(context, activity)) {
			try {
				HttpURLConnection urlc = (HttpURLConnection) (new URL(
					"http://www.google.com").openConnection());
				urlc.setRequestProperty("User-Agent", "Test");
				urlc.setRequestProperty("Connection", "close");
				urlc.setConnectTimeout(1500);
				urlc.connect();
				return (urlc.getResponseCode() == 200);
			} catch (IOException e) {
				Common.Log(methodInfo, "Error checking internet connection");
				Common.Log(methodInfo, Common.getString(e));
			}
		} else {
			Common.Log(methodInfo, "No network available!");
		}
		return false;
	}

	private static boolean isNetworkAvailable(Context context, Activity activity) {
		ConnectivityManager connectivityManager = (ConnectivityManager) activity
			.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null;

	}

	public static String arrayToCommaDelimited(int[] loyaltyCards) {
		String methodInfo = "<Common.arrayToCommaDelimited(int[]):String>";
		String toReturn = "";
		String delimiter = "";
		if (loyaltyCards == null) {
			return null;
		}
		if (loyaltyCards.length == 0) {
			return null;
		}
		for (int i = 0; i < loyaltyCards.length; i++) {
			toReturn += delimiter + String.valueOf(loyaltyCards[i]);
			delimiter = ",";
		}

		Common.Log(methodInfo, String.format("toReturn: %s", toReturn));
		return toReturn;
	}

	public static int[] stringArrayToIntArray(String[] stringArray) {
		String methodInfo = "<Common.stringArrayToIntArray(String[]):int[]>";
		if (stringArray == null) {
			return null;
		}
		int[] toReturn = new int[stringArray.length];
		if (stringArray.length == 0) {
			return null;
		}
		int parsedInt = Integer.MIN_VALUE;
		for (int i = 0; i < stringArray.length; i++) {
			try {
				parsedInt = Integer.parseInt(stringArray[i]);
			} catch (NumberFormatException e) {
				Common.Log(methodInfo,
					String.format("Integer.parseInt(%s)", stringArray[i]));
				return null;
			}
			toReturn[i] = parsedInt;
		}
		// TODO Auto-generated method stub
		return toReturn;
	}
}
