package com.example.first;

import android.graphics.Color;
import android.widget.TextView;

public class Func_Notice {
    public void changeNotice(float uvi, TextView uviText, TextView notice, TextView noticeText) {
        if (uvi < 3) {
            uviText.setTextColor(Color.parseColor("#3EA72D"));
            notice.setText("자외선지수 단계 : 낮음");
            notice.setTextColor(Color.parseColor("#3EA72D"));
            noticeText.setText("- 햇볕 노출에 대한 보호조치가 필요하지 않음\n\n- 그러나 햇볕에 민감한 피부를 가진 분은 자외선 차단제를 발라야 함\n");
        } else if (3 <= uvi && uvi < 6) {
            uviText.setTextColor(Color.parseColor("#FED98E"));
            notice.setText("자외선지수 단계 : 보통");
            notice.setTextColor(Color.parseColor("#FED98E"));
            noticeText.setText("- 2~3시간 내에도 햇볕에 노출 시에 피부 화상을 입을 수 있음\n\n- 모자, 선글라스 이용\n\n- 자외선 차단제를 발라야 함\n");
        } else if (6 <= uvi && uvi < 8) {
            uviText.setTextColor(Color.parseColor("#F17A25"));
            notice.setText("자외선지수 단계 : 높음");
            notice.setTextColor(Color.parseColor("#F17A25"));
            noticeText.setText("- 햇볕에 노출 시 1~2시간 내에도 피부 화상을 입을 수 있어 위험함\n\n- 한낮에는 그늘에 머물러야 함\n\n- 외출 시 긴 소매 옷, 모자, 선글라스 이용\n\n- 자외선 차단제를 정기적으로 발라야 함\n");
        } else if (8 <= uvi && uvi < 11) {
            uviText.setTextColor(Color.parseColor("#E6310F"));
            notice.setText("자외선지수 단계 : 매우높음");
            notice.setTextColor(Color.parseColor("#E6310F"));
            noticeText.setText("- 햇볕에 노출 시 수십 분 이내에도 피부 화상을 입을 수 있어 매우 위험함\n\n- 오전 10시부터 오후 3시까지 외출을 피하고 실내나 그늘에 머물러야 함\n\n- 외출 시 긴 소매 옷, 모자, 선글라스 이용\n\n- 자외선 차단제를 정기적으로 발라야 함\n");
        } else if (11 <= uvi) {
            uviText.setTextColor(Color.parseColor("#6F4B9B"));
            notice.setText("자외선지수 단계 : 위험");
            notice.setTextColor(Color.parseColor("#6F4B9B"));
            noticeText.setText("- 햇볕에 노출 시 수십 분 이내에도 피부 화상을 입을 수 있어 가장 위험함\n\n- 가능한 실내에 머물러야 함\n\n- 외출 시 긴 소매 옷, 모자, 선글라스 이용\n\n- 자외선 차단제를 정기적으로 발라야 함\n");
        }
    }
}