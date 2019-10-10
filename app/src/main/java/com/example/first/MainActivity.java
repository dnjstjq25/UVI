package com.example.first;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor lightSensor;

    private TextView illumText;
    private TextView solarZenithText;
    private TextView gpsText;
    private TextView uviText;
    private TextView notice;
    private TextView noticeText;
    private Button illumButton;
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

    // Notice UVI class
    private Func_Notice funcNotice;

    // BackPressCloseHandler class
    private BackPressCloseHandler backPressCloseHandler;

    private boolean illumFlag;
    private int illumValue;
    private double latitude;
    private double longitude;
    private double solarZenith;
    private float uvi;

    static String directory;
    private String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // 기기, 사진, 미디어, 파일 엑세스 권한
            Manifest.permission.ACCESS_FINE_LOCATION, // 위치 권한
            Manifest.permission.ACCESS_COARSE_LOCATION // 위치 권한
    };

    private static final int MULTIPLE_PERMISSIONS = 101;

    //okhttp 성공 실패
    private class CallbackToDownloadFile implements Callback {

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
                this.directory.mkdir();
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
                            "다운로드 완료",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        directory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS + "/Capstone") + "";

        //okhttp 경로 파일명
        CallbackToDownloadFile cbToDownloadFile = new CallbackToDownloadFile(
                directory,
                "model.tflite"
        );

        //okhttp 선언
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://210.102.142.16/model.tflite")
                .build();

        //okhttp 요청
        client.newCall(request).enqueue(cbToDownloadFile);

        if (Build.VERSION.SDK_INT >= 23) { // 안드로이드 6.0 이상일 경우 퍼미션 체크
            checkPermissions();
        }

        backPressCloseHandler = new BackPressCloseHandler(this);

        // 조도 측정
        illumText = (TextView) findViewById(R.id.illumText);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        if (lightSensor == null) {
            Toast.makeText(MainActivity.this, "조도 센서를 찾을 수 없습니다!", Toast.LENGTH_SHORT).show();
            illumButton.setEnabled(true);
        }
        illumFlag = true;
        illumButton = (Button) findViewById(R.id.illumButton);
        illumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (illumFlag) {
                    illumFlag = false;
                    illumButton.setText("측정 중지");
                    sensorManager.registerListener(MainActivity.this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
                } else {
                    illumFlag = true;
                    illumButton.setText("조도 측정");
                    sensorManager.unregisterListener(MainActivity.this);
                }
            }
        });

        // GPS 측정 및 태양천정각, UVI 계산
        gpsText = (TextView) findViewById(R.id.gpsText);
        solarZenithText = (TextView) findViewById(R.id.solarZenithText);
        uviText = (TextView) findViewById(R.id.uviText);
        notice = (TextView) findViewById(R.id.notice);
        noticeText = (TextView) findViewById(R.id.noticeText);
        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 권한 요청
                if (!isPermission) {
                    checkPermissions();
                    return;
                }

                gps = new Func_GPS(MainActivity.this);
                funcSolarZenith = new Func_SolarZenith();
                funcUVI = new Func_UVI();
                funcNotice = new Func_Notice();

                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {
                    // gps 출력
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    gpsText.setText("위도 : " + String.format("%.2f", latitude) + "°\n경도 : " + String.format("%.2f", longitude) + "°");

                    // 태양천정각 출력
                    solarZenith = funcSolarZenith.getSolarZenith(latitude);
                    solarZenithText.setText(solarZenith + "°");

                    // UVI 출력
                    uvi = funcUVI.Output(MainActivity.this, (float) solarZenith, (float) illumValue);
                    uviText.setText(String.format("%.5f", uvi));

                    // UVI 단계별 안내 출력
                    funcNotice.changeNotice(uvi, uviText, notice, noticeText);

                    gps.stopUsingGPS();
                } else {
                    // GPS를 사용할 수 없을경우
                    gps.showSettingAlert();
                }
            }
        });
        checkPermissions();  // 위치 권한 요청
    }

    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[i])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showToast_PermissionDeny();
                            }
                        }
                    }
                } else {
                    showToast_PermissionDeny();
                    finish(); //동의 안하면 꺼짐
                }
                return;
            }
        }

    }

    private void showToast_PermissionDeny() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
    }



    @Override
    protected void onResume() {
        super.onResume();
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

    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }
}