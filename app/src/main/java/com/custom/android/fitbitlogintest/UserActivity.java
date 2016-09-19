package com.custom.android.fitbitlogintest;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class UserActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // get user info
        // get string as json format
        String jsonString = FitbitApi.getData("https://api.fitbit.com/1/user/-/profile.json", getAccess());
        // transform json string to object
        JSONObject userData = FitbitApi.convertStringToJson(jsonString);
        // post name to app textview
        TextView nameText = (TextView)findViewById(R.id.fullName);
        String accountName;
        try {
            accountName = "Logged In As: "+ userData.getJSONObject("user").getString("fullName");
            nameText.setText(accountName);
        }
        catch (JSONException e){
            Log.e("ERROR", e.getMessage(), e);
        }
        catch (NullPointerException e){
            Log.e("ERROR", e.getMessage(), e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.logout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                // create alert dialog to log out or not
                new AlertDialog.Builder(this)
                        .setMessage("Do you want to log out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                revoke();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        })
                        .show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void revoke(){
        // revoke access token and delete token on storage
        FitbitApi.revokeToken(getAccess(), getResources().getString(R.string.client_id), getResources().getString(R.string.client_secret));
        removeAccess();

        // show toast to say user has logged out
        Context context = getApplicationContext();
        CharSequence text = "You have successfully logged out";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        // delete this activity and return to login
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    public void getStepsTime(View v){
        TextView console = (TextView)findViewById(R.id.resultWindow);
        String jsonString = FitbitApi.getData("https://api.fitbit.com/1/user/-/activities/steps/date/today/7d.json", getAccess());
        JSONObject stepsObj = FitbitApi.convertStringToJson(jsonString);
        int objLen;

        try {
            JSONArray stepsArray = stepsObj.getJSONArray("activities-steps");
            objLen = stepsArray.length();
            String consoleText = "";
            for (int i=objLen-1; i>=0; i--){
                stepsObj = stepsArray.getJSONObject(i);
                consoleText += "Date: " + stepsObj.getString("dateTime") + "\tSteps: " + stepsObj.getString("value");
                if (i!=0){
                    consoleText += "\n";
                }
            }
            console.setText(consoleText);
        }
        catch (JSONException e){
            Log.e("ERROR", e.getMessage(), e);
        }
    }

    public void getTodaySummary(View v){
        TextView console = (TextView)findViewById(R.id.resultWindow);
        String jsonString = FitbitApi.getData("https://api.fitbit.com/1/user/-/activities/date/today.json", getAccess());
        try {
            JSONObject stepsObj = FitbitApi.convertStringToJson(jsonString).getJSONObject("summary");
            String consoleText =
                    "Activity Calories: " + stepsObj.getString("activityCalories") +
                    "\nCalories BMR: " + stepsObj.getString("caloriesBMR") +
                    "\nCalories Out: " + stepsObj.getString("caloriesOut") +
                    "\nFairly Active Minutes: " + stepsObj.getString("fairlyActiveMinutes") +
                    "\nLightly Active Minutes: " + stepsObj.getString("lightlyActiveMinutes") +
                    "\nMarginal Calories: " + stepsObj.getString("marginalCalories") +
                    "\nSedentary Minutes: " + stepsObj.getString("sedentaryMinutes") +
                    "\nSteps: " + stepsObj.getString("steps") +
                    "\nVery Active Minutes: " + stepsObj.getString("veryActiveMinutes");
            console.setText(consoleText);
        }
        catch (JSONException e){
            Log.e("ERROR", e.getMessage(), e);
        }
    }

    // get and set methods to use access token in preferences
    private String getAccess(){
        try{
            Date d = new Date(getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).firstInstallTime);
            String encrypted = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("AUTH_TOKEN", "NULL");
            if (encrypted.equals("NULL")){
                return "NULL";
            }
            String decrypted = Encryptor.decrypt(d.toString(), encrypted);
            return decrypted;
        }
        catch (PackageManager.NameNotFoundException e){
            Log.e("ERROR", e.getMessage(), e);
            return "NULL";
        }
    }
    private void setAccess(String token){
        try{
            // use first install time as key
            Date d = new Date(getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).firstInstallTime);
            String encrypted = Encryptor.encrypt(d.toString(), token);
            PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("AUTH_TOKEN", encrypted).apply();
        }
        catch (PackageManager.NameNotFoundException e){
            Log.e("ERROR", e.getMessage(), e);
        }
    }
    private void removeAccess(){
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().remove("AUTH_TOKEN").commit();
    }

}
