package com.custom.android.fitbitlogintest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.net.URL;

/**
 * Created by Hafiz Waleed Hussain on 12/6/2014.
 * GetAccessToken is used to get the AccessToken from any social network. If any exception occur. The activity will return result
 * cancel and print the exception stack trace.
 */
public class GetAccessToken extends AsyncTask<String,Void,String> {

    /** _Callback is an interface. */
    private Callback _Callback;
    /** _Activity is a reference of Calling activity. */
    private Activity _Activity;
    /** _Dialog is a ProgressDialog which is used to shown in request processing */
    private ProgressDialog _Dialog;

    /**
     * One argument constructor.
     * @param activity Calling activity take as an argument.
     */
    public GetAccessToken(Activity activity) {
        try {
            this._Callback = (Callback) activity;
            _Activity = activity;
            _Dialog = new ProgressDialog(activity);
            _Dialog.setCancelable(false);
            _Dialog.setCanceledOnTouchOutside(false);
        }catch (ClassCastException e){
            throw new UnsupportedOperationException("Implement GetAccessToken.Callback interface.");
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        _Dialog.show();
    }

    @Override
    protected String doInBackground(String... params) {
        URL url = null;
        return params[0];
        /*
        try {
            url = new URL(params[0]);
            URLConnection urlConnection = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            return br.readLine();
        } catch (MalformedURLException e) {
            exceptionHandler(e);
        } catch (IOException e) {
            exceptionHandler(e);
        }
        return null;
        */
    }

    @Override
    protected void onPostExecute(String line) {
        super.onPostExecute(line);
        if(_Dialog != null && _Dialog.isShowing())
            _Dialog.dismiss();
        _Callback.onComplete(line);
    }

    /**
     *  This function is simply handle the exception if occur during Accessing Social Network
     *  AccessToken.
     * @param e
     */
    private void exceptionHandler(Exception e) {
        e.printStackTrace();
        if(_Dialog != null && _Dialog.isShowing())
            _Dialog.dismiss();
        _Activity.setResult(Activity.RESULT_CANCELED);
        _Activity.finish();
    }

    /**
     * Callback interface for GetAccessToken WebRequest.
     */
    public static interface Callback{
        void onComplete(String line);
    }
}
