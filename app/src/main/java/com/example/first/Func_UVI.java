package com.example.first;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class Func_UVI {
    public float Output(float solarZenith, float illumination, String modelPath) {
        float[][] input = new float[1][2];
        float[][] output = new float[][]{{0}};
        input[0][0] = solarZenith;
        input[0][1] = illumination / 10000.0f;

        Interpreter tflite = getTfliteInterpreter(modelPath);
        tflite.run(input, output);

        return output[0][0];
    }

    // 모델 파일 인터프리터를 생성하는 함수
    private Interpreter getTfliteInterpreter(String modelPath) {
        try {
            return new Interpreter(loadModelFile(modelPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 모델을 읽어오는 함수 (텐서플로 라이트 홈페이지 참고)
    // MappedByteBuffer 바이트 버퍼를 Interpreter 객체에 전달하면 모델 해석을 할 수 있다.
    private MappedByteBuffer loadModelFile(String modelPath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(modelPath);
        FileChannel fileChannel = fileInputStream.getChannel();
        long fileSize = fileChannel.size();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
    }
}