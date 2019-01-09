package com.teammanagementapp.ankush.streamer.activity.movies;

import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.teammanagementapp.ankush.streamer.R;
import com.teammanagementapp.ankush.streamer.activity.anime.NineAnime;

import org.adblockplus.libadblockplus.android.settings.AdblockHelper;
import org.adblockplus.libadblockplus.android.webview.AdblockWebView;

import java.io.IOException;
import java.io.InputStream;

public class Fmovies extends AppCompatActivity {
    private ProgressBar progress;
    private FrameLayout customViewContainer;
    private AdblockWebView webView;
    public static final boolean USE_EXTERNAL_ADBLOCKENGINE = false;
    public static final boolean DEVELOPMENT_BUILD = true;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private View mCustomView;
    private Toolbar mTopToolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fmovies);

        if(savedInstanceState==null)
            bindControls(true);
        else
            bindControls(false);

        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initControls();

    }



    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    private void bindControls(boolean isItNewActivity) {

        progress = (ProgressBar) findViewById(R.id.main_progress);
        webView = (AdblockWebView) findViewById(R.id.main_webview);
        if(isItNewActivity)
            webView.loadUrl("https://www.fmovies.io");
        customViewContainer = (FrameLayout) findViewById(R.id.customViewContainer);
        mTopToolbar = (Toolbar) findViewById(R.id.toolbar);

    }

    private void setProgressVisible(boolean visible) {
        progress.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
    }

    private void initControls() {

        initAdblockWebView();

        setProgressVisible(false);

        // to get debug/warning log output
        webView.setDebugMode(DEVELOPMENT_BUILD);

        // render as fast as we can
        webView.setAllowDrawDelay(0);

        // to show that external WebViewClient is still working
        webView.setWebViewClient(webViewClient);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setSaveFormData(true);
        // to show that external WebChromeClient is still working
        webView.setWebChromeClient(mWebChromeClient);

    }

    private WebChromeClient  mWebChromeClient = new WebChromeClient() {

        private Bitmap mDefaultVideoPoster;
        private View mVideoProgressView;


        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            super.onProgressChanged(view, newProgress);
            progress.setProgress(newProgress);
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            getSupportActionBar().setTitle(title);
        }

        @Override
        public void onShowCustomView(View view, int requestedOrientation, CustomViewCallback callback) {
            onShowCustomView(view,  callback);
        }

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            super.onShowCustomView(view, callback);

            if (mCustomView != null) {
                callback.onCustomViewHidden();
                return;
            }

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mTopToolbar.setVisibility(View.INVISIBLE);
            mCustomView = view;
            webView.setVisibility(View.GONE);
            customViewContainer.setVisibility(View.VISIBLE);
            customViewContainer.addView(view);
            customViewCallback = callback;


        }

        @Override
        public View getVideoLoadingProgressView() {

            if (mVideoProgressView == null) {
                LayoutInflater inflater = LayoutInflater.from(Fmovies.this);
                mVideoProgressView = inflater.inflate(R.layout.video_progress, null);
            }
            return mVideoProgressView;
        }
        @Override
        public void onHideCustomView() {
            super.onHideCustomView();    //To change body of overridden methods use File | Settings | File Templates.
            if (mCustomView == null)
                return;

            mTopToolbar.setVisibility(View.VISIBLE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            webView.setVisibility(View.VISIBLE);
            customViewContainer.setVisibility(View.GONE);

            // Hide the custom view.
            mCustomView.setVisibility(View.GONE);

            // Remove the custom view from its container.
            customViewContainer.removeView(mCustomView);
            customViewCallback.onCustomViewHidden();

            mCustomView = null;
        }


    };

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater myMenuInflater = getMenuInflater();
        myMenuInflater.inflate(R.menu.defined_website_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.backArrow:
                loadPrev();
                break;

            case R.id.forwardArrow:
                loadForward();
                break;

            case R.id.download:
                //enabling downloader to download videos from 9anime
                break;

            case R.id.Settings:

                break;
        }
        return true;
    }

    private WebViewClient webViewClient = new WebViewClient() {

        private void injectScriptFile(WebView view, String scriptFile) {
            InputStream input;
            try {
                input = getAssets().open(scriptFile);
                byte[] buffer = new byte[input.available()];
                input.read(buffer);
                input.close();

                // String-ify the script byte-array using BASE64 encoding !!!
                String encoded = Base64.encodeToString(buffer, Base64.NO_WRAP);
                view.loadUrl("javascript:(function() {" +
                        "var parent = document.getElementsByTagName('head').item(0);" +
                        "var script = document.createElement('script');" +
                        "script.type = 'text/javascript';" +
                        "script.innerHTML = window.atob('$encoded');" +
                        "parent.appendChild(script)" +
                        "})()");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }



        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            setProgressVisible(true);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            setProgressVisible(false);
            //attaching my own js script here
            // injectScriptFile(view, "js/script.js");
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if(url.endsWith(".jpg"))
                return true;

            return !Uri.parse(url).getHost().contains("fmovies");
        }


    };

    private void initAdblockWebView() {
        if (USE_EXTERNAL_ADBLOCKENGINE) {
            // external AdblockEngine
            webView.setProvider(AdblockHelper.get().getProvider());
        } else {
            // AdblockWebView will create internal AdblockEngine instance

        }
    }

    private void loadPrev() {
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }


    private void loadForward() {
        if (webView.canGoForward()) {
            webView.goForward();
        } else {
            Toast.makeText(this, "Can't go further!", Toast.LENGTH_SHORT).show();
        }
    }




    @Override
    protected void onDestroy() {
        webView.dispose(null);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }



}
