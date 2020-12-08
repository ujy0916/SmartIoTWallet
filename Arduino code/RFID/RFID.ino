#include <SPI.h>
#include <MFRC522.h>

#define RST_PIN         6           // Configurable, see typical pin layout above
#define SS_PIN           7           // Configurable, see typical pin layout above

// 라이브러리 생성
MFRC522 rfid(SS_PIN, RST_PIN); // Instance of the class

MFRC522::MIFARE_Key key; 

//이전 ID와 비교하기위한 변수
byte nuidPICC[4];

void setup() { 
  Serial.begin(9600);
  SPI.begin(); // SPI 시작
  rfid.PCD_Init(); // RFID 시작

  //초기 키 ID 초기화
  for (byte i = 0; i < 6; i++) {
    key.keyByte[i] = 0xFF;
  }
  
  Serial.println(F("This code scan the MIFARE Classsic NUID."));
  Serial.print(F("Using the following key:"));
  //printHex(key.keyByte, MFRC522::MF_KEY_SIZE);
}
 
void loop() {

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

    //Serial.print(F("In hex: "));
    //16진수로 변환해서 출력
   // printHex(rfid.uid.uidByte, rfid.uid.size);
   // Serial.println();


   Serial.print(F("In dec: "));
    //10진수로 출력
   String Dec = printDec(rfid.uid.uidByte, rfid.uid.size);
   //String Payload = "state:{\"reported\":{\"temperature\":\",t,led);
   Serial.print(Dec);

  // 만약 바로 전에 인식한 RF 카드와 다르다면..
 /* if (rfid.uid.uidByte[0] != nuidPICC[0] || 
    rfid.uid.uidByte[1] != nuidPICC[1] || 
    rfid.uid.uidByte[2] != nuidPICC[2] || 
    rfid.uid.uidByte[3] != nuidPICC[3] ) {
    Serial.println(F("A new card has been detected."));

    // ID를 저장해둔다.    
    for (byte i = 0; i < 4; i++) {
      nuidPICC[i] = rfid.uid.uidByte[i];
    }
   
    //모니터 출력
    Serial.println(F("The NUID tag is:"));

    Serial.print(F("In hex: "));
    //16진수로 변환해서 출력
    printHex(rfid.uid.uidByte, rfid.uid.size);
    Serial.println();


    Serial.print(F("In dec: "));
    //10진수로 출력
    printDec(rfid.uid.uidByte, rfid.uid.size);
    Serial.println();
  }*/
  //else Serial.println(F("Card read previously.")); //바로 전에 인식한 것과 동일하다면 

  // PICC 종료
  rfid.PICC_HaltA();

  // 암호화 종료(?)
  rfid.PCD_StopCrypto1();

  //다시 처음으로 돌아감.
}
/*
//16진수로 변환하는 함수
String printHex(byte *buffer, byte bufferSize) {
  String Hex="";
  for (byte i = 0; i < bufferSize; i++) {
    Serial.print(buffer[i] < 0x10 ? "0" : "");
    //Serial.print(buffer[i], HEX);
    Hex.concat(String(buffer[i], HEX));
  }
  return Hex;
}
*/
//10진수로 변환하는 함수
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
