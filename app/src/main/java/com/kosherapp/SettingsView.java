/**
 * 
 */
package com.kosherapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;

/**
 * @author Josh
 * 
 */
public class SettingsView extends Activity {

	private CheckBox	deepDiscountCheckBox					= null;
	private CheckBox	enhancedDiscountCheckBox	= null;

	private Button			okButton																	= null;
	private Button			cancelButton													= null;
	private Button			clearButton														= null;

	private Intent			data																					= null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settingsview);

		this.data = getIntent();

		this.okButton = (Button) findViewById(R.id.settingsOKButton);
		this.cancelButton = (Button) findViewById(R.id.settingsCancelButton);

		this.okButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					okTouched();
				return true;
			}
		});

		this.cancelButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					cancelTouched();
				return false;
			}
		});

		this.clearButton = (Button) findViewById(R.id.settingsClearButton);
		this.clearButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				SettingsView.this.enhancedDiscountCheckBox.setChecked(true);
				SettingsView.this.deepDiscountCheckBox.setChecked(true);
				return true;
			}
		});

		boolean isDeepDiscountFilter = data.getBooleanExtra(
			Common.INTENT_KEY_SETTINGS_DEEP_DISCOUNT, true);
		this.DeepDiscountCheckBox().setChecked(isDeepDiscountFilter);
		boolean isEnhancedDiscountFilter = data.getBooleanExtra(
			Common.INTENT_KEY_SETTINGS_ENHANCED_DISCOUNT, true);
		this.EnhancedDiscountCheckBox().setChecked(isEnhancedDiscountFilter);
	}

	private CheckBox DeepDiscountCheckBox() {
		if (this.deepDiscountCheckBox == null) {
			this.deepDiscountCheckBox = (CheckBox) findViewById(R.id.deepDiscountCheckBox);
		}
		return this.deepDiscountCheckBox;
	}

	private CheckBox EnhancedDiscountCheckBox() {
		if (this.enhancedDiscountCheckBox == null) {
			this.enhancedDiscountCheckBox = (CheckBox) findViewById(R.id.enhancedDiscountCheckBox);
		}
		return this.enhancedDiscountCheckBox;
	}

	protected void cancelTouched() {
		this.setResult(Activity.RESULT_CANCELED);
		finish();
	}

	protected void okTouched() {
		Intent data = new Intent();

		boolean deepDiscount = this.DeepDiscountCheckBox().isChecked();
		boolean enhancedDiscount = this.EnhancedDiscountCheckBox().isChecked();

		data.putExtra(Common.INTENT_KEY_SETTINGS_DEEP_DISCOUNT, deepDiscount);
		data.putExtra(Common.INTENT_KEY_SETTINGS_ENHANCED_DISCOUNT, enhancedDiscount);

		this.setResult(Activity.RESULT_OK, data);
		finish();
	}

}
