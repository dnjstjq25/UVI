package com.example.first;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;

    private TextView illumText;
    private TextView solarZenithText;
    private TextView gpsText;
    private TextView uviText;
    private Button startButton;

    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;

    // GPS Tracker class
    private Func_GPS gps;

    // Calculate Solar Zenith class
    private Func_SolarZenith funcSolarZenith;

    // Calculate UVI class
    private Func_UVI funcUVI;

    private int illumValue;
    private double latitude;
    private double longitude;
    private double solarZenith;
    private String uvi;

    //OkHttp 관련 선언
    OkHttpClient client = new OkHttpClient();
    Request request = new Request.Builder()
            .url("http://210.102.142.16/model.tflite")
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 조도
        illumText = (TextView) findViewById(R.id.illumText);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor == null) {
            Toast.makeText(this, "조도 센서를 찾을 수 없습니다!", Toast.LENGTH_SHORT).show();
        }

        // GPS 측정 및 태양천정각, UVI 계산
        gpsText = (TextView) findViewById(R.id.gpsText);
        solarZenithText = (TextView) findViewById(R.id.solarZenithText);
        uviText = (TextView) findViewById(R.id.uviText);
        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 권한 요청
                if (!isPermission) {
                    callPermission();
                    return;
                }

                gps = new Func_GPS(MainActivity.this);
                funcSolarZenith = new Func_SolarZenith();
                funcUVI = new Func_UVI();

                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {
                    // gps 출력
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    gpsText.setText("위도 : " + String.format("%.2f", latitude) + "°\n경도 : " + String.format("%.2f", longitude) + "°");

                    // 태양천정각 출력
                    solarZenith = funcSolarZenith.getSolarZenith(latitude);
                    solarZenithText.setText(solarZenith + "°");

                    // 태양천정각, 조도 출력 테스트용
//                    String[] illum = String.valueOf(illumText.getText()).split(" ");
//                    uviText.setText(Double.toString(solarZenith) + "\n" + Double.parseDouble(illum[0]));

                    // UVI 출력
//                    uvi = funcUVI.Output(MainActivity.this, (float)solarZenith, (float)illumValue);

                    // UVI 출력 테스트용
                    uvi = funcUVI.Test(MainActivity.this);

                    uviText.setText(uvi);

                    gps.stopUsingGPS();
                } else {
                    // GPS를 사용할 수 없을경우
                    gps.showSettingAlert();
                }
            }
        });
        callPermission();  // 권한 요청
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT) {
            illumValue = (int) sensorEvent.values[0];
            illumText.setText("" + illumValue + " lx");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    // 권한 요청
    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_ACCESS_FINE_LOCATION);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }

    //OkHttp 코드
    private class CallbackToDownloadFile implements Callback {

        MainActivity.CallbackToDownloadFile cbToDownloadFile = new MainActivity.CallbackToDownloadFile(
                "/storage/extSdCard/Android/data/cf.domone.android.example",
                "file_name_example"

        );


        private File directory;
        private File fileToBeDownloaded;

        public CallbackToDownloadFile(String directory, String fileName) {
            this.directory = new File(directory);
            this.fileToBeDownloaded = new File(this.directory.getAbsolutePath() + "/" + fileName);
        }

        @Override
        public void onFailure(Request request, IOException e) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(
                            MainActivity.this,
                            "파일을 다운로드할 수 없습니다. 인터넷 연결을 확인하세요.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }

        @Override
        public void onResponse(Response response) throws IOException {
            if (!this.directory.exists()) {
                this.directory.mkdirs();
            }

            if (this.fileToBeDownloaded.exists()) {
                this.fileToBeDownloaded.delete();
            }

            try {
                this.fileToBeDownloaded.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(
                                MainActivity.this,
                                "다운로드 파일을 생성할 수 없습니다.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });

                return;
            }

            InputStream is = response.body().byteStream();
            OutputStream os = new FileOutputStream(this.fileToBeDownloaded);

            final int BUFFER_SIZE = 2048;
            byte[] data = new byte[BUFFER_SIZE];

            int count;
            long total = 0;

            while ((count = is.read(data)) != -1) {
                total += count;
                os.write(data, 0, count);
            }

            os.flush();
            os.close();
            is.close();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(
                            MainActivity.this,
                            "다운로드가 완료되었습니다.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }
    }
}