package com.kosherapp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import android.util.Log;

public class ListParser {

	private static final Object	ID																																					= "id";
	private static final Object	NAME																																			= "name";
	private static final Object	PHONE																																		= "phone";
	private static final Object	POSTADDRESS																												= "postAddress";
	private static final Object	LONGITUDE																														= "longitude";
	private static final Object	LATITUDE																															= "latitude";
	private static final Object	DISCOUNT																															= "discount";
	private static final Object	WEBSITE																																= "website";
	private static final Object	FOODTYPE																															= "foodType";
	private static final Object	LOYALTYID																														= "LoyaltyId";
	private static final Object	LOYALTYITEMID																										= "LoyaltyItemId";
	private static final Object	LOYALTYNAME																												= "Name";
	private static final Object	LOYALTYCARDDESCRIPTION																	= "Description";
	private static final Object	LOYALTYCARDDEFAULTPUNCHAMT													= "DefaultPunchAmt";
	private static final Object	LOYALTYCARDTOTALVALUE																		= "TotalValue";
	private static final Object	LOYALTYCARDREDEMPTIONS																	= "Redemptions";
	private static final Object	LOYALTYCARDREDEMPTIONAMT															= "RedemptionAmt";
	private static final Object	LOYALTYCARDEFFECTIVEVALUE														= "EffectiveValue";
	private static final Object	LOYALTYCARDCANREDEEM																			= "CanRedeem";
	private static final Object	LOYALTYCARDLATESTLOYALTYREDEMPTIONDATE	= "LatestLoyaltyRedemptionDate";
	private static final Object	LOYALTYPHONE																											= "Phone";
	private static final Object	LOYALTYADDRESS																									= "Address";
	private static final Object	LOYALTYWEBSITE																									= "Website";
	private static final Object	LOYALTYDISTANCE																								= "Distance";
	private static final Object	LOYALTYLONGITUDE																							= "longitude";
	private static final Object	LOYALTYLATITUDE																								= "latitude";

	public static List<KosherPlace> ParseList(String listValue, String type) {
		String methodInfo = "<ListParser.ParseList(String, String): List<KosherPlace>>";
		Common.Log(methodInfo, "START");

		Common.Log(methodInfo, String.format("type: %s", type));

		if (listValue == null) {
			Common.Log(methodInfo,
				String.format("ListValue == null: %s", String.valueOf(listValue == null)));
			return null;
		}
		if (listValue.equals("")) {
			Common.Log(
				methodInfo,
				String.format("ListValue.equals(\"\"): %s",
					String.valueOf(listValue.equals(""))));
			return null;
		}

		ArrayList<HashMap<String, String>> records = JSONParser.parseJSON(listValue);

		List<KosherPlace> locations = new ArrayList<KosherPlace>();

		// Common.Log(methodInfo, String.format("records.size(): %s", String
		// .valueOf(records.size())));
        FoodTypeEnumerator foodTypeEnumerator = new FoodTypeEnumerator();

        for (int i = 0; i < records.size(); i++) {

			HashMap<String, String> locationMap = records.get(i);
			// Common.Log(methodInfo, String.format("locationMap.size(): %s", String
			// .valueOf(locationMap.size())));
			KosherPlace place = null;
			if (type.equals(Common.LIST_TYPE_LOYALTY)) {
				String idString = locationMap.get(LOYALTYID);
				Common.Log(methodInfo, String.format("idString: %s", idString));
				int id = Integer.parseInt(idString);

				String name = locationMap.get(LOYALTYNAME);
				// Common.Log(methodInfo, String.format("name: %s", name));

				String phone = locationMap.get(LOYALTYPHONE);
				// Common.Log(methodInfo, String.format("phone: %s", phone));

				String postAddress = locationMap.get(LOYALTYADDRESS);
				// Common.Log(methodInfo, String.format("postAddress: %s", postAddress));

				String website = locationMap.get(LOYALTYWEBSITE);

				String longitudeStr = locationMap.get(LOYALTYLONGITUDE);
				Double longitude = Double.parseDouble(longitudeStr);

				String latitudeStr = locationMap.get(LOYALTYLATITUDE);
				Double latitude = Double.parseDouble(latitudeStr);

				place = new KosherPlace(id, name, phone, postAddress, longitude, latitude,
					null, website, type, 0, id, foodTypeEnumerator);

			} else {
				String foodTypeString = locationMap.get(FOODTYPE);
				int foodType = 0;
				if (foodTypeString != null) {
					foodType = Integer.parseInt(foodTypeString);
				}
				String idString = locationMap.get(ID);
				// Common.Log(methodInfo, String.format("idString: %s", idString));
				int id = Integer.parseInt(idString);

				String loyaltyIdString = locationMap.get(LOYALTYID);
//				Common.Log(methodInfo,
//					String.format("loyaltyIdString: %s", loyaltyIdString));
				int loyaltyId;
				if (loyaltyIdString == null) {
					loyaltyId = Integer.MIN_VALUE;
				} else if (loyaltyIdString.equals("")) {
					loyaltyId = Integer.MIN_VALUE;
				} else {
					loyaltyId = Integer.parseInt(loyaltyIdString);
				}

				String name = locationMap.get(NAME);
				// Common.Log(methodInfo, String.format("name: %s", name));

				String phone = locationMap.get(PHONE);
				// Common.Log(methodInfo, String.format("phone: %s", phone));

				String postAddress = locationMap.get(POSTADDRESS);
				// Common.Log(methodInfo, String.format("postAddress: %s", postAddress));

				String longitudeString = locationMap.get(LONGITUDE);
				// Common
				// .Log(methodInfo, String.format("longitudeString: %s", longitudeString));
		
				Double longitude = Double.parseDouble(longitudeString);

				String latitudeString = locationMap.get(LATITUDE);
				// Common.Log(methodInfo, String.format("latitudeString: %s",
				// latitudeString));
				Double latitude = Double.parseDouble(latitudeString);

				String discount = locationMap.get(DISCOUNT);
				// Common.Log(methodInfo, String.format("discount: %s", discount));

				String website = locationMap.get(WEBSITE);
				// Common.Log(methodInfo, String.format("website: %s", website));
				place = new KosherPlace(id, name, phone, postAddress, longitude, latitude,
					discount, website, type, foodType, loyaltyId, foodTypeEnumerator);
			}
			locations.add(place);

		}

		Common.Log(methodInfo, "Done parsing location data...");
		Common.Log(methodInfo,
			String.format("locations.size(): %s", String.valueOf(locations.size())));
		Common.Log(methodInfo, "END");

		return locations;
	}

