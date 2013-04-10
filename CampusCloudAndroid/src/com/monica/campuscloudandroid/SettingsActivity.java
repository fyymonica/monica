package com.monica.campuscloudandroid;

import com.monica.campuscloudandroid.component.Settings;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends Activity {

	EditText mAddress = null;
	Settings _settings = null;
	Button mSaveBtn = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this._settings = Settings.instance(this.getApplication());
		setContentView(R.layout.activity_settings);
		
		mSaveBtn = (Button)findViewById(R.id.saveBtn);

		mAddress = (EditText) findViewById(R.id.editText_address);
		mSaveBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String address = mAddress.getText().toString();
				_settings.set("address", address);
			}
		}
		);
		
		
		
		//fill form
		String address = this._settings.get("address","");
		this.mAddress.setText(address);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

}
