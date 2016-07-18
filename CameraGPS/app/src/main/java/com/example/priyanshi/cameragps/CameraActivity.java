package com.example.priyanshi.cameragps;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraActivity extends Activity {
    private static final String TAG = "CameraActivity";
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    LookATActivity mLookAtListener = new LookATActivity(this);
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        mCamera = getCameraInstance();
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        if(null != mCamera) {
            mCameraPreview = new CameraPreview(this, mCamera);
            preview.addView(mCameraPreview);
        }
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        GPSLocation mlocListener = new GPSLocation(this);
        Location gpsLocation = mlocListener.getLocation(LocationManager.GPS_PROVIDER);
        if(null != gpsLocation){
            Double latitude = gpsLocation.getLatitude();
            Double longitude = gpsLocation.getLongitude();
            Toast.makeText(this, "GPS Location: "+ latitude + " " + longitude, Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "GPS DISABLED", Toast.LENGTH_SHORT).show();
        }
        Location nwLocation = mlocListener.getLocation(LocationManager.NETWORK_PROVIDER);
        if(null != nwLocation){
            Double latitude = nwLocation.getLatitude();
            Double longitude = nwLocation.getLongitude();
            Toast.makeText(this, "NW Location: "+ latitude + " " + longitude, Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "NW DISABLED", Toast.LENGTH_SHORT).show();
        }
        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });
    }


    /**
     * Helper method to access the camera returns null if it cannot get the
     * camera or does not exist
     *
     * @return
     */
    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            Log.i(TAG, " cannot get camera or does not exist");
        }
        return camera;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = getOutputMediaFile();
            if (pictureFile == null) {
                Log.i("PictureFile", "Picture File is null");
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                        .format(new Date());
                File myFile = new File("/sdcard/"+timeStamp+".txt");
                myFile.createNewFile();
                FileOutputStream fOut = new FileOutputStream(myFile);
                OutputStreamWriter myOutWriter =
                        new OutputStreamWriter(fOut);
                String outText = "For image: "+ pictureFile.toString()+ " \n\n " + LookATActivity.getLookAtText() + " \n\n " + GPSLocation.getUtmText();
                myOutWriter.append(outText);
                myOutWriter.close();
                fOut.close();
                Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(Uri.fromFile(pictureFile));
                sendBroadcast(mediaScanIntent);
                camera.startPreview();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "NewCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp+".jpg");

        return mediaFile;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener( mLookAtListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(mLookAtListener, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*if(null != mCamera){
            mCamera.release();
            mCamera = null;
        }*/
        mSensorManager.unregisterListener(mLookAtListener);
    }
}
