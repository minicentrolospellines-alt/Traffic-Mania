package cl.negociospyme.trafficmania;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.webkit.WebViewAssetLoader;
import androidx.webkit.WebViewClientCompat;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.ump.*;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;

public class MainActivity extends ComponentActivity {
    private WebView web;
    private LinearLayout root;
    private AdView banner;
    private InterstitialAd interstitial;
    private RewardedAd rewarded;
    private ConsentInformation consent;
    private boolean adsInitialized, adShowing, destroyed, pageReady;
    private long interstitialLoaded, rewardedLoaded, lastFullScreen;
    private static final String ORIGIN = "https://appassets.androidplatform.net";

    @SuppressLint("SetJavaScriptEnabled")
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(16,39,53));
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
            v.setPadding(bars.left,bars.top,bars.right,bars.bottom); return insets;
        });
        web = new WebView(this); web.setBackgroundColor(Color.rgb(16,39,53));
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true); settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false); settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setMediaPlaybackRequiresUserGesture(true);
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        web.setWebViewClient(new WebViewClientCompat() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                Uri uri=request.getUrl();
                if ("https".equals(uri.getScheme()) && "appassets.androidplatform.net".equals(uri.getHost())) {
                    WebResourceResponse result=loader.shouldInterceptRequest(uri);
                    if(result!=null) return result;
                }
                return new WebResourceResponse("text/plain","UTF-8",403,"Blocked",java.util.Collections.emptyMap(),new ByteArrayInputStream(new byte[0]));
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) { return true; }
            @Override public void onPageFinished(WebView view, String url) { pageReady=true; }
        });
        web.addJavascriptInterface(new Bridge(), "Android");
        root.addView(web,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        getOnBackPressedDispatcher().addCallback(this,new OnBackPressedCallback(true){
            @Override public void handleOnBackPressed(){ if(pageReady)js("window.onNativeBack&&window.onNativeBack()");else finish(); }
        });
        web.loadUrl(ORIGIN+"/assets/game/index.html");
        requestConsent();
    }
    private void requestConsent() {
        consent=UserMessagingPlatform.getConsentInformation(this);
        ConsentRequestParameters parameters=new ConsentRequestParameters.Builder().build();
        consent.requestConsentInfoUpdate(this,parameters,()->{
            UserMessagingPlatform.loadAndShowConsentFormIfRequired(this,error->initializeAds());
            initializeAds();
        },error->initializeAds());
        initializeAds();
    }
    private void initializeAds(){
        if(destroyed||adsInitialized||!consent.canRequestAds())return;
        adsInitialized=true;
        new Thread(()->MobileAds.initialize(this,status->runOnUiThread(()->{
            if(destroyed)return;loadBanner();loadInterstitial();loadRewarded();
        }))).start();
    }
    private void loadBanner(){
        if(destroyed||!consent.canRequestAds())return;
        banner=new AdView(this);
        banner.setAdUnitId(BuildConfig.BANNER_ID);
        int width=(int)(getResources().getDisplayMetrics().widthPixels/getResources().getDisplayMetrics().density);
        banner.setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this,width));
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.gravity=Gravity.CENTER_HORIZONTAL;
        // Separate native row: never overlaps game buttons or the board.
        root.addView(banner,p);banner.setVisibility(View.GONE);
        banner.setAdListener(new AdListener(){
            @Override public void onAdLoaded(){if(!destroyed)banner.setVisibility(View.VISIBLE);}
            @Override public void onAdFailedToLoad(@NonNull LoadAdError error){if(!destroyed)banner.setVisibility(View.GONE);}
        });
        banner.loadAd(new AdRequest.Builder().build());
    }
    private void loadInterstitial(){
        if(destroyed||!adsInitialized||!consent.canRequestAds())return;
        InterstitialAd.load(this,BuildConfig.INTERSTITIAL_ID,new AdRequest.Builder().build(),new InterstitialAdLoadCallback(){
            @Override public void onAdLoaded(@NonNull InterstitialAd ad){if(!destroyed){interstitial=ad;interstitialLoaded=System.currentTimeMillis();}}
            @Override public void onAdFailedToLoad(@NonNull LoadAdError error){interstitial=null;}
        });
    }
    private void loadRewarded(){
        if(destroyed||!adsInitialized||!consent.canRequestAds())return;
        RewardedAd.load(this,BuildConfig.REWARDED_ID,new AdRequest.Builder().build(),new RewardedAdLoadCallback(){
            @Override public void onAdLoaded(@NonNull RewardedAd ad){if(!destroyed){rewarded=ad;rewardedLoaded=System.currentTimeMillis();}}
            @Override public void onAdFailedToLoad(@NonNull LoadAdError error){rewarded=null;}
        });
    }
    private void js(String code){if(!destroyed&&web!=null)web.evaluateJavascript(code,null);}
    private void rewardResult(String token,boolean earned){js("window.onNativeReward&&window.onNativeReward("+JSONObject.quote(token)+","+earned+")");}
    public class Bridge {
        @JavascriptInterface public void exit(){runOnUiThread(()->finish());}
        @JavascriptInterface public void vibrate(){runOnUiThread(()->{Vibrator v=(Vibrator)getSystemService(VIBRATOR_SERVICE);if(v!=null&&v.hasVibrator()){if(android.os.Build.VERSION.SDK_INT>=26)v.vibrate(VibrationEffect.createOneShot(25,VibrationEffect.DEFAULT_AMPLITUDE));else v.vibrate(25);}});}
        @JavascriptInterface public void reward(String token){runOnUiThread(()->{
            if(token==null||token.length()>100)return;
            if(adShowing||rewarded==null||!consent.canRequestAds()||System.currentTimeMillis()-rewardedLoaded>3500000){rewardResult(token,false);loadRewarded();return;}
            final boolean[] earned={false}; RewardedAd ad=rewarded;rewarded=null;adShowing=true;
            ad.setFullScreenContentCallback(new FullScreenContentCallback(){
                private void finishReward(){adShowing=false;lastFullScreen=System.currentTimeMillis();rewardResult(token,earned[0]);loadRewarded();}
                @Override public void onAdDismissedFullScreenContent(){finishReward();}
                @Override public void onAdFailedToShowFullScreenContent(@NonNull AdError e){finishReward();}
            });
            ad.show(MainActivity.this,rewardItem->earned[0]=true);
        });}
        @JavascriptInterface public void interstitial(){runOnUiThread(()->{
            long now=System.currentTimeMillis();
            if(adShowing||interstitial==null||!consent.canRequestAds()||now-interstitialLoaded>3500000||now-lastFullScreen<90000){js("window.onNativeInterstitialDone&&window.onNativeInterstitialDone()");if(interstitial==null||now-interstitialLoaded>3500000)loadInterstitial();return;}
            InterstitialAd ad=interstitial;interstitial=null;adShowing=true;
            ad.setFullScreenContentCallback(new FullScreenContentCallback(){
                private void finishAd(){adShowing=false;lastFullScreen=System.currentTimeMillis();js("window.onNativeInterstitialDone&&window.onNativeInterstitialDone()");loadInterstitial();}
                @Override public void onAdDismissedFullScreenContent(){finishAd();}
                @Override public void onAdFailedToShowFullScreenContent(@NonNull AdError e){finishAd();}
            });
            ad.show(MainActivity.this);
        });}
        @JavascriptInterface public void privacy(){runOnUiThread(()->{
            if(consent.getPrivacyOptionsRequirementStatus()==ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED){
                UserMessagingPlatform.showPrivacyOptionsForm(MainActivity.this,error->{
                    if(!consent.canRequestAds()){interstitial=null;rewarded=null;if(banner!=null){root.removeView(banner);banner.destroy();banner=null;}}
                    else if(!adsInitialized)initializeAds();
                    else {if(banner!=null){root.removeView(banner);banner.destroy();banner=null;}loadBanner();loadInterstitial();loadRewarded();}
                });
            }else Toast.makeText(MainActivity.this,"No hay un formulario de opciones disponible en este momento.",Toast.LENGTH_LONG).show();
        });}
    }
    @Override protected void onPause(){js("window.onNativePause&&window.onNativePause()");if(web!=null)web.onPause();if(banner!=null)banner.pause();super.onPause();}
    @Override protected void onResume(){super.onResume();if(web!=null){web.onResume();js("window.onNativeResume&&window.onNativeResume()");}if(banner!=null)banner.resume();}
    @Override protected void onDestroy(){destroyed=true;if(banner!=null)banner.destroy();if(web!=null){web.removeJavascriptInterface("Android");root.removeView(web);web.destroy();}super.onDestroy();}
}
