package com.example.first;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Func_UVI {
    public String Test(Activity activity) {
        int[][] input = new int[][]{{3}, {7}};
        int[] output = new int[]{0};

        Interpreter tflite = getTfliteInterpreter(activity, "test.tflite");
        tflite.run(input, output);

        return String.valueOf(output[0]);
    }

    public String Output(Activity activity, float solarZenith, float illumination) {
        float[][] input = new float[1][2];
        float[][] output = new float[][]{{0}};
        input[0][0]=solarZenith;
        input[0][1]=illumination/10000.0F;


        Interpreter tflite = getTfliteInterpreter(activity,"model.tflite");
        tflite.run(input, output);
        return String.valueOf(output[0][0]);
    }

    // 모델 파일 인터프리터를 생성하는 함수
    private Interpreter getTfliteInterpreter(Activity activity, String modelPath) {
        try {
            return new Interpreter(loadModelFile(activity, modelPath));
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
