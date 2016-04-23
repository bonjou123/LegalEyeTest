package com.gmac.juvenal.legaleye;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class VideoUpload extends AppCompatActivity {

    private String myFileName;
    private String responseString = "";
    private String apiKey;
    private String sessionID;

    /**************** ADDED *****************************/
    private ProgressBar progressBar;
    private TextView txtPercentage;
    private VideoView vidView;
    /*************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_upload);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            myFileName = extras.getString("fileName");
        }
        apiKey = UploadData.getInstance().getApiKey();
        sessionID = UploadData.getInstance().getSession();
        new StartVideoUpload().execute();

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        /********* ADDED *********************************************************/
        progressBar = (ProgressBar)findViewById(R.id.progressBarTry);
        txtPercentage = (TextView) findViewById(R.id.txtPercentage);
        vidView = (VideoView) findViewById(R.id.videoView);

        String myVideoPath = Environment.getExternalStorageDirectory() + "/storage/emulated/0/QuickVid/" + myFileName;


//            /storage/emulated/0/QuickVid/QuickVid(151).mp4

        vidView.setVideoPath(myVideoPath);
        vidView.start();
        /***********************************************************************/

    }

    public class StartVideoUpload extends AsyncTask<Void, Integer, String> {

        @Override
        protected void onPreExecute() {

            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            txtPercentage.setText(String.valueOf(values[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {

            URL url;
            HttpURLConnection conn = null;
            DataOutputStream dos;
            DataInputStream inStream;
            String lineEnd = "\r\n";
            String twoHyphens = "--";
            String boundary = "*****";
            String myVideoPath = Environment.getExternalStorageDirectory() + "/QuickVid/" + myFileName;
            int bytesRead, bytesAvailable, bufferSize;
            byte[] buffer;
            int maxBufferSize = 1 * 1024 * 1024;
            String urlString = "http://leagleeye.azurewebsites.net/api/" + apiKey + "/events/" +
                    sessionID + "/attachments";

            try {
                FileInputStream fileInputStream = new FileInputStream(new File(myVideoPath));
                url = new URL(urlString);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + myVideoPath + "\"" + lineEnd);
                dos.writeBytes(lineEnd);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];
                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                /**************** ADDED **********************************************************/
                try {
                    for(int i = 0; i <= 90; i++) {
                    publishProgress(i);
                    Thread.sleep(100);
                    }
                }catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /**************** ADDED **********************************************************/

                while (bytesRead > 0) {
                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                }

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
                Log.e("Debug", "File is written");
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {
                Log.e("Debug", "error: " + ex.getMessage(), ex);
            } catch (IOException ioe) {
                Log.e("Debug", "error: " + ioe.getMessage(), ioe);
            }

            try {
                inStream = new DataInputStream(conn.getInputStream());
                while ((responseString = inStream.readLine()) != null) {
                    Log.e("Debug", "Server Response " + responseString);
                }

            } catch (IOException ioe) {
                Log.e("Debug", "error: " + ioe.getMessage(), ioe);
            }

            /**************** ADDED **********************************************************/
            for(int i = 91; i <= 100; i++) {

                publishProgress(i);

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                e.printStackTrace();
                }
            }
            /**************** ADDED **********************************************************/

            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);

            call();
        }
    }

    /**************** ADDED *****************************************************************************/
    private void call() {

        String phoneNumber = UploadData.getInstance().getDialNumber();
        Intent in = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+phoneNumber));
        try{
            startActivity(in);
        }
        catch (android.content.ActivityNotFoundException ex){
            Toast.makeText(getApplicationContext(), "not found", Toast.LENGTH_SHORT).show();
        }
        this.finishAffinity();
    }
    /********************************************************************************************************/
}
