package com.hansung.android.smartiotwalletapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.hansung.android.smartiotwalletapp.R;

public class MainActivity extends AppCompatActivity {
    final static String TAG = "AndroidAPITest";
    String getCardNameUrl;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //카드 등록
        Button registerCardBtn = findViewById(R.id.registerCardBtn);
        registerCardBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getCardNameUrl = "";
                Log.i(TAG, "getCardNameUrl=" + getCardNameUrl);
                if (getCardNameUrl == null || getCardNameUrl.equals("")) {
                    Toast.makeText(MainActivity.this, "카드 조회 API URI가 설정되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, RegisterCardActivity.class);
                //intent.putExtra("getCardNameUrl", getCardNameUrl);
                startActivity(intent);
                //MainActivity->CardListActivity->GetTings(extends, GetResquest)
            }
        });

        //카드 조회
        Button listCardBtn = findViewById(R.id.listCardBtn);
        listCardBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getCardNameUrl = "https://ok3w5bjv3l.execute-api.ap-northeast-2.amazonaws.com/prod/devices/MyMKRWiFi1010/log";
                Log.i(TAG, "getCardNameUrl=" + getCardNameUrl);
                if (getCardNameUrl == null || getCardNameUrl.equals("")) {
                    Toast.makeText(MainActivity.this, "카드 조회 API URI가 설정되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, CardListActivity.class);
                intent.putExtra("getCardNameUrl", getCardNameUrl);
                startActivity(intent);
                //MainActivity->CardListActivity->GetTings(extends, GetResquest)
            }
        });

        //카드 빈도수 조회
        Button freqCardBtn = findViewById(R.id.freqCardBtn);
        freqCardBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getCardNameUrl = "";
                Log.i(TAG, "getCardNameUrl=" + getCardNameUrl);
                if (getCardNameUrl == null || getCardNameUrl.equals("")) {
                    Toast.makeText(MainActivity.this, "카드 조회 API URI가 설정되어 있지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(MainActivity.this, CardFreqActivity.class);
                //intent.putExtra("getCardNameUrl", getCardNameUrl);
                startActivity(intent);
                //MainActivity->CardListActivity->GetTings(extends, GetResquest)
            }
        });

    }
}
