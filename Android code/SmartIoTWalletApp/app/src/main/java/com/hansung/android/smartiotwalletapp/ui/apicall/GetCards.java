package com.hansung.android.smartiotwalletapp.ui.apicall;

import android.app.Activity;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hansung.android.smartiotwalletapp.R;
import com.hansung.android.smartiotwalletapp.httpconnection.GetRequest;
import com.hansung.android.smartiotwalletapp.ui.CardLogActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class GetCards extends GetRequest {
    final static String TAG = "AndroidAPITest";
    String urlStr;
    public GetCards(Activity activity, String urlStr) {
        super(activity);
        this.urlStr = urlStr;
    }

    /*doInBackground 메소드를 가지고 있는 GetRequest를 확장한 GetCards 클래스는
    onPreExecute : network Operation에 UI의 값을 넘겨주는 메소드
    onPostExecute : network Operation의 결과값을 UI로 넘겨주는 메소드를 가짐
     */
    @Override
    protected void onPreExecute() {
        try {
            url = new URL(urlStr); //GetRequest에 필요한 url 값을 여기서 설정
        } catch (MalformedURLException e) {
            Toast.makeText(activity,"URL is invalid:"+urlStr, Toast.LENGTH_SHORT).show();
            activity.finish();
            e.printStackTrace();
        }
        TextView cardname = activity.findViewById(R.id.cardname);
        cardname.setText("카드를 조회 중입니다.");
    }

    //GetRequest의 결과를 onPostExecute로 보냄
    @Override
    protected void onPostExecute(String jsonString) {
        TextView cardname = activity.findViewById(R.id.cardname);
        if (jsonString == null || jsonString.equals("")) {
            cardname.setText("현재 등록하신 카드가 없습니다.");
            return;
        }
        cardname.setText("");
        ArrayList<Tag> arrayList = getArrayListFromJSONString(jsonString);

        final ArrayAdapter adapter = new ArrayAdapter(activity,
                android.R.layout.simple_list_item_1,
                arrayList.toArray());
        ListView cardlist = activity.findViewById(R.id.cardlist);
        cardlist.setAdapter(adapter);
        cardlist.setDividerHeight(10);
    }

    protected ArrayList<Tag> getArrayListFromJSONString(String jsonString) {
        ArrayList<Tag> output = new ArrayList();
        try {
            jsonString = jsonString.substring(1,jsonString.length()-1);
            jsonString = jsonString.replace("\\\"","\"");

            Log.i(TAG, "jsonString="+jsonString);

            JSONObject root = new JSONObject(jsonString);
            JSONArray jsonArray = root.getJSONArray("data");

            for (int i=0; i<jsonArray.length();i++) {
                JSONObject jsonObject = (JSONObject)jsonArray.get(i);

                Tag thing = new Tag(jsonObject.getString("Card_name"),
                                    jsonObject.getString("Card_rfid"),
                                    jsonObject.getString("time"));
                output.add(thing);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return output;
    }

    class Tag {
        String Card_name;
        String Card_rfid;
        String Time;
        String deviceId;

        public Tag(String name, String rfid, String time) {
            Card_name = name;
            Card_rfid = rfid;
            Time = time;
        }
        public String toString() {
            return String.format("%s    RFID : %s", Card_name, Card_rfid);
        }
    }
}
