package com.gmac.juvenal.legaleye;

/**
 * Created by Michael Brooks on 3/23/2016.
 */
public class UploadData {
    private String apiKey;
    private String session;
    private String dialNumber;


    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getDialNumber() {
        return dialNumber;
    }

    public void setDialNumber(String dialNumber) {
        this.dialNumber = dialNumber;
    }

   private static UploadData ourInstance = new UploadData();

    public static UploadData getInstance() {
        return ourInstance;
    }

    private UploadData() {
    }
}
