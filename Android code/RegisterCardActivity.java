package com.hansung.android.smartiotwalletapp.ui;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.hansung.android.smartiotwalletapp.R;
import com.hansung.android.smartiotwalletapp.ui.apicall.UpdateShadowReported;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;

public class RegisterCardActivity extends AppCompatActivity {
    String urlStr="https://ok3w5bjv3l.execute-api.ap-northeast-2.amazonaws.com/prod/devices/MyMKRWiFi1010";
    final static String TAG = "AndroidAPITest";

    EditText getCardName;
    String getRFIDCard;

    //블루투스 관련
    private static final UUID BT_MODULE_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // #defines for identifying shared types between calling functions
    private final static int REQUEST_ENABLE_BT = 1; // used to identify adding bluetooth names
    public final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTING_STATUS = 3; // used in bluetooth handler to identify message status

    private TextView mBluetoothStatus;
    private TextView mReadBuffer;
    private Button mScanBtn;
    private Button mOffBtn;
    private Button mListPairedDevicesBtn;
    private Button mDiscoverBtn;
    private ListView mDevicesListView;

    private BluetoothAdapter mBTAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private ArrayAdapter<String> mBTArrayAdapter;

    private Handler mHandler; // Our main handler that will receive callback notifications
    private ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_card);

        getCardName = findViewById(R.id.registerCardName); //입력한 카드 별칭

        getRFIDCard = "1234567890"; //임의의 rfid card (추후 수정)

        //블루투스 관련
        mBluetoothStatus = (TextView)findViewById(R.id.bluetooth_status); //블루투스 상태
        mReadBuffer = (TextView) findViewById(R.id.registerCardRFID); //블루투스로 읽어오는 정보
        mScanBtn = (Button)findViewById(R.id.scan);  //블루투스 켜기
        mOffBtn = (Button)findViewById(R.id.off);  //블루투스 끄기
        mDiscoverBtn = (Button)findViewById(R.id.discover);  //새로운 디바이스 찾기
        mListPairedDevicesBtn = (Button)findViewById(R.id.paired_btn);  //페어링된 디바이스 찾기

        //리스트 어뎁터 (블루투스 목록들)
        mBTArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio

        mDevicesListView = (ListView)findViewById(R.id.devices_list_view);  //블루투스 목록 리스트
        mDevicesListView.setAdapter(mBTArrayAdapter); // assign model to view
        mDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Ask for location permission if not already allowed  //위치 허가가 되어 있지 않은 경우 위치 허가 받음
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        // Ask for location permission if not already allowed  //위치 허가가 되어 있지 않은 경우 위치 허가 받음
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);

        /*
        핸들러에 있는 sendMessage()를 통해서 Message(=작업)를 전달합니다.
        그럼 핸들러는 Message Queue에 차례대로 넣습니다.
        그럼 Looper가 Message Queue로부터 하나씩 Message를 뽑아서 핸들러로 전달합니다.
        Looper로부터 전달받은 메시지는 handleMessage()를 통해서 작업을 하게 됩니다.
         */
        mHandler = new Handler(Looper.getMainLooper()){
            //getMainLooper는 MainLooper를 반환해 준다.
            @Override
            public void handleMessage(Message msg){
                if(msg.what == MESSAGE_READ){
                    /*
                    처리 결과를 Message를 이용해서 Handler에 전달할 때,
                    각 메시지를 구분하기 위해 정수값(message.what)을 사용한다.
                     */
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");  //UTF-8로 읽은 msg의 값을 변환
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    mReadBuffer.setText(readMessage);
                }

                if(msg.what == CONNECTING_STATUS){
                    if(msg.arg1 == 1)
                        mBluetoothStatus.setText("Connected to Device: " + msg.obj); //msg.obj는 설정한 블루투스 이름값
                    else
                        mBluetoothStatus.setText("Connection Failed");  //연결 실패
                }
            }
        };

        if (mBTArrayAdapter == null) {  //아무 디바이스도 뜨지 않은 경우
            // Device does not support Bluetooth
            mBluetoothStatus.setText("Status: Bluetooth not found");
            Toast.makeText(getApplicationContext(),"Bluetooth device not found!",Toast.LENGTH_SHORT).show();
        }
        else {
            mScanBtn.setOnClickListener(new View.OnClickListener() {
                //BLUETOOTH ON 버튼을 누름 - 블루투스 켜짐
                @Override
                public void onClick(View v) {
                    bluetoothOn();
                }
            });

            mOffBtn.setOnClickListener(new View.OnClickListener(){
                //BLUETOOTH OFF 버튼을 누름 - 블루투스 꺼짐
                @Override
                public void onClick(View v){
                    bluetoothOff();
                }
            });

            mListPairedDevicesBtn.setOnClickListener(new View.OnClickListener() {
                //페어링된 디바이스 목록을 가져오는 함수
                @Override
                public void onClick(View v){
                    listPairedDevices();
                }
            });

            mDiscoverBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    discover();
                }
            });
        }

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

    private void bluetoothOn(){
        //블루투스를 켤 수있도록 하는 함수
        if (!mBTAdapter.isEnabled()) {  //블루투스가 활성화 되어 있지 않을 경우
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            mBluetoothStatus.setText("Bluetooth enabled");
            Toast.makeText(getApplicationContext(),"Bluetooth turned on",Toast.LENGTH_SHORT).show();

        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on", Toast.LENGTH_SHORT).show();
        }
    }

    private void bluetoothOff(){
        //블루투스를 끌 수 있도록 하는 함수
        mBTAdapter.disable(); // turn off
        mBluetoothStatus.setText("Bluetooth disabled");
        Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();
    }

    // Enter here after user selects "yes" or "no" to enabling radio
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        //현재 상태가 이용가능한지 아닌지를 STATUS에 표시
        // Check which request we're responding to
        super.onActivityResult(requestCode, resultCode, Data);
        if (requestCode == REQUEST_ENABLE_BT) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                mBluetoothStatus.setText("Enabled");
            } else
                mBluetoothStatus.setText("Disabled");
        }
    }

    private void discover(){
        //새로운 디바이스를 가지고 오는 함수
        // Check if the device is already discovering
        if(mBTAdapter.isDiscovering()){
            mBTAdapter.cancelDiscovery();
            Toast.makeText(getApplicationContext(),"디바이스 찾기 멈춤",Toast.LENGTH_SHORT).show();
        }
        else{
            if(mBTAdapter.isEnabled()) {
                mBTArrayAdapter.clear(); // clear items
                mBTAdapter.startDiscovery();
                Toast.makeText(getApplicationContext(), "디바이스 찾기 시작", Toast.LENGTH_SHORT).show();
                registerReceiver(blReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            else{
                Toast.makeText(getApplicationContext(), "블루투스가 켜져 있지 않습니다", Toast.LENGTH_SHORT).show();
            }
        }
    }

    final BroadcastReceiver blReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name to the list
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                mBTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    private void listPairedDevices(){
        //페어링 된 디바이스를 가지고 오는 함수
        mBTArrayAdapter.clear();
        mPairedDevices = mBTAdapter.getBondedDevices();
        if(mBTAdapter.isEnabled()) {
            // put it's one to the adapter
            for (BluetoothDevice device : mPairedDevices)
                mBTArrayAdapter.add(device.getName() + "\n" + device.getAddress());

            Toast.makeText(getApplicationContext(), "페어링된 디바이스 목록을 불러옵니다", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(), "블루투스가 켜져 있지 않습니다", Toast.LENGTH_SHORT).show();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        //리스트에서 항목을 선택한 경우
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            if(!mBTAdapter.isEnabled()) {
                Toast.makeText(getBaseContext(), "블루투스가 켜져 있지 않습니다", Toast.LENGTH_SHORT).show();
                return;
            }

            mBluetoothStatus.setText("연결중");
            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) view).getText().toString();
            final String address = info.substring(info.length() - 17);
            final String name = info.substring(0,info.length() - 17);

            // Spawn a new thread to avoid blocking the GUI one
            new Thread()
            {
                @Override
                public void run() {
                    boolean fail = false;

                    BluetoothDevice device = mBTAdapter.getRemoteDevice(address);

                    try {
                        mBTSocket = createBluetoothSocket(device);
                    } catch (IOException e) {
                        fail = true;
                        Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                    }
                    // Establish the Bluetooth socket connection.
                    try {
                        mBTSocket.connect();
                    } catch (IOException e) {
                        try {
                            fail = true;
                            mBTSocket.close();
                            mHandler.obtainMessage(CONNECTING_STATUS, -1, -1)
                                    .sendToTarget();
                        } catch (IOException e2) {
                            //insert code to deal with this
                            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    if(!fail) {
                        mConnectedThread = new ConnectedThread(mBTSocket, mHandler);
                        mConnectedThread.start();

                        mHandler.obtainMessage(CONNECTING_STATUS, 1, -1, name)
                                .sendToTarget();
                    }
                }
            }.start();
        }
    };

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BT_MODULE_UUID);
        } catch (Exception e) {
            Log.e(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BT_MODULE_UUID);
    }


}