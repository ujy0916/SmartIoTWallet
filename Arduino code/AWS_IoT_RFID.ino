#include <ArduinoBearSSL.h>
#include <ArduinoECCX08.h>
#include <ArduinoMqttClient.h>
#include <WiFiNINA.h> // change to #include <WiFi101.h> for MKR1000

#include "arduino_secrets.h"
#define btn           11 //버튼


int state = 1; //지갑 상태(OPEN/CLOSE)
//int count = 2; //지갑 닫힌 시간
int control = 1; //1은 활성화, 0은 비활성화
int dec = 0; //rfid 값

int rfid1 = 253832; //네이버페이
int rfid2 = 247213677; //카카오뱅크
int rfid3 = 183177411; //신한신용
int rfid4 = 21116512; //국민체크
int rfid5 =21511121; //K뱅크 
int rfid6 =10000; //K뱅크 

#include <ArduinoJson.h>

/////// Enter your sensitive data in arduino_secrets.h
const char ssid[]        = SECRET_SSID;
const char pass[]        = SECRET_PASS;
const char broker[]      = SECRET_BROKER;
const char* certificate  = SECRET_CERTIFICATE;

WiFiClient    wifiClient;            // Used for the TCP socket connection
BearSSLClient sslClient(wifiClient); // Used for SSL/TLS connection, integrates with ECC508
MqttClient    mqttClient(sslClient);

//unsigned long lastMillis = 0;


void setup() {
  Serial.begin(115200);
  delay(5000);
  while (!Serial);

  pinMode(btn, INPUT); //버튼


  if (!ECCX08.begin()) {
    Serial.println("No ECCX08 present!");
    while (1);
  }

  ArduinoBearSSL.onGetTime(getTime);

  sslClient.setEccSlot(0, certificate);

  mqttClient.onMessage(onMessageReceived);
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    connectWiFi();
  }

  if (!mqttClient.connected()) {
    // MQTT client is disconnected, connect
    connectMQTT();
  }

  // poll for new MQTT messages and send keep alives
  mqttClient.poll();

   char payload[512];
  // publish a message roughly every 5 seconds.

  //버튼 
    char i;
    if(digitalRead(btn) == HIGH){state = 1;} //OPEN
    else if(digitalRead(btn) == LOW){
      state = 0;
      getRFID_Status(payload, '6');
    }
    
    if (Serial.available() > 0) {
      
      i = Serial.read();
      
      getRFID_Status(payload, i);
  }
}

unsigned long getTime() {
  // get the current time from the WiFi module  
  return WiFi.getTime();
}

void connectWiFi() {
  Serial.print("Attempting to connect to SSID: ");
  Serial.print(ssid);
  Serial.print(" ");

  while (WiFi.begin(ssid, pass) != WL_CONNECTED) {
    // failed, retry
    Serial.print(".");
    delay(5000);
  }
  Serial.println();

  Serial.println("You're connected to the network");
  Serial.println();
}

void connectMQTT() {
  Serial.print("Attempting to MQTT broker: ");
  Serial.print(broker);
  Serial.println(" ");

  while (!mqttClient.connect(broker, 8883)) {
    // failed, retry
    Serial.print(".");
    delay(5000);
  }
  Serial.println();

  Serial.println("You're connected to the MQTT broker");
  Serial.println();

  // subscribe to a topic
  mqttClient.subscribe("$aws/things/MyMKRWiFi1010/shadow/update/delta");
}

void getRFID_Status(char* payload, char i) {
  // rfid가 비활성화 모드일 경우, 카드를 읽지 않도록 한다.
  if ( control == 0 ) {
    Serial.print("[비활성]control: ");Serial.println(control);
    return;
  }

  else{
    Serial.print("[활성]control: ");Serial.println(control);
    if(state == 0){ //지갑이 닫혀있는 상황일 때, 지갑이 닫혔음을 알려줌.
       dec=0;
       sprintf(payload,"{\"state\":{\"reported\":{\"Card_rfid\":\"%d\",\"State\":\"%d\"}}}",dec,state);
     }
    else{
    switch(i){
      case '1': dec=rfid1; break;
      case '2': dec=rfid2; break;
      case '3': dec=rfid3; break;
      case '4': dec=rfid4; break;
      case '5': dec=rfid5; break;
      } 
      sprintf(payload,"{\"state\":{\"reported\":{\"Card_rfid\":\"%d\",\"State\":\"%d\"}}}",dec,state);
     }
     sendMessage(payload);
  }
}


void sendMessage(char* payload) {
  char TOPIC_NAME[]= "$aws/things/MyMKRWiFi1010/shadow/update";
  
  Serial.print("Publishing send message:");
  Serial.println(payload);
  mqttClient.beginMessage(TOPIC_NAME);
  mqttClient.print(payload);
  mqttClient.endMessage();
}


void onMessageReceived(int messageSize) {
  // we received a message, print out the topic and contents
  Serial.print("Received a message with topic '");
  Serial.print(mqttClient.messageTopic());
  Serial.print("', length ");
  Serial.print(messageSize);
  Serial.println(" bytes:");

  // store the message received to the buffer
  char buffer[512] ;
  int count=0;
  while (mqttClient.available()) {
     buffer[count++] = (char)mqttClient.read();
  }
  buffer[count]='\0'; // 버퍼의 마지막에 null 캐릭터 삽입
  Serial.println(buffer);
  Serial.println();
  // JSon 형식의 문자열인 buffer를 파싱하여 필요한 값을 얻어옴.
  // 디바이스가 구독한 토픽이 $aws/things/MyMKRWiFi1010/shadow/update/delta 이므로,
  // JSon 문자열 형식은 다음과 같다.
  // {
  //    "version":391,
  //    "timestamp":1572784097,
  //    "state":{
  //        "LED":"ON" -->여기부분에 DISABLED 삽입
  //    },
  //    "metadata":{
  //        "LED":{
  //          "timestamp":15727840
  //         }
  //    }
  // }
  //
  
  DynamicJsonDocument doc(1024);
  deserializeJson(doc, buffer);
  JsonObject root = doc.as<JsonObject>();
  JsonObject state = root["state"];
  const char* DisAbled = state["DISABLED"];
  
  char payload[512];

  if (strcmp(DisAbled,"abled")==0) {
    Serial.print("DISABLED:");Serial.println(DisAbled);
    control = 1;
    
  } //else if (strcmp(DisAbled,"Disabled")==0) {
  else {
    Serial.print("DISABLED:");Serial.println(DisAbled);
    control = 0;
  }
}
