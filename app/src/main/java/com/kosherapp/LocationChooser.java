/**
 * 
 */
package com.kosherapp;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView.OnEditorActionListener;

/**
 * @author Josh
 * 
 */
public class LocationChooser extends Activity {

	private RadioButton	customButton		= null;
	private RadioButton	currentButton	= null;
	private Button						okButton						= null;
	private Button						cancelButton		= null;

	private EditText				locationInput	= null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.locationchooser);

		Intent data = getIntent();
		String address = data.getStringExtra(Common.INTENT_KEY_LOCATION_ADDRESS);
		boolean isCurrent = data.getBooleanExtra(
			Common.INTENT_KEY_LOCATION_IS_CURRENT_LOCATION, false);

		this.locationInput = (EditText) findViewById(R.id.locationInputEditText);
		this.locationInput.setText(address);

		this.customButton = (RadioButton) findViewById(R.id.customLocationRadio);
		this.currentButton = (RadioButton) findViewById(R.id.currentLocationRadio);

		this.okButton = (Button) findViewById(R.id.okButtonLocationChooser);
		this.cancelButton = (Button) findViewById(R.id.cancelButtonLocationChooser);

		setListeners();
		if (isCurrent)
			this.currentButton.setChecked(true);
		else
			this.customButton.setChecked(true);
	}

	/**
	 * 
	 */
	private void setListeners() {
		this.customButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked)
					locationInput.setEnabled(true);
				else
					locationInput.setEnabled(false);
			}
		});

		locationInput.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (event == null)
					return false;
				if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
					&& event.getAction() == MotionEvent.ACTION_DOWN)
					okClicked();
				// TODO Auto-generated method stub
				return true;
			}
		});

		okButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					okClicked();
				return true;
			}
		});

		cancelButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					cancelButtonClicked();
				return true;
			}
		});
	}

	protected void okClicked() {
		if (currentButton.isChecked()) {
			Intent data = new Intent();
			data.putExtra(Common.INTENT_KEY_LOCATION_IS_CURRENT_LOCATION, true);
			setResult(Activity.RESULT_OK, data);
			finish();
		} else {
			Address address = null;
			address = getAddress(this.locationInput);

			if (address == null)
				Toast.makeText(this, R.string.unrecognizedLocationMessage,
					Toast.LENGTH_LONG).show();
			else {
				StringBuilder addressBuilder = new StringBuilder();
				for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
					if (i > 0)
						addressBuilder.append(" ");
					addressBuilder.append(address.getAddressLine(i));
				}
				String addressString = addressBuilder.toString();

				Intent data = new Intent();
				data.putExtra(Common.INTENT_KEY_LOCATION_ADDRESS, addressString);
				data.putExtra(Common.INTENT_KEY_LOCATION_LONGITUDE, address.getLongitude());
				data.putExtra(Common.INTENT_KEY_LOCATION_LATITUDE, address.getLatitude());
				data.putExtra(Common.INTENT_KEY_LOCATION_IS_CURRENT_LOCATION,
					this.currentButton.isChecked());

				this.setResult(Activity.RESULT_OK, data);
				finish();
			}
		}
	}

	protected Address getAddress(TextView textView) {
		Address address = null;
		String locationAddress = locationInput.getText().toString();

		if (locationAddress == null)
			return null;
		if (locationAddress.length() == 0)
			return null;

		Geocoder geocoder = new Geocoder(this);
		List<Address> addressCollection;
		try {
			addressCollection = geocoder.getFromLocationName(locationAddress,
				Integer.MAX_VALUE);
		} catch (IOException e) {
			return null;
		}
		if (addressCollection == null)
			return null;
		if (addressCollection.size() == 0)
			return null;

		address = addressCollection.get(0);
		return address;
	}

	protected void cancelButtonClicked() {
		setResult(Activity.RESULT_CANCELED);
		finish();
	}
}
