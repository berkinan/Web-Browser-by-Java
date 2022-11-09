package com.example.elifgonulayar_browserapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.app.AlertDialog;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity
{
    EditText main_Url;
    Button search_Button;
    WebView web_View;
    ProgressBar progressBar;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        main_Url = findViewById(R.id.url_Bar);
        search_Button = findViewById(R.id.search_Button);
        web_View = findViewById(R.id.web_View);

        progressBar = findViewById(R.id.loading_Phase);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");

        WebSettings webSettings = web_View.getSettings();
        webSettings.setBuiltInZoomControls(true);
        webSettings.setJavaScriptEnabled(true);

        setTitle("Web Browser");
        web_View.setWebViewClient(new MyWebViewClient());

        if (!checkConnection())
        {
            showDialog();
        }

        search_Button.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View view)
            {

                String url = main_Url.getText().toString().trim();

                if (!url.startsWith("http://") || !url.startsWith("https://"))
                {
                    url = "https://" + url;
                }

                web_View.loadUrl(url);
            }
        });

        web_View.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress)
            {
                setTitle("Loading...");
                progressBar.setProgress(newProgress);
                progressDialog.show();

                if (newProgress == 100)
                {
                    setTitle(web_View.getTitle());
                    progressDialog.dismiss();
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        web_View.setDownloadListener(new MyDownLoadListener());
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {

            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon)
        {
            super.onPageStarted(view, url, favicon);
            progressBar.setVisibility(View.VISIBLE);
            web_View.setVisibility(View.GONE);
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
            super.onPageFinished(view, url);
            progressBar.setVisibility(View.GONE);
            web_View.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed()
    {
        if (web_View.canGoBack())
        {
            web_View.goBack();
        }
        else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to exit???")
                    .setCancelable(false)
                    .setPositiveButton("Yes!", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            finish();
                        }
                    })
                    .setNegativeButton("No!", new DialogInterface.OnClickListener()
                    {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.navigate_Prev:
                onBackPressed();
                break;
            case R.id.navigate_Next:
                if (web_View.canGoForward())
                {
                    web_View.goForward();
                }
                break;
            case R.id.page_Reload:
                checkConnection();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean checkConnection()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null)
        {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
                if (capabilities != null)
                {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
                    {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                    {
                        return true;
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET))
                    {
                        return true;
                    }
                }
            } else
            {
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected())
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void showDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Connect to Internet or Exit!!!")
                .setCancelable(false)
                .setPositiveButton("Connect to Internet", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public class MyDownLoadListener implements DownloadListener
    {
        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {

            if (url != null)
            {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        }
    }
}