package com.hansung.android.smartiotwalletapp.ui.apicall;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import java.net.MalformedURLException;
import java.net.URL;

import com.hansung.android.smartiotwalletapp.httpconnection.PutRequest;

public class UpdateShadowDesired extends PutRequest {
    final static String TAG = "AndroidAPITest";
    String urlStr;

    public UpdateShadowDesired(Activity activity, String urlStr) {

        super(activity);
        this.urlStr = urlStr;
    }

    @Override
    protected void onPreExecute() {
        try {
            Log.e(TAG, urlStr);
            url = new URL(urlStr);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Toast.makeText(activity,"URL is invalid:"+urlStr, Toast.LENGTH_SHORT).show();
            activity.finish();

        }
    }
    @Override
    protected void onPostExecute(String result) {

        //Toast.makeText(activity,"등록되었습니다.", Toast.LENGTH_SHORT).show();
    }

}
