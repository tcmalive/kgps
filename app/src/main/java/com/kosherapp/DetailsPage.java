/**
 * 
 */
package com.kosherapp;

import com.kosherapp.util.Maps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Josh
 * 
 */
public class DetailsPage extends Activity {

	private Button			detailsGPSButton						= null;
	private Button			detailsCallButton					= null;
	private Button			detailsEmailButton				= null;
	private Button			detailsContactsButton	= null;
	private Button			detailsWebsiteButton		= null;

	protected Intent	data																		= null;
	protected int				layoutToUse											= Integer.MIN_VALUE;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		if (layoutToUse == Integer.MIN_VALUE) {
			setContentView(R.layout.details);
		} else {
			setContentView(layoutToUse);
		}

		this.data = getIntent();

		this.detailsCallButton = (Button) findViewById(R.id.detailsCallButton);
		this.detailsGPSButton = (Button) findViewById(R.id.detailsMapButton);
		this.detailsEmailButton = (Button) findViewById(R.id.detailsEmailButton);
		this.detailsContactsButton = (Button) findViewById(R.id.detailsContactsButton);
		this.detailsWebsiteButton = (Button) findViewById(R.id.detailsWebsiteButton);

		this.detailsContactsButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					addContact(v);
				return true;
			}
		});

		this.detailsCallButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					callRestaurant(v);
				return true;
			}
		});

		this.detailsGPSButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					gpsRestaurant(v);
				return true;
			}
		});

		this.detailsEmailButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event == null)
					return false;
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					email();
				return true;
			}
		});

		this.detailsWebsiteButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event == null)
					return false;
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					gotoWebsite();
				return true;
			}
		});

		String restaurantName = data.getStringExtra(Common.INTENT_KEY_LOCATION_NAME);
		String restaurantAddress = data
			.getStringExtra(Common.INTENT_KEY_LOCATION_ADDRESS);
		String restaurantPhonenumber = data
			.getStringExtra(Common.INTENT_KEY_LOCATION_PHONENUMBER);
		String restaurantDistance = data
			.getStringExtra(Common.INTENT_KEY_LOCATION_DISTANCE);
		String restaurantDiscount = data
			.getStringExtra(Common.INTENT_KEY_LOCATION_DISCOUNT);
		String restaurantWebsite = data
			.getStringExtra(Common.INTENT_KEY_LOCATION_WEBSITE);
		String locationType = data.getStringExtra(Common.INTENT_KEY_LOCATION_TYPE);

		TextView restaurantNameView = (TextView) findViewById(R.id.detailsRestaurantName);
		restaurantNameView.setText(restaurantName);
		TextView restaurantAddressView = (TextView) findViewById(R.id.detailsRestaurantAddress);
		restaurantAddressView.setText(restaurantAddress);
		TextView restaurantPhoneNumberView = (TextView) findViewById(R.id.detailsRestaurantPhoneNumber);
		restaurantPhoneNumberView.setText(restaurantPhonenumber);
		TextView restaurantDiscountView = (TextView) findViewById(R.id.detailsRestaurantDiscount);
		restaurantDiscountView.setText(restaurantDiscount);
		TextView restaurantDistanceView = (TextView) findViewById(R.id.detailsRestaurantDistance);
		restaurantDistanceView.setText(restaurantDistance);
		TextView restaurantWebsiteView = (TextView) findViewById(R.id.detailsWebsiteText);
		restaurantWebsiteView.setText(restaurantWebsite);
		restaurantWebsiteView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					TextView websiteView = (TextView) v;
					String website = websiteView.getText().toString();
					Common.gotoHyperlink(website, DetailsPage.this);
				}
				return false;
			}
		});

		TextView messageView = (TextView) findViewById(R.id.detailsMessage);
		if (locationType.equals(Common.LIST_TYPE_RESTAURANT)) {
			messageView.setText(R.string.details_restaurantMessage);
		} else {
			messageView.setText(R.string.details_message);
		}
		Button okButton = (Button) findViewById(R.id.detailsOK);
		okButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String methodInfo = "<DetailsPage.onCreate(Bundle):void.new OnClickListener() {...}.onClick(View):void>";
				Common.LogMethodStart(methodInfo);

				finish();

				Common.LogMethodEnd(methodInfo);
			}
		});
	}

	protected void gotoWebsite() {
		TextView restaurantWebsiteView = (TextView) findViewById(R.id.detailsWebsiteText);
		String website = restaurantWebsiteView.getText().toString();
		if (website == null) {
			return;
		}
		if (website.equals(""))
			return;

		Boolean successful = Common.gotoHyperlink(website, this);
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

	protected void addContact(View v) {
		TextView tv = (TextView) this.findViewById(R.id.detailsRestaurantPhoneNumber);
		String phoneNumber = tv.getText().toString();

		tv = (TextView) this.findViewById(R.id.detailsRestaurantAddress);
		String address = tv.getText().toString();

		tv = (TextView) this.findViewById(R.id.detailsRestaurantName);
		String locationName = tv.getText().toString();

		this.addContact(locationName, phoneNumber, address);
	}

	protected void gpsRestaurant(View v) {

		TextView tv = (TextView) findViewById(R.id.detailsRestaurantAddress);
		String endAddressString = tv.getText().toString();

		Maps.showMap(this, endAddressString);
	}

	protected void callRestaurant(View v) {
		TextView tv = (TextView) this.findViewById(R.id.detailsRestaurantPhoneNumber);
		String phoneNumber = tv.getText().toString();
		try {
			this.startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
				+ phoneNumber)));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void email() {
		TextView tv = (TextView) this.findViewById(R.id.detailsRestaurantName);
		String locationName = tv.getText().toString();
		tv = (TextView) this.findViewById(R.id.detailsRestaurantAddress);
		String locationAddress = tv.getText().toString();

		final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

		emailIntent.setType("plain/text");

		emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
			new String[] { "kosherdroid@yahoo.com" });

		emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
			"Android app feedback");

		emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, String.format(
			"Location Name: %s%sAddress:%s%sComments:", locationName, "\r\n",
			locationAddress, "\r\n"));

		startActivity(Intent.createChooser(emailIntent, "Send mail..."));
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == Common.REQUEST_CODE_DIRECTION_MAP_VIEW) {
			onDirectionMapViewResult(resultCode, data);
			return;
		}
	}

	private void onDirectionMapViewResult(int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_CANCELED)
			Toast.makeText(this, R.string.directionsUnavailableMessage,
				Toast.LENGTH_LONG).show();
		// TODO Auto-generated method stub

	}

	private void addContact(String contactName, String contactPhone,
		String contactAddress) {
		Intent addContactIntent = new Intent(Contacts.Intents.Insert.ACTION,
			Contacts.People.CONTENT_URI);
		addContactIntent.putExtra(Contacts.Intents.Insert.NAME, contactName);
		addContactIntent.putExtra(Contacts.Intents.Insert.PHONE, contactPhone);
		addContactIntent.putExtra(Contacts.Intents.Insert.POSTAL, contactAddress);
		this.startActivity(addContactIntent);
	}
}
