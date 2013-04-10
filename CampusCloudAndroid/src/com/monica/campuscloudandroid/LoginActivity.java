package com.monica.campuscloudandroid;

import com.monica.campuscloudandroid.component.Network;
import com.monica.campuscloudandroid.component.Settings;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class LoginActivity extends Activity {
	
	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;
	private CheckBox mAutoLogin;
	private CheckBox mSavePassword;
	// misc
	Settings _settings = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this._settings = Settings.instance(this.getApplication());
		setContentView(R.layout.activity_login);
		

		// Set up the login form.
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);
		
		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
		
		mSavePassword = (CheckBox)findViewById(R.id.checkBox_savepassword);
		mAutoLogin = (CheckBox)findViewById(R.id.checkBox_autologin);
		
		//fill form
		String email = this._settings.get("email","");
		this.mEmailView.setText(email);
		
		String pass = this._settings.get("pass","");
		this.mPasswordView.setText(pass);
		
		Boolean isSavePass = Boolean.parseBoolean(this._settings.get("save-pass","false"));
		this.mSavePassword.setChecked(isSavePass);
		
		Boolean isAutoLogin = Boolean.parseBoolean(this._settings.get("auto-login", "false"));
		this.mAutoLogin.setChecked(isAutoLogin);
		
		String ipAddr = _settings.get("address", "");
		if(TextUtils.isEmpty(ipAddr)) {
			this.jumpToSettingsActivity();
		} else {
			String notAutoLogin = getIntent().getStringExtra("notAutoLogin");
			if(isAutoLogin && null == notAutoLogin) {
				this.attemptLogin();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(Menu.NONE,Menu.FIRST,1,"Setting").setIcon(
				android.R.drawable.ic_menu_edit);
		menu.add(Menu.NONE,Menu.FIRST,2,"Exit").setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);


		return true;
	}
	
	private void jumpToSettingsActivity() {
		Intent intent = new Intent(LoginActivity.this,
				SettingsActivity.class);
		startActivity(intent);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		CharSequence cs = item.getTitle();
		String title = cs.toString();
		Log.d("onOptionItemSelected", title);
		if (title == "Setting") {
			this.jumpToSettingsActivity();
		}
		if(title == "Exit"){
			this.finish();
		}
		return true;
	}

	private void saveSettings() {
		String email = mEmailView.getText().toString();
		_settings.set("email", email);
		String pass = mPasswordView.getText().toString();
		_settings.set("pass", pass);
		String isSave = String.valueOf(mSavePassword.isChecked());
		_settings.set("save-pass", isSave);
		String isAutoLogin = String.valueOf(mAutoLogin.isChecked());
		_settings.set("auto-login", isAutoLogin);
		
	}
	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}
		
		this.saveSettings();
		

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} 

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute((Void) null);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {

			//Check network
			if(!Network.isNetworkAvailable(LoginActivity.this)) {
				mError = "Network is unavailable!";
				return false;
			}
			//TODO Try Login
			
			return true;
		}
		
		protected String mError = null;

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				jumpToMainActivity();
				finish();
			} else {
				if (null == mError) {
					mEmailView.setError(mError);
					
				} else {
					mPasswordView
							.setError(getString(R.string.error_incorrect_password));
					mPasswordView.requestFocus();
				}
			}
		}

		private void jumpToMainActivity() {
			Intent intent = new Intent(LoginActivity.this, MainActivity.class);
			startActivity(intent);
			LoginActivity.this.finish();
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}
}
