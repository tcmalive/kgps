/**
 * 
 */
package com.kosherapp;

import java.util.LinkedList;

import com.kosherapp.database.DbAdapter;
import com.koshersense.*;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MenuPage extends Activity {

	private WebView imageAdWebView = null;

	private SharedPreferences preferences = null;
	final static public String PREFS_NAME = "KosherAppPrefs";

	static final public String PREFS_KEY_RESTAURANTS_LOAD_ID = "restaurantsLoadId";
	static final public String PREFS_KEY_MIKVEHS_LOAD_ID = "mikvehsLoadId";
	static final public String PREFS_KEY_MINYAN_LOAD_ID = "minyanLoadId";
	static final public String PREFS_KEY_RESTAURANT_DB_UPDATE_DATE = "restaurantDBUpdateDate";
	static final public String PREFS_KEY_MIKVEH_DB_UPDATE_DATE = "mikvehDBUpdateDate";
	static final public String PREFS_KEY_MINYAN_DB_UPDATE_DATE = "minyanDBUpdateDate";

	private Location currentLocation = null;

	private DbAdapter dbAdapter = null;

	private LocationManager locationManager = null;

	private boolean windowHasFocus;

	private CountDownTimer countDownTimer = null;

	private AdController adController = null;

	public boolean loadingFirstDatabase = false;

	private AsyncTask<String, Integer, Integer> updateMikvehDatabaseTask;
	private AsyncTask<String, Integer, Integer> updateMinyanDatabaseTask;
	private AsyncTask<String, Integer, Integer> updateRestaurantDatabaseTask;

	private ListService lService = null;

	private LinkedList<String> oldLoadIds = null;

	private Button minyanButton = null;
	private Button mikvehButton = null;
	private Button restaurantButton = null;
	private Button loyaltyCardButton = null;
	private ProgressBar restaurantProgressBar = null;
	private ProgressBar minyanProgressBar = null;
	private ProgressBar mikvehProgressBar = null;
	private ProgressBar loyaltyProgressBar = null;

	private ProgressDialog progressDialog = null;

	private Double longitude;
	private Double latitude;

	private int listType;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.menupage);

		setGUIListeners();

		this.longitude = getIntent().getDoubleExtra(
				Common.INTENT_KEY_LOCATION_LONGITUDE, 0);
		this.latitude = getIntent().getDoubleExtra(
				Common.INTENT_KEY_LOCATION_LATITUDE, 0);
		this.listType = getIntent().getIntExtra(Common.INTENT_KEY_LISTTYPE, 1);

		// RestaurantProgressBar().setVisibility(ProgressBar.INVISIBLE);
		// RestaurantProgressBar().bringToFront();
		// MinyanProgressBar().setVisibility(ProgressBar.INVISIBLE);
		// MinyanProgressBar().bringToFront();
		// MikvehProgressBar().setVisibility(ProgressBar.INVISIBLE);
		// MikvehProgressBar().bringToFront();
		// LoyaltyProgressBar().setVisibility(ProgressBar.INVISIBLE);
		// LoyaltyProgressBar().bringToFront();
	}

	private View LoyaltyProgressBar() {
		if (this.loyaltyProgressBar == null) {
			this.loyaltyProgressBar = (ProgressBar) this
					.findViewById(R.id.MenuPageLoyaltyProgressBar);
		}

		return this.loyaltyProgressBar;
	}

	private View MikvehProgressBar() {
		if (this.mikvehProgressBar == null) {
			this.mikvehProgressBar = (ProgressBar) this
					.findViewById(R.id.MenuPageMikvehProgressBar);
		}

		return this.mikvehProgressBar;
	}

	private View MinyanProgressBar() {
		if (this.minyanProgressBar == null) {
			this.minyanProgressBar = (ProgressBar) this
					.findViewById(R.id.MenuPageMinyanProgressBar);
		}

		return this.minyanProgressBar;
	}

	private ProgressBar RestaurantProgressBar() {
		if (this.restaurantProgressBar == null) {
			this.restaurantProgressBar = (ProgressBar) this
					.findViewById(R.id.MenuPageRestaurantProgressBar);
		}
		return this.restaurantProgressBar;
	}

	private void setGUIListeners() {
		RestaurantButton().setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					Intent returnData = new Intent();
					returnData.putExtra(Common.INTENT_KEY_LISTTYPE,
							Common.LIST_TYPE_RESTAURANT);
					setResult(Activity.RESULT_OK, returnData);
					finish();
				}
				return true;
			}
		});
		MinyanButton().setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					Intent returnData = new Intent();
					returnData.putExtra(Common.INTENT_KEY_LISTTYPE,
							Common.LIST_TYPE_MINYAN);
					setResult(Activity.RESULT_OK, returnData);
					finish();
				}
				return true;
			}
		});
		MikvehButton().setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					Intent returnData = new Intent();
					returnData.putExtra(Common.INTENT_KEY_LISTTYPE,
							Common.LIST_TYPE_MIKVEH);
					setResult(Activity.RESULT_OK, returnData);
					finish();
				}
				return true;
			}
		});
		LoyaltyButton().setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					Intent returnData = new Intent();
					returnData.putExtra(Common.INTENT_KEY_LISTTYPE,
							Common.LIST_TYPE_LOYALTY);
					setResult(Activity.RESULT_OK, returnData);
					finish();
				}
				return true;
			}
		});
	}

	private Button LoyaltyButton() {
		if (this.loyaltyCardButton == null) {
			this.loyaltyCardButton = (Button) this
					.findViewById(R.id.menuPageLoyaltyButton);
		}

		return this.loyaltyCardButton;
	}

	private Button MikvehButton() {
		if (this.mikvehButton == null) {
			this.mikvehButton = (Button) this
					.findViewById(R.id.menuPageMikvehButton);
		}

		return this.mikvehButton;
	}

	private Button MinyanButton() {
		if (this.minyanButton == null) {
			this.minyanButton = (Button) this
					.findViewById(R.id.menuPageMinyanButton);
		}

		return this.minyanButton;
	}

	private Button RestaurantButton() {
		if (this.restaurantButton == null) {
			this.restaurantButton = (Button) this
					.findViewById(R.id.menuPageRestaurantButton);
		}

		return this.restaurantButton;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		String methodInfo = "<KosherApp.onWindowFocusChanged(boolean):void>";
		Common.LogMethodStart(methodInfo);
		if (hasFocus) {
			Common.Log(methodInfo, "KosherApp Window has focus");
			this.windowHasFocus = true;
			actionOnWindowHasFocus();
		} else {
			this.windowHasFocus = false;
			actionOnWindowLosesFocus();
		}

		super.onWindowFocusChanged(hasFocus);
		Common.LogMethodEnd(methodInfo);
	}

	private void actionOnWindowHasFocus() {
		String methodInfo = "<KosherApp.actionOnWindowHasFocus():void>";
		Common.LogMethodStart(methodInfo);

		AdController().searchForAds(true);

		Common.LogMethodEnd(methodInfo);
	}

	private void actionOnWindowLosesFocus() {
		AdController().dismissAdSearch();
	}
	
	private LocationManager LocationManager() {
		if (this.locationManager == null) {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		return this.locationManager;
	}
	
	private AdController AdController() {
		if (this.adController == null) {
			this.adController = new AdController(KosherApp.KOSHER_SENSE_APP_ID, this, this.getContentResolver(), ImageAdWebView(), LocationManager());
		}
		return this.adController;
	}

	private void activateScreen(int buttonId) {
		if (R.id.menuPageMikvehButton == buttonId) {
			this.MikvehProgressBar().setVisibility(View.INVISIBLE);
			this.MikvehButton().setVisibility(View.VISIBLE);
			this.MikvehButton().setEnabled(true);
		}
		if (R.id.menuPageMinyanButton == buttonId) {
			this.MinyanProgressBar().setVisibility(View.INVISIBLE);
			this.MinyanButton().setVisibility(View.VISIBLE);
			this.MinyanButton().setEnabled(true);
		}
		if (R.id.menuPageRestaurantButton == buttonId) {
			this.RestaurantProgressBar().setVisibility(View.INVISIBLE);
			this.RestaurantButton().setVisibility(View.VISIBLE);
			this.RestaurantButton().setEnabled(true);
		}
		if (R.id.menuPageLoyaltyButton == buttonId) {
			this.LoyaltyProgressBar().setVisibility(View.INVISIBLE);
			this.LoyaltyButton().setVisibility(View.VISIBLE);
			this.LoyaltyButton().setEnabled(true);
		}
	}

	private WebView ImageAdWebView() {
		if (this.imageAdWebView == null) {
			this.imageAdWebView = (WebView) findViewById(R.id.imageAdWebView);
		}
		return this.imageAdWebView;
	}

	public Location CurrentLocation() {
		return this.currentLocation;
	}
}
