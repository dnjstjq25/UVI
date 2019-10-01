package com.example.first;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
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

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private TextView gpsText;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private TextView illumText;
    private TextView solarZenithText;
    private TextView uviText;
    private Button startButton;
    //    private Button mapButton;
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;

    // GPS Tracker class
    private Func_GPS gps;

    // Calculate Solar Zenith class
    private Func_SolarZenith funcSolarZenith;

    private int illumValue;
    private double latitude;
    private double longitude;
    private double solarZenith;

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

                // GPS 사용유무 가져오기
                if (gps.isGetLocation()) {
                    // gps 출력
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    gpsText.setText("위도 : " + String.valueOf(latitude) + "\n경도 : " + String.valueOf(longitude));

                    // 태양천정각 출력
                    solarZenith = funcSolarZenith.getSolarZenith(latitude);
                    solarZenithText.setText(solarZenith + "°");

                    // UVI 출력
                    String[] illum = String.valueOf(illumText.getText()).split(" ");
//                    String[] input_1 = new String[]{Double.toString(solarZenith)};
//                    double[] input_2 = new double[]{Double.parseDouble(illum[0])};
//                    Object[][] inputs = new Object[][]{{input_1}, {input_2}};
////                    double[][] input = new double[][]{{solarZenith}, {Double.parseDouble(String.valueOf(uviText.getText()))}};
////                    java.util.Map<Integer, Object> outputs = new java.util.HashMap();
//                    double[] output = new double[]{0};
////                    outputs.put(0, output);
//                    Interpreter tflite = getTfliteInterpreter("model.tflite");
////                    tflite.runForMultipleInputsOutputs(inputs, outputs);
//                    tflite.run(inputs, output);
                    uviText.setText(solarZenith +"\n" + illum[0]);

                    uviText.setText(Double.toString(solarZenith) + "\n" + Double.parseDouble(illum[0]));

                    gps.stopUsingGPS();
                } else {
                    // GPS를 사용할 수 없을경우
                    gps.showSettingAlert();
                }
            }
        });

//        // 맵
//        mapButton = (Button) findViewById(R.id.mapButton);
//        mapButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent mapIntent = new Intent(MainActivity.this, MapActivity.class);
//                startActivity(mapIntent);
//            }
//        });

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

    // 전화번호 권한 요청
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

    // 모델 파일 인터프리터를 생성하는 함수
    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(MainActivity.this, modelPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 모델을 읽어오는 함수 (텐서플로 라이트 홈페이지 참고)
    // MappedByteBuffer 바이트 버퍼를 Interpreter 객체에 전달하면 모델 해석을 할 수 있다.
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}