package stream.rocketnotes;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;
import com.jetradar.desertplaceholder.DesertPlaceholder;
import com.pyze.android.Pyze;

import im.delight.android.webview.AdvancedWebView;
import stream.rocketnotes.utils.AnalyticsUtils;

public class WebViewActivity extends AppCompatActivity implements AdvancedWebView.Listener  {

    AdvancedWebView mWebView;
    DesertPlaceholder mWebViewErrorScreen;
    String sUrl, sTitle;
    private String mActivity = this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        Intent i = getIntent();
        sUrl = i.getStringExtra(Constants.URL);
        sTitle = i.getStringExtra(Constants.TITLE);
        InitializeAnalytics();

        mWebView = findViewById(R.id.webview);
        mWebViewErrorScreen = findViewById(R.id.WebViewErrorScreen);
        mWebViewErrorScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Refresh(sUrl);
            }
        });

        mWebView.setListener(this, this);
        mWebView.loadUrl(sUrl);

        ActionBar();
    }

    private void ActionBar() {
        ActionBar toolBar = getSupportActionBar();
        if (toolBar != null) {
            toolBar.setDisplayHomeAsUpEnabled(true);
            toolBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            toolBar.setCustomView(R.layout.toolbar_default);
            toolBar.setElevation(0);
            Toolbar parent = (Toolbar) toolBar.getCustomView().getParent();
            parent.setContentInsetsAbsolute(0,0);
        }

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(sTitle);

        ImageView toolbarBack = findViewById(R.id.toolbar_back);
        toolbarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void Refresh (final String urlString){
        mWebView.loadUrl(sUrl, true);
    }

    public void showLoadingScreen() {

        Log.d("Screen", "Loading");
        mWebViewErrorScreen.setVisibility(View.GONE);
    }

    public void showErrorScreen() {

        Log.d("Screen", "Error");
        mWebView.setVisibility(View.GONE);
        mWebViewErrorScreen.setVisibility(View.VISIBLE);
    }

    public void showContentScreen() {

        Log.d("Screen", "Content");
        mWebView.setVisibility(View.VISIBLE);
        mWebViewErrorScreen.setVisibility(View.GONE);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @SuppressLint("NewApi")
    @Override
    protected void onPause() {
        mWebView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mWebView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {

    }

    @Override
    public void onPageFinished(String url) {
        showContentScreen();
    }

    @Override
    public void onPageError(int errorCode, String description, String failingUrl) {
        Log.d("Error Code", String.valueOf(errorCode));
        if (errorCode == -2)
        {
            showErrorScreen();
        }
    }

    @Override
    public void onDownloadRequested(String url, String suggestedFilename, String mimeType, long contentLength, String contentDisposition, String userAgent) {

    }

    @Override
    public void onExternalPageRequest(String url) {

    }

    public void InitializeAnalytics()
    {
        if (!FlurryAgent.isSessionActive())
        {
            new FlurryAgent.Builder()
                    .withLogEnabled(true)
                    .build(this, Constants.FLURRY_API_KEY);
        }
        Pyze.initialize(getApplication());
//        UXCam.startWithKey(Constants.UXCAM_API_KEY);

        AnalyticsUtils.AnalyticEvent(mActivity, "Title", sTitle);
    }
}
