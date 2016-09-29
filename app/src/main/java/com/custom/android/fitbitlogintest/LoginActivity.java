package com.custom.android.fitbitlogintest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.sql.Timestamp;

public class LoginActivity extends AppCompatActivity {

    private int REQUEST_CODE= 0x1;

    // parameters (change if necessary)
    private final String authorizeUrl = "https://www.fitbit.com/oauth2/authorize";
    private final String responseType = "token"; // 'code' = authorization code grant flow | 'token' = implicit grant flow (currently only supports implicit)
    private final String redirectUri = "https://github.com/ArronVan";
    private final String expiresIn = "86400"; // 86400-1day | 604800-1week | 2592000-30days | 31536000-1yr

    // prompt options:
    // none - default behavior
    // consent - always require consent from user, with default login behavior
    // login - always require login from user, with default consent behavior
    // login consent - always require consent and login from user
    private final String prompt = "login";

    // add from list of these permissions: activity, heartrate, location, nutrition, profile, settings, sleep, social, weight
    private final String scopes =
            "activity%20" +
                    "heartrate%20" +
                    "location%20" +
                    "nutrition%20" +
                    "profile%20" +
                    "settings%20" +
                    "sleep%20" +
                    "social%20" +
                    "weight";

    private String loginUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginUrl = authorizeUrl + "?" +
                "client_id=" + getResources().getString(R.string.client_id) + "&" +
                "response_type=" + responseType + "&" +
                "scope=" + scopes + "&" +
                "redirect_uri=" + redirectUri + "&" +
                "expires_in=" + expiresIn + "&" +
                "prompt=" + prompt;

        if (getAccess().equals("NULL")){
            // no account stored or app has previously stored access key
            removeAccess();
        }
        else if (FitbitApi.getData("https://api.fitbit.com/1/user/-/profile.json", getAccess()).equals("java.io.IOException")){
            // login failed
            removeAccess();
        }
        else{
            // logged in successfully
            // delete this activity and start user screen activity
            Intent intent = new Intent(this, UserActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }

    }

    // methods to log in and get access token
    public void onStartAuth(View v){

        // create intent to store obtained access token
        Intent intent = new Intent(this, EasySocialAuthActivity.class);

        // put url in intent
        intent.putExtra(EasySocialAuthActivity.URL, loginUrl);
        intent.putExtra(EasySocialAuthActivity.REDIRECT_URL, redirectUri);

        String accessTokenUrl = "";
        intent.putExtra(EasySocialAuthActivity.ACCESS_TOKEN, accessTokenUrl);
        try {
            startActivityForResult(intent, REQUEST_CODE);
        }
        catch (Exception e){
            String exception = e.toString();
            Log.e("ERROR", e.getMessage(), e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(requestCode == REQUEST_CODE){
                String accessToken = data.getStringExtra("data");
                accessToken = EasySocialAuthActivity.getAccessTokenFromUrl(accessToken);
                setAccess(accessToken);
                Intent intent = new Intent(this, UserActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
            }
        }else if(resultCode == RESULT_CANCELED){
            if(requestCode == REQUEST_CODE) {
                Toast.makeText(this, data.getIntExtra(EasySocialAuthActivity.ERROR_CODE, 0) + "", Toast.LENGTH_LONG).show();
                //These error codes are present in WebViewClient.
                //http://developer.android.com/reference/android/webkit/WebViewClient.html
            }
        }
    }

    // get and set methods to use access token in preferences
    private String getAccess(){
        try{
            /*
            if (encrypted.equals("NULL")){
                return "NULL";
            }
            Timestamp d = new Timestamp(getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).firstInstallTime);
            String encrypted = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("AUTH_TOKEN", "NULL");
            String decrypted = Encryptor.decrypt(d.toString(), encrypted);
            return decrypted;
            */
            if (PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("AUTH_TOKEN", "NULL").equals("NULL")){
                return "NULL";
            }
            String dec = Encryptor.decrypt((new Timestamp(getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).firstInstallTime)).toString(),
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("AUTH_TOKEN", "NULL"));
            if (dec == null){
                throw new PackageManager.NameNotFoundException();
            }
            return dec;
        }
        catch (PackageManager.NameNotFoundException e){
            Log.e("ERROR", e.getMessage(), e);
            return "NULL";
        }
    }
    private void setAccess(String token){
        try{
            // use first install time as key
            /*
            Timestamp d = new Timestamp(getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).firstInstallTime);
            String encrypted = Encryptor.encrypt(d.toString(), token);
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("AUTH_TOKEN", encrypted).apply();
            */
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("AUTH_TOKEN",
                    Encryptor.encrypt((new Timestamp(getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).firstInstallTime)).toString(), token)).apply();
        }
        catch (PackageManager.NameNotFoundException e){
            Log.e("ERROR", e.getMessage(), e);
        }
    }
    private void removeAccess(){
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().remove("AUTH_TOKEN").commit();
    }

}
