package com.kosherapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.TextView;

public class TipView extends Activity {

	public void onCreate(Bundle savedInstanceState) {
		Log.d(Common.LOG_TAG, "Start TipView onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tips);

		Intent data = getIntent();

		String tipText = data.getStringExtra(Common.INTENT_KEY_TIP_TEXT);

		TextView tipTV = (TextView) this.findViewById(R.id.tipText);
		tipTV.setText(tipText);
		tipTV.setMovementMethod(new ScrollingMovementMethod());

		Button tipButton = (Button) this.findViewById(R.id.tipButton);
		tipButton.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event == null)
					return false;
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					finish();
					return true;
				}
				return false;
			}
		});
	}
}
