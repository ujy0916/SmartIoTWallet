#include <SoftwareSerial.h>
 
SoftwareSerial HC05(7,8);
 
void setup(){
  pinMode(4, OUTPUT);
  digitalWrite(4,HIGH);
  Serial.begin(9600);
  HC05.begin(9600);

  Serial.println("Bluetooth setup Mode");
  Serial.println("Select [Both NL & CR] in Serial Monitor");
}
 
void loop(){
  if (Serial.available()) {
    HC05.write(Serial.read());
  }
  if (HC05.available()) {
    Serial.write(HC05.read());
  }
}
