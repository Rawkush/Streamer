package com.teammanagementapp.ankush.streamer.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Patterns;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.teammanagementapp.ankush.streamer.R;
import com.teammanagementapp.ankush.streamer.activity.anime.NineAnime;

import org.adblockplus.libadblockplus.android.settings.AdblockHelper;
import org.adblockplus.libadblockplus.android.webview.AdblockWebView;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ProgressBar progress;
    private EditText url;
    private AdblockWebView webView;
    public static final boolean USE_EXTERNAL_ADBLOCKENGINE = false;
    public static final boolean DEVELOPMENT_BUILD = true;
    private Toolbar mTopToolbar;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private View mCustomView;
    private FrameLayout customViewContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState==null)
            bindControls(true);
        else
            bindControls(false);

        // binding controls to elements from xml to java
        setSupportActionBar(mTopToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initControls();


    }

    private void bindControls(boolean isItNewActivity) {

        url = (EditText) findViewById(R.id.main_url);
        progress = (ProgressBar) findViewById(R.id.main_progress);
        webView = (AdblockWebView) findViewById(R.id.main_webview);

        if(isItNewActivity)
            webView.loadUrl("https://www.google.com");

        mTopToolbar = (Toolbar) findViewById(R.id.toolbar);
        customViewContainer = (FrameLayout) findViewById(R.id.customViewContainer);

        url.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loadUrl(url.getText().toString());
                    return true;
                }
                return false;
            }
        });

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

        // to show that external WebChromeClient is still working
        webView.setWebChromeClient(mWebChromeClient);

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
            // show updated URL (because of possible redirection)
            MainActivity.this.url.setText(url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            setProgressVisible(false);
            //attaching my own js script here
            // injectScriptFile(view, "js/script.js");
        }

    };

            //////////////////
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        MenuInflater myMenuInflater = getMenuInflater();
        myMenuInflater.inflate(R.menu.super_menu, menu);

        /*
         **Search Bar
         */
        final MenuItem searchViewItem = menu.findItem(R.id.action_search);
        final SearchView searchViewAndroidActionBar = (SearchView) MenuItemCompat.getActionView(searchViewItem);

        searchViewAndroidActionBar.setFocusable(true);
        searchViewAndroidActionBar.setClickable(true);


        searchViewAndroidActionBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchViewAndroidActionBar.clearFocus();
                searchViewItem.collapseActionView();
                loadUrl(query);


                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
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

                break;

            case R.id.Settings:

                break;
        }
        return true;
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




            webView.setDownloadListener(new DownloadListener() {
                @Override
                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                    Uri myUri = Uri.parse(url);
                    Intent superIntent = new Intent(Intent.ACTION_VIEW);
                    superIntent.setData(myUri);
                    startActivity(superIntent);
                }
            });
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
            url.setVisibility(View.INVISIBLE);
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
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
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
            url.setVisibility(View.VISIBLE);
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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }


    private void initAdblockWebView() {
        if (USE_EXTERNAL_ADBLOCKENGINE) {
            // external AdblockEngine
            webView.setProvider(AdblockHelper.get().getProvider());
        } else {
            // AdblockWebView will create internal AdblockEngine instance

        }
    }

    private void loadPrev() {
        hideSoftwareKeyboard();
        if (webView.canGoBack()) {
            webView.goBack();
        }
    }

    private void hideSoftwareKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(url.getWindowToken(), 0);
    }

    private void loadForward() {
        hideSoftwareKeyboard();
        if (webView.canGoForward()) {
            webView.goForward();
        } else {
            Toast.makeText(this, "Can't go further!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUrl(String query) {
        hideSoftwareKeyboard();
        webView.loadUrl(prepareUrl(query));
    }

    private String prepareUrl(String query) {
        if(query.contains("9anime")){
            Intent intent= new Intent(this,NineAnime.class);
            startActivity(intent);
        }

        if(query.contains("fmovies")){
            Intent intent= new Intent(this,NineAnime.class);
            startActivity(intent);
        }

        if (query.startsWith("http"))
            return query;
        else

        if (query.startsWith("www"))
            return  "http://" + query;
        // make sure url is valid URL
        if(Patterns.WEB_URL.matcher(query).matches()){
            return "http://www." +query;
        }else{
                String s[]=query.split(" ");
                query="";
                for (String tmp:s) {
                    if (query.equals("")){
                        query=query + tmp;
                    }else{
                        query=query + "+" +tmp;
                    }
                }
            return "https://www.google.com/search?q=" + query;
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
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("Exit App");
            dialog.setMessage("Browser has nothing to go back, so what next?");
            dialog.setPositiveButton("EXIT ME", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.setCancelable(false);
            dialog.setNegativeButton("STAY HERE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();

        }
    }
}
