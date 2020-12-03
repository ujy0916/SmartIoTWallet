#include <ArduinoBLE.h>
BLEService newService("180A"); // creating the service

//우리는 RFID값을 읽어서 보내주기만 하면 되니까 randomReading에 뒤에 있는 BLERead | BLENotify 이것만 사용하면 될 듯!
BLEUnsignedCharCharacteristic randomReading("2A58", BLERead | BLENotify); // creating the Analog Value characteristic
BLEByteCharacteristic switchChar("2A57", BLERead | BLEWrite); // creating the LED characteristic

const int ledPin = 2;
long previousMillis = 0;


void setup() {
  Serial.begin(9600);    // initialize serial communication
  while (!Serial);       //starts the program if we open the serial monitor.

  pinMode(LED_BUILTIN, OUTPUT); // initialize the built-in LED pin to indicate when a central is connected
  pinMode(ledPin, OUTPUT); // initialize the built-in LED pin to indicate when a central is connected

  //initialize BLE library
  if (!BLE.begin()) {
    Serial.println("starting BLE failed!");
    while (1);
  }

  BLE.setLocalName("RFID Reader"); //이름 설정

  //UUID를 활성화시키는 코드(서비스 UUID가 상위, 특성 UUID가 하위에 존재)
  BLE.setAdvertisedService(newService);
  newService.addCharacteristic(switchChar); //add characteristics to a service
  newService.addCharacteristic(randomReading);
  BLE.addService(newService);  // 서비스 추가

  //UUID에 들어가는 초기값 0으로 설정
  switchChar.writeValue(0); //set initial value for characteristics
  randomReading.writeValue(0);

  //BLE기기가 주변으로 신호를 보냄
  BLE.advertise(); //start advertising the service
  Serial.println("Bluetooth device active, waiting for connections...");
}

void loop() {
  
  BLEDevice central = BLE.central(); // wait for a BLE central

  //보드와 핸드폰이 연결이 되면 시리얼 모니터에 연결된 기기의 MAC 어드레스를 표시
  if (central) {  // if a central is connected to the peripheral
    Serial.print("Connected to central: ");
    Serial.println(central.address()); // print the central's BT address
    
    digitalWrite(LED_BUILTIN, HIGH); // turn on the LED to indicate the connection

    // check the battery level every 200ms
    // while the central is connected:
    //연결된 동안 while 구문 안에 동작을 반복
    while (central.connected()) {
      long currentMillis = millis();
      
      if (currentMillis - previousMillis >= 200) { // if 200ms have passed, we check the battery level
        previousMillis = currentMillis;

        int randomValue = analogRead(A1);
        randomReading.writeValue(randomValue);

        if (switchChar.written()) {
          if (switchChar.value()) {   // any value other than 0
            Serial.println("LED on");
            digitalWrite(ledPin, HIGH);         // will turn the LED on
          } else {                              // a 0 value
            Serial.println(F("LED off"));
            digitalWrite(ledPin, LOW);          // will turn the LED off
          }
        }

      }
    }
    
    digitalWrite(LED_BUILTIN, LOW); // when the central disconnects, turn off the LED
    Serial.print("Disconnected from central: ");
    Serial.println(central.address());
  }
}
