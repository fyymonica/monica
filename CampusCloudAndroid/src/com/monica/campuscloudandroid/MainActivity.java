package com.monica.campuscloudandroid;

import com.monica.campuscloudandroid.component.Network;
import com.monica.campuscloudandroid.component.Settings;
import com.monica.campuscloudandroid.component.WebInterface;

import android.os.Bundle;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {

	boolean isOpenNew = false;
	String lastUrl = null;
	
	final class WebClient extends WebViewClient {
		
		
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if(isOpenNew) {
				isOpenNew = false;
				lastUrl = view.getUrl();
			}
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			_processDialog.hide();
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			_processDialog.show();
		}
	}
	
	final class MyWebChromeClient extends WebChromeClient{
		private String lastUrl = null;
		@Override
		public boolean onCreateWindow(WebView view, boolean isDialog,
				boolean isUserGesture, Message resultMsg) {
			isOpenNew = true;
			return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
		}
		@Override
		public void onCloseWindow(WebView window) {
			super.onCloseWindow(window);
			if(null == lastUrl) {
				navigate_remote(HOME_URL);
			}else {
				navigate_remote(lastUrl);
				lastUrl = null;
			}
		}
	}

	final String HOME_URL = "/eyeos/";

	WebView _webView = null;
	WebClient _webClient = null;
	MyWebChromeClient _webChromeClient = null;
	WebInterface _webInterface = null;
	Settings _settings = null;
	ProgressDialog _processDialog = null;

	private void jumpToSettingsActivity() {
		MainActivity.this.finish();
		Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
		intent.putExtra("notAutoLogin", "true");
		startActivity(intent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initialize();
	}

	@SuppressLint({ "HandlerLeak" })
	private void initialize() {
		this._settings = Settings.instance(this.getApplication());
		this._webInterface = new WebInterface(this._settings);
		this._webClient = new WebClient();
		this._webChromeClient = new MyWebChromeClient();
		this._processDialog = new ProgressDialog(this);
		this._processDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		this._processDialog.setMessage("loading...");

		_webView = (WebView) findViewById(R.id.webView_content);

		WebSettings settings = this._webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setSaveFormData(true);
		settings.setSavePassword(true);
		settings.setSupportZoom(false);
		settings.setDomStorageEnabled(true);
		settings.setBuiltInZoomControls(true);
		settings.setJavaScriptCanOpenWindowsAutomatically(true);
		settings.setAllowFileAccess(true);
		settings.setSupportMultipleWindows(false);
		
		this._webView.setWebViewClient(this._webClient);
		this._webView.setWebChromeClient(this._webChromeClient);
		this._webView.addJavascriptInterface(this._webInterface, "interface");

		
		// º”‘ÿ÷˜“≥
		navigate_remote(HOME_URL);
	}

	private void navigate_remote(String url) {
		String re_addr = _settings.get("address", "");
		if (!re_addr.startsWith("http://")) {
			re_addr = "http://" + re_addr;
		}
		final String address = re_addr;
		final String abs_url = address + url;
		if (Network.isNetworkAvailable(this)) {
			_webView.loadUrl(abs_url);
		} else {
			Toast.makeText(getApplicationContext(), "Network is unavailable",
					Toast.LENGTH_SHORT).show();
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		CharSequence cs = item.getTitle();
		String title = cs.toString();
		Log.d("onOptionItemSelected", title);
		if (title == "Exit") {
			this.finish();
		}
		if (title == "Reconnect") {
			jumpToSettingsActivity();
		}
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST, 1, "Reconnect").setIcon(
				android.R.drawable.ic_menu_edit);
		menu.add(Menu.NONE, Menu.FIRST, 2, "Exit").setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}
}
