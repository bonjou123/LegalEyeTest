package com.gmac.juvenal.legaleye;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Created by Michael Brooks on 4/20/2016.
 */
public class LocationManager implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Location mLastLocation;
    private Context mContext;
    private GoogleApiClient mGoogleApiClient;
    private String stringLong;
    private String stringLat;

    public LocationManager(Context context) {
        mContext = context;
    }

    public String getStringLong() {
        return stringLong;
    }

    public void setStringLong(String stringLong) {
        this.stringLong = stringLong;
    }

    public String getStringLat() {
        return stringLat;
    }

    public void setStringLat(String stringLat) {
        this.stringLat = stringLat;
    }

    @Override
    public void onConnected(Bundle bundle) {

        try {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        } catch (SecurityException sE) {
            sE.printStackTrace();
        }

        if (mLastLocation != null) {
            setStringLat(String.valueOf(mLastLocation.getLatitude()));
            setStringLong(String.valueOf(mLastLocation.getLongitude()));
        } else {
            setStringLat("28.5221690");
            setStringLong("-81.4641160");
        }
    }

    public void buildGoogleApiConnection () {
        // checking if play services are installed should be done here
        // and building google api client should be done if true
        // original method to check is depreicated: refer to: http://stackoverflow.com/questions/31016722/googleplayservicesutil-vs-googleapiavailability
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }


}
