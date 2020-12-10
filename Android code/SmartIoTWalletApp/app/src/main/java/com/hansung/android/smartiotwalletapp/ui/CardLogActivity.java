package com.hansung.android.smartiotwalletapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.hansung.android.smartiotwalletapp.R;
import com.hansung.android.smartiotwalletapp.ui.apicall.GetLog;


public class CardLogActivity extends AppCompatActivity {
    final static String TAG = "AndroidAPITest";

    private TextView textView_Date1;
    private TextView textView_Date2;
    private DatePickerDialog.OnDateSetListener callbackMethod;

    String getLogsURL="https://ok3w5bjv3l.execute-api.ap-northeast-2.amazonaws.com/prod/devices/";
    //https://xxxxxxxx.execute-api.ap-northeast-2.amazonaws.com/prod/devices/{RFID_value}/{from_to

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_log);

        Intent intent = getIntent(); //CardListActivity로부터 인텐트 받아옴
        getLogsURL = getLogsURL+intent.getStringExtra("Card_rfid")+"/value"; //받아온 Card_rfid값 넣음
        Log.i(TAG, "Card_rfid="+getLogsURL);

        Button startDateBtn = findViewById(R.id.start_date_button);
        startDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callbackMethod = new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                    {
                        textView_Date1 = (TextView)findViewById(R.id.textView_date1);
                        textView_Date1.setText(String.format("%d-%d-%d", year ,monthOfYear+1,dayOfMonth));
                    }
                };

                DatePickerDialog dialog = new DatePickerDialog(CardLogActivity.this, callbackMethod, 2020, 12, 0);

                dialog.show();


            }
        });

        Button endDateBtn = findViewById(R.id.end_date_button);
        endDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callbackMethod = new DatePickerDialog.OnDateSetListener()
                {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
                    {
                        textView_Date2 = (TextView)findViewById(R.id.textView_date2);
                        textView_Date2.setText(String.format("%d-%d-%d", year ,monthOfYear+1,dayOfMonth));
                    }
                };

                DatePickerDialog dialog = new DatePickerDialog(CardLogActivity.this, callbackMethod, 2020, 12, 0);

                dialog.show();


            }
        });

        Button start = findViewById(R.id.log_start_button);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetLog(CardLogActivity.this,getLogsURL).execute();
            }
        });
    }
}