package com.example.first;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        Handler hd = new Handler();
        hd.postDelayed(new splashhandler(), 3000);
    }

    private class splashhandler implements Runnable {
        public void run() {
            startActivity(new Intent(getApplication(), MainActivity.class));  // MainActivity로 전환
            SplashActivity.this.finish();  // SplashActivity 종료
        }
    }

    @Override
    public void onBackPressed() {
        // 스플래쉬 화면에서 뒤로가기 버튼 사용 불가
    }
}