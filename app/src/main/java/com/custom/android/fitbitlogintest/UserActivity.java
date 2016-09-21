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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class UserActivity extends AppCompatActivity {

    ListView console;
    ArrayAdapter<String> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // initialize listview
        console = (ListView)findViewById(R.id.resultWindow);

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
        // check if user input for days is valid
        EditText daysInput = (EditText)findViewById(R.id.daysInput);
        if(daysInput.getText().toString().equals("") || Integer.valueOf(daysInput.getText().toString())<1){
            // show warning toast to input valid number
            Context context = getApplicationContext();
            CharSequence text = "Please enter a day value greater than 0";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            return;
        }

        // get calendar object of today - inputed number of days
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -Integer.valueOf(daysInput.getText().toString()) + 1);
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String date = df.format(c.getTime());

        ListView console = (ListView)findViewById(R.id.resultWindow);
        String url = "https://api.fitbit.com/1/user/-/activities/steps/date/" + date + "/today.json";
        String jsonString = FitbitApi.getData(url, getAccess());
        JSONObject stepsObj = FitbitApi.convertStringToJson(jsonString);
        int objLen;

        try {
            JSONArray stepsArray = stepsObj.getJSONArray("activities-steps");
            objLen = stepsArray.length();
            List<String> consoleList = new ArrayList<>();
            for (int i=objLen-1; i>=0; i--){
                stepsObj = stepsArray.getJSONObject(i);
                consoleList.add("Date: " + stepsObj.getString("dateTime") + "\tSteps: " + stepsObj.getString("value"));
            }
            // initialize adapter and set to listview
            listAdapter = new ArrayAdapter<>(
                    this,
                    R.layout.list_item,
                    R.id.list_item_view,
                    consoleList
            );
            console.setAdapter(listAdapter);
        }
        catch (JSONException e){
            Log.e("ERROR", e.getMessage(), e);
        }
    }

    public void getTodaySummary(View v){
        try {

            // connect and get data
            String jsonString = FitbitApi.getData("https://api.fitbit.com/1/user/-/activities/date/today.json", getAccess());

            // store obtained data in json object and convert to string array
            JSONObject stepsObj = FitbitApi.convertStringToJson(jsonString).getJSONObject("summary");
            List<String> consoleList = new ArrayList<>();
            consoleList.add("Activity Calories: " + stepsObj.getString("activityCalories"));
            consoleList.add("Calories BMR: " + stepsObj.getString("caloriesBMR"));
            consoleList.add("Calories Out: " + stepsObj.getString("caloriesOut"));
            consoleList.add("Fairly Active Minutes: " + stepsObj.getString("fairlyActiveMinutes"));
            consoleList.add("Lightly Active Minutes: " + stepsObj.getString("lightlyActiveMinutes"));
            consoleList.add("Marginal Calories: " + stepsObj.getString("marginalCalories"));
            consoleList.add("Sedentary Minutes: " + stepsObj.getString("sedentaryMinutes"));
            consoleList.add("Steps: " + stepsObj.getString("steps"));
            consoleList.add( "Very Active Minutes: " + stepsObj.getString("veryActiveMinutes"));

            // initialize adapter and set to listview
            listAdapter = new ArrayAdapter<>(
                    this,
                    R.layout.list_item,
                    R.id.list_item_view,
                    consoleList
            );
            console.setAdapter(listAdapter);
        }
        catch (JSONException e){
            Log.e("ERROR", e.getMessage(), e);
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
            return Encryptor.decrypt((new Timestamp(getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).firstInstallTime)).toString(),
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("AUTH_TOKEN", "NULL"));
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
