package com.kosherapp;

import android.os.Bundle;
import android.widget.TextView;

public class RestaurantDetailsPage extends DetailsPage {

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.layoutToUse = R.layout.restaurant_details;
		super.onCreate(savedInstanceState);

		String restaurantFoodType = super.data
			.getStringExtra(Common.INTENT_KEY_LOCATION_FOOD_TYPE);
		TextView restaurantFoodTypeView = (TextView) findViewById(R.id.detailsRestaurantFoodType);
		if (restaurantFoodTypeView != null)
			restaurantFoodTypeView.setText(restaurantFoodType);
	}
}
