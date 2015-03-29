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

public class FullScreenAdView extends Activity {

	// private TextView notificationHeadlineTextView = null;
	private TextView	notificationTextView										= null;
	private TextView	notificationHyperlinkTextView	= null;
	private TextView	notificationOkButton										= null;
	private TextView	notificationGotoButton								= null;

	private TextView NotificationTextView() {
		if (this.notificationTextView == null) {
			this.notificationTextView = (TextView) findViewById(R.id.notificationText);
		}
		return this.notificationTextView;
	}

	// private TextView NotificationHeadlineTextView() {
	// if (this.notificationHeadlineTextView == null) {
	// this.notificationHeadlineTextView = (TextView)
	// findViewById(R.id.notificationHeadlineText);
	// }
	// return this.notificationHeadlineTextView;
	// }

	private TextView NotificationHyperlinkTextView() {
		if (this.notificationHyperlinkTextView == null) {
			this.notificationHyperlinkTextView = (TextView) findViewById(R.id.notificationHyperlinkText);
		}
		return this.notificationHyperlinkTextView;
	}

	private TextView NotificationOkButton() {
		if (this.notificationOkButton == null) {
			this.notificationOkButton = (TextView) findViewById(R.id.notificationOkButton);
		}
		return this.notificationOkButton;
	}

	private TextView NotificationGotoButton() {
		if (this.notificationGotoButton == null) {
			this.notificationGotoButton = (TextView) findViewById(R.id.notificationGoToButton);
		}
		return this.notificationGotoButton;
	}

	public void onCreate(Bundle savedInstanceState) {
		String methodInfo = "<NotificationView.onCreate(Bundle):void>";
		Common.Log(methodInfo, "START");

		super.onCreate(savedInstanceState);
		setContentView(R.layout.notification);

		Intent data = getIntent();

		String notificationText = data
			.getStringExtra(Common.INTENT_KEY_NOTIFICATION_DESCRIPTION);
		if (notificationText == null) {
			throw new IllegalArgumentException();
		}
		if (notificationText.equals("")) {
			throw new IllegalArgumentException();
		}
		NotificationTextView().setText(notificationText);
		NotificationTextView().setMovementMethod(new ScrollingMovementMethod());

		String notificationHyperlinkText = data
			.getStringExtra(Common.INTENT_KEY_NOTIFICATION_HYPERLINK_TEXT);
		if (notificationHyperlinkText == null) {
			throw new IllegalArgumentException();
		}
		if (notificationHyperlinkText.equals("")) {
			throw new IllegalArgumentException();
		}
		NotificationHyperlinkTextView().setText(notificationHyperlinkText);

		NotificationOkButton().setOnTouchListener(new OnTouchListener() {

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

		NotificationGotoButton().setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event == null)
					return false;
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					String hyperlinkText = NotificationHyperlinkTextView().getText()
						.toString();
					Common.gotoHyperlink(hyperlinkText, FullScreenAdView.this);

					Intent returnData = new Intent();
					returnData.putExtra(Common.INTENT_KEY_NOTIFICATION_CLICKED, true);
					setResult(Activity.RESULT_OK, returnData);
					finish();

					return true;
				}
				return false;
			}
		});
	}
}
