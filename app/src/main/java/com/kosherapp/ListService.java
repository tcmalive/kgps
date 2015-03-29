/**
 * 
 */
package com.kosherapp;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.http.util.ByteArrayBuffer;

import android.content.Context;
import android.util.Log;

/**
 * @author daniel.wunder
 * 
 */
public class ListService {
	private Context	context	= null;

	public ListService(Context context) {
		this.context = context;
	}

	public String RetrieveList(String listType, double latitude, double longitude,
		String mode) {
		String methodInfo = "<ListService.RetrieveList(String, double, double, String):String>";
		String toReturn = "";
		if (latitude == Double.MIN_VALUE || longitude == Double.MIN_VALUE)
			return toReturn;

		try {
			String dataController = this.context.getResources().getString(
				R.string.data_controller_v2);
			String address = dataController + "?listType=" + listType.toLowerCase()
				+ "&location=" + String.valueOf(latitude) + "," + String.valueOf(longitude)
				+ "&mode=" + mode.toLowerCase();
			Common.Log(methodInfo, String.format("address: %s", address));
			URL updateURL = new URL(address);
			URLConnection conn = updateURL.openConnection();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is, 8192);
			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}
			/* Convert the Bytes read to a String. */
			toReturn = new String(baf.toByteArray());
			// Common.Log(methodInfo, String.format("DB Content: %s", toReturn));
		} catch (Exception e) {
			toReturn = e.getMessage();
		}
		return toReturn;
	}

	public String RetrieveListLoyaltyCards(int loyaltyId, String deviceId) {
		String methodInfo = "<ListService.RetrieveListLoyaltyCards(int, String):String>";
		Common.LogMethodStart(methodInfo);
		String toReturn = "";
		String listType = "loyaltycards";
		if (loyaltyId < 0) {
			return "";
		}
		if (deviceId == null) {
			return "";
		}
		if (deviceId.equals("")) {
			return "";
		}

		try {
			String dataController = this.context.getResources().getString(
				R.string.data_controller_v3);
			String address = dataController + "?listType=" + listType + "&loyaltyId="
				+ String.valueOf(loyaltyId) + "&deviceId=" + deviceId;
			Common.Log(methodInfo, String.format("address: %s", address));
			URL updateURL = new URL(address);
			URLConnection conn = updateURL.openConnection();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is, 8192);
			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}
			/* Convert the Bytes read to a String. */
			toReturn = new String(baf.toByteArray());
			// Common.Log(methodInfo, String.format("DB Content: %s", toReturn));
		} catch (Exception e) {
			toReturn = e.getMessage();
		}
		return toReturn;
	}

	public String LoyaltyPunch(int loyaltyId, int loyaltyItemId, Double latitude,
		Double longitude, String deviceId, Double punchAmt, String code) {
		String methodInfo = "<ListService.LoyaltyPunch(int, int, Double, Double, String, Double, String):String>";
		Common.LogMethodStart(methodInfo);
		String toReturn = "";
		String mode = "punch";
		if (loyaltyId < 0) {
			throw new IllegalArgumentException(String.format("loyaltyId: %s",
				String.valueOf(loyaltyId)));
		}
		if (deviceId == null) {
			throw new IllegalArgumentException("deviceId == null");
		}
		if (deviceId.equals("")) {
			throw new IllegalArgumentException(String.format("deviceId: %s", deviceId));
		}

		try {
			String dataController = this.context.getResources().getString(
				R.string.loyalty_controller_v1);
			String address = String
				.format(
					"%s?mode=%s&loyaltyId=%s&loyaltyItemId=%s&latitude=%s&longitude=%s&deviceId=%s&punchAmt=%s&code=%s",
					dataController, mode, String.valueOf(loyaltyId),
					String.valueOf(loyaltyItemId), String.valueOf(latitude),
					String.valueOf(longitude), deviceId, String.valueOf(punchAmt), code);
			Common.Log(methodInfo, String.format("address: %s", address));
			URL updateURL = new URL(address);
			URLConnection conn = updateURL.openConnection();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is, 8192);
			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}
			/* Convert the Bytes read to a String. */
			toReturn = new String(baf.toByteArray());
			// Common.Log(methodInfo, String.format("DB Content: %s", toReturn));
		} catch (Exception e) {
			toReturn = e.getMessage();
		}
		return toReturn;
	}

	public String LoyaltyRedeem(int loyaltyId, int loyaltyItemId, Double latitude,
		Double longitude, String deviceId, String code) {
		String methodInfo = "<ListService.LoyaltyRedeem(int, int, Double, Double, String, String):String>";
		Common.LogMethodStart(methodInfo);
		String toReturn = "";
		String mode = "redeem";
		if (loyaltyId < 0) {
			return "";
		}
		if (deviceId == null) {
			return "";
		}
		if (deviceId.equals("")) {
			return "";
		}

		try {
			String dataController = this.context.getResources().getString(
				R.string.loyalty_controller_v1);
			String address = String
				.format(
					"%s?mode=%s&loyaltyId=%s&loyaltyItemId=%s&latitude=%s&longitude=%s&deviceId=%s&code=%s",
					dataController, mode, String.valueOf(loyaltyId),
					String.valueOf(loyaltyItemId), String.valueOf(latitude),
					String.valueOf(longitude), deviceId, code);
			Common.Log(methodInfo, String.format("address: %s", address));
			URL updateURL = new URL(address);
			URLConnection conn = updateURL.openConnection();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is, 8192);
			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}
			/* Convert the Bytes read to a String. */
			toReturn = new String(baf.toByteArray());
			// Common.Log(methodInfo, String.format("DB Content: %s", toReturn));
		} catch (Exception e) {
			toReturn = e.getMessage();
		}
		return toReturn;
	}

	public String getSubscriptionStatus(String deviceId) {
		String methodInfo = "<ListService.getSubscriptionStatus(String):String>";
		String subscriptionStatus = "";

		// http://kosherrestaurantsgps.com/iphone_subscription_controller.php?mode=lookup&device_id=12345
		try {
			String subscriptionController = this.context.getResources().getString(
				R.string.subscription_controller);
			String address = subscriptionController + "?mode=lookup&device_id="
				+ deviceId;
			URL updateURL = new URL(address);
			URLConnection conn = updateURL.openConnection();
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is);
			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			/* Convert the Bytes read to a String. */
			subscriptionStatus = new String(baf.toByteArray());
		} catch (Exception e) {
			subscriptionStatus = e.getMessage();
		}
		Common.Log(methodInfo,
			String.format("subscriptionStatus: %s", subscriptionStatus));
		return subscriptionStatus;
	}

	public String register(String emailAddress, String state, String deviceId,
		String platform) {
		String methodInfo = "<ListService.register(String, String, String):String>";
		String registrationResult = "";

		// http://kosherrestaurantsgps.com/iphone_subscription_controller.php?mode=register&email=email@address.com&device_id=12345
		try {
			String subscriptionController = this.context.getResources().getString(
				R.string.subscription_controller);
			String address = subscriptionController + "?mode=register&email="
				+ emailAddress + "&device_id=" + deviceId + "&state=" + state
				+ "&platform=" + platform;
			address = address.replace(" ", "%20");
			Common.Log(methodInfo, "address: " + address);
			URL updateURL = new URL(address);
			URLConnection conn = updateURL.openConnection();
			InputStream is = conn.getInputStream();
			Common.Log(methodInfo, "got the input stream");
			BufferedInputStream bis = new BufferedInputStream(is);
			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			/* Convert the Bytes read to a String. */
			registrationResult = new String(baf.toByteArray());
		} catch (Exception e) {
			registrationResult = e.getMessage();
		}
		Common.Log(methodInfo, "registrationResult: " + registrationResult);
		return registrationResult;
	}

	public String resubscribe(String deviceId) {
		String methodInfo = "<ListService.resubscribe(String):String>";

		String subscriptionResult = "";

		// http://kosherrestaurantsgps.com/iphone_subscription_controller.php?mode=register&email=email@address.com&device_id=12345
		try {
			String subscriptionController = this.context.getResources().getString(
				R.string.subscription_controller);
			String address = subscriptionController + "?mode=resubscribe&device_id="
				+ deviceId;
			Common.Log(methodInfo, "address: " + address);
			URL updateURL = new URL(address);
			URLConnection conn = updateURL.openConnection();
			InputStream is = conn.getInputStream();
			Common.Log(methodInfo, "got the input stream");
			BufferedInputStream bis = new BufferedInputStream(is);
			ByteArrayBuffer baf = new ByteArrayBuffer(50);

			int current = 0;
			while ((current = bis.read()) != -1) {
				baf.append((byte) current);
			}

			/* Convert the Bytes read to a String. */
			subscriptionResult = new String(baf.toByteArray());
		} catch (Exception e) {
			subscriptionResult = e.getMessage();
		}
		Common.Log(methodInfo, "subscriptionResult: " + subscriptionResult);
		return subscriptionResult;
	}
}

class RetrieveListArgs {
	public String	ListType;
	public double	Latitude;
	public double	Longitude;
	public String	Mode;
	public String	ListValue;
}
