package com.kosherapp;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LoyaltyCardsAdapter extends ArrayAdapter<LoyaltyCard> implements
		Filterable {

	private ArrayList<LoyaltyCard> allItems = null;
	public ArrayList<LoyaltyCard> subItems = null;
	private Context context = null;
	private View parentView = null;
	private Activity activity = null;

	private int loyaltyId;
	private String deviceId = null;
	private Double longitude;
	private Double latitude;

	public LoyaltyCardsAdapter(Context context, int textViewResourceId,
			ArrayList<LoyaltyCard> items, Activity activity, String deviceId,
			int loyaltyId, Double latitude, Double longitude) {
		super(context, textViewResourceId, items);
		String methodInfo = "<LoyaltyCardsAdapter.LoyaltyCardsAdapter(Context, int, ArrayList<LoyaltyCard>, LocationInfo)>";
		Common.LogMethodStart(methodInfo);
		this.subItems = items;
		this.allItems = this.subItems;
		this.context = context;
		this.activity = activity;

		this.loyaltyId = loyaltyId;
		this.deviceId = deviceId;
		this.latitude = latitude;
		this.longitude = longitude;
	}

	@Override
	public int getCount() {
		return this.subItems.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String methodInfo = "<LoyaltyCardsAdapter.getView(int, View, ViewGroup):View>";
		Common.LogMethodStart(methodInfo);

		parentView = convertView;

		LoyaltyCard o = subItems.get(position);

		if (parentView == null) {
			LayoutInflater vi = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			parentView = vi.inflate(R.layout.loyaltycard, null);
		}

		if (o != null) {
			Button punchButton = (Button) parentView
					.findViewById(R.id.loyaltyCard_punch);
			Button redeemButton = (Button) parentView
					.findViewById(R.id.loyaltyCard_redeem);
			TextView loyaltyCardName = (TextView) parentView
					.findViewById(R.id.loyaltycard_Name);
			TextView loyaltyCardDescription = (TextView) parentView
					.findViewById(R.id.loyaltycard_description);
			TextView loyaltyCardPunchesUsed = (TextView) parentView
					.findViewById(R.id.loyaltycard_punchesused);
			TextView loyaltyCardPunchesRemain = (TextView) parentView
					.findViewById(R.id.loyaltycard_punchesremain);
			TextView loyaltyCardId = (TextView) parentView
					.findViewById(R.id.loyaltycard_id);
			TextView loyaltyCardDefaultPunchAmt = (TextView) parentView
					.findViewById(R.id.loyaltycard_defaultpunchamt);

			if (loyaltyCardName != null) {
				loyaltyCardName.setText(o.Name());
			}
			if (loyaltyCardDescription != null) {
				loyaltyCardDescription.setText(o.Description());
			}
			if (loyaltyCardPunchesUsed != null) {
				DecimalFormat df = new DecimalFormat("#.##");
				loyaltyCardPunchesUsed.setText(df.format(o.PunchesUsed()));
			}
			if (loyaltyCardPunchesRemain != null) {
				DecimalFormat df = new DecimalFormat("#.##");
				Double punchesRemaining = o.PunchesRemaining();
				if (punchesRemaining < 0) {
					punchesRemaining = 0.0;
				}
				loyaltyCardPunchesRemain.setText(df.format(punchesRemaining));
			}
			if (loyaltyCardId != null) {
				loyaltyCardId.setText(String.valueOf(o.ItemId()));
			}
			if (loyaltyCardDefaultPunchAmt != null) {
				Common.Log(
						methodInfo,
						String.format("o.DefaultPunchAmt(): %s",
								String.valueOf(o.DefaultPunchAmt())));
				loyaltyCardDefaultPunchAmt.setText(String.valueOf(o
						.DefaultPunchAmt()));
			}

			punchButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					loadClickRedeemScreen(Common.LOYALTY_REDEEM_ACTION_PUNCH, v);
				}
			});

			if (!o.CanRedeem()) {
				parentView.setBackgroundColor(Color.WHITE);
				redeemButton.setVisibility(View.INVISIBLE);
				redeemButton.setEnabled(false);
			} else {
				parentView.setBackgroundColor(Color.YELLOW);

				redeemButton.setVisibility(View.VISIBLE);
				redeemButton.setEnabled(true);

				redeemButton.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						loadClickRedeemScreen(
								Common.LOYALTY_REDEEM_ACTION_REDEEM, v);
					}
				});
			}
		}
		return parentView;
	}

	protected void loadClickRedeemScreen(String action, View v) {
		String methodInfo = "<LoyaltyCardsAdapter.loadClickRedeemScreen(String, View):void>";
		Double loyaltyCardDefaultPunchAmt;
		View parent = (View) v.getParent().getParent();
		TextView cardDefaultPunchAmt = (TextView) parent
				.findViewById(R.id.loyaltycard_defaultpunchamt);
		loyaltyCardDefaultPunchAmt = Double.parseDouble(cardDefaultPunchAmt
				.getText().toString());
		Common.Log(
				methodInfo,
				String.format("loyaltyCardDefaultPunchAmt: %s",
						String.valueOf(loyaltyCardDefaultPunchAmt)));

		TextView cardName = (TextView) parent
				.findViewById(R.id.loyaltycard_Name);
		String loyaltyCardName = cardName.getText().toString();

		Intent loyaltyRedeemScreenIntent = new Intent(getContext(),
				LoyaltyRedeemScreen.class);
		loyaltyRedeemScreenIntent.putExtra(Common.INTENT_KEY_LOYALTY_ID,
				this.loyaltyId);
		loyaltyRedeemScreenIntent.putExtra(Common.INTENT_KEY_LOYALTYCARD_NAME,
				loyaltyCardName);
		TextView loyaltyItemIdTV = (TextView) parent
				.findViewById(R.id.loyaltycard_id);
		Common.Log(
				methodInfo,
				String.format("loyaltyItemIdTV == null: %s",
						String.valueOf(loyaltyItemIdTV == null)));
		Common.Log(
				methodInfo,
				String.format("loyaltyItemIdTV.getText() == null: %s",
						String.valueOf(loyaltyItemIdTV.getText() == null)));
		loyaltyRedeemScreenIntent.putExtra(Common.INTENT_KEY_LOYALTY_ITEM_ID,
				Integer.parseInt(loyaltyItemIdTV.getText().toString()));
		loyaltyRedeemScreenIntent.putExtra(Common.INTENT_KEY_LOCATION_LATITUDE,
				this.latitude);
		loyaltyRedeemScreenIntent.putExtra(
				Common.INTENT_KEY_LOCATION_LONGITUDE, this.longitude);
		loyaltyRedeemScreenIntent.putExtra(Common.INTENT_KEY_DEVICE_ID,
				this.deviceId);

		loyaltyRedeemScreenIntent.putExtra(Common.INTENT_KEY_REDEEM_ACTION,
				action);
		loyaltyRedeemScreenIntent.putExtra(
				Common.INTENT_KEY_LOYALTYCARD_DEFAULTPUNCHAMT,
				loyaltyCardDefaultPunchAmt);

		this.activity.startActivityForResult(loyaltyRedeemScreenIntent,
				Common.REQUEST_CODE_LOYALTY_REDEEM_SCREEN);

	}
}
