package com.gmac.juvenal.legaleye;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity { //implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String fileName = "UserData";
    private String filePath = "/data/data/com.gmac.juvenal.legaleye/files/UserData";
    private String[] states;
    private Spinner spinner;
    private SharedPreferences preferences;
    private EditText editEmailText;
    private EditText editPhoneText;

    // These are variables to hold user data. email, phone and state will be populated from either reading a file or
    // getting user input depending on whether the user has opened the app previously. stringLattitude and stringLogitude

    private String stringLatitude;
    private String stringLongitude;
    private String email;
    private String phone;
    private String state;
    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private final String SEGMENT = "1";
    private static final String TAG = "MainActivity";
    private String apiKey;
    private String id;
    private String dialNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (checkAndRequestPermissions()) {

            continueFlow();
        }


    }

    public void saveButtonClick(View v) {

        editEmailText = (EditText) findViewById(R.id.editText_email);
        editPhoneText = (EditText) findViewById(R.id.editText_phone);

        email = editEmailText.getText().toString();
        phone = editPhoneText.getText().toString();
        state = spinner.getSelectedItem().toString();

        if (!isValidEmail(email)) {
            editEmailText.setError("Invalid Email");
            return;
        }

        if (!isValidMobile((phone))) {
            editPhoneText.setError("Invalid Phone Number");
            return;
        }

        if (spinner.getSelectedItemPosition() == 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "Enter your state", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        FileOutputStream outputStream = null;
        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(email.getBytes());
            outputStream.write('\n');
            outputStream.write(phone.getBytes());
            outputStream.write('\n');
            outputStream.write(state.getBytes());
            outputStream.write('\n');
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        initUserVarsFromFile();

        new JSONTask().execute(Config.SERVER_URL);

//        Intent myIntent = new Intent(MainActivity.this, CamActivity.class);
//        MainActivity.this.startActivity(myIntent);
    }

    // validating email id
    private boolean isValidEmail(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isValidMobile(String phone) {
        return android.util.Patterns.PHONE.matcher(phone).matches();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void initUserVarsFromFile() {
        String line = "";
        try {
            FileInputStream fStream = new FileInputStream(("/data/data/com.gmac.juvenal.legaleye/files/UserData"));
            DataInputStream in = new DataInputStream(fStream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            if ((line = br.readLine()) != null) {
                email = line;
            }
            if ((line = br.readLine()) != null) {
                phone = line;
            }
            if ((line = br.readLine()) != null) {
                state = line;
            }

        } catch (IOException iOE) {
            Log.e("LegaalEye", "exception:" + iOE.getMessage());
        }

    }

    public void continueFlow() {

        LocationManager locationManager = new LocationManager(this);
        locationManager.buildGoogleApiConnection();
        File file = new File(filePath);

        if ((locationManager.getStringLat() != null && !locationManager.getStringLat().equals(""))
                || (locationManager.getStringLong() != null && locationManager.getStringLong().equals(""))) {
            stringLatitude = locationManager.getStringLat();
            stringLongitude = locationManager.getStringLong();
        } else {
            stringLatitude = "28.5221690";
            stringLongitude = "-81.4641160";
        }

        if (file.exists()) {
            initUserVarsFromFile();

            new JSONTask().execute(Config.SERVER_URL);


        } else {
            setContentView(R.layout.activity_main);
            states = getResources().getStringArray(R.array.states_list);
            spinner = (Spinner) findViewById(R.id.state_spinner);

            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, states);
            dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(dataAdapter);

            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
        }
    }

    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                //connection.connect();

                JSONObject request = new JSONObject();
                try {
                    request.put("segment", SEGMENT);
                    request.put("emailAddress", email);
                    request.put("phoneNumber", phone);
                    request.put("latitude", stringLatitude);
                    request.put("longitude", stringLongitude);
                    request.put("state", state);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                String postParam2 = "request=";
                postParam2 += request.toString();

                connection.setDoInput(true);
                connection.setRequestMethod("POST");
                connection.setFixedLengthStreamingMode(postParam2.getBytes().length);
                connection.setRequestProperty("Content", "json\\application"); //-Type", "x-www-form-urlencoded");

                PrintWriter out = new PrintWriter(connection.getOutputStream());
                out.print(postParam2);
                out.close();

                int statusCode = connection.getResponseCode();
                String message = connection.getResponseMessage();

                if (statusCode != HttpURLConnection.HTTP_CREATED) {
                    return "could not find " + statusCode + "\n" + message + "\n" + postParam2;

                }

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                String finJson = buffer.toString();

                JSONObject responseObject = new JSONObject(finJson);
                id = responseObject.getJSONObject("data").getString("session"); //.getString("session");
                dialNum = responseObject.getJSONObject("data").getString("dial");
                apiKey = responseObject.getJSONObject("data").getString("apiKey");
                UploadData.getInstance().setApiKey(apiKey);
                UploadData.getInstance().setSession(id);
                UploadData.getInstance().setDialNumber(dialNum);


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect();

                try {
                    if (reader != null)
                        reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Intent myIntent = new Intent(MainActivity.this, CamActivity.class);
            MainActivity.this.startActivity(myIntent);
        }
    }

    private boolean checkAndRequestPermissions() {
        int permissionWriteStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int microphonePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (microphonePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with all required permissions
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.ACCESS_COARSE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for all permissions
                    if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "permissions granted");
                        // process the normal flow

                        continueFlow();
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                                || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO) || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                            showDialogOK("Camera, microphone, storage and Location Services Permission required for this app",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    System.exit(1);
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        }
                        //permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                        else {
                            Toast.makeText(this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                            System.exit(1);
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }
}
