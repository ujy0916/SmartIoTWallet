package com.jung.android.smartiotwalletapp.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.jung.android.smartiotwalletapp.R;
import com.jung.android.smartiotwalletapp.ui.apicall.GetCards;
import com.jung.android.smartiotwalletapp.ui.apicall.GetCardsFreq;
import com.jung.android.smartiotwalletapp.ui.apicall.GetLog;

import org.w3c.dom.Text;

public class CardFreqActivity extends AppCompatActivity {
    final static String TAG = "AndroidAPITest";
    //private DatePickerDialog.OnDateSetListener callbackMethod;
    private TextView textView_Month1;
    private TextView textView_Month2;
    String getCardNamesUrl;
    String getLogsURL="https://ok3w5bjv3l.execute-api.ap-northeast-2.amazonaws.com/prod/devices/";
    Button startDateBtn;
    int Year, Month;

    DatePickerDialog.OnDateSetListener callbackMethod = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth){
            Log.d("YearMonthPickerTest", "year = " + year + ", month = " + monthOfYear + ", day = " + dayOfMonth);
            int Year = year;
            int Month = monthOfYear;
            Log.d("업로드 된 값", "year = " + year + ", month = " + monthOfYear + ", day = " + dayOfMonth);
            textView_Month1 = (TextView) findViewById(R.id.textView_month1);
            textView_Month1.setText(String.format("%d-%d", Year, Month));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_freq);

        //Main에서 가져온 url 인텐트 값
        Intent intent = getIntent();
        getCardNamesUrl = intent.getStringExtra("getCardNameUrl");
        Log.i(TAG, "getCardNamesUrl="+getCardNamesUrl);

        startDateBtn = findViewById(R.id.start_month_button);
        startDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                YearMonthPickerDialog pd = new YearMonthPickerDialog();
                pd.setListener(callbackMethod);
                pd.show(getSupportFragmentManager(), "YearMonthPickerTest");
            }
        });

        Button start = findViewById(R.id.freq_start_button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetCards(CardFreqActivity.this, getCardNamesUrl).execute(); //카드 이름과 rfid 불러오기

                ListView cardlist = findViewById(R.id.cardlist);
                int len = cardlist.getMaxScrollAmount();
                Log.d("리스트뷰의 총 길이", "는 "+len);

            }
        });


    }
}