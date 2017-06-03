#include <SoftwareSerial.h>

SoftwareSerial serOne(8, 9); // RX, TX
SoftwareSerial serTwo(10, 11); // RX, TX

uint8_t get_val() {
  while(!serOne.available());
  return serOne.read();
}

void setup() {
  Serial.begin(9600); //For testing
  
  serOne.begin(74880);
  serTwo.begin(74880);
  serTwo.print("Hello???");
}

void loop() {
  //Serial.println("Sending one...");
  
  serOne.listen();
  while (serOne.available()) {
    Serial.write(serOne.read());
  }   
//
//  Serial.println();
//
//  serTwo.listen();
//  serOne.print("World??");
//  while (serTwo.available()) {
//    Serial.write(serTwo.read());
//  }


  //delay(500);

}
