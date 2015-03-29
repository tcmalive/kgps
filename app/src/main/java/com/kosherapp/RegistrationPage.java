/**
 * 
 */
package com.kosherapp;

import java.util.Comparator;

import com.kosherapp.storefront.StoreFront;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * @author Josh
 * 
 */
public class RegistrationPage extends Activity {

	private final String	REGISTRATION_MESSAGE_BAD_EMAIL								= "The e-mail you entered is invalid. Please try again";
	private final String	REGISTRATION_MESSAGE_BAD_REGISTRATION	= "The registration failed. Please try again.";

	private String							deviceId																														= "";
	private String							emailAddress																										= "";
	private String							state																																	= "";
	private int										registrationType																						= Integer.MIN_VALUE;

	private EditText					emailEditText																									= null;
	private Spinner						stateDropDown																									= null;

	private ListService		listService																											= null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.registration);

		this.listService = new ListService(this);
		getIntent().getBooleanExtra(Common.INTENT_KEY_IS_REGISTERED, false);

		this.stateDropDown = (Spinner) findViewById(R.id.autocomplete_state);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
			R.array.state_list, android.R.layout.simple_spinner_item);
		adapter
			.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		adapter.sort(new Comparator<CharSequence>() {
			@Override
			public int compare(CharSequence object1, CharSequence object2) {
				return object1.toString().compareTo(object2.toString());
			};
		});

		this.stateDropDown.setAdapter(adapter);

		Intent data = getIntent();
		String emailAddress = data.getStringExtra(Common.INTENT_KEY_EMAIL);
		emailEditText = (EditText) findViewById(R.id.emailInput);
		emailEditText.setText(emailAddress);

		String state = data.getStringExtra(Common.INTENT_KEY_STATE);
		int pos = Common.getSpinnerChildPosition(this.stateDropDown, state);
		Log.d(Common.LOG_TAG, String.format("pos: %s", String.valueOf(pos)));
		if (pos > -1)
			this.stateDropDown.setSelection(pos);

		this.deviceId = getIntent().getStringExtra(Common.INTENT_KEY_DEVICE_ID);

		this.registrationType = getRegistrationType();

		if (this.registrationType == Common.REGISTRATION_TYPE_REPEAT_REGISTER) {
			// inAppPurchase();
		} else if (this.registrationType == Common.REGISTRATION_TYPE_UNNECESSARY) {
			Intent returnData = new Intent();
			returnData.putExtra(Common.INTENT_KEY_IS_REGISTERED, true);
			setResult(Activity.RESULT_OK, returnData);
			finish();
		}

		emailEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (event == null)
					return false;
				if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
					&& event.getAction() == KeyEvent.ACTION_DOWN) {
					setResult(Activity.RESULT_OK);
					register(v);
					return true;
				}
				return false;
			}
		});

		Button registerButton = (Button) findViewById(R.id.registrationOK);
		registerButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					setResult(Activity.RESULT_OK);
					if (RegistrationPage.this.registrationType == Common.REGISTRATION_TYPE_REGISTER) {
						register(v);
					} else if (RegistrationPage.this.registrationType == Common.REGISTRATION_TYPE_REPEAT_REGISTER) {
						// inAppPurchase();
					}
				}
				return true;
			}
		});

		Button cancelButton = (Button) findViewById(R.id.registrationCancel);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			}
		});
	}

	private int getRegistrationType() {
		int registrationType = Integer.MIN_VALUE; // if unrecognized

		String subscriptionStatus = listService.getSubscriptionStatus(this.deviceId);
		if (subscriptionStatus.equalsIgnoreCase(Common.REGISTRATION_STATUS_ACTIVE))
			registrationType = Common.REGISTRATION_TYPE_UNNECESSARY;
		else if (subscriptionStatus
			.equalsIgnoreCase(Common.REGISTRATION_STATUS_EXPIRED)) {
			registrationType = Common.REGISTRATION_TYPE_REPEAT_REGISTER;
		} else if (subscriptionStatus
			.equalsIgnoreCase(Common.REGISTRATION_STATUS_NOT_SUBSCRIBER)) {
			registrationType = Common.REGISTRATION_TYPE_REGISTER;
		}

		return registrationType;
	}

	protected void inAppPurchase() {
		Intent storeFrontIntent = new Intent(this, StoreFront.class);

		String[] skus = new String[] { "kosherApp_01" };
		int[] nameIds = new int[] { R.string.kosher_app };
		String[] manageds = new String[] { "unmanaged" };

		storeFrontIntent.putExtra(com.kosherapp.Common.INTENT_KEY_SKUS, skus);
		storeFrontIntent.putExtra(com.kosherapp.Common.INTENT_KEY_NAME_IDS, nameIds);
		storeFrontIntent.putExtra(com.kosherapp.Common.INTENT_KEY_MANAGEDS, manageds);

		this.startActivityForResult(storeFrontIntent, StoreFront.REQUEST_CODE);
	}

	protected void register(View v) {
		String methodInfo = "<RegistrationPage.register(View):void>";
		String emailAddress = "";
		EditText emailInput = (EditText) findViewById(R.id.emailInput);
		emailAddress += emailInput.getText().toString();
		emailAddress = emailAddress.trim();

		Spinner stateSpinner = (Spinner) findViewById(R.id.autocomplete_state);
		this.state = (String) stateSpinner.getSelectedItem();

		boolean isValidAddress = getIsValidAddress(emailAddress);

		if (!isValidAddress) {
			TextView registrationMessage = (TextView) findViewById(R.id.registrationMessage);
			registrationMessage.setText(REGISTRATION_MESSAGE_BAD_EMAIL);
			registrationMessage.setTextColor(Color.RED);
			return;
		}

		this.emailAddress = emailAddress;

		boolean isRegistrationSuccessful = false;
		Context context = getApplicationContext();
		String platform = context.getString(R.string.platform);
		Common.Log(methodInfo, String.format("platform: %s", platform));
		isRegistrationSuccessful = register(this.emailAddress, this.state, platform);

		if (isRegistrationSuccessful) {
			Intent data = new Intent();
			data.putExtra(Common.INTENT_KEY_EMAIL, this.emailAddress);
			data.putExtra(Common.INTENT_KEY_IS_REGISTERED, true);
			setResult(Activity.RESULT_OK, data);
			finish();
		} else {
			TextView registrationMessage = (TextView) findViewById(R.id.registrationMessage);
			registrationMessage.setText(REGISTRATION_MESSAGE_BAD_REGISTRATION);
			registrationMessage.setTextColor(Color.RED);
			return;
		}
	}

	private boolean register(String emailAddress, String state, String platform) {
		boolean isRegistered = false;

		String registrationResult = "";

		if (this.registrationType == Common.REGISTRATION_TYPE_REGISTER) {
			registrationResult = this.listService.register(emailAddress, state,
				this.deviceId, platform);
		} else if (this.registrationType == Common.REGISTRATION_TYPE_REPEAT_REGISTER) {
			registrationResult = this.listService.resubscribe(this.deviceId);
		}

		if (registrationResult
			.equalsIgnoreCase(Common.REGISTRATION_SERVICE_STATUS_FAILURE))
			isRegistered = false;
		else if (registrationResult
			.equalsIgnoreCase(Common.REGISTRATION_SERVICE_STATUS_SUCCESS))
			isRegistered = true;

		return isRegistered;
	}

	protected boolean getIsValidAddress(String emailAddress) {
		boolean isValidAddress = false;

		isValidAddress = Common.isValidEmailAddress(emailAddress);

		return isValidAddress;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		String methodInfo = "<RegistrationPage.onActivityResult(int, int, Intent):void>";
		if (requestCode == StoreFront.REQUEST_CODE) {
			if (resultCode == Activity.RESULT_CANCELED) {
				setResult(Activity.RESULT_CANCELED);
				finish();
			} else if (resultCode == Activity.RESULT_OK) {
				Context context = getApplicationContext();
				String platform = context.getString(R.string.platform);
				Common.Log(methodInfo, String.format("platform: %s", platform));
				boolean successful = register(this.emailAddress, this.state, platform);
				try {
					if (!successful)
						throw new Exception("registration unsuccessful after purchase");
				} catch (Exception e) {
					setResult(Activity.RESULT_CANCELED);
					finish();
				}

				Intent registrationData = new Intent();
				data.putExtra(Common.INTENT_KEY_EMAIL, this.emailAddress);
				data.putExtra(Common.INTENT_KEY_IS_REGISTERED, successful);
				setResult(Activity.RESULT_OK, registrationData);
				finish();
			}
		}
	}
}
