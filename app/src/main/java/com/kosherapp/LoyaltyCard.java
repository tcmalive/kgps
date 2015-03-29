package com.kosherapp;

import java.util.Arrays;
import java.util.Date;

public class LoyaltyCard {

	private int					placeId;
	private int					itemId;
	private String		name;
	private String		description;
	private Double		defaultPunchAmt;
	private Double		totalValue;
	private Double		loyaltyCardRedemptions;
	private Double		redemptionAmt;
	private Double		effectiveValue;
	private Boolean	canRedeem;
	private Date				latestRedemption;

	public LoyaltyCard(int placeId, int itemId, String name, String description,
		Double defaultPunchAmt, Double totalValue, Double loyaltyCardRedemptions,
		Double redemptionAmt, Double effectiveValue, Boolean canRedeem,
		Date latestRedemption) {
		this.placeId = placeId;
		this.itemId = itemId;
		this.name = name;
		this.description = description;
		this.defaultPunchAmt = defaultPunchAmt;
		this.totalValue = totalValue;
		this.loyaltyCardRedemptions = loyaltyCardRedemptions;
		this.redemptionAmt = redemptionAmt;
		this.effectiveValue = effectiveValue;
		this.canRedeem = canRedeem;
		this.latestRedemption = latestRedemption;
	}

	public int ItemId() {
		String methodInfo = "LoyaltyCard.ItemId():int";
		Common.Log(methodInfo,
			String.format("this.itemId: %s", String.valueOf(this.itemId)));
		return this.itemId;
	}

	public int PlaceId() {
		String methodInfo = "LoyaltyCard.PlaceId():int";
		Common.Log(methodInfo,
			String.format("this.placeId: %s", String.valueOf(this.placeId)));
		return this.placeId;
	}

	public String Name() {
		return this.name;
	}

	public String Description() {
		return this.description;
	}

	public Double PunchesUsed() {
		return this.effectiveValue;
	}

	public Double PunchesNeeded() {
		return this.redemptionAmt;
	}

	public Double PunchesRemaining() {
		return this.redemptionAmt - this.effectiveValue;
	}

	public Date getRecentRedemption() {
		return this.latestRedemption;
	}

	public Double RedemptionCount() {
		return this.loyaltyCardRedemptions;
	}

	public Double DefaultPunchAmt() {
		return this.defaultPunchAmt;
	}

	public Boolean CanRedeem() {
		return this.canRedeem;
	}

}
