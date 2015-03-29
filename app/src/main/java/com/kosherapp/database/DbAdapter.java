package com.kosherapp.database;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.kosherapp.Common;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteMisuseException;
import android.os.AsyncTask;
import android.util.Log;

public class DbAdapter {

	// Database fields
	public static final String						KEY_ROWID																		= "_id";
	public static final String						KEY_LOAD_DATE														= "loadDate";
	public static final String						KEY_LOAD_ID																= "loadId";
	public static final String						KEY_LOCATION_ID												= "locationId";
	public static final String						KEY_NAME																			= "name";
	public static final String						KEY_PHONE																		= "phone";
	public static final String						KEY_POST_ADDRESS											= "postAddress";
	public static final String						KEY_LONGITUDE														= "longitude";
	public static final String						KEY_LATITUDE															= "latitude";
	public static final String						KEY_DISCOUNT															= "discount";
	public static final String						KEY_WEBSITE																= "website";
	public static final String						KEY_LOCATION_TYPE										= "locationType";
	public static final String						KEY_FOOD_TYPE														= "foodType";
	public static final String						KEY_LOYALTY_ID													= "loyaltyId";

	public static final String						KEY_AD_ID																		= "adId";
	public static final String						KEY_NOTIFICATION_USED_DATE	= "dateUsed";

	private static final String					RESTAURANTS_TABLE										= "restaurant";
	private static final String					MINYAN_TABLE															= "minyan";
	private static final String					MIKVAH_TABLE															= "mikvah";
	private static final String					LOYALTY_TABLE														= "loyalty";
	private static final String					NOTIFICATION_TABLE									= "notification";
	private Context																	context;
	private SQLiteDatabase										databaseWriteable;
	private SQLiteDatabase										databaseReadOnly;
	private DatabaseHelper										dbHelper;

	private static final DateFormat	formatter																		= new SimpleDateFormat(
																																																													"yyyy-MM-dd HH:mm:ss.SSS");

	public DbAdapter(Context context) {
		this.context = context;
	}

	public void deleteDatabase() {
		this.DbHelper().deleteDatabase();

	}

	private DatabaseHelper DbHelper() {
		if (this.dbHelper == null) {
			dbHelper = new DatabaseHelper(context);
		}
		return this.dbHelper;
	}

