package com.hansung.android.smartiotwalletapp.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hansung.android.smartiotwalletapp.R;
import com.hansung.android.smartiotwalletapp.ui.apicall.UpdateShadowReported;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RegisterCardActivity extends AppCompatActivity {
    String urlStr="https://ok3w5bjv3l.execute-api.ap-northeast-2.amazonaws.com/prod/devices/MyMKRWiFi1010";
    final static String TAG = "AndroidAPITest";

    EditText getCardName;

    String getRFIDCard;



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_card);

        getCardName = findViewById(R.id.registerCardName); //입력한 카드 별칭

        getRFIDCard = "1234567890"; //임의의 rfid card (추후 수정)


        //등록하기 버튼
        Button CardInfoBtn = findViewById(R.id.registerCardInfo);
        CardInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject payload = new JSONObject();

                try {
                    JSONArray jsonArray = new JSONArray();
                    String cardName_input = getCardName.getText().toString();
                    String cardRFID_input = getRFIDCard.toString();

                    //카드 별칭
                    if (cardName_input != null && !cardName_input.equals("")) {
                        JSONObject tag1 = new JSONObject();
                        tag1.put("tagName", "Card_name");
                        tag1.put("tagValue", cardName_input);

                        jsonArray.put(tag1);
                    }

                    //카드 rfid 값
                    if (cardRFID_input != null && !cardRFID_input.equals("")) {
                        JSONObject tag1 = new JSONObject();
                        tag1.put("tagName", "Card_rfid");
                        tag1.put("tagValue", cardRFID_input);

                        jsonArray.put(tag1);
                    }


                    if (jsonArray.length() > 0)
                        payload.put("tags", jsonArray);
                } catch (JSONException e) {
                    Log.e(TAG, "JSONEXception");
                }
                Log.i(TAG, "payload=" + payload);
                if (payload.length() > 0)
                    new UpdateShadowReported(RegisterCardActivity.this, urlStr).execute(payload);
                else
                    Toast.makeText(RegisterCardActivity.this, "입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}