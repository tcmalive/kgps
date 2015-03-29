/**
 *
 */
package com.kosherapp;

import com.kosherapp.util.Maps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Contacts;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Josh
 */
public class LoyaltyRedeemScreen extends Activity {

    private Button okButton = null;
    private Button cancelButton = null;
    private TextView codeLabel = null;
    private TextView codeInput = null;
    private TextView punchAmtLabel = null;
    private TextView punchAmtInput = null;
    private String action = null;
    private ListService listService = null;
    private Boolean processingAction = false;
    int loyaltyId;
    int loyaltyItemId;
    Double latitude;
    Double longitude;
    String deviceId;
    String code;

    protected Intent data = null;
    private TextView cardName = null;

    private View punchView = null;
    private View redeemView = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        String methodInfo = "<LoyaltyRedeemScreen.onCreate(Bundle):void>";

        super.onCreate(savedInstanceState);

        setContentView(R.layout.loyaltyredeemscreen);

        this.data = getIntent();
        this.loyaltyId = this.data.getIntExtra(Common.INTENT_KEY_LOYALTY_ID, -1);
        this.loyaltyItemId = this.data.getIntExtra(Common.INTENT_KEY_LOYALTY_ITEM_ID,
                -1);
        this.latitude = this.data.getDoubleExtra(Common.INTENT_KEY_LOCATION_LATITUDE,
                Double.MIN_VALUE);
        this.longitude = this.data.getDoubleExtra(
                Common.INTENT_KEY_LOCATION_LONGITUDE, Double.MIN_VALUE);
        this.deviceId = this.data.getStringExtra(Common.INTENT_KEY_DEVICE_ID);

        this.action = this.data.getStringExtra(Common.INTENT_KEY_REDEEM_ACTION);
        if (Common.LOYALTY_REDEEM_ACTION_PUNCH.equals(this.action)) {
            this.setTitle("Hand to cashier for punch");
        }
        if (Common.LOYALTY_REDEEM_ACTION_REDEEM.equals(this.action)) {
            this.setTitle("Hand to cashier for redemption");
        }

        this.okButton = (Button) findViewById(R.id.loyaltyredeemscreen_ok);
        this.cancelButton = (Button) findViewById(R.id.loyaltyredeemscreen_cancel);
        this.codeLabel = (TextView) findViewById(R.id.loyaltyredeemscreen_codelabel);
        this.codeInput = (TextView) findViewById(R.id.loyaltyredeemscreen_codeinput);
        this.punchAmtLabel = (TextView) findViewById(R.id.loyaltyredeemscreen_punchamtlabel);
        this.punchAmtInput = (TextView) findViewById(R.id.loyaltyredeemscreen_punchamtinput);
        this.cardName = (TextView) findViewById(R.id.loyaltyredeemscreen_cardName);

        if (Common.LOYALTY_REDEEM_ACTION_REDEEM.equals(this.action)) {
            this.punchAmtInput.setVisibility(View.INVISIBLE);
            this.punchAmtLabel.setVisibility(View.INVISIBLE);
            this.punchAmtInput.setEnabled(false);
            this.punchAmtLabel.setEnabled(false);
        }

        this.cardName.setText(this.data
                .getStringExtra(Common.INTENT_KEY_LOYALTYCARD_NAME));

        this.okButton.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String methodInfo = "<LoyaltyRedeemScreen.onCreate(Bundle):void.new OnTouchListener() {...}.onTouch(View, MotionEvent):boolean>";
                Common.LogMethodStart(methodInfo);
                Button b = (Button) v;

