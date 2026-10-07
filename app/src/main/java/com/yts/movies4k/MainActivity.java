package com.yts.movies4k;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private static final String TARGET_URL = "https://en.yts.lu/browse-movies?keyword=&quality=2160p&genre=all&rating=0&year=0&order_by=latest";

    // Known ad networks and popup trackers to block
    private static final List<String> AD_DOMAINS = Arrays.asList(
            "cloudfront.net/?afjpd=",
            "adservice", "popads", "syndication", "adsterra",
            "monetag", "propeller", "bet365", "doubleclick",
            "googleads", "googlesyndication", "onclick", "trafficjunky",
            "exoclick", "juicyads", "adnxs", "histats", "yadro",
            "banner", "popup", "promotions", "sponsor", "onclickmega",
            "adkeeper", "coinhive", "streamad"
    );

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Strict Anti-Popup Settings
        settings.setSupportMultipleWindows(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);

        swipeRefreshLayout.setOnRefreshListener(() -> webView.reload());

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    swipeRefreshLayout.setRefreshing(false);
                    injectAdBlockCss(view);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString().toLowerCase();
                for (String ad : AD_DOMAINS) {
                    if (url.contains(ad)) {
                        return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream("".getBytes()));
                    }
                }
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                return handleUrlNavigation(url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrlNavigation(url);
            }

            private boolean handleUrlNavigation(String url) {
                String lower = url.toLowerCase();

                // 1. Torrents / Magnets
                if (lower.startsWith("magnet:") || lower.startsWith("intent:") || lower.endsWith(".torrent")) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "No torrent app installed", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }

                // 2. Block Ad URLs
                for (String ad : AD_DOMAINS) {
                    if (lower.contains(ad)) {
                        return true;
                    }
                }

                // 3. Allow only clean movie/stream domains
                if (lower.contains("yts.lu") || lower.contains("vidsrc") || 
                    lower.contains("tmdb.org") || lower.contains("2embed") || 
                    lower.contains("stream") || lower.contains("player") || lower.contains("video")) {
                    return false;
                }

                return true; // Block any unknown ad redirect
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                swipeRefreshLayout.setRefreshing(false);
                injectAdBlockCss(view);
            }
        });

        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Unable to download", Toast.LENGTH_SHORT).show();
            }
        });

        // Android Back Button
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        webView.loadUrl(TARGET_URL);
    }

    // Remove Ad overlays and hidden popups via CSS
    private void injectAdBlockCss(WebView view) {
        String css = "javascript:(function() {" +
                "var style = document.createElement('style');" +
                "style.innerHTML = 'iframe[src*=\"ad\"], .ad, .ads, .popup, [id*=\"banner\"], [class*=\"banner\"], [id*=\"sponsor\"], [class*=\"sponsor\"], div[style*=\"z-index: 9999\"] { display: none !important; opacity: 0 !important; pointer-events: none !important; }';" +
                "document.head.appendChild(style);" +
                "})()";
        view.loadUrl(css);
    }
}
