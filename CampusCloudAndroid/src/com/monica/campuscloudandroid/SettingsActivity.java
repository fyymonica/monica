package com.monica.campuscloudandroid;

import com.monica.campuscloudandroid.component.Network;
import com.monica.campuscloudandroid.component.Settings;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	EditText mAddress = null;
	Settings _settings = null;
	Button mSaveBtn = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this._settings = Settings.instance(this.getApplication());
		setContentView(R.layout.activity_settings);

		mSaveBtn = (Button) findViewById(R.id.saveBtn);

		mAddress = (EditText) findViewById(R.id.editText_address);
		mSaveBtn.setOnClickListener(new View.OnClickListener() {

			private void showMessage(String msg) {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
						.show();
			}

			private void jumpToMainActivity() {
				if (!Network.isNetworkAvailable(SettingsActivity.this)) {
					this.showMessage("Network is unavailable");
				} else {
					Intent intent = new Intent(SettingsActivity.this,
							MainActivity.class);
					startActivity(intent);
					SettingsActivity.this.finish();
				}
			}

			@Override
			public void onClick(View v) {
				String address = mAddress.getText().toString();
				if (address == "") {
					this.showMessage("address is empty!");
				} else {
					_settings.set("address", address);
					jumpToMainActivity();
				}
			}
		});

		// fill form
		String address = this._settings.get("address", "192.168.1.101");
		this.mAddress.setText(address);

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		CharSequence cs = item.getTitle();
		String title = cs.toString();
		if (title == "Exit") {
			this.finish();
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.add(Menu.NONE, Menu.FIRST, 1, "Exit").setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

}
