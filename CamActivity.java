package com.gmac.juvenal.legaleye;

import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;
import java.io.File;
import java.io.IOException;

public class CamActivity extends AppCompatActivity implements SurfaceHolder.Callback,
        MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

    /**Global variable declarations. **/

    private Button btnStop = null;
    private Button btnPause = null;
    private Button btnFlash = null;
    private VideoView vvCam = null;
    private SurfaceHolder holder = null;
    private Camera camera = null;
    private MediaRecorder recorder = null;
    private String fileName = null;
    private int videoNumber;
    private CamcorderProfile profile;
    private File dirQuickVid;
    private File outFile;
    private String TAG = "VideoRecorder";
    private int orientation;
    private boolean pauseClicked;
    private boolean stopClicked;
    private Camera.Parameters params;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        btnStop = (Button) findViewById(R.id.btnSTOP);
        btnPause = (Button) findViewById(R.id.btnPause);
        btnPause.setBackgroundResource(R.drawable.pause_60x60);
        vvCam = (VideoView) findViewById(R.id.vv1);
        videoNumber = 1;

        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pauseClicked) {
                    v.setBackgroundResource(R.drawable.pause_60x60);
                } else {
                    v.setBackgroundResource(R.drawable.play_60x60);
                }
                pauseClicked = !pauseClicked;
            }
        });

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    /**This method prepares the MediaRecroder for use. **/
    private void createRecorder() {

        Log.v(TAG, "In method createRecorder");
        if(recorder != null) return;

        /**Creates a new directory in the user's external storage. **/
        dirQuickVid = new File(Environment.getExternalStorageDirectory()
                + "/QuickVid/");

        dirQuickVid.mkdirs();

        if (!dirQuickVid.exists()) {
            if (!dirQuickVid.mkdirs()) {
                Log.e(TAG, "Problem creating directory");
                finish();
            }
        }

        /**Loops through and chooses a filename not in use. **/
        do
        {
            fileName = "QuickVid" + "(" + videoNumber + ")" + ".mp4";

            outFile = new File(dirQuickVid.toString(),fileName);

            videoNumber++;



        }while(outFile.exists());


        /**This gives all the settings for the MediaRecorder. **/
        try
        {
            camera.unlock();

            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);

            recorder = new MediaRecorder();
            recorder.setCamera(camera);
            recorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
            recorder.setVideoFrameRate(40);/**Frame Rate is 40. **/
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.MPEG_4_SP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setMaxDuration(300000); /** Max recording time is 5 minutes. **/
            recorder.setPreviewDisplay(holder.getSurface());
            recorder.setOutputFile(outFile.getAbsolutePath());/**Sets what file to record to. **/

            /** Sets recorder orientation if screen is in portrait. **/
            if (orientation == Configuration.ORIENTATION_PORTRAIT ) {
                recorder.setOrientationHint(90);
            }

            recorder.prepare();
            Log.v("@string/TAG", "Recorder prepared");
            startRecord();
        }catch (Exception e)
        {
            Log.v(TAG, "MediaRecorder failed to start");
            e.printStackTrace();
        }

    }

    /**Button that stops the recording then plays it back. **/
    public void btnStopClick(View view) {

        Log.v(TAG, "In btnStopClick");
        stopRecord();

        stopClicked = true;
        btnStop.setEnabled(false);
        btnStop.setVisibility(View.INVISIBLE);
        btnStop.setVisibility(View.INVISIBLE);

    }

    /**Method that stops the recording. **/
    private void stopRecord()
    {
        if (recorder != null)
        {
            recorder.setOnInfoListener(null);
            recorder.setOnErrorListener(null);
            try {
                recorder.stop();
            }catch (IllegalStateException ise)
            {
                Log.e(TAG, "ERROR - Got an Illegal State Exception, stopping the video.");
                ise.printStackTrace();
            }
            releaseRecorder();
            releaseCamera();

        }

        String myFileName = fileName;
        //finish();
        Intent myIntent = new Intent(CamActivity.this, VideoUpload.class);
        myIntent.putExtra("fileName", myFileName);
        CamActivity.this.startActivity(myIntent);

    }
    /**Method  that auto starts the recording. **/
    public void startRecord() {
        recorder.setOnInfoListener(this);
        recorder.setOnErrorListener(this);
        recorder.start();
        btnStop.setEnabled(true);


    }

    /** sets the camera output to the SurfaceHolder. also sets display orientation correctly **/
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        Log.v(TAG, "In Surface Created");
        try {
            camera.setPreviewDisplay(holder);


        } catch (IOException e) {
            Log.v(TAG, "ERROR - Could not start preview");
            e.printStackTrace();
        }

        if (orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            camera.setDisplayOrientation(90);
        }
        createRecorder();
    }


    /**Allows the user to zoom in and out during record. **/
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {

    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {

    }

    @Override
    protected void onPause()
    {
        super.onPause();

        /**Stops recording and stops the app, if the user leaves the app. if the user clicked the
         * rotated button the app won't stop and will restart. **/

        if(!stopClicked){
            stopRecord();
        }

    }

    /**Set ups buttons and gets the current orientation. **/
    @Override
    protected void onResume()
    {

        Log.v(TAG, "In OnResume");
        super.onResume();
        //rotated = false;
        stopClicked = false;

        orientation = getResources().getConfiguration().orientation;
        btnStop.setEnabled(false);

        /** App exits if  an error occurs **/
        if (!buildRecorder()) {
            exitApp();
        }


    }
    /** Method that starts to build the first parts of the app, camera surface holder. **/
    private boolean buildRecorder() {
        try {
            camera = Camera.open();
            params = camera.getParameters();
            camera.lock();


            holder = vvCam.getHolder();
            holder.addCallback(this);

            holder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        } catch (RuntimeException re) {
            Log.v(TAG, "ERROR - Could not initialize camera");
            re.printStackTrace();
            return false;
        }

        return true;
    }

    /** Releases the camera object **/
    private void releaseCamera() {
        if(camera != null){
            try{
                camera.reconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.release();
            camera = null;
        }

    }
    /** Releases the MediaRecorder object. **/
    private void releaseRecorder() {
        if (recorder != null)
        {
            recorder.release();
            recorder = null;
        }
    }

    public void exitApp()
    {

        finish();
        System.exit(1);
    }


}
