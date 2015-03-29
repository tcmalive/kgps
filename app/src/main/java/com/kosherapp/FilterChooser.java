/**
 * 
 */
package com.kosherapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TextView.OnEditorActionListener;

/**
 * @author Josh
 * 
 */
public class FilterChooser extends Activity {

	private EditText locationNameEditText = null;
	private CheckBox discountCheckBox = null;
	private EditText distanceEditText = null;

	private Button okButton = null;
	private Button cancelButton = null;
	private Button clearButton = null;
	private double defaultDistance = 20;
	private double maxDistance = Double.MAX_VALUE;
	private CharSequence distanceMessage = String
			.format("The maximum distance is %s miles. Please fix the distance filter and try again.",
					String.valueOf(this.maxDistance));
	private Spinner foodTypeSpinner = null;

	private Intent data = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filterchooser);

		this.data = getIntent();
		this.locationNameEditText = (EditText) findViewById(R.id.locationFilterValue);
		String nameFilter = data.getStringExtra(Common.INTENT_KEY_NAME_FILTER);
		this.locationNameEditText.setText(nameFilter);
		this.locationNameEditText
				.setOnEditorActionListener(new OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (event == null)
							return false;

						if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
								&& event.getAction() == KeyEvent.ACTION_DOWN) {
							FilterChooser.this.DiscountCheckBox()
									.requestFocus();
							return true;
						}
						return false;
					}
				});

		this.distanceEditText = (EditText) findViewById(R.id.distanceFilterValue);
		double distanceFilter = data.getDoubleExtra(
				Common.INTENT_KEY_DISTANCE_FILTER, Double.MAX_VALUE);
		if (distanceFilter != Double.MAX_VALUE)
			this.distanceEditText.setText(String.valueOf(distanceFilter));
		this.distanceEditText
				.setOnEditorActionListener(new OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (event == null)
							return false;
						if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER
								&& event.getAction() == KeyEvent.ACTION_DOWN) {
							okTouched();
							return true;
						}
						return false;
					}
				});

		this.okButton = (Button) findViewById(R.id.filterOKButton);
		this.cancelButton = (Button) findViewById(R.id.filterCancelButton);

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

		this.clearButton = (Button) findViewById(R.id.filterClearButton);
		this.clearButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				locationNameEditText.setText("");
				distanceEditText.setText(String
						.valueOf(FilterChooser.this.defaultDistance));
				DiscountCheckBox().setChecked(false);
				int p = Common
						.getSpinnerChildPosition(FoodTypeSpinner(), "all");
				if (p > -1) {
					FoodTypeSpinner().setSelection(p);
				}
				return true;
			}
		});

		boolean isDiscountFilter = data.getBooleanExtra(
				Common.INTENT_KEY_DISCOUNT_FILTER, false);
		this.DiscountCheckBox().setChecked(isDiscountFilter);

		this.setFoodTypeButtonListeners();
	}

	private CheckBox DiscountCheckBox() {
		if (this.discountCheckBox == null) {
			this.discountCheckBox = (CheckBox) findViewById(R.id.discountFilterCheckBox);
		}
		return this.discountCheckBox;
	}

	protected void cancelTouched() {
		this.setResult(Activity.RESULT_CANCELED);
		finish();
	}

	protected void okTouched() {
		Intent data = new Intent();

		String locationNameFilter = this.locationNameEditText.getText()
				.toString();
		boolean discountFilter = this.DiscountCheckBox().isChecked();
		double distanceFilter = Double.MAX_VALUE;
		try {
			distanceFilter = Double.parseDouble(this.distanceEditText.getText()
					.toString());
		} catch (NumberFormatException e) {
			distanceFilter = Double.MAX_VALUE;
		}
		if (distanceFilter > this.maxDistance) {
			Toast.makeText(this, this.distanceMessage, Toast.LENGTH_LONG)
					.show();
			return;
		}

		String foodTypeFilter = "";
		foodTypeFilter = (String) FoodTypeSpinner().getSelectedItem();
		data.putExtra(Common.INTENT_KEY_NAME_FILTER, locationNameFilter);
		data.putExtra(Common.INTENT_KEY_DISCOUNT_FILTER, discountFilter);
		data.putExtra(Common.INTENT_KEY_DISTANCE_FILTER, distanceFilter);
		data.putExtra(Common.INTENT_KEY_FOODTYPE_FILTER, foodTypeFilter);

		this.setResult(Activity.RESULT_OK, data);
		finish();
	}

	private Spinner FoodTypeSpinner() {
		if (this.foodTypeSpinner == null) {
			this.foodTypeSpinner = (Spinner) findViewById(R.id.foodTypeSpinner);
			ArrayAdapter<CharSequence> listTypeAdapter = ArrayAdapter
					.createFromResource((Context) this, R.array.foodType_array,
							R.layout.spinnerlayout);
			listTypeAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			foodTypeSpinner.setAdapter(listTypeAdapter);
			String foodTypeFilter = this.data
					.getStringExtra(Common.INTENT_KEY_FOODTYPE_FILTER);
			int pos = Common.getSpinnerChildPosition(this.FoodTypeSpinner(),
					foodTypeFilter);
			Log.d(Common.LOG_TAG, String.format("pos: %s", String.valueOf(pos)));
			if (pos > -1)
				this.FoodTypeSpinner().setSelection(pos);
		}
		return this.foodTypeSpinner;
	}

	private void setFoodTypeButtonListeners() {
		this.FoodTypeSpinner().setOnItemSelectedListener(
				new MyFoodTypeSelectedListener());
	}

	public class MyFoodTypeSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}

	}

}
