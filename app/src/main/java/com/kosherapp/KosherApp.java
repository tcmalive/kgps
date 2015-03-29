package com.kosherapp;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.http.util.ByteArrayBuffer;

import com.koshersense.*;
import com.kosherapp.database.DbAdapter;
import com.kosherapp.util.Maps;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class KosherApp extends android.app.ListActivity implements
        LocationInfo {

    protected static String KOSHER_SENSE_APP_ID = "626D419A-2137-401D-8306-2961425E7D0D";

    private ArrayList<KosherPlace> kosherPlaces = null;
    private ArrayList<KosherPlace> restaurantPlaces = null;
    private ArrayList<KosherPlace> mikvehPlaces = null;
    private ArrayList<KosherPlace> minyanPlaces = null;
    private ArrayList<KosherPlace> loyaltyPlaces = null;
    private KosherPlaceAdapter kosherPlaceAdapter = null;

    private boolean getImmediateAd = true;

    private static final int NOTIFICATION_ID = 1;

    private DbAdapter dbAdapter = null;

    private ListService lService = null;

    private Location currentLocation = null;

    private Comparator<KosherPlace> sorter = null;

    private SharedPreferences preferences = null;

    private String locationNameFilter = "";
    private boolean discountFilter = false;
    // private double distanceFilter = 20; // miles
    private double distanceFilter = 20; // miles
    private String foodTypeFilter = "All";

    private ProgressDialog progressDialog = null;

    private LocationManager locationManager = null;

    private int progressDialogCounter = 0;
    private Spinner locationSpinner = null;
    private Spinner listTypeSpinner = null;
    private Button refreshButton = null;
    private AlertDialog sortAlert = null;

    private TextView textAdDestinationUrl = null;
    private TextView textAdHeading = null;
    private TextView textAdDescription = null;
    private RelativeLayout textAdRelative = null;

    private WebView imageAdWebView = null;
    // private TextView imageAdDestinationUrl = null;
    // private TextView imageAdHeading = null;
    // private TextView imageAdDescription = null;
    private RelativeLayout imageAdRelative = null;

    private LinearLayout googleAdLinear = null;

    private AdController adController = null;

    private LinkedList<String> oldLoadIds = null;

    // hour

    // minutes

    public Boolean getUseCurrentLocation() {
        String methodInfo = "<KosherApp.getUseCurrentLocation():Boolean>";
        Boolean useCurrentLocation = Preferences().getBoolean(
                Common.PREFS_KEY_USE_CURRENT_LOCATION, true);

        return useCurrentLocation;
    }

    protected void setUseCurrentLocation(Boolean useCurrentLocation) {
        SharedPreferences.Editor editor = Preferences().edit();
        editor.putBoolean(Common.PREFS_KEY_USE_CURRENT_LOCATION,
                useCurrentLocation);
        this.commitPreferencesEditor(editor);
    }

    private AdController AdController() {
        if (this.adController == null) {
            this.adController = new AdController(KOSHER_SENSE_APP_ID, this,
                    this.getContentResolver(), ImageAdWebView(),
                    LocationManager());
        }
        return this.adController;
    }

    private boolean isDbUpdating() {
        Boolean dbUpdating = Preferences().getBoolean(
                Common.PREFS_KEY_DB_UPDATING, false);

        return dbUpdating;
    }

    private Long getAdMillis() {
        Long adMillis = Preferences().getLong(Common.PREFS_KEY_AD_MILLIS,
                Common.AD_MILLIS_DEFAULT);

        return adMillis;
    }

    private void setAdMillis(Long adMillis) {
        SharedPreferences.Editor editor = Preferences().edit();
        editor.putLong(Common.PREFS_KEY_AD_MILLIS, adMillis);
        this.commitPreferencesEditor(editor);
    }

    private long getNotificationMillis() {
        long notificationMillis = Preferences().getLong(
                Common.PREFS_KEY_NOTIFICATION_MILLIS,
                Common.NOTIFICATION_MILLIS_DEFAULT);

        return notificationMillis;
    }

    private void setNotificationMillis(long notificationMillis) {
        SharedPreferences.Editor editor = Preferences().edit();
        editor.putLong(Common.PREFS_KEY_NOTIFICATION_MILLIS, notificationMillis);
        this.commitPreferencesEditor(editor);
    }

    private Boolean getEnhancedDiscountsEnabled() {
        Boolean enabled = Preferences().getBoolean(
                Common.PREFS_KEY_ENHANCED_DISCOUNTS, true);

        return enabled;
    }

    private void setEnhancedDiscountsEnabled(Boolean isEnabled) {
        SharedPreferences.Editor editor = Preferences().edit();
        editor.putBoolean(Common.PREFS_KEY_ENHANCED_DISCOUNTS, isEnabled);
        this.commitPreferencesEditor(editor);
    }

    private Boolean getNotificationDiscountsEnabled() {
        Boolean enabled = Preferences().getBoolean(
                Common.PREFS_KEY_NOTIFICATION_DISCOUNTS, true);

        return enabled;
    }

    private MyLocationListener gpsLocationListener = null;
    private MyLocationListener networkLocationListener = null;

    private CountDownTimer countDownTimer = null;
    private AsyncTask<String, Integer, Integer> updateDatabaseTask;
    public boolean loadingFirstDatabase = false;
    private boolean windowHasFocus;
    private CountDownTimer deleteOldDataCountDownTimer = null;
    private static HashMap<String, String> listTypeHashMap = null;

    @Override
    public Object onRetainNonConfigurationInstance() {
        return dbAdapter;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        String methodInfo = "<KosherApp.onConfigurationChanged(Configuration):void>";
        Common.Log(methodInfo, "START");
        Common.Log(
                methodInfo,
                String.format("this.dbUpdating: %s",
                        String.valueOf(this.isDbUpdating())));

        // if (!this.dbUpdating)
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        String methodInfo = "<KosherApp.onCreate(Bundle):void>";
        Common.Log(methodInfo, "START");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (savedInstanceState != null) {
            if ((savedInstanceState.getBoolean("waiting"))) {
                showProgressDialog();
            }
        }

        try {
            LocationManager().requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 0, 0,
                    NetworkLocationListener());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] params) {
                String methodInfo = "<KosherApp.onCreate(Bundle):void.AsyncTask.doInBackground(Object[]):Object>";

                String possibleEmail = "";
                try {
                    Account[] accounts = AccountManager.get(KosherApp.this).getAccountsByType("com.google");
                    for (Account account : accounts) {
                        possibleEmail
                                += account.name;
                    }
                } catch (Exception e) {
                    Log.i("Exception", "Exception:" + e);
                }

                if (possibleEmail.equals("")) {
                    try {
                        Account[] accounts = AccountManager.get(KosherApp.this).getAccounts();
                        for (Account account : accounts) {
                            if (Common.isValidEmailAddress(account.name)) {
                                possibleEmail += account.name;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        Log.i("Exception", "Exception:" + e);
                    }

                }
                Log.i("Exception", "mails:" + possibleEmail);

                String result = "";
                if (!possibleEmail.equals("")) {
                    try {
//                        newLogin - email, password, registrationId
                        String deviceId = Common.getDeviceId(KosherApp.this.getApplicationContext(),
                                KosherApp.this.getContentResolver());
                        String address = "http://www.kosherrestaurantsgps.com/mobile_subscription_controller.v4.php?mode=newLoginAndroid&email=" + possibleEmail + "&password=android" + "&registrationId=" + deviceId;
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
                        result = new String(baf.toByteArray());
                        // Common.Log(methodInfo, String.format("DB Content: %s", toReturn));
                    } catch (Exception e) {
                        result = e.getMessage();
                    }

                }
                return null;
            }
        }.execute();

        setGUIListeners();

        KosherApp.this.OldDataCountDownTimer();

        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                new OnCreateTask().execute();
            }
        });

        register();

        Intent menuPageIntent = new Intent(this, MenuPage.class);

        menuPageIntent.putExtra(Common.INTENT_KEY_LOCATION_LATITUDE,
                getAppLatitude());
        menuPageIntent.putExtra(Common.INTENT_KEY_LOCATION_LONGITUDE,
                getAppLongitude());
        menuPageIntent.putExtra(Common.INTENT_KEY_LISTTYPE,
                Common.LIST_TYPE_RESTAURANT);

        this.startActivityForResult(menuPageIntent,
                Common.REQUEST_CODE_MENU_PAGE);

        Common.Log(methodInfo, "END");
    }

    @Override
    protected void onPause() {
        String methodInfo = "<KosherApp.onPause():void>";
        Common.Log(methodInfo, "START");

        super.onPause();
        Common.Log(methodInfo, "END");
    }

    @Override
    protected void onResume() {
        String methodInfo = "<KosherApp.onResume():void>";
        Common.LogMethodStart(methodInfo);

        super.onResume();
        Common.LogMethodEnd(methodInfo);
    }

    private void removeDatabaseDateStamp() {
        SharedPreferences.Editor editor = Preferences().edit();
        editor.remove(Common.PREFS_KEY_DB_UPDATE_DATE);
        this.commitPreferencesEditor(editor);
    }

    private CountDownTimer CountDownTimer() {
        if (this.countDownTimer == null) {
            // this.countDownTimer = new CountDownTimer(Maps.TWO_MINUTES, 10000)
            // {
            this.countDownTimer = new CountDownTimer(1, 10000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    String methodInfo = "<KosherApp.CountDownTimer():CountDownTimer.new CountDownTimer() {...}.onTick(long):void>";
                    Common.Log(
                            methodInfo,
                            "millisUntilFinished: "
                                    + String.valueOf(millisUntilFinished));
                }

                @Override
                public void onFinish() {
                    KosherApp.this.LocationManager().removeUpdates(
                            GPSLocationListener());
                }
            };
        }
        return this.countDownTimer;
    }

    private CountDownTimer OldDataCountDownTimer() {
        if (this.deleteOldDataCountDownTimer == null) {
            this.deleteOldDataCountDownTimer = new CountDownTimer(60000, 10000) {

                @Override
                public void onTick(long millisUntilFinished) {
                    String methodInfo = "<KosherApp.CountDownTimer():DeleteOldDataCountDownTimer.new CountDownTimer() {...}.onTick(long):void>";
                    Common.Log(
                            methodInfo,
                            "millisUntilFinished: "
                                    + String.valueOf(millisUntilFinished));
                }

                @Override
                public void onFinish() {
                    if (KosherApp.this.oldLoadIds == null) {
                        return;
                    }
                    String oldLoadId = KosherApp.this.oldLoadIds.getFirst();
                    if (oldLoadId == null) {
                        return;
                    }
                    if (oldLoadId.equals("")) {
                        return;
                    }
                    new DeleteOldListDataTask().execute(oldLoadId);
                    this.start();
                }
            };
        }
        return this.deleteOldDataCountDownTimer;
    }

    private MyLocationListener GPSLocationListener() {
        if (this.gpsLocationListener == null) {
            this.gpsLocationListener = new MyLocationListener();
        }
        return this.gpsLocationListener;
    }

    private MyLocationListener NetworkLocationListener() {
        if (this.networkLocationListener == null) {
            this.networkLocationListener = new MyLocationListener();
        }
        return this.networkLocationListener;
    }

    private LocationManager LocationManager() {
        if (this.locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        return this.locationManager;
    }

    private ProgressDialog ProgressDialog() {
        return this.ProgressDialog(null);
    }

    private ProgressDialog ProgressDialog(String message) {
        if (message == null) {
            message = "Please wait...";
        }
        if (this.progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Progress Dialog");
        }
        progressDialog.setMessage(message);
        return this.progressDialog;
    }

    private SharedPreferences Preferences() {
        if (this.preferences == null) {
            this.preferences = this.getSharedPreferences(Common.PREFS_NAME,
                    Context.MODE_PRIVATE);
        }
        return this.preferences;
    }

    private DbAdapter DbAdapter() {
        if (this.dbAdapter == null) {
            this.dbAdapter = (DbAdapter) getLastNonConfigurationInstance();
            if (this.dbAdapter == null) {
                this.dbAdapter = new DbAdapter(this);
            }
        }

        return this.dbAdapter;
    }

    private ListService ListService() {
        if (this.lService == null) {
            this.lService = new ListService(this);
        }
        return this.lService;
    }

    private Spinner ListTypeSpinner() {
        if (KosherApp.this.listTypeSpinner == null) {
            KosherApp.this.listTypeSpinner = (Spinner) findViewById(R.id.listTypeSpinner);
            new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] params) {
                    String[] listTypeArray = getListTypeArray();
                    ArrayAdapter<String> listTypeAdapter = new ArrayAdapter<String>(
                            (Context) KosherApp.this, R.layout.spinnerlayout, listTypeArray);
                    listTypeAdapter
                            .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    KosherApp.this.listTypeSpinner.setAdapter(listTypeAdapter);
                    int position = Common.getSpinnerChildPosition(KosherApp.this.listTypeSpinner,
                            Common.LIST_TYPE_RESTAURANT);
                    if (position > -1)

                    {
                        KosherApp.this.listTypeSpinner.setSelection(position);
                    }
                    ;
                    return null;
                }
            }.execute();

        }
        return this.listTypeSpinner;
    }

    private HashMap<String, String> ListTypeHashMap() {
        String methodInfo = "<KosherApp.ListTypeHashMap():HashmMap<String, String>>";
        if (listTypeHashMap == null) {
            listTypeHashMap = new HashMap<String, String>();
            ArrayList<HashMap<String, String>> foodTypeMaps = JSONParser
                    .parseJSON(RetrieveListTypeValues());
            if (foodTypeMaps == null) {
                String message = "There was an error. The program will shut down. If your internet is not connected, please connect it and restart the program.";
                Common.Log(methodInfo, message);
            }
            if (foodTypeMaps.size() == 0) {
                String message = "There was an error. The program will shut down. If your internet is not connected, please connect it and restart the program.";
                Common.Log(methodInfo, message);
            }
            for (int i = 0; i < foodTypeMaps.size(); i++) {
                HashMap<String, String> item = foodTypeMaps.get(i);
                String key = item.get("id");
                String value = item.get("type");
                listTypeHashMap.put(key, value);
            }
        }
        return listTypeHashMap;
    }

    private void exit(String message) {
        String methodInfo = "<KosherApp.exit(String):void>";
        Common.Log(methodInfo, "START");
        AlertDialog alertDialog = null;
        alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Exiting");
        alertDialog.setMessage(message);
        alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        alertDialog.show();

        finish();
        Common.Log(methodInfo, "END");
    }

    private String RetrieveListTypeValues() {
        String methodInfo = "<KosherApp.RetrieveListTypeValues():String>";
        String toReturn = "";

        try {
            String address = "http://www.kosherrestaurantsgps.com/listType_json.v2.php?";
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
            SharedPreferences.Editor editor = this.Preferences().edit();
            editor.putString(Common.PREFS_KEY_LIST_TYPE_VALUES, toReturn);
            commitPreferencesEditor(editor);
            Common.Log(methodInfo, String.format("DB Content: %s", toReturn));
        } catch (Exception e) {
            toReturn = this.Preferences().getString(
                    Common.PREFS_KEY_LIST_TYPE_VALUES, "");
        }
        return toReturn;
    }

    private String[] getListTypeArray() {
        String methodInfo = "<KosherApp.getListTypeArray()String[]>";
        Common.Log(methodInfo, "START");
        String[] listTypeArray = null;
        Collection<String> listTypeCollection = ListTypeHashMap().values();
        Common.Log(
                methodInfo,
                String.format("listTypeCollection.size(): %s",
                        String.valueOf(listTypeCollection.size())));
        listTypeArray = new String[listTypeCollection.size()];
        listTypeArray = listTypeCollection.toArray(listTypeArray);
        Common.Log(
                methodInfo,
                String.format("listTypeArray.length: %s",
                        String.valueOf(listTypeArray.length)));

        return listTypeArray;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        String methodInfo = "<KosherApp.onWindowFocusChanged(boolean):void>";
        Common.LogMethodStart(methodInfo);
        if (hasFocus) {
            Common.Log(methodInfo, "KosherApp Window has focus");
            this.windowHasFocus = true;
            actionOnWindowHasFocus();
        } else {
            this.windowHasFocus = false;
            actionOnWindowLosesFocus();
        }

        super.onWindowFocusChanged(hasFocus);
        Common.LogMethodEnd(methodInfo);
    }

    // public void onWindowFocusChanged(boolean hasFocus) {
    // String methodInfo = "<KosherApp.onWindowFocusChanged(boolean):void>";
    // if (hasFocus) {
    // Common.Log(methodInfo, "KosherApp Window has focus");
    // actionOnWindowHasFocus();
    // } else {
    // actionOnWindowLosesFocus();
    // }
    //
    // super.onWindowFocusChanged(hasFocus);
    // }

    /**
     *
     */
    private void actionOnWindowLosesFocus() {
        CountDownTimer().start();

        AdController().dismissAdSearch();

        KosherApp.this.LocationManager().removeUpdates(GPSLocationListener());
        KosherApp.this.LocationManager().removeUpdates(
                NetworkLocationListener());
    }

    /**
     *
     */
    private void actionOnWindowHasFocus() {
        String methodInfo = "<KosherApp.actionOnWindowHasFocus():void>";
        Common.LogMethodStart(methodInfo);

        AdController().searchForAds(true);

        CountDownTimer().cancel();

        if (!databaseIsRegistered()) {
            showProgressDialog("Loading initial Kosher Locations to database...");
        }
        updateDatabase();

        try {
            LocationManager().requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 0, 0, GPSLocationListener());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        Common.LogMethodEnd(methodInfo);
    }

    private void updateDatabase() {
        String methodInfo = "<KosherApp.updateDatabase():void>";
        Common.LogMethodStart(methodInfo);
        boolean showProgressDialog = false;

        if (this.updateDatabaseTask != null)
            return;

        Boolean databaseIsUpToDate = databaseIsUpToDate();
        Boolean databaseIsPopulated = getIsDatabasePopulated();

        if (databaseIsPopulated && databaseIsUpToDate) {
            return;
        }

        boolean databaseIsRegistered = databaseIsRegistered();
        Common.Log(
                methodInfo,
                String.format("databaseIsRegistered: %s",
                        String.valueOf(databaseIsRegistered)));
        if (!databaseIsRegistered) {
            this.loadingFirstDatabase = true;
            showProgressDialog = true;
        }

        if (showProgressDialog) {
            this.showProgressDialog("Updating Kosher Locations in Database...");
        }

        this.updateDatabaseTask = new UpdateDatabaseTask().execute();
        Common.LogMethodEnd(methodInfo);
    }

    private Boolean getIsDatabasePopulated() {
        String methodInfo = "<KosherApp.getIsDatabasePopulated():Boolean>";
        Common.Log(methodInfo, "START");

        Boolean isDatabasePopulated = false;

        String loadId = getOldLoadId(Common.LIST_TYPE_RESTAURANT);

        try {
            DbAdapter().openReadOnly();
        } catch (NullPointerException e) {
            Common.Log(methodInfo, Common.getString(e));
            return false;
        }
        if (!DbAdapter().isReadOnlyDatabaseAvailable()) {
            return false;
        }

        Cursor cursor = DbAdapter().fetchAllRestaurants(loadId);
        if (cursor == null) {
            return isDatabasePopulated;
        }

        while (cursor.moveToNext()) {
            isDatabasePopulated = true;
            break;
        }
        cursor.close();
        // dbAdapter.close();

        Common.Log(
                methodInfo,
                String.format("isDatabasePopulated: %s",
                        String.valueOf(isDatabasePopulated)));

        return isDatabasePopulated;
    }

    private void initializeLocation() {
        this.updateCurrentLocation(LocationManager().getLastKnownLocation(
                LocationManager.GPS_PROVIDER));
        this.updateCurrentLocation(LocationManager().getLastKnownLocation(
                LocationManager.NETWORK_PROVIDER));
    }

    @Override
    public boolean onSearchRequested() {
        loadFilterViewer();
        return false;
    }

    private void register() {
        String methodInfo = "<KosherApp.register():void>";

        Boolean isRegistered = this.Preferences().getBoolean(
                Common.PREFS_KEY_IS_REGISTERED, true);
        Common.Log(methodInfo,
                String.format("isRegistered: %s", String.valueOf(isRegistered)));

        if (!haveInternet() || isRegistered)
            return;

        Intent registrationIntent = new Intent(this, RegistrationPage.class);

        String emailAddress = this.Preferences().getString(
                Common.PREFS_KEY_EMAIL, "");
        String state = this.Preferences().getString(Common.PREFS_KEY_STATE, "");

        registrationIntent.putExtra(Common.INTENT_KEY_EMAIL, emailAddress);
        registrationIntent.putExtra(Common.INTENT_KEY_STATE, state);

        String deviceId = Common.getDeviceId(getBaseContext(),
                getContentResolver());
        registrationIntent.putExtra(Common.INTENT_KEY_DEVICE_ID, deviceId);

        registrationIntent.putExtra(Common.INTENT_KEY_IS_REGISTERED,
                isRegistered);

        this.startActivityForResult(registrationIntent,
                Common.REQUEST_CODE_REGISTRATION);
    }

    private void updateKosherList() {
        String methodInfo = "<KosherApp.updateKosherList():void>";
        Common.LogMethodStart(methodInfo);

        String listType = (String) this.ListTypeSpinner().getSelectedItem();
        Common.Log(methodInfo, "listType: " + listType);

        String listName = "";

        if (listType == null) {
            return;
        }
        if (listType.equalsIgnoreCase("restaurant"))
            listName = Common.LIST_TYPE_RESTAURANT;
        else if (listType.equalsIgnoreCase("mikvah"))
            listName = Common.LIST_TYPE_MIKVEH;
        else if (listType.equalsIgnoreCase("minyan"))
            listName = Common.LIST_TYPE_MINYAN;
        else if (listType.equalsIgnoreCase("Loyalty Places"))
            listName = Common.LIST_TYPE_LOYALTY;
        else
            return;
        Common.Log(methodInfo, "listName: " + listName);

        double longitudeToUse = Double.MIN_VALUE;
        double latitudeToUse = Double.MIN_VALUE;

        Location appLocation = getAppLocation();
        if (appLocation == null) {
            return;
        }
        longitudeToUse = appLocation.getLongitude();
        latitudeToUse = appLocation.getLatitude();

        // if (this.getUseCurrentLocation()) {
        Common.Log(methodInfo, String.format("listName: %s", listName));
        new UpdateLocationSpinnerTextTask().execute(listName);
        // }
        Common.Log(
                methodInfo,
                String.format("longitudeToUse: %s",
                        String.valueOf(longitudeToUse)));
        Common.Log(
                methodInfo,
                String.format("latitudeToUse: %s",
                        String.valueOf(latitudeToUse)));

        // // if (!this.getUseCurrentLocation()) {
        // // new UpdateKosherListTask().execute(listName,
        // String.valueOf(latitudeToUse),
        // // String.valueOf(longitudeToUse), "all");
        //
        // showProgressDialog();
        // }
        Common.LogMethodEnd(methodInfo);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.filter:
                loadFilterViewer();
                return true;
            case R.id.sort:
                this.SortAlert().show();
                return true;
            case R.id.email:
                email();
                return true;
            case R.id.settings:
                settings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void settings() {
        Intent filterIntent = new Intent(this, SettingsView.class);

        filterIntent.putExtra(Common.INTENT_KEY_SETTINGS_ENHANCED_DISCOUNT,
                this.getEnhancedDiscountsEnabled());
        filterIntent.putExtra(Common.INTENT_KEY_SETTINGS_DEEP_DISCOUNT,
                this.getNotificationDiscountsEnabled());

        startActivityForResult(filterIntent, Common.REQUEST_CODE_SETTINGS_VIEW);
    }

    private AlertDialog SortAlert() {
        String methodInfo = "<KosherApp.SortAlert():AlertDialog>";
        if (this.sortAlert == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Sort by");
            builder.setSingleChoiceItems(R.array.sort_array, -1,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            String methodInfo = "<KosherApp.SortAlert():AlertDialog.new OnClickListener() {...}.onClick(DialogInterface, int):void>";
                            String sortBy = getResources().getStringArray(
                                    R.array.sort_array)[item];
                            Common.Log(methodInfo,
                                    String.format("sortBy: %s", sortBy));
                            if (sortBy.equalsIgnoreCase("distance"))
                                KosherApp.this
                                        .setSorter(new KosherApp.DistanceComparator());
                            else if (sortBy.equalsIgnoreCase("name"))
                                KosherApp.this
                                        .setSorter(new KosherApp.NameComparator());
                            else
                                Common.Log(methodInfo,
                                        "sort param not recognized");

                            updateKosherList();
                            KosherApp.this.SortAlert().dismiss();
                        }
                    });
            this.sortAlert = builder.create();
        }

        return this.sortAlert;
    }

    private void email() {
        final Intent emailIntent = new Intent(
                android.content.Intent.ACTION_SEND);

        emailIntent.setType("plain/text");

        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                new String[]{"kosherdroid@yahoo.com"});

        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
                "Android app feedback");

        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, String.format(
                "Location Name:%sAddress:%sComments:", "\r\n", "\r\n"));

        startActivity(Intent.createChooser(emailIntent, "Send mail..."));
    }

    private void showProgressDialog() {
        showProgressDialog(null);
    }

    private void showProgressDialog(String message) {
        String methodInfo = "<KosherApp.showProgressDialog():void>";
        Common.Log(methodInfo, "START");
        if (!this.windowHasFocus)
            return;

        if (progressDialogCounter == 0)
            ProgressDialog(message).show();

        progressDialogCounter++;
        Common.Log(
                methodInfo,
                String.format("progressDialogCounter: %s",
                        String.valueOf(progressDialogCounter)));

        Common.Log(
                methodInfo,
                String.format("progressDialogCounter: %s",
                        String.valueOf(progressDialogCounter)));
        Common.Log(methodInfo, "END");
    }

    private void dismissProgressDialog() {
        String methodInfo = "<KosherApp.dismissProgressDialog():void>";
        Common.Log(methodInfo, "START");

        if (progressDialogCounter > 0) {
            progressDialogCounter--;
        }
        Common.Log(
                methodInfo,
                String.format("progressDialogCounter: %s",
                        String.valueOf(progressDialogCounter)));

        if (progressDialogCounter < 0) {
            throw new IllegalArgumentException(
                    "progressDialogCounter is NEGATIVE");
        }

        if (progressDialogCounter == 0) {
            Common.Log(methodInfo, "Attempting to dismiss Progress Dialog");
            // Log.d(Common.LOG_TAG, String.format(
            // "progressDialog.isShowing(): %s", String
            // .valueOf(ProgressDialog().isShowing())));
            try {
                ProgressDialog().dismiss();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            Common.Log(methodInfo, "Progress Dialog dismissed");
        }

        Common.Log(
                methodInfo,
                String.format("progressDialogCounter: %s",
                        String.valueOf(progressDialogCounter)));
        Common.Log(methodInfo, "END");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("waiting", ProgressDialog().isShowing());
        dismissProgressDialog();
    }

    private void display() {
        ArrayList<KosherPlace> lKosherPlaces = getKosherPlaces();
        this.kosherPlaceAdapter = new KosherPlaceAdapter(this,
                R.layout.kosheritem, lKosherPlaces, this);
        this.kosherPlaceAdapter.setActivity(this);
        this.kosherPlaceAdapter.filterBy(this.locationNameFilter,
                this.discountFilter, this.distanceFilter, this.foodTypeFilter);
        this.kosherPlaceAdapter.sortWith(this.Sorter());
        setListAdapter(this.kosherPlaceAdapter);

        dismissProgressDialog();
    }

    private void setGUIListeners() {
        setListTypeButtonListeners();

        LocationSpinner().setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    loadLocationViewer(v);
                return true;
            }
        });

        this.RefreshButton().setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String methodInfo = "<KosherApp.setGUIListeners():void.new OnTouchListener() {...}.onTouch(View, MotionEvent):boolean>";

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Common.Log(methodInfo, "Refresh Button Clicked");
//                    Toast.makeText(KosherApp.this, "Refresh", Toast.LENGTH_LONG).show();
                    refresh();
                }
                return false;
            }
        });

    }

    protected void refresh() {
        KosherApp.this.updateCurrentLocation(KosherApp.this.LocationManager()
                .getLastKnownLocation(LocationManager.GPS_PROVIDER));
        updateDatabase();
        updateKosherList();
    }

    private Spinner LocationSpinner() {
        if (this.locationSpinner == null) {
            this.locationSpinner = (Spinner) this
                    .findViewById(R.id.locationEditText);
        }
        return this.locationSpinner;
    }

    private Button RefreshButton() {
        if (this.refreshButton == null) {
            this.refreshButton = (Button) findViewById(R.id.refreshButton);
        }
        return this.refreshButton;
    }

    private WebView ImageAdWebView() {
        if (this.imageAdWebView == null) {
            this.imageAdWebView = (WebView) findViewById(R.id.imageAdWebView);
        }
        return this.imageAdWebView;
    }

    public Location getCurrentLocation() {
        return this.currentLocation;
    }

    public Double getCurrentLatitude() {
        return this.currentLocation.getLatitude();
    }

    public Double getCurrentLongitude() {
        return this.currentLocation.getLongitude();
    }

    public Comparator<KosherPlace> Sorter() {
        if (this.sorter == null) {
            sorter = new DistanceComparator();
        }
        return this.sorter;
    }

    public void setSorter(Comparator<KosherPlace> sorter) {
        this.sorter = sorter;
    }

    private void updateCurrentLocation(Location newLocation) {
        Boolean isBetterLocation = Maps.isBetterLocation(newLocation,
                this.getCurrentLocation());

        if (isBetterLocation) {
            this.setCurrentLocation(newLocation);
        }
    }

    private void setCurrentLocation(Location newLocation) {
        String methodInfo = "<KosherApp.setCurrentLocation(Location):void>";
        Common.Log(methodInfo, "START");
        this.currentLocation = newLocation;
    }

    protected void loadFilterViewer() {
        Intent filterIntent = new Intent(this, FilterChooser.class);

        filterIntent.putExtra(Common.INTENT_KEY_NAME_FILTER,
                this.locationNameFilter);
        filterIntent.putExtra(Common.INTENT_KEY_DISTANCE_FILTER,
                this.distanceFilter);
        filterIntent.putExtra(Common.INTENT_KEY_DISCOUNT_FILTER,
                this.discountFilter);
        filterIntent.putExtra(Common.INTENT_KEY_FOODTYPE_FILTER,
                this.foodTypeFilter);

        startActivityForResult(filterIntent, Common.REQUEST_CODE_FILTER_CHOOSER);
    }

    protected void loadLocationViewer(View v) {
        Intent locationIntent = new Intent(this, LocationChooser.class);

        locationIntent.putExtra(Common.INTENT_KEY_LOCATION_ADDRESS,
                this.getAppAddressString());

        locationIntent.putExtra(Common.INTENT_KEY_LOCATION_IS_CURRENT_LOCATION,
                this.getUseCurrentLocation());

        startActivityForResult(locationIntent,
                Common.REQUEST_CODE_LOCATION_CHOOSER);
    }

    private String getAppAddressString() {
        String address = Preferences().getString(
                Common.PREFS_KEY_LOCATION_ADDRESS, "");

        return address;
    }

    private void setListTypeButtonListeners() {
        this.ListTypeSpinner().setOnItemSelectedListener(
                new MyListTypeSelectedListener());
    }

    public void setAppLatitude(double latitude) {
        SharedPreferences.Editor editor = Preferences().edit();
        editor.putFloat(Common.PREFS_KEY_LOCATION_LATITUDE, (float) latitude);
        this.commitPreferencesEditor(editor);
    }

    public Double getAppLatitude() {
        double latitude;
        if (getUseCurrentLocation()) {
            try {
                latitude = getCurrentLocation().getLatitude();
            } catch (NullPointerException e) {
                latitude = 0;
            }
        } else {
            latitude = (double) Preferences().getFloat(
                    Common.PREFS_KEY_LOCATION_LATITUDE, 0);
        }

        return latitude;
    }

    public void setAppLongitude(double longitude) {
        SharedPreferences.Editor editor = Preferences().edit();
        editor.putFloat(Common.PREFS_KEY_LOCATION_LONGITUDE, (float) longitude);
        this.commitPreferencesEditor(editor);
    }

    public Double getAppLongitude() {
        double longitude;
        if (getUseCurrentLocation()) {
            try {
                longitude = getCurrentLocation().getLongitude();
            } catch (NullPointerException e) {
                longitude = 0;
            }
        } else {
            longitude = Preferences().getFloat(
                    Common.PREFS_KEY_LOCATION_LONGITUDE, 0f);
        }

        return longitude;
    }

    private void setAppAddressString(String address) {
        SharedPreferences.Editor editor = Preferences().edit();
        editor.putString(Common.PREFS_KEY_LOCATION_ADDRESS, address);
        this.commitPreferencesEditor(editor);
    }

    /**
     * Sets showing text on Location Spinner
     *
     * @param address
     */
    private void setLocationText(String address) {
        List<CharSequence> places = new ArrayList<CharSequence>(1);
        places.add(address);
        ArrayAdapter<CharSequence> locationAdapter = new ArrayAdapter<CharSequence>(
                (Context) this, R.layout.spinnerlayout, places);
        locationAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.LocationSpinner().setAdapter(locationAdapter);
    }

    public void showMap(View view) {
        Intent i = new Intent(this, LocationMapView.class);
        i.putExtra("currentLocation", this.getUseCurrentLocation());
        i.putExtra("longitude", this.getAppLongitude());
        i.putExtra("latitude", this.getAppLatitude());
        startActivityForResult(i, Common.REQUEST_CODE_GOOGLE_MAPS_CODE);
    }

    private class DistanceComparator implements Comparator<KosherPlace> {

        @Override
        public int compare(KosherPlace arg0, KosherPlace arg1) {
            Location appLocation = KosherApp.this.getAppLocation();
            if (appLocation == null)
                throw new NullPointerException();

            double arg0Distance = Double.MIN_VALUE;
            arg0Distance = arg0.getDistance(appLocation.getLatitude(),
                    appLocation.getLongitude());

            double arg1Distance = Double.MIN_VALUE;
            arg1Distance = arg1.getDistance(appLocation.getLatitude(),
                    appLocation.getLongitude());

            if (arg0Distance > arg1Distance)
                return 1;
            if (arg0Distance < arg1Distance)
                return -1;
            return 0;
        }
    }

    private class NameComparator implements Comparator<KosherPlace> {

        @Override
        public int compare(KosherPlace arg0, KosherPlace arg1) {
            String arg0Name = arg0.Name();
            String arg1Name = arg1.Name();
            int result = arg0Name.compareToIgnoreCase(arg1Name);
            return result;
        }
    }

    public Location getAppLocation() {
        String methodInfo = "<KosherApp.getAppLocation():Location>";
        // Common.Log(methodInfo, "START");
        Location location = new Location(LocationManager.GPS_PROVIDER);

        if (getUseCurrentLocation()) {
            // Common.Log(methodInfo, "Using Current Location");
            location = getCurrentLocation();
            if (location == null) {
                String message = "Your current location is temporarily unavailable";
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                Common.Log(methodInfo, message);
            }
        } else {
            location.setLongitude(this.getAppLongitude());
            location.setLatitude(this.getAppLatitude());
        }

        return location;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String methodInfo = "<KosherApp.onActivityResult(int, int, Intent):void>";
        Common.LogMethodStart(methodInfo);
        if (requestCode == Common.REQUEST_CODE_LOCATION_CHOOSER) {
            onLocationChooserResult(resultCode, data);
            return;
        }

        if (requestCode == Common.REQUEST_CODE_DIRECTION_MAP_VIEW) {
            onDirectionMapViewResult(resultCode, data);
            return;
        }

        if (requestCode == Common.REQUEST_CODE_GOOGLE_MAPS_CODE)
            return;

        if (requestCode == Common.REQUEST_CODE_FILTER_CHOOSER) {
            onFilterChooserResult(resultCode, data);
            return;
        }

        if (requestCode == Common.REQUEST_CODE_REGISTRATION) {
            onRegistrationResult(resultCode, data);
            return;
        }

        if (requestCode == Common.REQUEST_CODE_SETTINGS_VIEW) {
            onSettingsResult(resultCode, data);
            return;
        }

        if (requestCode == Common.REQUEST_CODE_NOTIFICATION_VIEW) {
            onNotificationsResult(resultCode, data);
            return;
        }

        if (requestCode == Common.REQUEST_CODE_MENU_PAGE) {
            onMenuPageResult(resultCode, data);
            return;
        }

        Common.LogMethodEnd(methodInfo);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            Intent menuPageIntent = new Intent(this, MenuPage.class);

            menuPageIntent.putExtra(Common.INTENT_KEY_LOCATION_LATITUDE,
                    getAppLatitude());
            menuPageIntent.putExtra(Common.INTENT_KEY_LOCATION_LONGITUDE,
                    getAppLongitude());
            menuPageIntent.putExtra(Common.INTENT_KEY_LISTTYPE,
                    ListTypeSpinner().getSelectedItemPosition() + 1);

            this.startActivityForResult(menuPageIntent,
                    Common.REQUEST_CODE_MENU_PAGE);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void onMenuPageResult(int resultCode, Intent data) {
        String methodInfo = "<KosherApp.onMenuPageResult(int, Intent):void>";
        Common.LogMethodStart(methodInfo);
        if (data == null) {
            finish();
            return;
        }
        String listType = data.getStringExtra(Common.INTENT_KEY_LISTTYPE);
        int listTypePos = Common.getSpinnerChildPosition(ListTypeSpinner(),
                listType);
        ListTypeSpinner().setSelection(listTypePos);
    }

    private void onNotificationsResult(int resultCode, Intent data) {
        String methodInfo = "<KosherApp.onNotificationsResult(int, intent):void>";
        Common.Log(methodInfo, "START");
        if (data == null) {
            return;
        }
        Boolean notificationAdClicked = data.getBooleanExtra(
                Common.INTENT_KEY_NOTIFICATION_CLICKED, false);
        Common.Log(
                methodInfo,
                String.format("notificationAdClicked: %s",
                        String.valueOf(notificationAdClicked)));
    }

    private void onSettingsResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED) {
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            Boolean enhancedDiscounts = data.getBooleanExtra(
                    Common.INTENT_KEY_SETTINGS_ENHANCED_DISCOUNT, true);
            this.setEnhancedDiscountsEnabled(enhancedDiscounts);

            Boolean deepDiscounts = data.getBooleanExtra(
                    Common.INTENT_KEY_SETTINGS_DEEP_DISCOUNT, true);
            // this.setNotificationDiscountsEnabled(deepDiscounts);
        }
    }

    private void onDirectionMapViewResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED)
            Toast.makeText(this, R.string.directionsUnavailableMessage,
                    Toast.LENGTH_LONG).show();
    }

    private void onRegistrationResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                finish();
                return;
            }
            String emailAddress = data.getStringExtra(Common.INTENT_KEY_EMAIL);
            if (emailAddress != null) {
                SharedPreferences.Editor editor = this.Preferences().edit();
                editor.putString(Common.INTENT_KEY_EMAIL, emailAddress);
                commitPreferencesEditor(editor);
            }
            Boolean isRegistered = data.getBooleanExtra(
                    Common.INTENT_KEY_IS_REGISTERED, false);
            if (isRegistered) {
                SharedPreferences.Editor editor = this.Preferences().edit();
                editor.putBoolean(Common.INTENT_KEY_IS_REGISTERED, isRegistered);
                commitPreferencesEditor(editor);
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            finish();
        }
    }

    /**
     * @param resultCode
     * @param data
     */
    private void onFilterChooserResult(int resultCode, Intent data) {
        String methodInfo = "<KosherApp.onFilterChooserResult(int, Intent):void>";
        if (resultCode == Activity.RESULT_CANCELED)
            return;

        if (resultCode == Activity.RESULT_OK) {
            this.locationNameFilter = data
                    .getStringExtra(Common.INTENT_KEY_NAME_FILTER);
            this.discountFilter = data.getBooleanExtra(
                    Common.INTENT_KEY_DISCOUNT_FILTER, false);
            this.distanceFilter = data.getDoubleExtra(
                    Common.INTENT_KEY_DISTANCE_FILTER, Double.MIN_VALUE);
            this.foodTypeFilter = data
                    .getStringExtra(Common.INTENT_KEY_FOODTYPE_FILTER);

            Common.Log(
                    methodInfo,
                    String.format(
                            "<KosherApp.onFilterChooserResult(int, Intent):void> foodTypeFilter: %s",
                            foodTypeFilter));
            updateKosherList();
        }
    }

    /**
     * @param resultCode
     * @param data
     */
    private void onLocationChooserResult(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_CANCELED)
            return;
        if (resultCode == Activity.RESULT_OK) {
            String locationAddress = data
                    .getStringExtra(Common.INTENT_KEY_LOCATION_ADDRESS);
            double longitude = data.getDoubleExtra(
                    Common.INTENT_KEY_LOCATION_LONGITUDE, Double.MIN_VALUE);
            double latitude = data.getDoubleExtra(
                    Common.INTENT_KEY_LOCATION_LATITUDE, Double.MIN_VALUE);
            boolean useCurrentLocation = data.getBooleanExtra(
                    Common.INTENT_KEY_LOCATION_IS_CURRENT_LOCATION,
                    this.getUseCurrentLocation());

            this.setUseCurrentLocation(useCurrentLocation);
            this.setAppLatitude(latitude);
            this.setAppLongitude(longitude);
            this.setAppAddressString(locationAddress);

            updateKosherList();
        }
    }

    private void commitPreferencesEditor(Editor editor) {
        while (true) {
            boolean result = editor.commit();
            if (result == true)
                break;
        }
    }

    /**
     * @return boolean return true if the application can access the internet
     */
    private boolean haveInternet() {
        NetworkInfo info = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return false;
        }
        if (info.isRoaming()) {
            // here is the roaming option you can change it if you want to
            // disable internet while roaming, just return false
            return true;
        }
        return true;
    }

    private void updateMinyanDatabase() {
        String methodInfo = "<KosherApp.updateMinyanDatabase():void>";

        String sKosherList = "";

        sKosherList = ListService().RetrieveList(Common.LIST_TYPE_MINYAN, 0, 0,
                "all");
        minyanPlaces = (ArrayList<KosherPlace>) ListParser.ParseList(
                sKosherList, Common.LIST_TYPE_MINYAN);

        if (minyanPlaces == null) {
            return;
        }
        if (minyanPlaces.size() == 0) {
            return;
        }

        UUID loadUUID = UUID.randomUUID();
        String loadId = loadUUID.toString();
        new AddMinyanPlacesToDBTask().execute(loadId);
    }

    private void updateLoyaltyDatabase() {
        String methodInfo = "<KosherApp.updateLoyaltyDatabase():void>";
        String sKosherList = "";

        sKosherList = ListService().RetrieveList(Common.LIST_TYPE_LOYALTY, 0,
                0, "all");

        Common.Log(methodInfo, String.format("sKosherList: %s", sKosherList));
        loyaltyPlaces = (ArrayList<KosherPlace>) ListParser
                .ParseList(sKosherList, Common.LIST_TYPE_LOYALTY);
        if (loyaltyPlaces == null) {
            return;
        }
        if (loyaltyPlaces.size() == 0) {
            return;
        }

        UUID loadUUID = UUID.randomUUID();
        String loadId = loadUUID.toString();
        new AddLoyaltyPlacesToDBTask().execute(loadId);
    }

    private void updateMikvehDatabase() {
        String methodInfo = "<KosherApp.updateMikvehDatabase():void>";
        String sKosherList = "";

        sKosherList = ListService().RetrieveList(Common.LIST_TYPE_MIKVEH, 0, 0,
                "all");
        mikvehPlaces = (ArrayList<KosherPlace>) ListParser
                .ParseList(sKosherList, Common.LIST_TYPE_MIKVEH);
        if (mikvehPlaces == null) {
            return;
        }
        if (mikvehPlaces.size() == 0) {
            return;
        }

        UUID loadUUID = UUID.randomUUID();
        String loadId = loadUUID.toString();
        new AddMikvehPlacesToDBTask().execute(loadId);
    }

    private void deleteOldData(String oldLoadId) {
        if (oldLoadId == null) {
            return;
        }
        if (oldLoadId.equals("")) {
            return;
        }
        if (oldLoadIds == null) {
            oldLoadIds = new LinkedList<String>();
        }

        oldLoadIds.add(oldLoadId);
    }

    private void updateRestaurantDatabase() {
        String methodInfo = "<KosherApp.updateRestaurantDatabase():void>";
        Common.Log(methodInfo, "START");

        String sKosherList = "";

        sKosherList = ListService().RetrieveList(Common.LIST_TYPE_RESTAURANT,
                0, 0, "all");
        restaurantPlaces = (ArrayList<KosherPlace>) ListParser.ParseList(
                sKosherList, Common.LIST_TYPE_RESTAURANT);

        if (restaurantPlaces == null) {
            return;
        }
        if (restaurantPlaces.size() == 0) {
            Common.Log(methodInfo, "FAILED");

            return;
        }

        UUID loadUUID = UUID.randomUUID();
        String loadId = loadUUID.toString();
        new AddRestaurantPlacesToDBTask().execute(loadId);

        Common.Log(methodInfo, "END");
    }

    public class MyListTypeSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            clearFilters();
            updateKosherList();
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }

    }

    public class MySortingSelectedListener implements OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int pos,
                                   long id) {
            String methodInfo = "<KosherApp.MySortingSelectedListener.onItemSelected(AdapterView<?>, View, int, long):void>";
            String sortBy = (String) parent.getItemAtPosition(pos);
            Common.Log(methodInfo, String.format("sortBy: %s", sortBy));

            if (sortBy.equalsIgnoreCase("distance"))
                KosherApp.this.setSorter(new KosherApp.DistanceComparator());
            else if (sortBy.equalsIgnoreCase("name"))
                KosherApp.this.setSorter(new KosherApp.NameComparator());
            else
                Common.Log(methodInfo, "sort param not recognized");

            updateKosherList();
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }

    }

    private class UpdateDatabaseTask extends
            AsyncTask<String, Integer, Integer> {

        protected Integer doInBackground(String... listInfo) {
            String methodInfo = "<KosherApp.UpdateDatabaseTask.doInBackground(String...):Integer>";
            Common.Log(methodInfo, "START");

            int result = 0;
            updateRestaurantDatabase();
            updateMikvehDatabase();
            updateMinyanDatabase();
            updateLoyaltyDatabase();

            Common.Log(methodInfo, "END");
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            String methodInfo = "<KosherApp.UpdateDatabaseTask.onPostExecute(Integer):void>";
            Common.Log(methodInfo, "START");
            Common.Log(methodInfo,
                    String.format("result: %s", String.valueOf(result)));

            KosherApp.this.updateDatabaseTask = null;
            if (KosherApp.this.loadingFirstDatabase) {
                KosherApp.this.loadingFirstDatabase = false;
                KosherApp.this.dismissProgressDialog();
            }
            KosherApp.this.dismissProgressDialog();
            if (result == 0) {
                KosherApp.this.doneUpdatingDatabaseMessage();
            }
            super.onPostExecute(result);
            Common.Log(methodInfo, "END");
        }
    }

    private class OnCreateTask extends AsyncTask<String, Integer, Integer> {

        protected Integer doInBackground(String... listInfo) {
            String methodInfo = "<KosherApp.OnCreateTask.doInBackground(String...):Integer>";
            Common.Log(methodInfo, "START");

            initializeLocation();

            String previousAppVersion = KosherApp.this.Preferences().getString(
                    Common.PREFS_KEY_VERSION, "");

            String appVersion = getResources().getString(R.string.versionName);
            if (!appVersion.equals(previousAppVersion)) {
                boolean hasActiveInternetConnection = Common
                        .hasActiveInternetConnection(getApplicationContext(),
                                KosherApp.this);
                if (!hasActiveInternetConnection) {
                    KosherApp.this.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            AlertDialog alertDialog = null;
                            alertDialog = new AlertDialog.Builder(
                                    KosherApp.this).create();
                            alertDialog.setTitle("Exiting");
                            alertDialog
                                    .setMessage("The internet is currently unavailable. Please try again when your internet connection is connected.");
                            alertDialog.setButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(
                                                DialogInterface dialog,
                                                int which) {
                                            KosherApp.this.finish();
                                            return;
                                        }
                                    });
                            alertDialog.show();

                        }
                    });

                }
                SharedPreferences.Editor tempEditor = KosherApp.this
                        .Preferences().edit();
                tempEditor.clear();
                KosherApp.this.commitPreferencesEditor(tempEditor);

                DbAdapter().deleteDatabase();
                KosherApp.this.removeDatabaseDateStamp();
                Intent tipIntent = new Intent(KosherApp.this, TipView.class);
                String tipMessage = getResources().getString(
                        R.string.newVersionMessage);
                tipIntent.putExtra(Common.INTENT_KEY_TIP_TEXT, tipMessage);

                startActivity(tipIntent);
            }

            SharedPreferences.Editor editor = KosherApp.this.Preferences()
                    .edit();
            editor.putString(Common.PREFS_KEY_VERSION, KosherApp.this
                    .getResources().getString(R.string.versionName));
            KosherApp.this.commitPreferencesEditor(editor);

            // updateDatabase();

            KosherApp.this.updateCurrentLocation(LocationManager()
                    .getLastKnownLocation(LocationManager.GPS_PROVIDER));

            int result = 0;

            Common.Log(methodInfo, "END");
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            String methodInfo = "<KosherApp.OnCreateTask.onPostExecute(Integer):void>";
            Common.Log(methodInfo, "START");
            Common.Log(methodInfo,
                    String.format("result: %s", String.valueOf(result)));

            super.onPostExecute(result);

            Common.Log(methodInfo, "END");
        }
    }

    private class DeleteOldListDataTask extends
            AsyncTask<String, Integer, Integer> {

        private String oldLoadId = "";

        protected Integer doInBackground(String... listInfo) {
            String methodInfo = "<KosherApp.DeleteOldListDataTask.doInBackground(String...):Integer>";
            Common.Log(methodInfo, "START");

            int result = 0;

            int count = listInfo.length;
            if (count != 1)
                try {
                    throw new Exception(String.format(
                            "I got %s args instead of %s.",
                            String.valueOf(count), String.valueOf(1)));
                } catch (Exception e) {
                    Common.Log(methodInfo, Common.getString(e));
                    return -1;
                }

            oldLoadId = listInfo[0];

            DbAdapter().beginTransaction();

            if (!DbAdapter().deleteLocations(oldLoadId)) {
                return -1;
            }
            if (!DbAdapter().setTransactionSuccessful()) {
                return -1;
            }
            if (!DbAdapter().endTransaction()) {
                return -1;
            }
            Common.Log(methodInfo, "END");
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            String methodInfo = "<KosherApp.DeleteOldListDataTask.onPostExecute(Integer):void>";
            Common.Log(methodInfo, "START");
            super.onPostExecute(result);
            if (result == 0) {
                KosherApp.this.oldLoadIds.remove();
            }
            Common.Log(methodInfo, "END");
        }
    }

    private class AddLoyaltyPlacesToDBTask extends
            AsyncTask<String, Integer, Integer> {

        private int expectedArgsCount = 1;

        protected Integer doInBackground(String... listInfo) {
            String methodInfo = "<KosherApp.AddLoyaltyPlacesToDBTask.doInBackground(String...):Integer>";
            Common.LogMethodStart(methodInfo);

            int result = 0;

            int count = listInfo.length;
            if (count != expectedArgsCount) {
                String message = String.format(
                        "I got %s args instead of %s.",
                        String.valueOf(count), String.valueOf(expectedArgsCount));
                Common.Log(methodInfo, message);
                return -1;
            }

            String loadId = listInfo[0];

            Boolean isSuccessful = addLoyaltyPlacesToDB(loadId);

            Common.Log(methodInfo,
                    String.format("isSuccessful: %s", String.valueOf(isSuccessful)));

            if (isSuccessful) {
                String oldLoadId = getOldLoadId(Common.LIST_TYPE_LOYALTY);
                deleteOldData(oldLoadId);
                putNewLoadId(Common.LIST_TYPE_LOYALTY, loadId);
            } else {
                result = -1;
                DbAdapter().beginTransaction();
                DbAdapter().deleteLocations(loadId);
                DbAdapter().setTransactionSuccessful();
                DbAdapter().endTransaction();
            }

            Common.LogMethodEnd(methodInfo);
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            String methodInfo = "<KosherApp.AddLoyaltyPlacesToDBTask.onPostExecute(Integer):void>";
            Common.LogMethodStart(methodInfo);

            if (result >= 0) {
                KosherApp.this.registerDatabaseUpdate();
            }

            Common.LogMethodEnd(methodInfo);
            super.onPostExecute(result);
        }
    }

    private class AddRestaurantPlacesToDBTask extends
            AsyncTask<String, Integer, Integer> {

        private int expectedArgsCount = 1;

        protected Integer doInBackground(String... listInfo) {
            String methodInfo = "<KosherApp.AddRestaurantPlacesToDBTask.doInBackground(String...):Integer>";
            Common.LogMethodStart(methodInfo);

            int result = 0;

            int count = listInfo.length;
            if (count != expectedArgsCount) {
                String message = String.format(
                        "I got %s args instead of %s.",
                        String.valueOf(count), String.valueOf(expectedArgsCount));
                Common.Log(methodInfo, message);
                return -1;
            }

            String loadId = listInfo[0];

            Boolean isSuccessful = addRestaurantPlacesToDB(loadId);

            Common.Log(methodInfo,
                    String.format("isSuccessful: %s", String.valueOf(isSuccessful)));

            if (isSuccessful) {
                String oldLoadId = getOldLoadId(Common.LIST_TYPE_RESTAURANT);
                deleteOldData(oldLoadId);
                putNewLoadId(Common.LIST_TYPE_RESTAURANT, loadId);

                Common.Log(methodInfo, "test");
            } else {
                result = -1;
                DbAdapter().beginTransaction();
                DbAdapter().deleteLocations(loadId);
                DbAdapter().setTransactionSuccessful();
                DbAdapter().endTransaction();
            }

            Common.LogMethodEnd(methodInfo);
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            String methodInfo = "<KosherApp.AddRestaurantPlacesToDBTask.onPostExecute(Integer):void>";
            Common.LogMethodStart(methodInfo);

            if (result >= 0) {
                KosherApp.this.registerDatabaseUpdate();
            }

            Common.LogMethodEnd(methodInfo);
            super.onPostExecute(result);
        }
    }

    private class AddMikvehPlacesToDBTask extends
            AsyncTask<String, Integer, Integer> {

        private int expectedArgsCount = 1;

        protected Integer doInBackground(String... listInfo) {
            String methodInfo = "<KosherApp.AddMikvehPlacesToDBTask.doInBackground(String...):Integer>";
            Common.LogMethodStart(methodInfo);

            int result = 0;

            int count = listInfo.length;
            if (count != expectedArgsCount) {
                String message = String.format(
                        "I got %s args instead of %s.",
                        String.valueOf(count), String.valueOf(expectedArgsCount));
                Common.Log(methodInfo, message);
                return -1;
            }

            String loadId = listInfo[0];

            Boolean isSuccessful = addMikvehPlacesToDB(loadId);

            Common.Log(methodInfo,
                    String.format("isSuccessful: %s", String.valueOf(isSuccessful)));

            if (isSuccessful) {
                String oldLoadId = getOldLoadId(Common.LIST_TYPE_MIKVEH);
                deleteOldData(oldLoadId);
                putNewLoadId(Common.LIST_TYPE_MIKVEH, loadId);
            } else {
                result = -1;
                DbAdapter().beginTransaction();
                DbAdapter().deleteLocations(loadId);
                DbAdapter().setTransactionSuccessful();
                DbAdapter().endTransaction();
            }

            Common.LogMethodEnd(methodInfo);
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            String methodInfo = "<KosherApp.AddRestaurantPlacesToDBTask.onPostExecute(Integer):void>";
            Common.LogMethodStart(methodInfo);

            if (result >= 0) {
                KosherApp.this.registerDatabaseUpdate();
            }

            Common.LogMethodEnd(methodInfo);
            super.onPostExecute(result);
        }
    }

    private class AddMinyanPlacesToDBTask extends
            AsyncTask<String, Integer, Integer> {

        private int expectedArgsCount = 1;

        protected Integer doInBackground(String... listInfo) {
            String methodInfo = "<KosherApp.AddMinyanPlacesToDBTask.doInBackground(String...):Integer>";
            Common.LogMethodStart(methodInfo);

            int result = 0;

            int count = listInfo.length;
            if (count != expectedArgsCount) {
                String message = String.format(
                        "I got %s args instead of %s.",
                        String.valueOf(count), String.valueOf(expectedArgsCount));
                Common.Log(methodInfo, message);
                return -1;
            }

            String loadId = listInfo[0];

            Boolean isSuccessful = addMinyanPlacesToDB(loadId);

            Common.Log(methodInfo,
                    String.format("isSuccessful: %s", String.valueOf(isSuccessful)));

            if (isSuccessful) {
                String oldLoadId = getOldLoadId(Common.LIST_TYPE_MINYAN);
                deleteOldData(oldLoadId);
                putNewLoadId(Common.LIST_TYPE_MINYAN, loadId);
            } else {
                result = -1;
                DbAdapter().beginTransaction();
                DbAdapter().deleteLocations(loadId);
                DbAdapter().setTransactionSuccessful();
                DbAdapter().endTransaction();
            }

            Common.LogMethodEnd(methodInfo);
            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
            String methodInfo = "<KosherApp.AddMinyanPlacesToDBTask.onPostExecute(Integer):void>";
            Common.LogMethodStart(methodInfo);

            if (result >= 0) {
                KosherApp.this.registerDatabaseUpdate();
            }

            Common.LogMethodEnd(methodInfo);
            super.onPostExecute(result);
        }
    }

    private class UpdateKosherListTask extends
            AsyncTask<String, Integer, Integer> {
        protected Integer doInBackground(String... listInfo) {
            String methodInfo = "<KosherApp.UpdateKosherListTask.doInBackground(String...):Integer>";

            Common.Log(methodInfo, "START");

            int result = 0;

            int count = listInfo.length;
            if (count != 4)
                try {
                    throw new Exception(String.format(
                            "I got %s args instead of %s.",
                            String.valueOf(count), String.valueOf(1)));
                } catch (Exception e) {
                    Common.Log(methodInfo, Common.getString(e));
                    return -1;
                }

            String listType = listInfo[0];
            Common.Log(methodInfo, String.format("listType: %s", listType));

            kosherPlaces = getKosherPlacesFromDB(listType);
            Common.Log(
                    methodInfo,
                    String.format("kosherPlaces.size(): %s",
                            String.valueOf(kosherPlaces.size())));
            setKosherPlaces(kosherPlaces);

            Common.Log(
                    methodInfo,
                    String.format("getKosherPlaces().size(): %s",
                            String.valueOf(getKosherPlaces().size())));

            Common.LogMethodEnd(methodInfo);
            return result;
        }

        protected void onPostExecute(Integer result) {
            String methodInfo = "<KosherApp.UpdateKosherListTask.onPostExecute(Integer):void>";
            Common.Log(methodInfo, "START");
            display();
        }
    }

    private class UpdateLocationSpinnerTextTask extends
            AsyncTask<String, Integer, Integer> {
        private Integer expectedArgs = 1;
        private String listName = "";
        private String addressToUse = "";

        protected Integer doInBackground(String... listInfo) {
            String methodInfo = "<KosherApp.UpdateLocationSpinnerTextTask.doInBackground(String...):Integer>";
            Common.LogMethodStart(methodInfo);

            int result = 0;

            int count = listInfo.length;
            if (count != expectedArgs)
                try {
                    throw new Exception(
                            String.format("I got %s args instead of %s.",
                                    String.valueOf(count),
                                    String.valueOf(expectedArgs)));
                } catch (Exception e) {
                    Common.Log(methodInfo, Common.getString(e));
                    return -1;
                }

            this.listName = listInfo[0];

            Common.Log(methodInfo, String.format("this.listName = %s", this.listName));
            String[] streetAddress = Maps.getAddress(KosherApp.this,
                    KosherApp.this.getAppLatitude(),
                    KosherApp.this.getAppLongitude());

            this.addressToUse = "";
            String space = "";
            for (int i = 0; i < streetAddress.length; i++) {
                this.addressToUse += space + streetAddress[i];
                space = " ";
            }
            if (this.addressToUse.equals("")) {
                this.addressToUse = "Near Me";
            }
            Common.Log(methodInfo,
                    String.format("addressToUse: %s", addressToUse));

            return result;
        }

        protected void onPostExecute(Integer result) {
            String methodInfo = "<KosherApp.UpdateLocationSpinnerTextTask.onPostExecute(Integer):void>";
            Common.Log(methodInfo, "START");

            KosherApp.this.setLocationText(String.format("%s", addressToUse));
            Common.Log(methodInfo, String.format("listName: %s", listName));
            new UpdateKosherListTask().execute(listName,
                    String.valueOf(KosherApp.this.getAppLatitude()),
                    String.valueOf(KosherApp.this.getAppLongitude()), "all");
            showProgressDialog("Updating Kosher Locations...");
        }
    }

    public ArrayList<KosherPlace> getKosherPlacesFromDB(String type) {
        String methodInfo = "<KosherApp.getKosherPlacesFromDB(String):ArrayList<KosherPlace>>";
        Common.Log(methodInfo, String.format("type: %s", type));

        ArrayList<KosherPlace> kosherPlaces = new ArrayList<KosherPlace>();

        FoodTypeEnumerator foodTypeEnumerator = new FoodTypeEnumerator();

        if (this.isDbUpdating()) {
            Common.Log(
                    methodInfo,
                    String.format("this.isDbUpdating(): %s",
                            String.valueOf(this.isDbUpdating())));
            return kosherPlaces;
        }

        String loadId = getOldLoadId(type);

        if (dbAdapter == null) {
            return kosherPlaces;
        }
        dbAdapter.openReadOnly();
        if (!dbAdapter.isReadOnlyDatabaseAvailable()) {
            Common.Log(methodInfo, String.format(
                    "dbAdapter.isReadOnlyDatabaseAvailable(): %s",
                    String.valueOf(dbAdapter.isReadOnlyDatabaseAvailable())));
            return kosherPlaces;
        }

        Cursor c = null;
        if (type.equalsIgnoreCase(Common.LIST_TYPE_RESTAURANT)) {
            c = this.DbAdapter().fetchAllRestaurants(loadId);
        } else if (type.equalsIgnoreCase(Common.LIST_TYPE_MIKVEH)) {
            c = this.DbAdapter().fetchAllMikvahs(loadId);
        } else if (type.equalsIgnoreCase(Common.LIST_TYPE_MINYAN)) {
            c = this.DbAdapter().fetchAllMinyans(loadId);
        } else if (type.equalsIgnoreCase(Common.LIST_TYPE_LOYALTY)) {
            c = this.DbAdapter().fetchAllLoyaltyLocations(loadId);
        }
        if (c == null) {
            Common.Log(methodInfo,
                    String.format("c == null: %s", String.valueOf(c == null)));
            return kosherPlaces;
        }

        while (c.moveToNext()) {
            int columnIndex = c.getColumnIndex(DbAdapter.KEY_LOCATION_ID);
            int id = c.getInt(columnIndex);
            Common.Log(methodInfo, String.format("id: %s", String.valueOf(id)));
            columnIndex = c.getColumnIndex(DbAdapter.KEY_NAME);
            String name = c.getString(columnIndex);
            columnIndex = c.getColumnIndex(DbAdapter.KEY_PHONE);
            String phone = c.getString(columnIndex);
            columnIndex = c.getColumnIndex(DbAdapter.KEY_POST_ADDRESS);
            String address = c.getString(columnIndex);
            columnIndex = c.getColumnIndex(DbAdapter.KEY_LONGITUDE);
            Double longitude = c.getDouble(columnIndex);
            columnIndex = c.getColumnIndex(DbAdapter.KEY_LATITUDE);
            Double latitude = c.getDouble(columnIndex);
            columnIndex = c.getColumnIndex(DbAdapter.KEY_WEBSITE);
            String website = c.getString(columnIndex);
            int loyaltyId = Integer.MIN_VALUE;
            if (type.equals(Common.LIST_TYPE_RESTAURANT)) {
                columnIndex = c.getColumnIndex(DbAdapter.KEY_LOYALTY_ID);
                loyaltyId = c.getInt(columnIndex);
            }
            if (type.equals(Common.LIST_TYPE_LOYALTY)) {
                columnIndex = c.getColumnIndex(DbAdapter.KEY_LOCATION_ID);
                loyaltyId = c.getInt(columnIndex);
            }
            String discount = null;
            int foodType = -1;
            if (Common.LIST_TYPE_LOYALTY.equals(type)) {
            } else {
                columnIndex = c.getColumnIndex(DbAdapter.KEY_DISCOUNT);
                discount = c.getString(columnIndex);
                columnIndex = c.getColumnIndex(DbAdapter.KEY_FOOD_TYPE);
                foodType = c.getInt(columnIndex);
            }

            KosherPlace kp = new KosherPlace(id, name, phone, address,
                    longitude, latitude, discount, website, type, foodType,
                    loyaltyId, foodTypeEnumerator);

            kosherPlaces.add(kp);
        }
        c.close();
        // dbAdapter.close();

        return kosherPlaces;
    }

    public void doneUpdatingDatabaseMessage() {
        Toast.makeText(
                this,
                "The database of locations has been updated. You may now refresh the data.",
                Toast.LENGTH_LONG).show();

        if (this.kosherPlaceAdapter == null) {
            refresh();
        } else if (this.kosherPlaceAdapter.subItems == null) {
            refresh();
        } else if (this.kosherPlaceAdapter.subItems.size() == 0) {
            refresh();
        }

    }

    public void clearFilters() {
        this.locationNameFilter = "";
        this.discountFilter = false;
        this.distanceFilter = 20;
        this.foodTypeFilter = "All";
    }

    public boolean databaseIsRegistered() {
        String methodInfo = "<KosherApp.databaseIsRegistered():boolean>";

        Long thenMilliseconds = this.Preferences().getLong(
                Common.PREFS_KEY_DB_UPDATE_DATE, Long.MIN_VALUE);

        Boolean isDefaultDifference = thenMilliseconds == Long.MIN_VALUE;
        Common.Log(
                methodInfo,
                String.format("isDefaultDifference: %s",
                        String.valueOf(isDefaultDifference)));
        if (isDefaultDifference) {
            return false;
        }

        return true;
    }

    private boolean databaseIsUpToDate() {
        String methodInfo = "<KosherApp.databaseIsUpToDate():boolean>";
        Date now = new Date();
        Long nowMilliseconds = now.getTime();

        Long thenMilliseconds = this.Preferences().getLong(
                Common.PREFS_KEY_DB_UPDATE_DATE, Long.MIN_VALUE);

        Boolean isDefaultDifference = thenMilliseconds == Long.MIN_VALUE;
        Common.Log(
                methodInfo,
                String.format("isDefaultDifference: %s",
                        String.valueOf(isDefaultDifference)));
        if (isDefaultDifference) {
            return false;
        }

        // if thenMilliseconds is default (and negative), the difference will be
        // greater than Common.ONE_DAY_IN_MILLIS
        Boolean isBigDifference = (nowMilliseconds - thenMilliseconds) >= Common.ONE_DAY_IN_MILLIS;
        if (isBigDifference)
            return false;
        Common.Log(
                methodInfo,
                String.format("isBigDifference: %s",
                        String.valueOf(isBigDifference)));

        return true;
    }

    public Boolean putNewLoadId(String listType, String loadId) {
        SharedPreferences.Editor editor = KosherApp.this.Preferences().edit();

        if (listType.equals(Common.LIST_TYPE_MIKVEH)) {
            editor.putString(Common.PREFS_KEY_MIKVEHS_LOAD_ID, loadId);
            this.commitPreferencesEditor(editor);
            return true;
        }
        if (listType.equals(Common.LIST_TYPE_MINYAN)) {
            editor.putString(Common.PREFS_KEY_MINYAN_LOAD_ID, loadId);
            this.commitPreferencesEditor(editor);
            return true;
        }
        if (listType.equals(Common.LIST_TYPE_RESTAURANT)) {
            editor.putString(Common.PREFS_KEY_RESTAURANTS_LOAD_ID, loadId);
            this.commitPreferencesEditor(editor);
            return true;
        }
        if (listType.equals(Common.LIST_TYPE_LOYALTY)) {
            editor.putString(Common.PREFS_KEY_LOYALTY_LOAD_ID, loadId);
            this.commitPreferencesEditor(editor);
            return true;
        }

        return false;
    }

    public String getOldLoadId(String listType) {
        String oldLoadId = "";

        if (listType.equals(Common.LIST_TYPE_MIKVEH))
            return this.Preferences().getString(
                    Common.PREFS_KEY_MIKVEHS_LOAD_ID, "");
        if (listType.equals(Common.LIST_TYPE_MINYAN))
            return this.Preferences().getString(
                    Common.PREFS_KEY_MINYAN_LOAD_ID, "");
        if (listType.equals(Common.LIST_TYPE_RESTAURANT))
            return this.Preferences().getString(
                    Common.PREFS_KEY_RESTAURANTS_LOAD_ID, "");
        if (listType.equals(Common.LIST_TYPE_LOYALTY))
            return this.Preferences().getString(
                    Common.PREFS_KEY_LOYALTY_LOAD_ID, "");

        return oldLoadId;
    }

    public Boolean addRestaurantPlacesToDB(String loadId) {
        String methodInfo = "<KosherApp.addRestaurantPlacesToDB(String):Boolean>";
        Common.Log(methodInfo, "START");
        Boolean isSuccessful = true;
        Common.Log(methodInfo, String.format("loadId: %s", loadId));

        Date loadDate = new Date();
        Date start = new Date();
        this.DbAdapter().beginTransaction();

        ArrayList<KosherPlace> kosherPlaces = restaurantPlaces;

        Common.Log(methodInfo, String.format("kosherPlaces.size(): %s", String.valueOf(kosherPlaces.size())));

        for (int i = 0; i < kosherPlaces.size(); i++) {
            long result = 0;
            KosherPlace kosherPlace = kosherPlaces.get(i);
            if (kosherPlace.Type()
                    .equalsIgnoreCase(Common.LIST_TYPE_RESTAURANT)) {
                result = this.DbAdapter().addRestaurant(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Discount(), kosherPlace.Website(),
                        kosherPlace.Type(), kosherPlace.FoodType(),
                        kosherPlace.getLoyaltyId());
            } else if (kosherPlace.Type().equalsIgnoreCase(
                    Common.LIST_TYPE_MIKVEH)) {
                result = this.DbAdapter().addMikvah(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Discount(), kosherPlace.Website(),
                        kosherPlace.Type(), kosherPlace.FoodType());
            } else if (kosherPlace.Type().equalsIgnoreCase(
                    Common.LIST_TYPE_MINYAN)) {
                result = this.DbAdapter().addMinyan(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Discount(), kosherPlace.Website(),
                        kosherPlace.Type(), kosherPlace.FoodType());
            } else if (kosherPlace.Type().equalsIgnoreCase(
                    Common.LIST_TYPE_LOYALTY)) {
                result = this.DbAdapter().addLoyaltyLocation(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Website(), kosherPlace.Type());
            }
            if (-1 == result) {
                Common.Log(methodInfo,
                        String.format("result: %s", String.valueOf(result)));
                return false;
            }
        }
        Boolean successful = this.DbAdapter().setTransactionSuccessful();
        if (!successful) {
            Common.Log(methodInfo, String.format(
                    "this.DbAdapter().setTransactionSuccessful(): %s",
                    String.valueOf(successful)));
            return false;
        }
        successful = this.DbAdapter().endTransaction();
        if (!successful) {
            Common.Log(methodInfo, String.format(
                    "this.DbAdapter().endTransaction(): %s",
                    String.valueOf(successful)));
            return false;
        }
        Date end = new Date();
        long diff = end.getTime() - start.getTime();
        Common.Log(methodInfo, String.format("DB Insert Time: %s millis",
                String.valueOf(diff)));

        return isSuccessful;
    }

    public Boolean addMinyanPlacesToDB(String loadId) {
        String methodInfo = "<KosherApp.addMinyanPlacesToDB(String):Boolean>";
        Common.Log(methodInfo, "START");
        Boolean isSuccessful = true;
        Common.Log(methodInfo, String.format("loadId: %s", loadId));

        Date loadDate = new Date();
        Date start = new Date();
        this.DbAdapter().beginTransaction();

        ArrayList<KosherPlace> kosherPlaces = minyanPlaces;

        Common.Log(methodInfo, String.format("kosherPlaces.size(): %s", String.valueOf(kosherPlaces.size())));

        for (int i = 0; i < kosherPlaces.size(); i++) {
            long result = 0;
            KosherPlace kosherPlace = kosherPlaces.get(i);
            if (kosherPlace.Type()
                    .equalsIgnoreCase(Common.LIST_TYPE_RESTAURANT)) {
                result = this.DbAdapter().addRestaurant(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Discount(), kosherPlace.Website(),
                        kosherPlace.Type(), kosherPlace.FoodType(),
                        kosherPlace.getLoyaltyId());
            } else if (kosherPlace.Type().equalsIgnoreCase(
                    Common.LIST_TYPE_MIKVEH)) {
                result = this.DbAdapter().addMikvah(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Discount(), kosherPlace.Website(),
                        kosherPlace.Type(), kosherPlace.FoodType());
            } else if (kosherPlace.Type().equalsIgnoreCase(
                    Common.LIST_TYPE_MINYAN)) {
                result = this.DbAdapter().addMinyan(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Discount(), kosherPlace.Website(),
                        kosherPlace.Type(), kosherPlace.FoodType());
            } else if (kosherPlace.Type().equalsIgnoreCase(
                    Common.LIST_TYPE_LOYALTY)) {
                result = this.DbAdapter().addLoyaltyLocation(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Website(), kosherPlace.Type());
            }
            if (-1 == result) {
                Common.Log(methodInfo,
                        String.format("result: %s", String.valueOf(result)));
                return false;
            }
        }
        Boolean successful = this.DbAdapter().setTransactionSuccessful();
        if (!successful) {
            Common.Log(methodInfo, String.format(
                    "this.DbAdapter().setTransactionSuccessful(): %s",
                    String.valueOf(successful)));
            return false;
        }
        successful = this.DbAdapter().endTransaction();
        if (!successful) {
            Common.Log(methodInfo, String.format(
                    "this.DbAdapter().endTransaction(): %s",
                    String.valueOf(successful)));
            return false;
        }
        Date end = new Date();
        long diff = end.getTime() - start.getTime();
        Common.Log(methodInfo, String.format("DB Insert Time: %s millis",
                String.valueOf(diff)));

        return isSuccessful;
    }

    public Boolean addMikvehPlacesToDB(String loadId) {
        String methodInfo = "<KosherApp.addMikvehPlacesToDB(String):Boolean>";
        Common.Log(methodInfo, "START");
        Boolean isSuccessful = true;
        Common.Log(methodInfo, String.format("loadId: %s", loadId));

        Date loadDate = new Date();
        Date start = new Date();
        this.DbAdapter().beginTransaction();

        ArrayList<KosherPlace> kosherPlaces = mikvehPlaces;

        Common.Log(methodInfo, String.format("kosherPlaces.size(): %s", String.valueOf(kosherPlaces.size())));

        for (int i = 0; i < kosherPlaces.size(); i++) {
            long result = 0;
            KosherPlace kosherPlace = kosherPlaces.get(i);
            if (kosherPlace.Type()
                    .equalsIgnoreCase(Common.LIST_TYPE_RESTAURANT)) {
                result = this.DbAdapter().addRestaurant(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Discount(), kosherPlace.Website(),
                        kosherPlace.Type(), kosherPlace.FoodType(),
                        kosherPlace.getLoyaltyId());
            } else if (kosherPlace.Type().equalsIgnoreCase(
                    Common.LIST_TYPE_MIKVEH)) {
                result = this.DbAdapter().addMikvah(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Discount(), kosherPlace.Website(),
                        kosherPlace.Type(), kosherPlace.FoodType());
            } else if (kosherPlace.Type().equalsIgnoreCase(
                    Common.LIST_TYPE_MINYAN)) {
                result = this.DbAdapter().addMinyan(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Discount(), kosherPlace.Website(),
                        kosherPlace.Type(), kosherPlace.FoodType());
            } else if (kosherPlace.Type().equalsIgnoreCase(
                    Common.LIST_TYPE_LOYALTY)) {
                result = this.DbAdapter().addLoyaltyLocation(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Website(), kosherPlace.Type());
            }
            if (-1 == result) {
                Common.Log(methodInfo,
                        String.format("result: %s", String.valueOf(result)));
                return false;
            }
        }
        Boolean successful = this.DbAdapter().setTransactionSuccessful();
        if (!successful) {
            Common.Log(methodInfo, String.format(
                    "this.DbAdapter().setTransactionSuccessful(): %s",
                    String.valueOf(successful)));
            return false;
        }
        successful = this.DbAdapter().endTransaction();
        if (!successful) {
            Common.Log(methodInfo, String.format(
                    "this.DbAdapter().endTransaction(): %s",
                    String.valueOf(successful)));
            return false;
        }
        Date end = new Date();
        long diff = end.getTime() - start.getTime();
        Common.Log(methodInfo, String.format("DB Insert Time: %s millis",
                String.valueOf(diff)));

        return isSuccessful;
    }

    public Boolean addLoyaltyPlacesToDB(String loadId) {
        String methodInfo = "<KosherApp.addLoyaltyPlacesToDB(String):Boolean>";
        Common.Log(methodInfo, "START");
        Boolean isSuccessful = true;
        Common.Log(methodInfo, String.format("loadId: %s", loadId));

        Date loadDate = new Date();
        Date start = new Date();
        this.DbAdapter().beginTransaction();

        ArrayList<KosherPlace> kosherPlaces = loyaltyPlaces;

        Common.Log(methodInfo, String.format("kosherPlaces.size(): %s", String.valueOf(kosherPlaces.size())));

        for (int i = 0; i < kosherPlaces.size(); i++) {
            long result = 0;
            KosherPlace kosherPlace = kosherPlaces.get(i);
            if (kosherPlace.Type()
                    .equalsIgnoreCase(Common.LIST_TYPE_RESTAURANT)) {
                result = this.DbAdapter().addRestaurant(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Discount(), kosherPlace.Website(),
                        kosherPlace.Type(), kosherPlace.FoodType(),
                        kosherPlace.getLoyaltyId());
            } else if (kosherPlace.Type().equalsIgnoreCase(
                    Common.LIST_TYPE_MIKVEH)) {
                result = this.DbAdapter().addMikvah(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Discount(), kosherPlace.Website(),
                        kosherPlace.Type(), kosherPlace.FoodType());
            } else if (kosherPlace.Type().equalsIgnoreCase(
                    Common.LIST_TYPE_MINYAN)) {
                result = this.DbAdapter().addMinyan(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Discount(), kosherPlace.Website(),
                        kosherPlace.Type(), kosherPlace.FoodType());
            } else if (kosherPlace.Type().equalsIgnoreCase(
                    Common.LIST_TYPE_LOYALTY)) {
                result = this.DbAdapter().addLoyaltyLocation(loadDate, loadId,
                        kosherPlace.getKosherId(), kosherPlace.Name(),
                        kosherPlace.Phone(), kosherPlace.Address(),
                        kosherPlace.longitude, kosherPlace.Latitude(),
                        kosherPlace.Website(), kosherPlace.Type());
            }
            if (-1 == result) {
                Common.Log(methodInfo,
                        String.format("result: %s", String.valueOf(result)));
                return false;
            }
        }
        Boolean successful = this.DbAdapter().setTransactionSuccessful();
        if (!successful) {
            Common.Log(methodInfo, String.format(
                    "this.DbAdapter().setTransactionSuccessful(): %s",
                    String.valueOf(successful)));
            return false;
        }
        successful = this.DbAdapter().endTransaction();
        if (!successful) {
            Common.Log(methodInfo, String.format(
                    "this.DbAdapter().endTransaction(): %s",
                    String.valueOf(successful)));
            return false;
        }
        Date end = new Date();
        long diff = end.getTime() - start.getTime();
        Common.Log(methodInfo, String.format("DB Insert Time: %s millis",
                String.valueOf(diff)));

        return isSuccessful;
    }

    @Override
    public void finish() {
        this.DbAdapter().close();

        super.finish();
    }

    private void setKosherPlaces(ArrayList<KosherPlace> kosherPlaces) {
        String methodInfo = "<KosherApp.setKosherPlaces(ArrayList<KosherPlace>):void>";
        if (kosherPlaces == null) {
            Common.Log(methodInfo, "no kosherPlaces found");
            return;
        }
        if (kosherPlaces.size() == 0) {
            Common.Log(methodInfo, "no kosherPlaces found");
            updateDatabase();
            return;
        }
        this.kosherPlaces = kosherPlaces;
    }

    private ArrayList<KosherPlace> getKosherPlaces() {
        if (kosherPlaces == null) {
            kosherPlaces = new ArrayList<KosherPlace>();
        }
        return kosherPlaces;
    }

    private void registerDatabaseUpdate() {
        SharedPreferences.Editor editor = KosherApp.this.Preferences().edit();
        Date now = new Date();
        Long nowMilliseconds = now.getTime();
        editor.putLong(Common.PREFS_KEY_DB_UPDATE_DATE, nowMilliseconds);
        KosherApp.this.commitPreferencesEditor(editor);
    }

    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location newLocation) {
            String methodInfo = "<KosherApp.MyLocationListener.onLocationChanged(Location):void>";
            Common.Log(methodInfo, "START");

            KosherApp.this.updateCurrentLocation(newLocation);
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

    }

}