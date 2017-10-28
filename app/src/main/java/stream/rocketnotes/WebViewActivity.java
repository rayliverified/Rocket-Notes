package stream.rocketnotes;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.pyze.android.Pyze;

import stream.rocketnotes.utils.AnalyticsUtils;

public class WebViewActivity extends AppCompatActivity {

    WebView mWebView;
    ProgressBar mProgressBar;
    TextView mErrorText;
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


        mErrorText = findViewById(R.id.webview_error_text);
        mErrorText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Refresh(sUrl);
            }
        });
        mProgressBar = findViewById(R.id.webview_progressbar);
        mProgressBar.setIndeterminate(true);

        mWebView = findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient() {

            //If you will not use this method url links are opeen in new brower not in webview
            @SuppressWarnings("deprecation")
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                view.loadUrl(url);
                return true;
            }

            @TargetApi(Build.VERSION_CODES.N)
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                final Uri uri = request.getUrl();
                return true;
            }

            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                showLoadingScreen();
            }

            public void onPageFinished(WebView view, String url) {

                showContentScreen();
            }

            @SuppressWarnings("deprecation")
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

                showErrorScreen();
                Toast.makeText(getApplicationContext(), errorCode + " " + description + " " + failingUrl, Toast.LENGTH_SHORT).show();
            }

            @TargetApi(Build.VERSION_CODES.N)
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                // Redirect to deprecated method, so you can use it in all SDK versions
                onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
            }
        });
        mWebView.setWebChromeClient(new WebChromeClient() {

            public void onProgressChanged(WebView view, int progress) {
                if(progress < 15) {
                    mProgressBar.setIndeterminate(true);
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                if(progress >= 90) {
                    mProgressBar.setVisibility(View.GONE);
                }
                else if(progress > 15) {
                    mProgressBar.setIndeterminate(false);
                    mProgressBar.setProgress(progress);
                }
            }
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
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
        mWebView.loadUrl(urlString);
    }

    public void showLoadingScreen() {

        Log.d("Screen", "Loading");
        mErrorText.setVisibility(View.GONE);
    }

    public void showErrorScreen() {

        Log.d("Screen", "Error");
        mWebView.setVisibility(View.GONE);
        mErrorText.setVisibility(View.VISIBLE);
    }

    public void showContentScreen() {

        Log.d("Screen", "Content");
        mWebView.setVisibility(View.VISIBLE);
        mErrorText.setVisibility(View.GONE);
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
