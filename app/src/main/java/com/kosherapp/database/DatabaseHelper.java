package com.kosherapp.database;

import com.kosherapp.Common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

	private Context													context																			= null;

	private static final String	DATABASE_NAME													= "applicationdata";

	private static final int				DATABASE_VERSION										= 5;

	// Database creation sql statement
	// [{"id":"698","name":"K-Burger","phone":"(718)544-6755","postAddress":"74-45 Main Street Flushing NY 11367","longitude":"-73.819675","latitude":"40.723039","distance":"0.11","discount":"","website":""}]
	// [{"id":"1323","name":"10 Bis(Meat)","phone":"(718)382-8222","postAddress":"1733 Coney Island Ave Brooklyn NY","longitude":"-73.962977","latitude":"40.615065","discount":"","website":""},{"

	private static final String	RESTAURANT_TABLE_CREATE			= "create table if not exists restaurant "
																																																								+ "(_id integer primary key autoincrement"
																																																								+ ", loadDate text not null"
																																																								+ ", loadId text not null"
																																																								+ ", locationId integer not null"
																																																								+ ", name text not null"
																																																								+ ", phone text not null"
																																																								+ ", postAddress text not null"
																																																								+ ", longitude real not null"
																																																								+ ", latitude real not null"
																																																								+ ", discount text"
																																																								+ ", website text"
																																																								+ ", foodType integer not null"
																																																								+ ", locationType text"
																																																								+ ", loyaltyId integer not null"
																																																								+ ");";

	private static final String	LOYALTY_TABLE_CREATE						= "create table if not exists loyalty "
																																																								+ "(_id integer primary key autoincrement"
																																																								+ ", loadDate text not null"
																																																								+ ", loadId text not null"
																																																								+ ", locationId integer not null"
																																																								+ ", name text not null"
																																																								+ ", phone text not null"
																																																								+ ", postAddress text not null"
																																																								+ ", longitude real not null"
																																																								+ ", latitude real not null"
																																																								+ ", website text"
																																																								+ ", locationType text"
																																																								+ ", loyaltyCards text"
																																																								+ ");";

	private static final String	MINYAN_TABLE_CREATE							= "create table if not exists minyan "
																																																								+ "(_id integer primary key autoincrement"
																																																								+ ", loadDate text not null"
																																																								+ ", loadId text not null"
																																																								+ ", locationId integer not null"
																																																								+ ", name text not null"
																																																								+ ", phone text not null"
																																																								+ ", postAddress text not null"
																																																								+ ", longitude real not null"
																																																								+ ", latitude real not null"
																																																								+ ", discount text"
																																																								+ ", website text"
																																																								+ ", foodType integer not null"
																																																								+ ", locationType text"
																																																								+ ");";

	private static final String	MIKVAH_TABLE_CREATE							= "create table if not exists mikvah "
																																																								+ "(_id integer primary key autoincrement"
																																																								+ ", loadDate text not null"
																																																								+ ", loadId text not null"
																																																								+ ", locationId integer not null"
																																																								+ ", name text not null"
																																																								+ ", phone text not null"
																																																								+ ", postAddress text not null"
																																																								+ ", longitude real not null"
																																																								+ ", latitude real not null"
																																																								+ ", discount text"
																																																								+ ", website text"
																																																								+ ", foodType integer not null"
																																																								+ ", locationType text"
																																																								+ ");";

	private static final String	NOTIFICATION_TABLE_CREATE	= "create table if not exists notification "
																																																								+ "(_id integer primary key autoincrement"
																																																								+ ", adId integer not null"
																																																								+ ", dateUsed Integer"
																																																								+ " not null" + ");";

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	public void deleteDatabase() {
		this.context.deleteDatabase(DATABASE_NAME);
	}

	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase database) {
		String methodInfo = "<DatabaseHelper.onCreate(SQLiteDatabase):void>";
		database.execSQL(RESTAURANT_TABLE_CREATE);
		database.execSQL(MINYAN_TABLE_CREATE);
		database.execSQL(MIKVAH_TABLE_CREATE);
		database.execSQL(LOYALTY_TABLE_CREATE);
		database.execSQL(NOTIFICATION_TABLE_CREATE);
		Common
			.Log(methodInfo, String.format("NOTIFICATION_DATABASE_CREATE: %s",
				NOTIFICATION_TABLE_CREATE));
	}

	// Method is called during an upgrade of the database, e.g. if you increase
	// the database version
	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		String methodInfo = "<DatabaseHelper.onUpgrade(SQLiteDatabase, int, int):void>";
		Log.w(DatabaseHelper.class.getName(), "Upgrading database from version "
			+ oldVersion + " to " + newVersion + ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS todo");
		onCreate(database);
	}

}