	public static List<LoyaltyCard> ParseLoyaltyCards(String listValue) {
		String methodInfo = "<ListParser.ParseLoyaltyCards(String): List<LoyaltyCard>>";

		Common.Log(methodInfo, "START");
		if (listValue == null) {
			Common.Log(methodInfo,
				String.format("ListValue == null: %s", String.valueOf(listValue == null)));
			return null;
		}
		if (listValue.equals("")) {
			Common.Log(
				methodInfo,
				String.format("ListValue.equals(\"\"): %s",
					String.valueOf(listValue.equals(""))));
			return null;
		}

		ArrayList<HashMap<String, String>> records = JSONParser.parseJSON(listValue);

		List<LoyaltyCard> locations = new ArrayList<LoyaltyCard>();

		Common.Log(methodInfo,
			String.format("records.size(): %s", String.valueOf(records.size())));
		for (int i = 0; i < records.size(); i++) {

			HashMap<String, String> locationMap = records.get(i);
			// Common
			// .Log(methodInfo, String.format("locationMap.size(): %s",
			// String.valueOf(locationMap.size())));

			LoyaltyCard place = null;
			String idString = locationMap.get(LOYALTYID);
			Common.Log(methodInfo, String.format("idString: %s", idString));
			int id = Integer.parseInt(idString);

			String itemIdString = locationMap.get(LOYALTYITEMID);
			Common.Log(methodInfo, String.format("itemIdString: %s", itemIdString));
			int itemId = Integer.parseInt(itemIdString);

			String name = locationMap.get(LOYALTYNAME);
			// Common.Log(methodInfo, String.format("name: %s", name));

			String description = locationMap.get(LOYALTYCARDDESCRIPTION);
			// Common.Log(methodInfo, String.format("description: %s", description));

			String defaultPunchAmtStr = locationMap.get(LOYALTYCARDDEFAULTPUNCHAMT);
			Common.Log(methodInfo,
				String.format("defaultPunchAmtStr: %s", defaultPunchAmtStr));
			Double defaultPunchAmt = Double.parseDouble(defaultPunchAmtStr);

			String totalValueStr = locationMap.get(LOYALTYCARDTOTALVALUE);
			// Common.Log(methodInfo, String.format("totalValueStr: %s", totalValueStr));
			Double totalValue = 0.0000;
			if (!totalValueStr.equals("")) {
				totalValue = Double.parseDouble(totalValueStr);
			}

			String loyaltyCardRedemptionsStr = locationMap.get(LOYALTYCARDREDEMPTIONS);
			// Common.Log(methodInfo,
			// String.format("loyaltyCardRedemptionsStr: %s",
			// loyaltyCardRedemptionsStr));
			Double loyaltyCardRedemptions = 0.0000;
			if (!loyaltyCardRedemptionsStr.equals("")) {
				loyaltyCardRedemptions = Double.parseDouble(loyaltyCardRedemptionsStr);
			}

			String redemptionAmtStr = locationMap.get(LOYALTYCARDREDEMPTIONAMT);
			// Common.Log(methodInfo,
			// String.format("redemptionAmtStr: %s", redemptionAmtStr));
			Double redemptionAmt = Double.parseDouble(redemptionAmtStr);

			String effectiveValueStr = locationMap.get(LOYALTYCARDEFFECTIVEVALUE);
			Common.Log(methodInfo,
				String.format("effectiveValueStr: %s", effectiveValueStr));
			Double effectiveValue = 0.0000;
			if (!effectiveValueStr.equals("")) {
				effectiveValue = Double.parseDouble(effectiveValueStr);
			}

			String canRedeemStr = locationMap.get(LOYALTYCARDCANREDEEM);
			// Common.Log(methodInfo, String.format("canRedeemStr: %s", canRedeemStr));
			Boolean canRedeem = false;
			if (canRedeemStr.equals("Y")) {
				canRedeem = true;
			}
			if (canRedeemStr.equals("N")) {
				canRedeem = false;
			}

			String latestRedemptionStr = locationMap
				.get(LOYALTYCARDLATESTLOYALTYREDEMPTIONDATE);
			// Common.Log(methodInfo,
			// String.format("latestRedemptionStr: %s", latestRedemptionStr));
			Date latestRedemption = null;
			if (latestRedemptionStr == null) {
				latestRedemption = null;
			}

			place = new LoyaltyCard(id, itemId, name, description, defaultPunchAmt,
				totalValue, loyaltyCardRedemptions, redemptionAmt, effectiveValue,
				canRedeem, latestRedemption);

			locations.add(place);
		}

		Common.Log(methodInfo, "Done parsing location data...");
		Common.Log(methodInfo,
			String.format("locations.size(): %s", String.valueOf(locations.size())));
		Common.Log(methodInfo, "END");

		return locations;
	}
}
