package com.custom.android.fitbitlogintest;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Hafiz Waleed Hussain on 12/6/2014.
 */
class EasyWebViewClient extends WebViewClient {

    /** _EasySocialAuthActivity is a Reference of an {@link EasySocialAuthActivity} */
    private EasySocialAuthActivity _EasySocialAuthActivity;
    /** Dialog is shown when we are in between the procedure of getting code. */
    private ProgressDialog _Dialog;

    /**
     * One argument constructor
     * @param easySocialAuthActivity is a reference of a EasySocialAuthActivity.
     */
    public EasyWebViewClient(EasySocialAuthActivity easySocialAuthActivity) {
        this._EasySocialAuthActivity = easySocialAuthActivity;
        _Dialog = new ProgressDialog(_EasySocialAuthActivity);
        _Dialog.setCancelable(false);
        _Dialog.setCanceledOnTouchOutside(false);

    }

    public ProgressDialog get_Dialog() {
        return _Dialog;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

        Uri uri = request.getUrl();
        String host = uri.getHost();

        Uri callbackUri = Uri.parse(_EasySocialAuthActivity.getRedirectUrl());
        String callbackHost = callbackUri.getHost();

        if(host.equals(callbackHost)){
            _EasySocialAuthActivity.onComplete(request.getUrl().toString());
            //String code = uri.getQueryParameter("code");
            //getAccessToken.execute(_EasySocialAuthActivity.getAccessToken()+code);

            //GetAccessToken getAccessToken = new GetAccessToken(_EasySocialAuthActivity);
            //getAccessToken.execute(url); // for implicit grant flow
        }

        return super.shouldOverrideUrlLoading(view, request);
    }

    @SuppressWarnings("depreciation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {

        Uri uri = Uri.parse(url);
        String host = uri.getHost();

        Uri callbackUri = Uri.parse(_EasySocialAuthActivity.getRedirectUrl());
        String callbackHost = callbackUri.getHost();

        if(host.equals(callbackHost)){
            _EasySocialAuthActivity.onComplete(url);
            //String code = uri.getQueryParameter("code");
            //getAccessToken.execute(_EasySocialAuthActivity.getAccessToken()+code);

            //GetAccessToken getAccessToken = new GetAccessToken(_EasySocialAuthActivity);
            //getAccessToken.execute(url); // for implicit grant flow
        }
        return super.shouldOverrideUrlLoading(view, url);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        if(_Dialog !=null) {
            _Dialog.show();
        }
        view.setVisibility(View.GONE);

    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if( _Dialog != null && _Dialog.isShowing())
            _Dialog.dismiss();
        view.setVisibility(View.VISIBLE);
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
        if( _Dialog != null && _Dialog.isShowing())
            _Dialog.dismiss();

        Intent data = new Intent();
        data.putExtra(_EasySocialAuthActivity.ERROR_CODE,errorCode);

        _EasySocialAuthActivity.setResult(Activity.RESULT_CANCELED, data);
        _EasySocialAuthActivity.finish();
    }
}
