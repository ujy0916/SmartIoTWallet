#include <SoftwareSerial.h>
#include <MFRC522.h>

SoftwareSerial HC05(7,8);

#define SS_PIN 10
#define RST_PIN 9
MFRC522 rfid(SS_PIN, RST_PIN);  // rfid 객체 생성

MFRC522::MIFARE_Key key; 

//이전 ID와 비교하기위한 변수
byte nuidPICC[4];
 

 
void setup(){
  pinMode(4, OUTPUT);
  digitalWrite(4,HIGH);
  Serial.begin(9600);
  HC05.begin(9600);

  SPI.begin(); // SPI 시작
  rfid.PCD_Init(); // RFID 시작

  //초기 키 ID 초기화
  for (byte i = 0; i < 6; i++) {
  key.keyByte[i] = 0xFF;
  }
  
  Serial.println(F("This code scan the MIFARE Classsic NUID."));
  Serial.print(F("Using the following key:"));
}
 
void loop(){

  // 카드가 인식되었다면 다음으로 넘어가고 아니면 더이상 
  // 실행 안하고 리턴
  if ( ! rfid.PICC_IsNewCardPresent())
    return;

  // ID가 읽혀졌다면 다음으로 넘어가고 아니면 더이상 
  // 실행 안하고 리턴
  if ( ! rfid.PICC_ReadCardSerial())
    return;

  Serial.print(F("PICC type: "));

  //카드의 타입을 읽어온다.
  MFRC522::PICC_Type piccType = rfid.PICC_GetType(rfid.uid.sak);

  //모니터에 출력
  Serial.println(rfid.PICC_GetTypeName(piccType));

  // MIFARE 방식인지 확인하고 아니면 리턴
  if (piccType != MFRC522::PICC_TYPE_MIFARE_MINI &&  
    piccType != MFRC522::PICC_TYPE_MIFARE_1K &&
    piccType != MFRC522::PICC_TYPE_MIFARE_4K) {
    Serial.println(F("Your tag is not of type MIFARE Classic."));
    return;
  }

   //모니터 출력
    Serial.println(F("The NUID tag is:"));
    char dec_char[20] = {0};
    String Dec = printDec(rfid.uid.uidByte, rfid.uid.size);
    Dec.toCharArray(dec_char, Dec.length());  
    Serial.print(F("In dec: "));
    Serial.println(dec_char);     
  if (Dec != NULL) {    
    //10진수로 출력 
    for(int i = 0; i<Dec.length();i++) {
      HC05.write(dec_char[i]);
      Serial.print(dec_char[i]);
    }
    
    // PICC 종료
    rfid.PICC_HaltA();

    // 암호화 종료(?)
    rfid.PCD_StopCrypto1();
  }
  if (HC05.available()) {
    Serial.write(HC05.read());
  }
}

String printDec(byte *buffer, byte bufferSize) {
  String Dec="";
  for (byte i = 0; i < bufferSize; i++) {
    //Serial.print(buffer[i] < 0x10 ? " 0" : "");
    //Serial.print(buffer[i], DEC);

    Serial.print(buffer[i] < 0x10 ? "0" : "");
    //Serial.print(buffer[i], DEC);
    Dec.concat(String(buffer[i], DEC));    
  }
  //Serial.println(Dec);
  return Dec;
}
