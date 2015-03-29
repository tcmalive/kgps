package com.kosherapp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import com.kosherapp.util.Maps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class KosherPlaceAdapter extends ArrayAdapter<KosherPlace> implements
	Filterable {

	private ArrayList<KosherPlace>	allItems					= null;
	public ArrayList<KosherPlace>		subItems					= null;
	private Context																context						= null;
	private View																			parentView			= null;
	private Activity															activity					= null;
	private LocationInfo											locationInfo	= null;
	private SharedPreferences						preferences		= null;

	public KosherPlaceAdapter(Context context, int textViewResourceId,
		ArrayList<KosherPlace> items, LocationInfo locationInfo) {
		super(context, textViewResourceId, items);
		String methodInfo = "<KosherPlaceAdapter.KosherPlaceAdapter(Context, int, ArrayList<KosherPlace>)>";
		Common.Log(methodInfo, "New KosherPlaceAdapter instance");
		this.subItems = items;
		this.allItems = this.subItems;
		this.context = context;
		this.locationInfo = locationInfo;
	}

	@Override
	public int getCount() {
		return this.subItems.size();
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String methodInfo = "<KosherPlaceAdapter.getView(int, View, ViewGroup):View>";
		parentView = convertView;

		KosherPlace o = subItems.get(position);

		if (parentView == null) {
			LayoutInflater vi = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			if (o.Type().equalsIgnoreCase(Common.LIST_TYPE_RESTAURANT)) {
				parentView = vi.inflate(R.layout.kosheritem, null);
			} else if (o.Type().equalsIgnoreCase(Common.LIST_TYPE_LOYALTY)) {
				parentView = vi.inflate(R.layout.loyaltyitem, null);
			} else {
				parentView = vi.inflate(R.layout.nonkosheritem, null);
			}
		}
		if (o != null) {
			Common.Log(methodInfo, String.format("o.Type(): %s", o.Type()));
			if (o.Type().equals(Common.LIST_TYPE_LOYALTY)) {
				TextView loyaltyPlace = (TextView) parentView
					.findViewById(R.id.loyaltyitem_placeName);
				TextView address = (TextView) parentView.findViewById(R.id.address);
				TextView distance = (TextView) parentView
					.findViewById(R.id.loyaltyitem_distance);
				TextView phone = (TextView) parentView.findViewById(R.id.phoneNumber);
				TextView website = (TextView) parentView.findViewById(R.id.website);

				Button websiteButton = (Button) parentView.findViewById(R.id.buttonWebsite);
				setWebsiteButtonListener(websiteButton);

				Button loyaltyButton = (Button) parentView
					.findViewById(R.id.kosheritemButtonLoyalty);
				if (o.getLoyaltyId() == Integer.MIN_VALUE) {
					loyaltyButton.setVisibility(View.INVISIBLE);
					loyaltyButton.setEnabled(false);
				} else {
					loyaltyButton.setVisibility(View.VISIBLE);
					loyaltyButton.setEnabled(true);
					setLoyaltyButtonListener(loyaltyButton);
				}

				Button callButton = (Button) parentView.findViewById(R.id.buttonCall);
				setCallButtonListener(callButton);

				TextView locationIdView = (TextView) parentView
					.findViewById(R.id.locationId);

				TextView loyaltyIdView = (TextView) parentView.findViewById(R.id.loyaltyId);

				Button gpsButton = (Button) parentView.findViewById(R.id.buttonGPS);
				setGPSButtonListener(gpsButton);

				if (loyaltyPlace != null) {
					loyaltyPlace.setText(o.Name());
				}
				if (address != null) {
					address.setText(o.Address());
				}
				if (distance != null) {
					Location appLocation = this.locationInfo.getAppLocation();
					if (appLocation != null) {
						distance.setText(o.DistanceText(appLocation.getLatitude(),
							appLocation.getLongitude()));
					}
				}
				if (phone != null) {
					phone.setText(o.Phone());
				}
				if (website != null) {
					website.setText(o.Website());
				}
				if (locationIdView != null) {
					locationIdView.setText(String.valueOf(o.getKosherId()));
				}

				if (loyaltyIdView != null) {
					Common.Log(methodInfo,
						String.format("o.getLoyaltyId(): %s", String.valueOf(o.getLoyaltyId())));
					loyaltyIdView.setText(String.valueOf(o.getLoyaltyId()));
				}

				LinearLayout itemLayout = (LinearLayout) parentView
					.findViewById(R.id.loyaltyItemView);
				itemLayout.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						openLoyaltyCards(v);
						return true;
					}
				});
			} else {

				TextView restaurant = (TextView) parentView.findViewById(R.id.restaurant);
				TextView address = (TextView) parentView.findViewById(R.id.address);
				TextView distance = (TextView) parentView.findViewById(R.id.distance);
				TextView discount = (TextView) parentView.findViewById(R.id.discount);
				TextView phone = (TextView) parentView.findViewById(R.id.phoneNumber);
				TextView foodType = (TextView) parentView.findViewById(R.id.foodType);
				TextView website = (TextView) parentView.findViewById(R.id.website);
				TextView loyaltyCards = (TextView) parentView
					.findViewById(R.id.kosheritem_loyalty);

				Button websiteButton = (Button) parentView.findViewById(R.id.buttonWebsite);
				setWebsiteButtonListener(websiteButton);

				Button loyaltyButton = (Button) parentView
					.findViewById(R.id.kosheritemButtonLoyalty);
				if (!(loyaltyButton == null)) {
					if (o.getLoyaltyId() == Integer.MIN_VALUE) {
						loyaltyButton.setVisibility(View.INVISIBLE);
						loyaltyButton.setEnabled(false);
					} else {
						loyaltyButton.setVisibility(View.VISIBLE);
						loyaltyButton.setEnabled(true);
						setLoyaltyButtonListener(loyaltyButton);
					}
				}
				Button callButton = (Button) parentView.findViewById(R.id.buttonCall);
				setCallButtonListener(callButton);
				TextView locationIdView = (TextView) parentView
					.findViewById(R.id.locationId);

				TextView loyaltyIdView = (TextView) parentView.findViewById(R.id.loyaltyId);

				Button gpsButton = (Button) parentView.findViewById(R.id.buttonGPS);
				setGPSButtonListener(gpsButton);

				if (restaurant != null) {
					restaurant.setText(o.Name());
				}
				if (address != null) {
					address.setText(o.Address());
				}
				if (distance != null) {
					Location appLocation = this.locationInfo.getAppLocation();
					if (appLocation != null) {
						distance.setText(o.DistanceText(appLocation.getLatitude(),
							appLocation.getLongitude()));
					}
				}
				if (discount != null) {
					discount.setText(o.Discount());
				}
				if (loyaltyCards != null) {
					loyaltyCards.setText("");
					if (o.getHasLoyaltyCards()) {
						loyaltyCards.setText("LOYALTY CARDS");
					}
				}
				if (phone != null) {
					phone.setText(o.Phone());
				}
				if (foodType != null) {
					foodType.setText(o.getFoodTypeText());
				}
				if (website != null) {
					website.setText(o.Website());
				}
				if (websiteButton != null) {
					if (o.Website() == null || o.Website().equals("")) {
						// ((ViewGroup) websiteButton.getParent()).removeView(websiteButton);
						// websiteButton.setHeight(0);
						// websiteButton.setWidth(0);
						// websiteButton.setBackgroundResource(0);
						// websiteButton.setEnabled(false);

						// websiteButton.setVisibility(View.INVISIBLE);
					}
				}

				if (locationIdView != null) {
					locationIdView.setText(String.valueOf(o.getKosherId()));
				}

				if (loyaltyIdView != null) {
					loyaltyIdView.setText(String.valueOf(o.getLoyaltyId()));
				}

				LinearLayout itemLayout = (LinearLayout) parentView
					.findViewById(R.id.kosherItemView);
				itemLayout.setOnLongClickListener(new OnLongClickListener() {

					@Override
					public boolean onLongClick(View v) {
						loadDetailsPage(v);
						return true;
					}
				});
			}
		}
		return parentView;
	}

	protected void emailFeedback(View v) {
		View parent = (View) v.getParent();
		TextView tv = (TextView) parent.findViewById(R.id.restaurant);
		String locationName = tv.getText().toString();
		tv = (TextView) parent.findViewById(R.id.address);
		String address = tv.getText().toString();

		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

		emailIntent.setType("plain/text");

		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
			new String[] { "kosherdroid@yahoo.com" });

		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
			"Android app feedback");

		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, String.format(
			"Location Name: %s%sAddress: %s%sComments:", locationName, "\r\n", address,
			"\r\n"));

		activity.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}

	public void filterBy(String locationNameFilter, boolean discountFilter,
		double distanceFilter, String foodTypeFilter) {
		String methodInfo = "<KosherPlaceAdapter.filterBy(String, boolean, double, String):void>";
		this.subItems = new ArrayList<KosherPlace>();
		Common.Log(
			methodInfo,
			String.format("preFilter: this.allItems.size(): %s",
				String.valueOf(this.allItems.size())));
		for (int i = 0; i < this.allItems.size(); i++) {
			KosherPlace kp = allItems.get(i);
			boolean isNameIncluded = getIsNameIncluded(kp, locationNameFilter);
			boolean isDiscountIncluded = getIsDiscountIncluded(kp, discountFilter);
			boolean isDistanceIncluded = getIsDistanceIncluded(kp, distanceFilter);
			boolean isFoodTypeIncluded = getIsFoodTypeIncluded(kp, foodTypeFilter);

			if (isNameIncluded && isDiscountIncluded && isDistanceIncluded
				&& isFoodTypeIncluded)
				this.subItems.add(kp);
		}
		Common.Log(
			methodInfo,
			String.format("postFilter: this.subItems.size(): %s",
				String.valueOf(this.subItems.size())));
		this.notifyDataSetChanged();
	}

	private boolean getIsFoodTypeIncluded(KosherPlace kp, String foodTypeFilter) {
		if (foodTypeFilter.equalsIgnoreCase("all"))
			return true;

		if (foodTypeFilter.equalsIgnoreCase(kp.getFoodTypeText()))
			return true;

		if (foodTypeFilter.equalsIgnoreCase(KosherPlace.FOODTYPE_UNDEFINED)) {
			return true;
		}

		return false;
	}

	public void sortWith(Comparator<KosherPlace> sorter) {
		if (this.subItems == null)
			return;
		Collections.sort(this.subItems, sorter);
	}

	private boolean getIsDistanceIncluded(KosherPlace kp, double distanceFilter) {
		boolean isDistanceIncluded = false;

		double distance = kp.getDistance(this.locationInfo.getAppLocation()
			.getLatitude(), this.locationInfo.getAppLocation().getLongitude());
		isDistanceIncluded = distance <= distanceFilter;

		return isDistanceIncluded;
	}

	private boolean getIsDiscountIncluded(KosherPlace kp, boolean discountFilter) {
		boolean isDiscountIncluded = true;
		if (!discountFilter)
			return true;
		if (kp.Discount() == null)
			return false;
		if (kp.Discount().equals(""))
			isDiscountIncluded = false;

		return isDiscountIncluded;
	}

	private boolean getIsNameIncluded(KosherPlace kp, String locationNameFilter) {
		boolean isNameIncluded = false;
		isNameIncluded = kp.Name().toLowerCase(Locale.US)
			.contains(locationNameFilter.toLowerCase(Locale.US));
		return isNameIncluded;
	}

	protected void loadDetailsPage(View v) {
		String methodInfo = "<KosherPlaceAdapter.loadDetailsPage(View):void>";

		Intent detailsIntent = null;

		TextView locationIdView = (TextView) v.findViewById(R.id.locationId);
		int locationId = Integer.parseInt(locationIdView.getText().toString());
		KosherPlace kosherPlace = getKosherPlace(locationId);
		if (kosherPlace == null)
			throw new NullPointerException();

		String type = kosherPlace.Type();

		if (type.equalsIgnoreCase(Common.LIST_TYPE_RESTAURANT)) {
			detailsIntent = new Intent(v.getContext(), RestaurantDetailsPage.class);
		} else {
			detailsIntent = new Intent(v.getContext(), DetailsPage.class);
		}

		TextView restaurantNameView = (TextView) v.findViewById(R.id.restaurant);
		String restaurantName = restaurantNameView.getText().toString();
		detailsIntent.putExtra(Common.INTENT_KEY_LOCATION_NAME, restaurantName);

		TextView restaurantAddressView = (TextView) v.findViewById(R.id.address);
		String restaurantAddress = restaurantAddressView.getText().toString();
		detailsIntent.putExtra(Common.INTENT_KEY_LOCATION_ADDRESS, restaurantAddress);

		TextView restaurantPhonenumberView = (TextView) v
			.findViewById(R.id.phoneNumber);
		String restaurantPhoneNumber = restaurantPhonenumberView.getText().toString();
		detailsIntent.putExtra(Common.INTENT_KEY_LOCATION_PHONENUMBER,
			restaurantPhoneNumber);

		TextView restaurantDistanceView = (TextView) v.findViewById(R.id.distance);
		String restaurantDistance = restaurantDistanceView.getText().toString();
		detailsIntent.putExtra(Common.INTENT_KEY_LOCATION_DISTANCE,
			restaurantDistance);

		TextView restaurantDiscountView = (TextView) v.findViewById(R.id.discount);
		if (restaurantDiscountView != null) {
			CharSequence cs = restaurantDiscountView.getText();
			if (cs != null) {
				String restaurantDiscount = cs.toString();
				detailsIntent.putExtra(Common.INTENT_KEY_LOCATION_DISCOUNT,
					restaurantDiscount);
			}
		}

		TextView restaurantFoodTypeView = (TextView) v.findViewById(R.id.foodType);
		if (restaurantFoodTypeView != null) {
			String restaurantFoodType = restaurantFoodTypeView.getText().toString();
			detailsIntent.putExtra(Common.INTENT_KEY_LOCATION_FOOD_TYPE,
				restaurantFoodType);
		}

		TextView restaurantWebsiteView = (TextView) v.findViewById(R.id.website);
		Common.Log(
			methodInfo,
			String.format("restaurantWebsiteView==null: %s",
				String.valueOf(restaurantWebsiteView == null)));
		String restaurantWebsite = restaurantWebsiteView.getText().toString();
		detailsIntent.putExtra(Common.INTENT_KEY_LOCATION_WEBSITE, restaurantWebsite);

		String locationType = null;
		locationType = kosherPlace.Type();
		detailsIntent.putExtra(Common.INTENT_KEY_LOCATION_TYPE, locationType);

		if (this.locationInfo.getCurrentLocation() != null) {
			detailsIntent.putExtra(Common.INTENT_KEY_CURRENT_LOCATION_LONGITUDE,
				this.locationInfo.getCurrentLocation().getLongitude());
			detailsIntent.putExtra(Common.INTENT_KEY_CURRENT_LOCATION_LATITUDE,
				this.locationInfo.getCurrentLocation().getLatitude());
		}

		context.startActivity(detailsIntent);
	}

	private KosherPlace getKosherPlace(int locationId) {
		for (int i = 0; i < this.allItems.size(); i++) {
			KosherPlace kosherPlace = this.allItems.get(i);
			if (kosherPlace.getKosherId() == locationId)
				return kosherPlace;
		}

		return null;
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
		View parent = (View) v.getParent().getParent().getParent();
		TextView tv = (TextView) parent.findViewById(R.id.address);
		String endAddressString = tv.getText().toString();

		Maps.showMap(activity, endAddressString);
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

	private void setLoyaltyButtonListener(Button loyaltyButton) {
		if (loyaltyButton == null) {
			return;
		}

		loyaltyButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					openLoyaltyCards(v);
				return true;
			}
		});
	}

	protected void openLoyaltyCards(View v) {
		String methodInfo = "<KosherPlaceAdapter.openLoyaltyCards(View)>";
		View parent = (View) v.getParent().getParent().getParent();
		TextView tv = (TextView) parent.findViewById(R.id.loyaltyId);
		String loyaltyId = tv.getText().toString();
		Common.Log(methodInfo, String.format("loyaltyId: '%s'", loyaltyId));
		loyaltyId = loyaltyId.trim();
		if (loyaltyId.equals(String.valueOf(Integer.MIN_VALUE))) {
			Toast.makeText(activity.getApplicationContext(),
				"No loyalty card available for the selected location.", Toast.LENGTH_LONG)
				.show();
			return;
		}
		TextView locationNameTV = (TextView) parent
			.findViewById(R.id.loyaltyitem_placeName);
		if (locationNameTV == null) {
			locationNameTV = (TextView) parent.findViewById(R.id.restaurant);
		}
		String locationName = locationNameTV.getText().toString();
		Common.Log(methodInfo, String.format("locationName: '%s'", locationName));

		TextView phoneNumberTV = (TextView) parent.findViewById(R.id.phoneNumber);
		String phoneNumber = phoneNumberTV.getText().toString();
		Common.Log(methodInfo, String.format("phoneNumber: '%s'", phoneNumber));

		TextView websiteTV = (TextView) parent.findViewById(R.id.website);
		String website = websiteTV.getText().toString();
		Common.Log(methodInfo, String.format("website: '%s'", website));

		TextView addressTV = (TextView) parent.findViewById(R.id.address);
		String address = addressTV.getText().toString();
		Common.Log(methodInfo, String.format("address: '%s'", address));

		Intent loyaltyCardsIntent = new Intent(getContext(), LoyaltyCards.class);

		loyaltyCardsIntent.putExtra(Common.INTENT_KEY_USE_CURRENT_LOCATION,
			this.locationInfo.getUseCurrentLocation());
		loyaltyCardsIntent.putExtra(Common.INTENT_KEY_NOTIFICATION_MILLIS,
			Preferences().getLong(Common.PREFS_KEY_NOTIFICATION_MILLIS, 0));
		loyaltyCardsIntent.putExtra(Common.INTENT_KEY_NOTIFICATION_DISCOUNTS_ENABLED,
			Preferences().getBoolean(Common.PREFS_KEY_NOTIFICATION_DISCOUNTS, true));
		loyaltyCardsIntent.putExtra(Common.INTENT_KEY_LOCATION_LONGITUDE,
			this.locationInfo.getAppLongitude());
		loyaltyCardsIntent.putExtra(Common.INTENT_KEY_LOCATION_LATITUDE,
			this.locationInfo.getAppLatitude());
		loyaltyCardsIntent.putExtra(Common.INTENT_KEY_ENHANCED_DISCOUNTS_ENABLED,
			Preferences().getBoolean(Common.PREFS_KEY_ENHANCED_DISCOUNTS, true));
		loyaltyCardsIntent.putExtra(Common.INTENT_KEY_APP_LONGITUDE,
			this.locationInfo.getAppLongitude());
		loyaltyCardsIntent.putExtra(Common.INTENT_KEY_APP_LATITUDE,
			this.locationInfo.getAppLatitude());
		loyaltyCardsIntent.putExtra(Common.INTENT_KEY_CURRENT_LOCATION_LATITUDE,
			this.locationInfo.getCurrentLatitude());
		loyaltyCardsIntent.putExtra(Common.INTENT_KEY_CURRENT_LOCATION_LONGITUDE,
			this.locationInfo.getCurrentLongitude());
		loyaltyCardsIntent.putExtra(Common.INTENT_KEY_LOYALTY_ID,
			Integer.parseInt(loyaltyId));
		loyaltyCardsIntent.putExtra(
			Common.INTENT_KEY_DEVICE_ID,
			Common.getDeviceId(this.activity.getApplicationContext(),
				this.context.getContentResolver()));
		loyaltyCardsIntent.putExtra(Common.INTENT_KEY_LOCATION_NAME, locationName);
		loyaltyCardsIntent.putExtra(Common.INTENT_KEY_LOCATION_ADDRESS, address);
		loyaltyCardsIntent.putExtra(Common.INTENT_KEY_LOCATION_WEBSITE, website);
		loyaltyCardsIntent.putExtra(Common.INTENT_KEY_LOCATION_PHONENUMBER,
			phoneNumber);

		this.activity.startActivityForResult(loyaltyCardsIntent,
			Common.REQUEST_CODE_LOYALTY_CARDS);

		// Toast.makeText(activity.getApplicationContext(),
		// "Sorry, haven't taken care of this screen yet.", Toast.LENGTH_LONG).show();

		// TODO Auto-generated method stub
	}

	private SharedPreferences Preferences() {
		if (this.preferences == null) {
			this.preferences = this.context.getSharedPreferences(Common.PREFS_NAME,
				Context.MODE_PRIVATE);
		}
		return this.preferences;
	}

	private KosherPlace getKosherPlaceFromLoyaltyId(int loyaltyId) {
		if (loyaltyId == Integer.MIN_VALUE) {
			return null;
		}

		for (int i = 0; i < this.allItems.size(); i++) {
			KosherPlace kosherPlace = this.allItems.get(i);
			if (kosherPlace.getLoyaltyId() == loyaltyId)
				return kosherPlace;
		}

		return null;
	}

	protected void openWebsite(View v) {
		View parent = (View) v.getParent().getParent().getParent();
		TextView tv = (TextView) parent.findViewById(R.id.website);
		CharSequence websiteChar = tv.getText();
		Boolean isWebsiteAvailable = true;
		if (websiteChar == null) {
			isWebsiteAvailable = false;
		}
		if (websiteChar.length() == 0) {
			isWebsiteAvailable = false;
		}
		String website = websiteChar.toString();
		if (website == null) {
			isWebsiteAvailable = false;
		}
		if (website.equals("")) {
			isWebsiteAvailable = false;
		}
		if (!isWebsiteAvailable) {
			Toast.makeText(activity, "Website unavailable for this location.",
				Toast.LENGTH_SHORT).show();
			return;
		}

		Boolean successful = Common.gotoHyperlink(website, activity);
		if (!successful) {
			AlertDialog alertDialog = null;
			alertDialog = new AlertDialog.Builder(activity).create();
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
		View parent = (View) v.getParent().getParent().getParent();
		TextView tv = (TextView) parent.findViewById(R.id.phoneNumber);
		String phoneNumber = tv.getText().toString();
		try {
			context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
				+ phoneNumber)));
		} catch (Exception e) {
			Log.d(Common.LOG_TAG, Common.getString(e));
			e.printStackTrace();
		}
	}
}
