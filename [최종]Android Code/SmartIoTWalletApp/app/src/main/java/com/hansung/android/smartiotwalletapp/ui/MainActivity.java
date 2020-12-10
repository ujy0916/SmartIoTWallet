package com.hansung.android.smartiotwalletapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.hansung.android.smartiotwalletapp.R;
import com.hansung.android.smartiotwalletapp.ui.apicall.UpdateShadowDesired;
import com.hansung.android.smartiotwalletapp.ui.apicall.UpdateShadowReported;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    final static String TAG = "AndroidAPITest";
    String getCardNameUrl;
    Boolean Able = true;

    String urlStr="https://ok3w5bjv3l.execute-api.ap-northeast-2.amazonaws.com/prod/devices/MyMKRWiFi1010/value"; //disable에서 사용

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //카드 등록
        Button registerCardBtn = findViewById(R.id.registerCardBtn);
        registerCardBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterCardActivity.class);
                startActivity(intent);
                //MainActivity->RegisterCardActivity(블루투스 : ConnectedThread)->UpdateShadowReported(extends PutResquest)
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
                //MainActivity->CardListActivity->GetCards(extends GetResquest)
            }
        });

        //비활성화 모드 ON / OFF
        final Button DisabledBtn = findViewById(R.id.DISABLEDBtn);
        DisabledBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(Able.equals(true))
                {
                    Able = false; //true->false, 비활성화 모드로 바뀌었다는 뜻. (카드 값을 계속 섀도우에 보내주어도 값은 DB에 저장되지 않음)
                    DisabledBtn.setText("비활성화 모드 ON\n비활성화 중입니다.");
                    JSONObject payload = new JSONObject();

                    try {
                        JSONArray jsonArray = new JSONArray();
                        String disabled_input = "Disabled";

                        //카드 별칭
                        if (disabled_input != null && !disabled_input.equals("")) {
                            JSONObject tag1 = new JSONObject();
                            tag1.put("tagName", "DISABLED");
                            tag1.put("tagValue", disabled_input);

                            jsonArray.put(tag1);
                        }
                        if (jsonArray.length() > 0)
                            payload.put("tags", jsonArray);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONEXception");
                    }
                    Log.i(TAG, "payload=" + payload);
                    Toast.makeText(MainActivity.this,"비활성화 모드 중에는 카드가 찍히지 않습니다.", Toast.LENGTH_LONG).show();
                    new UpdateShadowDesired(MainActivity.this, urlStr).execute(payload);

                }
                else {
                    Able = true; //false->true, 활성화 모드로 바뀌었다는 뜻. (섀도우에 올라간 카드 값이 DB에 저장됨)
                    DisabledBtn.setText("비활성화 모드 OFF\n활성화 중입니다.");

                    JSONObject payload = new JSONObject();

                    try {
                        JSONArray jsonArray = new JSONArray();
                        String disabled_input = "abled";

                        //비성화
                        if (disabled_input != null && !disabled_input.equals("")) {
                            JSONObject tag1 = new JSONObject();
                            tag1.put("tagName", "DISABLED");
                            tag1.put("tagValue", disabled_input);

                            jsonArray.put(tag1);
                        }
                        if (jsonArray.length() > 0)
                            payload.put("tags", jsonArray);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSONEXception");
                    }
                    Log.i(TAG, "payload=" + payload);
                    new UpdateShadowDesired(MainActivity.this, urlStr).execute(payload);
                }
                //MainActivity->UpdateShadowDesired(extends PutRequest)
            }
        });

    }
}
