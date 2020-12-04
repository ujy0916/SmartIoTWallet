package com.hansung.android.app_ble;

import android.Manifest;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.vise.baseble.ViseBle;
import com.vise.baseble.callback.scan.IScanCallback;
import com.vise.baseble.callback.scan.ScanCallback;
import com.vise.baseble.core.BluetoothGattChannel;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.BluetoothLeDeviceStore;

import java.util.ArrayList;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {
    Button startScan, stopScan;
    TextView txt_value;
    ListView listView;
    String dataValue;

    private static final UUID ServiceUUID = UUID.fromString("0000180F-0000-1000-8000-00805F9B34FB");
    private static final UUID CharacteristicUUID = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB");

    public static final String TAG = "BLE1_ACTIVITY";

    final ViseBle viseBleSet = ViseBle.getInstance();
    BluetoothGattChannel bluetoothGattChannel_Noti;

    final ArrayList<String> arrayList_information = new ArrayList<>();
    final ArrayList<String> arrayList_address = new ArrayList<>();
    final ArrayList<String> arrayList_name = new ArrayList<>();
    final ArrayList<Integer> arrayList_rssi = new ArrayList<>();
    final ArrayList<ParcelUuid[]> arrayList_uuid = new ArrayList<>();
    final ArrayList<BluetoothLeDevice> arrayList_device = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 초기화
        ViseBle.config()
                .setScanTimeout(-1)
                .setConnectTimeout(10 * 1000) // 연결 시간 초과 시간설정
                .setOperateTimeout(5 * 1000)  // 데이터 작업 시간 초과 설정
                .setConnectRetryCount(3)      // 연결 실패 재시도 횟수 설정
                .setConnectRetryInterval(1000)// 재시도 시간 간격 설정
                .setOperateRetryCount(3)      // 데이터 조작 실패한 재시도 설정
                .setOperateRetryInterval(1000)// 데이터 조작 실패에 대한 재시도 시간간격
                .setMaxConnectCount(3);       // 연결된 최대 장치 수 설정
        ViseBle.getInstance().init(this);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION}, 100);


        // 스캔 콜백함수
        final IScanCallback iScanCallback = new IScanCallback() {
            @Override
            public void onDeviceFound(BluetoothLeDevice bluetoothLeDevice) {
                arrayList_address.add(bluetoothLeDevice.getAddress());
                arrayList_name.add(bluetoothLeDevice.getName());
                arrayList_rssi.add(bluetoothLeDevice.getRssi());
                arrayList_device.add(bluetoothLeDevice);
                arrayList_uuid.add(bluetoothLeDevice.getDevice().getUuids());

                arrayList_information.add(("Device = " + bluetoothLeDevice.getName()
                        + "  UUID = " + bluetoothLeDevice.getDevice().getUuids()
                        + "Address = " + bluetoothLeDevice.getAddress()));

                ArrayAdapter arrayAdapter = new ArrayAdapter(getApplicationContext(),
                        R.layout.support_simple_spinner_dropdown_item,
                        arrayList_information);
                listView.setAdapter(arrayAdapter);

            }


            @Override
            public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {

            }

            @Override
            public void onScanTimeout() {

            }
        };
        final ScanCallback scanCallback = new ScanCallback(iScanCallback);

        startScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 검색(스캔)
                viseBleSet.startScan(scanCallback);
            }
        });

        stopScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 스캔 중지
                viseBleSet.stopScan(scanCallback);
            }
        });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViseBle.getInstance().disconnect();
    }
}