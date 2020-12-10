package com.hansung.android.smartiotwalletapp.ui;

import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.hansung.android.smartiotwalletapp.R;
import com.hansung.android.smartiotwalletapp.ui.apicall.GetCards;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;


public class CardListActivity extends AppCompatActivity {
    final static String TAG = "AndroidAPITest";
    String getCardNamesUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        Intent intent = getIntent();
        getCardNamesUrl = intent.getStringExtra("getCardNameUrl");
        Log.i(TAG, "getCardNamesUrl="+getCardNamesUrl);

        new GetCards(CardListActivity.this, getCardNamesUrl).execute();

        ListView cardlist = findViewById(R.id.cardlist);
        cardlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long l) {
                String rfid = ((Object)parent.getAdapter().getItem(position)).toString();
                rfid = rfid.replaceAll(" ","");
                String RFID = rfid.split(":")[1];

                Toast.makeText(getApplicationContext(),RFID,Toast.LENGTH_LONG).show();

                Log.v("TAG",rfid);
                Intent intent1 = new Intent(CardListActivity.this, CardLogActivity.class);
                intent1.putExtra("Card_rfid",RFID);

                startActivity(intent1);
            }
        });

    }

}