	public void openForWrite() throws SQLException {
		String methodInfo = "<DbAdapter.openForWrite():void>";
		// Common.LogMethodStart(methodInfo);

        //getting Writable Database
		if (databaseWriteable != null) {
//            Boolean databaseWriteableIsOpen = databaseWriteable.isOpen();
//			if (databaseWriteableIsOpen) {
//				return;
//			}
		} else {

            try {
                databaseWriteable = DbHelper().getWritableDatabase();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            Common.Log(
                    methodInfo,
                    String.format("databaseWriteable == null: %s",
                            String.valueOf(databaseWriteable == null)));
            if (databaseWriteable == null) {
                return;
            }
        }

        //now that we have a Writable DB, begin transaction
        try {
            DatabaseWriteable().beginTransaction();
        } catch (IllegalStateException e) {
            Common.Log(methodInfo, Common.getString(e));
        } catch (SQLiteException e) {
            Common.Log(methodInfo, Common.getString(e));
        }
	}

	public boolean isWritableDatabaseAvailable() {
		if (databaseWriteable == null) {
			return false;
		}
		if (!databaseWriteable.isOpen()) {
			return false;
		}

		return true;
	}

	public void openReadOnly() throws SQLException {
		String methodInfo = "<DbAdapter.openReadOnly():void>";
		Common.LogMethodStart(methodInfo);

		if (databaseReadOnly != null) {
			if (databaseReadOnly.isOpen()) {
				return;
			}
		}

		OpenReadOnlyDatabaseTask openReadOnlyDatabaseTask = new OpenReadOnlyDatabaseTask();
		openReadOnlyDatabaseTask.execute();

		Common.Log(
			methodInfo,
			String.format("this.databaseReadOnly == null: %s",
				String.valueOf(this.databaseReadOnly == null)));
	}

	public boolean isReadOnlyDatabaseAvailable() {
		if (databaseReadOnly == null) {
			return false;
		}

		if (!databaseReadOnly.isOpen()) {
			return false;
		}

		return true;
	}

	private SQLiteDatabase DatabaseWriteable() {
		String methodInfo = "<DbAdapter.DatabaseWriteable():SQLiteDatabase>";
		// Common.LogMethodStart(methodInfo);

		return databaseWriteable;
	}

	private SQLiteDatabase DatabaseReadOnly() {
		String methodInfo = "<DbAdapter.DatabaseReadOnly():SQLiteDatabase>";
		Common.LogMethodStart(methodInfo);

		return databaseReadOnly;
	}

	public void close() {
		String methodInfo = "<DbAdapter.close():void>";
		Common.LogMethodStart(methodInfo);

		new CloseDatabaseTask().execute();
	}

	/**
	 * Add new locations. If the locations are successfully added return the new
	 * _id for that location, otherwise return a -1 to indicate failure.
	 */
	public long addRestaurant(Date loadDate, String loadId, int locationId,
		String name, String phone, String postAddress, Double longitude,
		Double latitude, String discount, String website, String locationType,
		int foodType, int loyaltyId) {
		ContentValues initialValues = createRestaurantContentValues(loadDate, loadId,
			locationId, name, phone, postAddress, longitude, latitude, discount,
			website, locationType, foodType, loyaltyId);

		long result = DatabaseWriteable().insert(RESTAURANTS_TABLE, null,
			initialValues);

		return result;
	}

	public long addLoyaltyLocation(Date loadDate, String loadId, int locationId,
		String name, String phone, String postAddress, Double longitude,
		Double latitude, String website, String locationType) {
		ContentValues initialValues = createLoyaltyContentValues(loadDate, loadId,
			locationId, name, phone, postAddress, longitude, latitude, website,
			locationType);

		long result = DatabaseWriteable().insert(LOYALTY_TABLE, null, initialValues);

		return result;
	}

	public long addMikvah(Date loadDate, String loadId, int locationId,
		String name, String phone, String postAddress, Double longitude,
		Double latitude, String discount, String website, String locationType,
		int foodType) {
		ContentValues initialValues = createContentValues(loadDate, loadId,
			locationId, name, phone, postAddress, longitude, latitude, discount,
			website, locationType, foodType);

		long result = DatabaseWriteable().insert(MIKVAH_TABLE, null, initialValues);

		return result;
	}

	public long addMinyan(Date loadDate, String loadId, int locationId,
		String name, String phone, String postAddress, Double longitude,
		Double latitude, String discount, String website, String locationType,
		int foodType) {
		ContentValues initialValues = createContentValues(loadDate, loadId,
			locationId, name, phone, postAddress, longitude, latitude, discount,
			website, locationType, foodType);

		long result = DatabaseWriteable().insert(MINYAN_TABLE, null, initialValues);

		return result;
	}

	/**
	 * Add new Notification. If the Notification is successfully added return the
	 * new _id for that location, otherwise return a -1 to indicate failure.
	 */
	public long addNotification(int adId, Date dateUsed) {
		String methodInfo = "<DbAdapter.addNotification(int, Date):long>";
		ContentValues initialValues = createNotificationContentValues(adId, dateUsed);

		long result = DatabaseWriteable().insert(NOTIFICATION_TABLE, null,
			initialValues);

		return result;
	}

	public boolean deleteLocations(String loadId) {
		boolean result;

		try {
			this.beginTransaction();

			result = DatabaseWriteable().delete(RESTAURANTS_TABLE,
				KEY_LOAD_ID + "='" + loadId + "'", null) > 0;
			if (!this.setTransactionSuccessful()) {
				return false;
			}
			result = DatabaseWriteable().delete(MIKVAH_TABLE,
				KEY_LOAD_ID + "='" + loadId + "'", null) > 0;
			if (!this.setTransactionSuccessful()) {
				return false;
			}
			result = DatabaseWriteable().delete(MINYAN_TABLE,
				KEY_LOAD_ID + "='" + loadId + "'", null) > 0;
			if (!this.setTransactionSuccessful()) {
				return false;
			}
			result = DatabaseWriteable().delete(LOYALTY_TABLE,
				KEY_LOAD_ID + "='" + loadId + "'", null) > 0;
			if (!this.setTransactionSuccessful()) {
				return false;
			}
			if (!this.endTransaction()) {
				return false;
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return false;
		}
		return result;
	}

	/**
	 * Return a Cursor over the list
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllRestaurants(String loadId) {
		String methodInfo = "<DbAdapter.fetchAllRestaurants(String):Cursor>";
		Common.LogMethodStart(methodInfo);
		String whereQuery = KEY_LOAD_ID + "='" + loadId + "'";
		Common.Log(methodInfo, String.format("whereQuery: %s", whereQuery));

		if (loadId == null) {
			Common.Log(methodInfo,
				String.format("loadId == null: %s", String.valueOf(loadId == null)));
			return null;
		}
		if (loadId.equals("")) {
			Common
				.Log(methodInfo, String.format("loadId.equals(\"\"): %s",
					String.valueOf(loadId.equals(""))));
		}

		Cursor result = null;
		try {
			result = DatabaseReadOnly().query(RESTAURANTS_TABLE, null, whereQuery, null,
				null, null, null);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return null;
		} catch (SQLiteMisuseException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}

	public Cursor fetchAllMikvahs(String loadId) {
		String methodInfo = "<DbAdapter.fetchAllMikvahs(String):Cursor>";
		Common.LogMethodStart(methodInfo);
		String whereQuery = KEY_LOAD_ID + "='" + loadId + "'";
		Common.Log(methodInfo, String.format("whereQuery: %s", whereQuery));

		if (loadId == null) {
			Common.Log(methodInfo,
				String.format("loadId == null: %s", String.valueOf(loadId == null)));
			return null;
		}
		if (loadId.equals("")) {
			Common
				.Log(methodInfo, String.format("loadId.equals(\"\"): %s",
					String.valueOf(loadId.equals(""))));
		}

		Cursor result = null;
		try {
			result = DatabaseReadOnly().query(MIKVAH_TABLE, null, whereQuery, null,
				null, null, null);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return null;
		} catch (SQLiteMisuseException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}

	public Cursor fetchAllMinyans(String loadId) {
		String methodInfo = "<DbAdapter.fetchAllMinyans(String):Cursor>";
		Common.LogMethodStart(methodInfo);
		String whereQuery = KEY_LOAD_ID + "='" + loadId + "'";
		Common.Log(methodInfo, String.format("whereQuery: %s", whereQuery));

		if (loadId == null) {
			Common.Log(methodInfo,
				String.format("loadId == null: %s", String.valueOf(loadId == null)));
			return null;
		}
		if (loadId.equals("")) {
			Common
				.Log(methodInfo, String.format("loadId.equals(\"\"): %s",
					String.valueOf(loadId.equals(""))));
		}

		Cursor result = null;
		try {
			result = DatabaseReadOnly().query(MINYAN_TABLE, null, whereQuery, null,
				null, null, null);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return null;
		} catch (SQLiteMisuseException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}

	/**
	 * Deletes todo
	 */
	public boolean deleteNotificationsByEarliestDate(Date earliestDate) {
		String earliestDateFormatted = getFormattedDate(earliestDate);
		boolean result = DatabaseWriteable().delete(NOTIFICATION_TABLE,
			KEY_NOTIFICATION_USED_DATE + "<'" + earliestDateFormatted + "'", null) > 0;

		return result;
	}

	/**
	 * Return a Cursor over the list of all todo in the database
	 * 
	 * @return Cursor over all notes
	 */
	public Cursor fetchAllNotificationsByEarliestDate(Date earliestDate) {
		String methodInfo = "<DbAdapter.fetchAllNotificationsByEarliestDate(Date):Cursor>";
		Common.LogMethodStart(methodInfo);
		String earliestDateFormatted = getFormattedDate(earliestDate);
		String whereQuery = KEY_NOTIFICATION_USED_DATE + ">='"
			+ earliestDateFormatted + "'";
		Common.Log(methodInfo, String.format("whereQuery:%s", whereQuery));

		Cursor result = null;
		try {
			result = DatabaseReadOnly().query(NOTIFICATION_TABLE, null, whereQuery,
				null, null, null, null);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalMonitorStateException e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}

	public Cursor fetchAllNotificationsSortedByDateDesc() {
		Cursor result = null;
		try {
			result = DatabaseReadOnly().query(NOTIFICATION_TABLE, null, null, null,
				null, null, KEY_NOTIFICATION_USED_DATE + " DESC");
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
		return result;
	}

	private ContentValues createContentValues(Date loadDate, String loadId,
		int locationId, String name, String phone, String postAddress,
		Double longitude, Double latitude, String discount, String website,
		String locationType, int foodType) {
		String loadDateString = getFormattedDate(loadDate);

		ContentValues values = new ContentValues();
		values.put(KEY_LOAD_DATE, loadDateString);
		values.put(KEY_LOAD_ID, loadId);
		values.put(KEY_LOCATION_ID, locationId);
		values.put(KEY_NAME, name);
		values.put(KEY_PHONE, phone);
		values.put(KEY_POST_ADDRESS, postAddress);
		values.put(KEY_LONGITUDE, longitude);
		values.put(KEY_LATITUDE, latitude);
		values.put(KEY_DISCOUNT, discount);
		values.put(KEY_WEBSITE, website);
		values.put(KEY_LOCATION_TYPE, locationType);
		values.put(KEY_FOOD_TYPE, foodType);

		return values;
	}

	private ContentValues createRestaurantContentValues(Date loadDate,
		String loadId, int locationId, String name, String phone, String postAddress,
		Double longitude, Double latitude, String discount, String website,
		String locationType, int foodType, int loyaltyId) {
		String loadDateString = getFormattedDate(loadDate);

		ContentValues values = new ContentValues();
		values.put(KEY_LOAD_DATE, loadDateString);
		values.put(KEY_LOAD_ID, loadId);
		values.put(KEY_LOCATION_ID, locationId);
		values.put(KEY_NAME, name);
		values.put(KEY_PHONE, phone);
		values.put(KEY_POST_ADDRESS, postAddress);
		values.put(KEY_LONGITUDE, longitude);
		values.put(KEY_LATITUDE, latitude);
		values.put(KEY_DISCOUNT, discount);
		values.put(KEY_WEBSITE, website);
		values.put(KEY_LOCATION_TYPE, locationType);
		values.put(KEY_FOOD_TYPE, foodType);
		values.put(KEY_LOYALTY_ID, loyaltyId);

		return values;
	}

	private ContentValues createLoyaltyContentValues(Date loadDate, String loadId,
		int locationId, String name, String phone, String postAddress,
		Double longitude, Double latitude, String website, String locationType) {
		String loadDateString = getFormattedDate(loadDate);

		ContentValues values = new ContentValues();
		values.put(KEY_LOAD_DATE, loadDateString);
		values.put(KEY_LOAD_ID, loadId);
		values.put(KEY_LOCATION_ID, locationId);
		values.put(KEY_NAME, name);
		values.put(KEY_PHONE, phone);
		values.put(KEY_POST_ADDRESS, postAddress);
		values.put(KEY_LONGITUDE, longitude);
		values.put(KEY_LATITUDE, latitude);
		values.put(KEY_WEBSITE, website);
		values.put(KEY_LOCATION_TYPE, locationType);

		return values;
	}

	private ContentValues createNotificationContentValues(int adId, Date date) {
		String dateString = getFormattedDate(date);

		ContentValues values = new ContentValues();
		values.put(KEY_AD_ID, adId);
		values.put(KEY_NOTIFICATION_USED_DATE, dateString);

		return values;
	}

	private static String getFormattedDate(Date date) {
		String formattedDate = "";
		if (date == null)
			return formattedDate;

		formattedDate = formatter.format(date);

		return formattedDate;
	}

	public static Date parseDateString(String dateString) {
		Date date = null;
		if (dateString == null)
			return null;
		if (dateString.equals(""))
			return null;

		try {
			date = formatter.parse(dateString);
		} catch (ParseException e) {
			Log.d(Common.LOG_TAG, Common.getString(e));
		}

		return date;
	}

	public void beginTransaction() {
		String methodInfo = "<DbAdapter.beginTransaction():void>";
		// Common.LogMethodStart(methodInfo);

		openForWrite();
    }

    public Boolean endTransaction() {
		try {
			DatabaseWriteable().endTransaction();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		}
		// close();
		return true;
	}

	public Boolean setTransactionSuccessful() {
		try {
			DatabaseWriteable().setTransactionSuccessful();
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return false;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public Cursor fetchNotification(int adId) {
		String methodInfo = "<DbAdapter.fetchNotification(int):Cursor>";

		String whereQuery = KEY_AD_ID + "=" + String.valueOf(adId);
		Common.Log(methodInfo, String.format("whereQuery: %s", whereQuery));

		Cursor result = DatabaseReadOnly().query(NOTIFICATION_TABLE, null,
			whereQuery, null, null, null, null);

		return result;
	}

	private class CloseDatabaseTask extends AsyncTask<String, Integer, Integer> {

		protected Integer doInBackground(String... listInfo) {
			String methodInfo = "<DbAdapter.CloseDatabaseTask.doInBackground(String...):Integer>";
			Common.Log(methodInfo, "START");

			int result = 0;
			DbHelper().close();

			Common.Log(methodInfo, "END");
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			String methodInfo = "<DbAdapter.CloseDatabaseTask.onPostExecute(Integer):void>";
			Common.Log(methodInfo, "START");
			super.onPostExecute(result);
			Common.Log(methodInfo, "END");
		}
	}

	private class OpenReadOnlyDatabaseTask extends
		AsyncTask<String, Integer, Integer> {

		protected Integer doInBackground(String... listInfo) {
			String methodInfo = "<DbAdapter.OpenReadOnlyDatabaseTask.doInBackground(String...):Integer>";
			Common.Log(methodInfo, "START");

			int result = 0;

			try {
				databaseReadOnly = DbHelper().getReadableDatabase();
			} catch (SQLiteException e) {
				Common.Log(methodInfo, Common.getString(e));
				return 1;
			}
			if (databaseReadOnly == null) {
				Common.Log(
					methodInfo,
					String.format("databaseReadOnly == null: %s",
						String.valueOf(databaseReadOnly == null)));
				return 1;
			}

			Common.Log(methodInfo, "END");
			return result;
		}

		@Override
		protected void onPostExecute(Integer result) {
			String methodInfo = "<DbAdapter.OpenReadOnlyDatabaseTask.onPostExecute(Integer):void>";
			Common.Log(methodInfo, "START");
			super.onPostExecute(result);
			Common.Log(methodInfo, "END");
		}
	}

	public Cursor fetchAllLoyaltyLocations(String loadId) {
		String methodInfo = "<DbAdapter.fetchAllLoyaltyLocations(String):Cursor>";
		Common.LogMethodStart(methodInfo);
		String whereQuery = KEY_LOAD_ID + "='" + loadId + "'";
		Common.Log(methodInfo, String.format("whereQuery: %s", whereQuery));

		if (loadId == null) {
			Common.Log(methodInfo,
				String.format("loadId == null: %s", String.valueOf(loadId == null)));
			return null;
		}
		if (loadId.equals("")) {
			Common
				.Log(methodInfo, String.format("loadId.equals(\"\"): %s",
					String.valueOf(loadId.equals(""))));
		}

		Cursor result = null;
		try {
			result = DatabaseReadOnly().query(LOYALTY_TABLE, null, whereQuery, null,
				null, null, null);
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return null;
		} catch (SQLiteMisuseException e) {
			e.printStackTrace();
			return null;
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		}

		return result;
	}

}
