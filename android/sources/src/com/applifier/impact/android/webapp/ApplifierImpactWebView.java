package com.applifier.impact.android.webapp;

import java.lang.reflect.Method;

import org.json.JSONObject;

import com.applifier.impact.android.ApplifierImpactUtils;
import com.applifier.impact.android.data.ApplifierImpactDevice;
import com.applifier.impact.android.properties.ApplifierImpactConstants;
import com.applifier.impact.android.properties.ApplifierImpactProperties;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ApplifierImpactWebView extends WebView {

	private String _url = null;
	private IApplifierImpactWebViewListener _listener = null;
	private boolean _webAppLoaded = false;
	private ApplifierImpactWebBridge _webBridge = null;
	
	public ApplifierImpactWebView(Activity activity, IApplifierImpactWebViewListener listener, ApplifierImpactWebBridge webBridge) {
		super(activity);
		ApplifierImpactUtils.Log("Loading WebView from URL: " + ApplifierImpactProperties.WEBVIEW_BASE_URL, this);
		init(activity, ApplifierImpactProperties.WEBVIEW_BASE_URL, listener, webBridge);
	}

	public ApplifierImpactWebView(Activity activity, String url, IApplifierImpactWebViewListener listener, ApplifierImpactWebBridge webBridge) {
		super(activity);
		init(activity, url, listener, webBridge);
	}
	
	public boolean isWebAppLoaded () {
		return _webAppLoaded;
	}
	
	public void setWebViewCurrentView (String view) {
		setWebViewCurrentView(view, null);
	}
	
	public void setWebViewCurrentView (String view, JSONObject data) {		
		if (isWebAppLoaded()) {
			String dataString = "{}";
			
			if (data != null)
				dataString = data.toString();
			
			String javascriptString = String.format("%s%s(\"%s\", %s);", ApplifierImpactConstants.IMPACT_WEBVIEW_JS_PREFIX, ApplifierImpactConstants.IMPACT_WEBVIEW_JS_CHANGE_VIEW, view, dataString);
			ApplifierImpactProperties.CURRENT_ACTIVITY.runOnUiThread(new ApplifierImpactJavascriptRunner(javascriptString));
			ApplifierImpactUtils.Log("Send change view to WebApp: " + javascriptString, this);
		}
	}
	
	public void sendNativeEventToWebApp (String eventType, JSONObject data) {
		if (isWebAppLoaded()) {
			String dataString = "{}";
			
			if (data != null)
				dataString = data.toString();

			String javascriptString = String.format("%s%s(\"%s\", %s);", ApplifierImpactConstants.IMPACT_WEBVIEW_JS_PREFIX, ApplifierImpactConstants.IMPACT_WEBVIEW_JS_HANDLE_NATIVE_EVENT, eventType, dataString);
			ApplifierImpactUtils.Log("Send native event to WebApp: " + javascriptString, this);
			ApplifierImpactProperties.CURRENT_ACTIVITY.runOnUiThread(new ApplifierImpactJavascriptRunner(javascriptString));
		}
	}
	
	public void initWebApp (JSONObject data) {
		if (isWebAppLoaded()) {
			JSONObject initData = new JSONObject();
			
			try {				
				// Basic data
				initData.put(ApplifierImpactConstants.IMPACT_WEBVIEW_DATAPARAM_CAMPAIGNDATA_KEY, data);
				initData.put(ApplifierImpactConstants.IMPACT_WEBVIEW_DATAPARAM_PLATFORM_KEY, "android");
				initData.put(ApplifierImpactConstants.IMPACT_WEBVIEW_DATAPARAM_DEVICEID_KEY, ApplifierImpactDevice.getDeviceId());
				initData.put(ApplifierImpactConstants.IMPACT_WEBVIEW_DATAPARAM_OPENUDID_KEY, ApplifierImpactDevice.getOpenUdid());
				initData.put(ApplifierImpactConstants.IMPACT_WEBVIEW_DATAPARAM_MACADDRESS_KEY, ApplifierImpactDevice.getMacAddress());
				initData.put(ApplifierImpactConstants.IMPACT_WEBVIEW_DATAPARAM_SDKVERSION_KEY, ApplifierImpactConstants.IMPACT_VERSION);
				initData.put(ApplifierImpactConstants.IMPACT_WEBVIEW_DATAPARAM_GAMEID_KEY, ApplifierImpactProperties.IMPACT_GAME_ID);
				
				// Tracking data
				initData.put(ApplifierImpactConstants.IMPACT_WEBVIEW_DATAPARAM_SOFTWAREVERSION_KEY, ApplifierImpactDevice.getSoftwareVersion());
				initData.put(ApplifierImpactConstants.IMPACT_WEBVIEW_DATAPARAM_DEVICETYPE_KEY, ApplifierImpactDevice.getDeviceType());
			}
			catch (Exception e) {
				ApplifierImpactUtils.Log("Error creating webview init params", this);
				return;
			}
			
			String initString = String.format("%s%s(%s);", ApplifierImpactConstants.IMPACT_WEBVIEW_JS_PREFIX, ApplifierImpactConstants.IMPACT_WEBVIEW_JS_INIT, initData.toString());
			ApplifierImpactUtils.Log("Initializing WebView with JS call: " + initString, this);
			ApplifierImpactProperties.CURRENT_ACTIVITY.runOnUiThread(new ApplifierImpactJavascriptRunner(initString));
		}
	}

	
	/* INTENRAL METHODS */
	
	private void init (Activity activity, String url, IApplifierImpactWebViewListener listener, ApplifierImpactWebBridge webBridge) {
		_listener = listener;
		_url = url;
		_webBridge = webBridge;
		setupApplifierView();
		loadUrl(_url);
	}
	
	private void setupApplifierView ()  {
		getSettings().setJavaScriptEnabled(true);
		
		if (_url != null && _url.indexOf("_raw.html") != -1) {
			getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
			ApplifierImpactUtils.Log("startup() -> LOAD_NO_CACHE", this);
		}
		else {
			getSettings().setCacheMode(WebSettings.LOAD_NORMAL);
		}
		
		String appCachePath = getContext().getCacheDir().toString();
		
		getSettings().setSupportZoom(false);
		getSettings().setBuiltInZoomControls(false);
		getSettings().setLightTouchEnabled(false);
		getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
		getSettings().setSupportMultipleWindows(false);
		getSettings().setPluginsEnabled(false);
		getSettings().setAllowFileAccess(false);
		
		setHorizontalScrollBarEnabled(false);
		setVerticalScrollBarEnabled(false);		

		setClickable(true);
		setFocusable(true);
		setFocusableInTouchMode(true);
		setInitialScale(0);
		setLongClickable(false);
		
		setBackgroundColor(Color.BLACK);
		setBackgroundDrawable(null);
		setBackgroundResource(0);
		
		setWebViewClient(new ApplifierViewClient());
		setWebChromeClient(new ApplifierViewChromeClient());
			
		if (appCachePath != null) {
			boolean appCache = true;
  
			if (Integer.parseInt(android.os.Build.VERSION.SDK) <= 7) {
				appCache = false;
			}
  
			getSettings().setAppCacheEnabled(appCache);
			getSettings().setDomStorageEnabled(true);
			getSettings().setAppCacheMaxSize(1024*1024*10);
			getSettings().setAppCachePath(appCachePath);
			getSettings().setAllowFileAccess(true);
		}
		
		// WebView background will go white in SDK >= 11 if you don't set webview's
		// layer-type to software.
		try
		{
			Method layertype = View.class.getMethod("setLayerType", Integer.TYPE, Paint.class);
			layertype.invoke(this, 1, null);
		}
		catch (Exception e) {
			ApplifierImpactUtils.Log("Could not invoke setLayerType", this);
		}
		
		ApplifierImpactUtils.Log("Adding javascript interface", this);
		addJavascriptInterface(_webBridge, "applifierimpactnative");
	}
	
	
	/* OVERRIDE METHODS */
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
		    	if (_listener != null)
		    		_listener.onBackButtonClicked(this);
		    	return true;
		}
    	
    	return false;
    }
	
	
	/* SUBCLASSES */
	
	private class ApplifierViewChromeClient extends WebChromeClient {
		public void onConsoleMessage(String message, int lineNumber, String sourceID) {
			ApplifierImpactUtils.Log("JavaScript (line: " + lineNumber + "): " + message, this);
		}
		
		public void onReachedMaxAppCacheSize(long spaceNeeded, long totalUsedQuota, WebStorage.QuotaUpdater quotaUpdater) {
			quotaUpdater.updateQuota(spaceNeeded * 2);
		}
	}
	
	private class ApplifierViewClient extends WebViewClient {
		@Override
		public void onPageFinished (WebView webview, String url) {
			super.onPageFinished(webview, url);
			ApplifierImpactUtils.Log("Finished url: "  + url, this);
			if (_listener != null && !_webAppLoaded) {
				_webAppLoaded = true;
				ApplifierImpactUtils.Log("Adding javascript interface", this);
				addJavascriptInterface(_webBridge, "applifierimpactnative");
				_listener.onWebAppLoaded();
			}
		}
		
		@Override
		public boolean shouldOverrideUrlLoading (WebView view, String url) {
			ApplifierImpactUtils.Log("Trying to load url: " + url, this);
			return false;
		}
		
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {		
			ApplifierImpactUtils.Log("ApplifierViewClient.onReceivedError() -> " + errorCode + " (" + failingUrl + ") " + description, this);
			super.onReceivedError(view, errorCode, description, failingUrl);
		}

		@Override
		public void onLoadResource(WebView view, String url) {
			super.onLoadResource(view, url);
		}	
	}

	
	/* PRIVATE CLASSES */
	
	private class ApplifierImpactJavascriptRunner implements Runnable {
		
		private String _jsString = null;
		
		public ApplifierImpactJavascriptRunner (String jsString) {
			_jsString = jsString;
		}
		
		@Override
		public void run() {
			loadUrl(_jsString);
		}		
	}
}