                // b.setClickable(false);
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Common.Log(methodInfo,
                            String.format("b.isEnabled(): %s", String.valueOf(b.isEnabled())));
                    b.setEnabled(false);
                    Common.Log(methodInfo,
                            String.format("b.isEnabled(): %s", String.valueOf(b.isEnabled())));
                    if (processingAction == true) {
                        Toast.makeText(LoyaltyRedeemScreen.this, "Already processing...",
                                Toast.LENGTH_LONG).show();
                        Common.Log(methodInfo, "Already processing...");
                        return true;
                    } else {
                        processingAction = true;
                        Common.Log(methodInfo, "Processing new action...");
                    }
                    if (LoyaltyRedeemScreen.this.action
                            .equals(Common.LOYALTY_REDEEM_ACTION_REDEEM)) {
                        redeem(v);
                    }
                    if (LoyaltyRedeemScreen.this.action
                            .equals(Common.LOYALTY_REDEEM_ACTION_PUNCH)) {
                        punch(v);
                    }
                }
                return true;
            }

            ;
        });

        this.cancelButton.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    Intent data = new Intent();
                    data.putExtra(Common.INTENT_KEY_REDEEM_ACTION,
                            Common.LOYALTY_REDEEM_ACTION_REDEEM);

                    LoyaltyRedeemScreen.this.setResult(Activity.RESULT_CANCELED, data);
                    LoyaltyRedeemScreen.this.finish();
                }
                return true;
            }
        });

        Double defaultPunchAmt = data.getDoubleExtra(
                Common.INTENT_KEY_LOYALTYCARD_DEFAULTPUNCHAMT, Double.MIN_VALUE);

        Common.Log(methodInfo,
                String.format("defaultPunchAmt: %s", String.valueOf(defaultPunchAmt)));
        this.punchAmtInput.setText(String.valueOf(defaultPunchAmt));
    }

    protected void punch(View v) {
        this.punchView = v;
        new PunchTask().execute();
    }

    protected void redeem(View v) {
        String methodInfo = "<LoyaltyRedeemScreen.redeem(View):void>";
        this.redeemView = v;

        new RedeemTask().execute();
    }

    private ListService ListService() {
        if (listService == null) {
            listService = new ListService(this);
        }
        return this.listService;
    }

    private class PunchTask extends
            AsyncTask<String, Integer, Integer> {

        Button b;

        protected Integer doInBackground(String... listInfo) {
            String methodInfo = "<LoyaltyRedeemScreen.PunchTask.doInBackground(String...):Integer>";
            Common.LogMethodStart(methodInfo);

            int toReturn = 0;

            String punchAmtStr = LoyaltyRedeemScreen.this.punchAmtInput.getText().toString();
            Common.Log(methodInfo, String.format("punchAmtStr: %s", punchAmtStr));
            b = (Button) LoyaltyRedeemScreen.this.punchView;

            if (punchAmtStr == null) {
                processingAction = false;

                return -1;
            }

            if (punchAmtStr.equals("")) {
                processingAction = false;
                return -1;
            }

            Double punchAmt = Double.parseDouble(punchAmtStr);
            if (punchAmt <= 0)

            {
                processingAction = false;
                return -1;
            }

            LoyaltyRedeemScreen.this.code = LoyaltyRedeemScreen.this.codeInput.getText().

                    toString();

            String result = ListService().LoyaltyPunch(LoyaltyRedeemScreen.this.loyaltyId,
                    LoyaltyRedeemScreen.this.loyaltyItemId, LoyaltyRedeemScreen.this.latitude, LoyaltyRedeemScreen.this.longitude, LoyaltyRedeemScreen.this.deviceId, punchAmt,
                    LoyaltyRedeemScreen.this.code);
            Common.Log(methodInfo, String.format("result: %s", result));
            if (result.equals("-1"))

            {
                processingAction = false;
                return -1;
            }

            Intent data = new Intent();
            data.putExtra(Common.INTENT_KEY_REDEEM_ACTION,
                    Common.LOYALTY_REDEEM_ACTION_PUNCH);
            data.putExtra(Common.INTENT_KEY_PUNCH_RESULT, result);
            LoyaltyRedeemScreen.this.setResult(Activity.RESULT_OK, data);

            processingAction = false;
            LoyaltyRedeemScreen.this.

                    finish();

            Common.LogMethodEnd(methodInfo);
            return toReturn;
        }

        @Override
        protected void onPostExecute(Integer result) {
            String methodInfo = "<LoyaltyRedeemScreen.PunchTask.onPostExecute(Integer):void>";
            Common.LogMethodStart(methodInfo);

            if (result == -1) {
                Toast.makeText(getApplicationContext(),
                        "Your input is invalid. Please try again.", Toast.LENGTH_LONG)
                        .show();
            }
            b.setEnabled(true);

            Common.LogMethodEnd(methodInfo);
            super.onPostExecute(result);
        }
    }

    private class RedeemTask extends
            AsyncTask<String, Integer, Integer> {

        Button b;

        protected Integer doInBackground(String... listInfo) {
            String methodInfo = "<LoyaltyRedeemScreen.RedeemTask.doInBackground(String...):Integer>";
            Common.LogMethodStart(methodInfo);

            int toReturn = 0;

            LoyaltyRedeemScreen.this.code = LoyaltyRedeemScreen.this.codeInput.getText().toString();

            String result = ListService().LoyaltyRedeem(LoyaltyRedeemScreen.this.loyaltyId,
                    LoyaltyRedeemScreen.this.loyaltyItemId, LoyaltyRedeemScreen.this.latitude, LoyaltyRedeemScreen.this.longitude, LoyaltyRedeemScreen.this.deviceId, LoyaltyRedeemScreen.this.code);
            Common.Log(methodInfo, String.format("result: %s", result));

            if (result.equals("-1")) {
                processingAction = false;
                return -1;
            }

            Intent data = new Intent();
            data.putExtra(Common.INTENT_KEY_REDEEM_ACTION,
                    Common.LOYALTY_REDEEM_ACTION_REDEEM);
            data.putExtra(Common.INTENT_KEY_REDEEM_RESULT, result);

            LoyaltyRedeemScreen.this.setResult(Activity.RESULT_OK, data);
            LoyaltyRedeemScreen.this.finish();
            processingAction = false;
            return 0;
        }

        @Override
        protected void onPostExecute(Integer result) {
            String methodInfo = "<LoyaltyRedeemScreen.RedeemTask.onPostExecute(Integer):void>";
            Common.LogMethodStart(methodInfo);

            Button b = (Button) LoyaltyRedeemScreen.this.redeemView;
            b.setEnabled(true);

            Common.LogMethodEnd(methodInfo);
            super.onPostExecute(result);
        }
    }

}
