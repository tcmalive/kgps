package com.kosherapp;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.koshersense.*;
import com.kosherapp.database.DbAdapter;
import com.kosherapp.util.Maps;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class LoyaltyCards extends android.app.ListActivity implements
		LocationInfo {

	private ProgressDialog progressDialog = null;

	private LocationManager locationManager = null;
	private ArrayList<LoyaltyCard> loyaltyCards = null;
	private LoyaltyCardsAdapter loyaltyCardsAdapter = null;

	private DbAdapter dbAdapter = null;

	private ListService lService = null;

	private Location currentLocation = null;

	private Comparator<LoyaltyCard> sorter = null;

	private Button websiteButton = null;
	private Button callButton = null;
	private Button mapButton = null;
	private TextView locationTV = null;

	private WebView imageAdWebView = null;

	private AdController adController = null;
	private String locationName = null;
	private Boolean useCurrentLocation;
	private double appLongitude;
	private double appLatitude;
	private Location appLocation;
	private double currentLatitude;
	private double currentLongitude;
	private int loyaltyId;
	private String deviceId;
	private String phoneNumber = null;
	private String websiteAddress = null;
	private String locationAddress = null;

	private AdController AdController() {
		if (this.adController == null) {
			this.adController = new AdController(KosherApp.KOSHER_SENSE_APP_ID,
					this, this.getContentResolver(), ImageAdWebView(),
					LocationManager());
		}
		return this.adController;
	}

	private LocationManager LocationManager() {
		if (this.locationManager == null) {
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		}
		return this.locationManager;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return dbAdapter;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		String methodInfo = "<LoyaltyCards.onCreate(Bundle):void>";
		Common.Log(methodInfo, "START");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.loyaltycards);

		setGUIListeners();

		Intent data = getIntent();
		this.useCurrentLocation = data.getBooleanExtra(
				Common.INTENT_KEY_USE_CURRENT_LOCATION, true);
		this.appLongitude = data.getDoubleExtra(
				Common.INTENT_KEY_APP_LONGITUDE, Double.MIN_VALUE);
		this.appLatitude = data.getDoubleExtra(Common.INTENT_KEY_APP_LATITUDE,
				Double.MIN_VALUE);
		this.appLocation = new Location(LocationManager.PASSIVE_PROVIDER);
		this.appLocation.setLatitude(this.getAppLatitude());
		this.appLocation.setLongitude(this.getAppLongitude());
		this.currentLatitude = data.getDoubleExtra(
				Common.INTENT_KEY_CURRENT_LOCATION_LATITUDE, Double.MAX_VALUE);
		this.currentLongitude = data.getDoubleExtra(
				Common.INTENT_KEY_CURRENT_LOCATION_LONGITUDE, Double.MAX_VALUE);
		this.currentLocation = new Location(LocationManager.PASSIVE_PROVIDER);
		this.currentLocation.setLatitude(this.getCurrentLatitude());
		this.currentLocation.setLongitude(this.getCurrentLongitude());
		this.loyaltyId = data.getIntExtra(Common.INTENT_KEY_LOYALTY_ID,
				Integer.MIN_VALUE);
		this.deviceId = data.getStringExtra(Common.INTENT_KEY_DEVICE_ID);
		this.locationName = data
				.getStringExtra(Common.INTENT_KEY_LOCATION_NAME);
		this.locationAddress = data
				.getStringExtra(Common.INTENT_KEY_LOCATION_ADDRESS);
		this.websiteAddress = data
				.getStringExtra(Common.INTENT_KEY_LOCATION_WEBSITE);
		this.phoneNumber = data
				.getStringExtra(Common.INTENT_KEY_LOCATION_PHONENUMBER);

		this.locationTV = (TextView) this
				.findViewById(R.id.loyaltycards_restaurantname);
		this.locationTV.setText(this.locationName);

		this.websiteButton = (Button) this
				.findViewById(R.id.loyaltycards_websitebutton);
		setWebsiteButtonListener(this.websiteButton);
		this.callButton = (Button) this
				.findViewById(R.id.loyaltycards_callbutton);
		setCallButtonListener(this.callButton);
		this.mapButton = (Button) this
				.findViewById(R.id.loyaltycards_mapbutton);
		setGPSButtonListener(this.mapButton);

		Common.Log(methodInfo, "END");
	}

	private void setCallButtonListener(Button callButton) {
		callButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					callRestaurant(v);
				return true;
			}
		});
	}

	protected void callRestaurant(View v) {
		try {
			this.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
					+ this.phoneNumber)));
		} catch (Exception e) {
			Log.d(Common.LOG_TAG, Common.getString(e));
			e.printStackTrace();
		}
	}

	private void setGPSButtonListener(Button gpsButton) {
		gpsButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				String methodInfo = "<KosherPlaceAdapter.setGPSButtonListener(Button):void.new OnTouchListener(){...}onTouch(View, MotionEvent):boolean>";
				if (event == null)
					return false;
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Common.Log(methodInfo, "gpsButton touched");
					gpsRestaurant(v);
				}
				return true;
			}
		});
	}

	protected void gpsRestaurant(View v) {
		Maps.showMap(this, this.locationAddress);
	}

	private void setWebsiteButtonListener(Button websiteButton) {
		if (websiteButton == null) {
			return;
		}

		websiteButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					openWebsite(v);
				return true;
			}
		});
	}

	protected void openWebsite(View v) {
		Boolean isWebsiteAvailable = true;

		if (this.websiteAddress == null) {
			isWebsiteAvailable = false;
		}
		if (this.websiteAddress.equals("")) {
			isWebsiteAvailable = false;
		}
		if (!isWebsiteAvailable) {
			Toast.makeText(this, "Website unavailable for this location.",
					Toast.LENGTH_SHORT).show();
			return;
		}

		Boolean successful = Common.gotoHyperlink(this.websiteAddress, this);
		if (!successful) {
			AlertDialog alertDialog = null;
			alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Invalid URL");
			alertDialog.setMessage("Invalid URL. Please report the problem.");
			alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			});
			alertDialog.show();
		}
	}

	private void display() {
		String methodInfo = "<LoyaltyCards.display():void>";
		Common.LogMethodStart(methodInfo);
		ArrayList<LoyaltyCard> loyaltyCards = getLoyaltyCards();
		this.loyaltyCardsAdapter = new LoyaltyCardsAdapter(this,
				R.layout.loyaltycard, loyaltyCards, this, this.deviceId,
				this.loyaltyId, this.currentLatitude, this.currentLongitude);
		setListAdapter(this.loyaltyCardsAdapter);
		ProgressDialog().dismiss();
	}

	private ArrayList<LoyaltyCard> getLoyaltyCards() {
		if (loyaltyCards == null) {
			loyaltyCards = new ArrayList<LoyaltyCard>();
		}
		return loyaltyCards;
	}

	private ListService ListService() {
		if (this.lService == null) {
			this.lService = new ListService(this);
		}
		return this.lService;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		String methodInfo = "<LoyaltyCards.onWindowFocusChanged(boolean):void>";
		Common.LogMethodStart(methodInfo);
		if (hasFocus) {
			Common.Log(methodInfo, "LoyaltyCards Window has focus");
			actionOnWindowHasFocus();
		} else {
			actionOnWindowLosesFocus();
		}

		super.onWindowFocusChanged(hasFocus);
		Common.LogMethodEnd(methodInfo);
	}

	private void actionOnWindowLosesFocus() {
		AdController().dismissAdSearch();
	}

	private void actionOnWindowHasFocus() {
		String methodInfo = "<LoyaltyCards.actionOnWindowHasFocus():void>";
		Common.LogMethodStart(methodInfo);

		AdController().searchForAds(true);

		ProgressDialog("Loading Loyalty Cards...");

		new UpdateLoyaltyCardsTask().execute();

		Common.LogMethodEnd(methodInfo);
	}

	private void showProgressDialog(String message) {
		String methodInfo = "<KosherApp.showProgressDialog():void>";
		Common.Log(methodInfo, "START");
		ProgressDialog(message).show();

		Common.Log(methodInfo, "END");
	}

	private void dismissProgressDialog() {
		String methodInfo = "<KosherApp.dismissProgressDialog():void>";
		Common.Log(methodInfo, "START");

		ProgressDialog().dismiss();
		Common.Log(methodInfo, "Progress Dialog dismissed");
		Common.Log(methodInfo, "END");
	}

	private ProgressDialog ProgressDialog() {
		return this.ProgressDialog(null);
	}

	private ProgressDialog ProgressDialog(String message) {
		if (message == null) {
			message = "Please wait...";
		}
		if (this.progressDialog == null) {
			progressDialog = new ProgressDialog(this);
			progressDialog.setTitle("Progress Dialog");
		}
		progressDialog.setMessage(message);
		return this.progressDialog;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.filter:
			return true;
		case R.id.sort:
			return true;
		case R.id.email:
			return true;
		case R.id.settings:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void setGUIListeners() {
	}

	private WebView ImageAdWebView() {
		if (this.imageAdWebView == null) {
			this.imageAdWebView = (WebView) findViewById(R.id.imageAdWebView);
		}
		return this.imageAdWebView;
	}

	public Location getCurrentLocation() {
		return this.currentLocation;
	}

	public Double getCurrentLatitude() {
		return this.currentLocation.getLatitude();
	}

	public Double getCurrentLongitude() {
		return this.currentLocation.getLongitude();
	}

	public Comparator<LoyaltyCard> Sorter() {
		if (this.sorter == null) {
			sorter = new NameComparator();
		}
		return this.sorter;
	}

	public void setSorter(Comparator<LoyaltyCard> sorter) {
		this.sorter = sorter;
	}

	private void setCurrentLocation(Location newLocation) {
		String methodInfo = "<LoyaltyCards.setCurrentLocation(Location):void>";
		Common.Log(methodInfo, "START");
		this.currentLocation = newLocation;
	}

	public void showMap(View view) {
		Intent i = new Intent(this, LocationMapView.class);
		i.putExtra("currentLocation", this.getUseCurrentLocation());
		i.putExtra("longitude", this.getAppLongitude());
		i.putExtra("latitude", this.getAppLatitude());
		startActivityForResult(i, Common.REQUEST_CODE_GOOGLE_MAPS_CODE);
	}

	public Boolean getUseCurrentLocation() {
		return this.useCurrentLocation;
	}

	private class NameComparator implements Comparator<LoyaltyCard> {

		@Override
		public int compare(LoyaltyCard arg0, LoyaltyCard arg1) {
			String arg0Name = arg0.Name();
			String arg1Name = arg1.Name();
			int result = arg0Name.compareToIgnoreCase(arg1Name);
			return result;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		String methodInfo = "<LoyaltyCards.onActivityResult(int, int, Intent):void>";
		Common.LogMethodStart(methodInfo);
		if (requestCode == Common.REQUEST_CODE_LOYALTY_REDEEM_SCREEN) {
			if (data == null) {
				requestCode = Activity.RESULT_CANCELED;
			}
			if (resultCode == Activity.RESULT_OK) {
				String action = data
						.getStringExtra(Common.INTENT_KEY_REDEEM_ACTION);
				String result = "";
				if (Common.LOYALTY_REDEEM_ACTION_PUNCH.equals(action)) {
					result = data
							.getStringExtra(Common.INTENT_KEY_PUNCH_RESULT);
					if (result.equals("-2")) {
						Toast.makeText(
								this,
								"The punch(es) were not successfully registered.",
								Toast.LENGTH_LONG).show();

					}
					if (result.equals("1")) {
						Toast.makeText(this,
								"The punch(es) were successfully registered.",
								Toast.LENGTH_LONG).show();

					}
				}
				if (Common.LOYALTY_REDEEM_ACTION_REDEEM.equals(action)) {
					result = data
							.getStringExtra(Common.INTENT_KEY_REDEEM_RESULT);
					if (result.equals("-2")) {
						Toast.makeText(
								this,
								"The redemption was not successfully registered.",
								Toast.LENGTH_LONG).show();

					}
					if (result.equals("1")) {
						Toast.makeText(this,
								"The redemption was successfully registered.",
								Toast.LENGTH_LONG).show();

					}
				}
				return;
			}
			if (resultCode == Activity.RESULT_CANCELED) {
				Toast.makeText(this, "The action was canceled.",
						Toast.LENGTH_LONG).show();

			}

			ProgressDialog("Loading Loyalty Cards...");

			new UpdateLoyaltyCardsTask().execute();
			return;
		}

		Common.LogMethodEnd(methodInfo);
	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	public Location getAppLocation() {
		return this.appLocation;
	}

	@Override
	public Double getAppLatitude() {
		return this.appLatitude;
	}

	@Override
	public Double getAppLongitude() {
		return this.appLongitude;
	}

	private class UpdateLoyaltyCardsTask extends
			AsyncTask<String, Integer, Integer> {

		protected Integer doInBackground(String... listInfo) {
			String methodInfo = "<LoyaltyCards.UpdateLoyaltyCardsTask.doInBackground(String...):Integer>";
			Common.LogMethodStart(methodInfo);

			int result = 0;
			String sLoyaltyList = "";

			sLoyaltyList = ListService().RetrieveListLoyaltyCards(loyaltyId,
					deviceId);
			Common.Log(methodInfo,
					String.format("sLoyaltyList: %s", sLoyaltyList));

			if (sLoyaltyList == null) {
				Common.Log(methodInfo, "FAILED");
				return -1;
			}
			if (sLoyaltyList.equals("")) {
				Common.Log(methodInfo, "FAILED");
				return -1;
			}
			
			List<LoyaltyCard> loyaltyCards = ListParser
					.ParseLoyaltyCards(sLoyaltyList);
			setLoyaltyCards((ArrayList<LoyaltyCard>) loyaltyCards);

			if (getLoyaltyCards().size() == 0) {
				Common.Log(methodInfo, "FAILED");
				return -1;
			}

			Common.Log(methodInfo, "END");
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			String methodInfo = "<LoyaltyCards.UpdateLoyaltyCardsTask.onPostExecute(Integer):void>";
			Common.Log(methodInfo, "START");
			Common.Log(methodInfo,
					String.format("result: %s", String.valueOf(result)));

			if (result == 0) {
				display();
			}
			super.onPostExecute(result);
			Common.Log(methodInfo, "END");
		}
	}

	public void setLoyaltyCards(ArrayList<LoyaltyCard> loyaltyCards) {
		this.loyaltyCards = loyaltyCards;
	}

}